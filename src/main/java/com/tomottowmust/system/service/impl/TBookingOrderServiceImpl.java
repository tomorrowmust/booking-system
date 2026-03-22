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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tomottowmust.system.domain.constant.RedisConstant.ORDER_KEY;

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


    @Override
    @Transactional
    public Result saveBookingOrder(Long resourceId, Long stockId) {
        boolean success = resourceStockService.update()
                .eq("id", stockId)
                .eq("resource_id", resourceId)
                .gt("remain_stock", 0)            // 库存必须 > 0
                .setSql("remain_stock = remain_stock - 1, version = version + 1")  // 扣减 + 版本号
                .update();

        if (!success) {
            return Result.fail("库存不足或已被抢完！");
        }
        TResourceStock stock = resourceStockService.getById(stockId);
        //新增记录
        Long userId = UserContext.getUser().getId();
        TBookingOrder order = new TBookingOrder();
        long orderNum = redisIdWorker.nextId("order");
        order.setOrderNo(Long.toString(orderNum));
        order.setSlotStart(stock.getSlotStart());
        order.setStockDate(stock.getStockDate());
        order.setResourceId(resourceId);
        order.setStockId(stockId);
        order.setUserId(userId);
        save(order);

        return Result.ok(orderNum);
    }
}
