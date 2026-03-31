package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.service.ITBookingOrderService;
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
@RequestMapping("/booking")
public class TBookingOrderController {

    @Resource
    private ITBookingOrderService bookingOrderService;

    @PostMapping
    @Operation(description = "新增预约")
    public Result saveBookingOrder(@RequestParam(value = "stock_id", required = true) Long stockId){
        return bookingOrderService.saveBookingOrder(stockId);
    }

    @GetMapping("/my")
    @Operation(description = "查询我的预约")
    public Result queryMyBooking(@RequestParam(value = "current", defaultValue = "1") Integer current){
        return bookingOrderService.queryMyBooking(current);
    }

    @PutMapping("/cancel/{id}")
    @Operation(description = "取消预约")
    public Result cancelBooking(@PathVariable Long id){
        return bookingOrderService.cancelBooking(id);
    }
}
