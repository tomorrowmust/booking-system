package com.tomorrowmust.system.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.ratelimit.RateLimit;
import com.tomorrowmust.system.service.ITBookingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Tag(name = "预约相关接口")
@RestController
@RequestMapping("/user/booking")
public class TBookingOrderController {

    @Resource
    private ITBookingOrderService bookingOrderService;

    @RateLimit(
            limitType = RateLimit.LimitType.CUSTOM,
            qps = 0.2,                    // 5秒1次 = 0.2 QPS
            burstCapacity = 3,            // 允许突发3次
            algorithm = RateLimit.Algorithm.TOKEN_BUCKET,
            message = "操作太频繁，请5秒后再试"
    )
    @PostMapping
    @Operation(summary = "新增预约")
    public Result saveBookingOrder(@RequestParam(value = "stock_id", required = true) Long stockId){
        Long userId = StpUtil.getLoginIdAsLong();
        return bookingOrderService.saveBookingOrder(stockId, userId);
    }

    @RateLimit(
            limitType = RateLimit.LimitType.USER,
            qps = 10,
            algorithm = RateLimit.Algorithm.SLIDE_WINDOW,
            windowSeconds = 1
    )
    @GetMapping("/my")
    @Operation(summary = "查询我的预约")
    public Result queryMyBooking(@RequestParam(value = "current", defaultValue = "1") Integer current){
        return bookingOrderService.queryMyBooking(current);
    }
    @RateLimit(
            limitType = RateLimit.LimitType.USER,
            qps = 5,
            windowSeconds = 60,  // 1分钟5次
            algorithm = RateLimit.Algorithm.FIXED_WINDOW,
            message = "取消操作过于频繁"
    )
    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消预约")
    public Result cancelBooking(@PathVariable Long id){
        long userId  = StpUtil.getLoginIdAsLong();
        return bookingOrderService.cancelBooking(id, userId);
    }
}
