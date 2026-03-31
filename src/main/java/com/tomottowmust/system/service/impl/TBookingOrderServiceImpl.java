package com.tomottowmust.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomottowmust.system.common.RedisIdWorker;
import com.tomottowmust.system.common.UserContext;
import com.tomottowmust.system.domain.dto.BookingMessage;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TBookingOrder;
import com.tomottowmust.system.domain.po.TResource;
import com.tomottowmust.system.domain.po.TResourceStock;
import com.tomottowmust.system.domain.po.TStockChangeLog;
import com.tomottowmust.system.domain.vo.ResourceVO;
import com.tomottowmust.system.mapper.TBookingOrderMapper;
import com.tomottowmust.system.mapper.TResourceStockMapper;
import com.tomottowmust.system.service.ITBookingOrderService;
import com.tomottowmust.system.service.ITResourceService;
import com.tomottowmust.system.service.ITResourceStockService;
import com.tomottowmust.system.service.ITStockChangeLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.tomottowmust.system.domain.constant.MqConstant.BOOKING_TOPIC;

/**
 * <p>
 * 预约订单服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Slf4j
@Service
public class TBookingOrderServiceImpl extends ServiceImpl<TBookingOrderMapper, TBookingOrder> implements ITBookingOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private TResourceStockMapper resourceStockMapper;

    @Resource
    private ITResourceService resourceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private ITStockChangeLogService logService;

    // Lua 脚本
    public static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("booking.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result saveBookingOrder(Long stockId) {
        // 检测用户是否可以预约
        Long userId = UserContext.getUser().getId();

        // 执行 lua 脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                stockId.toString(),
                userId.toString()
        );

        int r = result.intValue();
        if (r == 3) {
            return Result.fail("数据不存在！");
        }
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足！" : "不能重复下单！");
        }

        long orderNum = redisIdWorker.nextId("order");
        BookingMessage message = new BookingMessage(stockId, userId, orderNum);
        String json = JSONUtil.toJsonStr(message);
        String destination = BOOKING_TOPIC + ":" + "order";

        try {
            SendResult sendResult = rocketMQTemplate.syncSend(
                    destination,
                    MessageBuilder.withPayload(json).build(),
                    3000
            );
            if (!sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RuntimeException("MQ 发送失败");
            }
        } catch (Exception e) {
            log.error("MQ 发送异常，回滚 Redis", e);
            rollbackRedis(stockId, userId);
            return Result.fail("系统繁忙");
        }

        return Result.ok(orderNum);
    }

    private void rollbackRedis(Long stockId, Long userId) {
        stringRedisTemplate.opsForValue().increment("stock:" + stockId);
        stringRedisTemplate.opsForSet().remove("order:" + stockId, userId.toString());
    }

    /**
     * MQ 消费者调用：创建订单
     */
    @Transactional
    public void createOrder(Long stockId, Long userId, Long orderNum) {
        // 重复下单检查
        Long count = query().eq("user_id", userId)
                .eq("stock_id", stockId)
                .count();
        if (count > 0) {
            log.error("已经预约过了！userId={}, stockId={}", userId, stockId);
            return;
        }

        // 乐观锁扣减库存
        LambdaUpdateWrapper<TResourceStock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TResourceStock::getId, stockId)
                .gt(TResourceStock::getRemainStock, 0);
        updateWrapper.setSql("remain_stock = remain_stock - 1, version = version + 1");
        
        int rows = resourceStockMapper.update(null, updateWrapper);

        if (rows <= 0) {
            log.error("乐观锁失败，stockId={}", stockId);
            throw new RuntimeException("库存不足");
        }

        TResourceStock stock = resourceStockMapper.selectById(stockId);
        if (stock == null) {
            log.error("库存记录不存在，stockId={}", stockId);
            throw new RuntimeException("数据不存在");
        }

        // 创建订单
        TBookingOrder order = new TBookingOrder();
        order.setOrderNo(Long.toString(orderNum));
        order.setSlotStart(stock.getSlotStart());
        order.setStockDate(stock.getStockDate());
        order.setResourceId(stock.getResourceId());
        order.setStockId(stockId);
        order.setUserId(userId);
        order.setStatus(1); // 1: 成功
        save(order);

        //记录日志
        TStockChangeLog changeLog = new TStockChangeLog();
        changeLog.setOrderNo(order.getOrderNo());
        changeLog.setStockId(stockId);
        changeLog.setChangeCount(1);
        changeLog.setRemainStock(stock.getRemainStock());
        logService.save(changeLog);
        // 缓存一致性处理
        handleCacheAfterOrder(stock);
    }

    @Override
    public Result queryMyBooking(Integer current) {
        Long userId = UserContext.getUser().getId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TBookingOrder> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, 5);
        query().eq("user_id", userId).orderByDesc("create_time").page(page);
        
        java.util.List<TBookingOrder> records = page.getRecords();
        if (records.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // 也可以构建 VO 来返回资源名称，为了简单暂时直接返回
        return Result.ok(records);
    }

    @Override
    @Transactional
    public Result cancelBooking(Long id) {
        Long userId = UserContext.getUser().getId();
        TBookingOrder order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail("预约不存在！");
        }
        if (order.getStatus() != 1) {
            return Result.fail("当前状态不可取消！");
        }

        // 更新订单状态
        order.setStatus(2); // 2: 取消
        updateById(order);

        // 增加库存
        Long stockId = order.getStockId();
        LambdaUpdateWrapper<TResourceStock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TResourceStock::getId, stockId);
        updateWrapper.setSql("remain_stock = remain_stock + 1");
        resourceStockMapper.update(null, updateWrapper);

        // 清理 Redis 缓存（为了保持一致性，最简单的办法是删掉相关缓存）
        stringRedisTemplate.delete("stock:" + stockId);
        // 如果有用户预约状态的缓存也需要清理
        stringRedisTemplate.opsForSet().remove("order:" + stockId, userId.toString());

        return Result.ok();
    }

    /**
     * 下单后的缓存处理
     */
    private void handleCacheAfterOrder(TResourceStock stock) {
        Long resourceId = stock.getResourceId();
        Long stockId = stock.getId();

        //删除库存明细 Hash 中的该条库存
        resourceService.deleteStock(resourceId, stockId);
        log.debug("删除库存明细缓存，resourceId={}, stockId={}", resourceId, stockId);

        //获取资源信息（type 可能为 null）
        TResource resource = resourceService.getById(resourceId);
        Integer type = (resource != null) ? resource.getType() : null;

        //重新构建 ResourceVO
        ResourceVO updatedVO = buildResourceVO(resourceId, resource);
        if (updatedVO == null) {
            log.warn("构建 ResourceVO 失败，resourceId={}", resourceId);
            return;
        }

        // 更新分页缓存
        // 更新第 1 页（热点数据通常在前面几页）
        updatePageCache(type, 1, updatedVO);

        // 如果库存为 0，从分页缓存中移除该资源（可选，视业务而定）
        if (updatedVO.getRemainStock() == 0) {
            log.info("资源库存为 0，从分页缓存移除，resourceId={}", resourceId);
            resourceService.removeResourceFromPageCache(type, 1, resourceId);
        }
    }

    /**
     * 更新分页缓存（尝试更新前几页）
     */
    private void updatePageCache(Integer type, int startPage, ResourceVO vo) {
        // 尝试更新第 1-3 页（通常订单影响的数据在前几页）
        for (int page = startPage; page <= 3; page++) {
            try {
                resourceService.updateResourceInPageCache(type, page, vo);
                log.debug("更新分页缓存成功，type={}, page={}, resourceId={}", type, page, vo.getId());
            } catch (Exception e) {
                log.warn("更新分页缓存失败，type={}, page={}", type, page, e);
            }
        }
    }

    /**
     * 构建最新的 ResourceVO
     */
    private ResourceVO buildResourceVO(Long resourceId, TResource resource) {
        if (resource == null) {
            resource = resourceService.getById(resourceId);
        }
        if (resource == null) {
            return null;
        }

        ResourceVO vo = new ResourceVO();
        vo.setId(resource.getId());
        vo.setName(resource.getName());
        vo.setType(resource.getType());

        // 重新计算库存汇总
        java.util.List<TResourceStock> stocks = resourceStockMapper.selectList(
            new LambdaQueryWrapper<TResourceStock>()
                .eq(TResourceStock::getResourceId, resourceId)
        );

        int totalStock = stocks.stream().mapToInt(TResourceStock::getTotalStock).sum();
        int remainStock = stocks.stream().mapToInt(TResourceStock::getRemainStock).sum();

        vo.setTotalStock(totalStock);
        vo.setRemainStock(remainStock);

        return vo;
    }
}