package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.ResourceStockDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.service.ITResourceStockService;
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

    @GetMapping
    @Operation(description = "根据资源id和库存id获取库存")
    public Result getResourceStock(@RequestParam Long resourceId,Long stockId){
        return stockService.getResourceStock(resourceId,stockId);
    }
}
