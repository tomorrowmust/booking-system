package com.tomottowmust.system.service.impl;


import cn.hutool.json.JSONUtil;
import com.tomottowmust.system.common.RedisIdWorker;
import com.tomottowmust.system.common.UserContext;
import com.tomottowmust.system.domain.dto.BookingMessage;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TBookingOrder;
import com.tomottowmust.system.domain.po.TResourceStock;
import com.tomottowmust.system.mapper.TBookingOrderMapper;
import com.tomottowmust.system.service.ITBookingOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomottowmust.system.service.ITResourceStockService;
import jakarta.annotation.Resource;
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
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

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
        long orderNum = redisIdWorker.nextId("order");
        BookingMessage message = new BookingMessage(stockId, userId, orderNum);
        String json = JSONUtil.toJsonStr(message);
        String destination = BOOKING_TOPIC+":"+"order";
        try {
            SendResult sendResult = rocketMQTemplate.syncSend(
                    destination,
                    json,
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
        //返回订单号
        return Result.ok(orderNum);
    }

    private void rollbackRedis(Long stockId, Long userId) {
        stringRedisTemplate.opsForValue().increment("stock:" + stockId);
        stringRedisTemplate.opsForSet().remove("order:" + stockId, userId.toString());
    }

    @Transactional
    public void createOrder(Long stockId, Long userId,Long orderNum) {
        Long count = query().eq("user_id", userId)
                .eq("stock_id", stockId)
                .count();
        if(count>0){
            log.error("已经预约过了！");
            return;
        }
        //乐观锁部分
        boolean success = resourceStockService.update()
                .eq("id", stockId)
                .gt("remain_stock", 0)
                .setSql("remain_stock = remain_stock - 1, version = version + 1")
                .update();

        if (!success) {
            log.error("乐观锁失败");
            throw new RuntimeException("库存不足");
        }
        TResourceStock stock = resourceStockService.getById(stockId);
        //新增记录
        TBookingOrder order = new TBookingOrder();
        order.setOrderNo(Long.toString(orderNum));
        order.setSlotStart(stock.getSlotStart());
        order.setStockDate(stock.getStockDate());
        order.setResourceId(stock.getResourceId());
        order.setStockId(stockId);
        order.setUserId(userId);
        save(order);
    }
}
