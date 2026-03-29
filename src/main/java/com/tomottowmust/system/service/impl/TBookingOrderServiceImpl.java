package com.tomottowmust.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomottowmust.system.common.RedisIdWorker;
import com.tomottowmust.system.common.UserContext;
import com.tomottowmust.system.domain.dto.BookingMessage;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TBookingOrder;
import com.tomottowmust.system.domain.po.TResource;
import com.tomottowmust.system.domain.po.TResourceStock;
import com.tomottowmust.system.domain.vo.ResourceVO;
import com.tomottowmust.system.mapper.TBookingOrderMapper;
import com.tomottowmust.system.service.ITBookingOrderService;
import com.tomottowmust.system.service.ITResourceService;
import com.tomottowmust.system.service.ITResourceStockService;
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
    private ITResourceStockService resourceStockService;

    @Resource
    private ITResourceService resourceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

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
        boolean success = resourceStockService.update()
                .eq("id", stockId)
                .gt("remain_stock", 0)
                .setSql("remain_stock = remain_stock - 1, version = version + 1")
                .update();

        if (!success) {
            log.error("乐观锁失败，stockId={}", stockId);
            throw new RuntimeException("库存不足");
        }

        TResourceStock stock = resourceStockService.getById(stockId);
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
        save(order);

        // 缓存一致性处理
        handleCacheAfterOrder(stock);
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
        java.util.List<TResourceStock> stocks = resourceStockService.query()
                .eq("resource_id", resourceId)
                .list();

        int totalStock = stocks.stream().mapToInt(TResourceStock::getTotalStock).sum();
        int remainStock = stocks.stream().mapToInt(TResourceStock::getRemainStock).sum();

        vo.setTotalStock(totalStock);
        vo.setRemainStock(remainStock);

        return vo;
    }
}