package com.tomottowmust.system.mq;

import cn.hutool.json.JSONUtil;
import com.tomottowmust.system.domain.dto.BookingMessage;
import com.tomottowmust.system.service.ITBookingOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.tomottowmust.system.domain.constant.MqConstant.BOOKING_TOPIC;
import static com.tomottowmust.system.domain.constant.RedisConstant.LOCK_ORDER_KEY;

@Component
@RocketMQMessageListener(
        topic = BOOKING_TOPIC,
        selectorExpression = "order",
        consumerGroup = "booking-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING
)
@Slf4j
public class BookingConsumer implements RocketMQListener<String> {

    @Resource
    private ITBookingOrderService orderService;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public void onMessage(String json) {
        log.info("接收到信息{}",json);
        BookingMessage message = JSONUtil.toBean(json, BookingMessage.class);
        Long userId = message.getUserId();
        Long orderNum = message.getOrderNum();
        Long stockId = message.getStockId();
        handleOrder(stockId,userId,orderNum);
    }
    private void handleOrder(Long stockId,Long userId,Long orderNum) {
        RLock lock = redissonClient.getLock(LOCK_ORDER_KEY + userId+":"+stockId);
        boolean isLock = false;
        try {
            isLock = lock.tryLock(5, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        }

        if (!isLock) {
            log.warn("获取锁失败，触发重试: userId={}", userId);
            throw new RuntimeException("获取锁失败");
        }

        try {
            // 如果 createOrder 内部抛出异常，会触发 MQ 重试
            orderService.createOrder(stockId, userId, orderNum);
        } catch (Exception e) {
            log.error("创建订单失败: stockId={}, userId={}, orderNum={}", stockId, userId, orderNum, e);
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
