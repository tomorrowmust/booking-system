package com.tomottowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TBookingOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
public interface ITBookingOrderService extends IService<TBookingOrder> {

    Result saveBookingOrder(Long stockId);


    void createOrder(Long stockId, Long userId, Long orderNum);
}
