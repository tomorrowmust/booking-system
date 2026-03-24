package com.tomottowmust.system.service.impl;


import com.tomottowmust.system.common.RedisIdWorker;
import com.tomottowmust.system.common.UserContext;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TBookingOrder;
import com.tomottowmust.system.domain.po.TResourceStock;
import com.tomottowmust.system.mapper.TBookingOrderMapper;
import com.tomottowmust.system.service.ITBookingOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomottowmust.system.service.ITResourceStockService;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.tomottowmust.system.domain.constant.RedisConstant.LOCK_ORDER_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Service
public class TBookingOrderServiceImpl extends ServiceImpl<TBookingOrderMapper, TBookingOrder> implements ITBookingOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ITResourceStockService resourceStockService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private ITBookingOrderService proxy;

    public static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT=new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("booking.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    @Transactional
    public Result saveBookingOrder(Long stockId) {
        //检测用户是否可以预约
        Long userId= UserContext.getUser().getId();
        //执行lua 脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                stockId.toString(),
                userId.toString()
        );
        //判断结果是否为0
        int r = result.intValue();
        if(r==3){
            return Result.fail("数据不存在！");
        }
        if (r!=0) {
            //不为0
            return Result.fail(r==1?"库存不足！":"不能重复下单！");
        }
        proxy = (ITBookingOrderService) AopContext.currentProxy();
        //TODO 消息队列
        handleOrder(stockId);
        //返回订单
        return Result.ok();
    }

    private void handleOrder(Long stockId) {
        Long userId = UserContext.getUser().getId();
        RLock lock = redissonClient.getLock(LOCK_ORDER_KEY + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("不允许重复下单");
            return;
        }
        try {
            proxy.createOrder(stockId,userId);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void createOrder(Long stockId, Long userId) {
        Long count = query().eq("user_id", userId)
                .eq("stock_id", stockId)
                .count();
        if(count>0){
            log.error("已经预约过了！");
            return;
        }
        boolean success = resourceStockService.update()
                .eq("id", stockId)
                .gt("remain_stock", 0)            // 库存必须 > 0
                .setSql("remain_stock = remain_stock - 1, version = version + 1")  // 扣减 + 版本号
                .update();

        if (!success) {
            log.error("预约失败！");
            return;
        }
        TResourceStock stock = resourceStockService.getById(stockId);
        //新增记录
        TBookingOrder order = new TBookingOrder();
        long orderNum = redisIdWorker.nextId("order");
        order.setOrderNo(Long.toString(orderNum));
        order.setSlotStart(stock.getSlotStart());
        order.setStockDate(stock.getStockDate());
        order.setResourceId(stock.getResourceId());
        order.setStockId(stockId);
        order.setUserId(userId);
        save(order);
    }
}
