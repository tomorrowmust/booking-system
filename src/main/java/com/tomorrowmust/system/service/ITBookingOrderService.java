package com.tomorrowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TBookingOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
public interface ITBookingOrderService extends IService<TBookingOrder> {

    Result saveBookingOrder(Long stockId, Long userId);

    void createOrder(Long stockId, Long userId, Long orderNum);

    Result queryMyBooking(Integer current);

    Result cancelBooking(Long id,Long userId);

}
