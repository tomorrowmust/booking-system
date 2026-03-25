package com.tomottowmust.system.controller;


import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@RestController
@RequestMapping("/stock-change-log")
public class TStockChangeLogController {

    @jakarta.annotation.Resource
    private com.tomottowmust.system.service.ITStockChangeLogService stockChangeLogService;

    @io.swagger.v3.oas.annotations.Operation(description = "库存变更日志分页查询（按订单号/库存ID过滤）")
    @io.swagger.v3.oas.annotations.tags.Tag(name = "库存变更日志接口")
    @org.springframework.web.bind.annotation.GetMapping("/page")
    public com.tomottowmust.system.domain.dto.Result queryStockChangeLogPage(
            @org.springframework.web.bind.annotation.RequestParam(value = "orderNo", required = false) String orderNo,
            @org.springframework.web.bind.annotation.RequestParam(value = "stockId", required = false) Long stockId,
            @org.springframework.web.bind.annotation.RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        return stockChangeLogService.queryStockChangeLogPage(orderNo, stockId, current);
    }
}
