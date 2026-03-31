package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.service.ITStockChangeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/admin/log")
public class TStockChangeLogController {

    @Resource
    private ITStockChangeLogService stockChangeLogService;

    @Operation(description = "库存变更日志分页查询（按订单号/库存ID过滤）")
    @Tag(name = "库存变更日志接口")
    @GetMapping("/page")
    public Result queryStockChangeLogPage(
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "stockId", required = false) Long stockId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        return stockChangeLogService.queryStockChangeLogPage(orderNo, stockId, current);
    }
}
