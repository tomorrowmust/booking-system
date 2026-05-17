package com.tomorrowmust.system.controller;


import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.ratelimit.RateLimit;
import com.tomorrowmust.system.service.ITResourceStockService;
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
@RestController
@RequestMapping("/user/stock")
@Tag(name = "用户操作库存相关接口")
public class TResourceStockController {

    @Resource
    private ITResourceStockService stockService;

    @RateLimit(
            limitType = RateLimit.LimitType.USER,
            qps = 10,
            windowSeconds = 1,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW,
            message = "操作太频繁"
    )
    @GetMapping
    @Operation(summary = "根据资源id获取库存列表")
    public Result getResourceStock(@RequestParam(required = false) Long resourceId,
                                   @RequestParam(required = false) Long stockId){
        if (resourceId != null && stockId != null) {
            return stockService.getResourceStock(resourceId, stockId);
        }
        if (resourceId != null) {
            return Result.ok(stockService.getStockByResourceId(resourceId));
        }
        return Result.fail("参数错误：需要提供resourceId或resourceId+stockId");
    }
}
