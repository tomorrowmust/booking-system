package com.tomorrowmust.system.controller;

import com.tomorrowmust.system.domain.dto.ResourceStockDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.service.ITResourceStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/stock")
@Tag(name = "管理员操作库存相关接口")
public class TResourceStockAdminController {
    @Resource
    private ITResourceStockService stockService;

    @PostMapping
    @Operation(description = "新增库存数据")
    public Result saveResourceStock(@RequestBody ResourceStockDTO resourceStockDTO){
        return stockService.saveResourceStock(resourceStockDTO);
    }
    @PutMapping
    @Operation(description = "修改库存数据")
    public Result updateResourceStock(@RequestBody ResourceStockDTO resourceStockDTO){
        return stockService.updateResourceStock(resourceStockDTO);
    }
    @Operation(description = "删除库存数据")
    @DeleteMapping
    public Result deleteResourceStock(@RequestParam Long resourceId, @RequestParam Long stockId){
        return stockService.deleteResourceStock(resourceId,stockId);
    }

    @Operation(description = "根据id 查询库存数据")
    @GetMapping
    public Result getResourceStock(@RequestParam Long resourceId, @RequestParam Long stockId){
        return stockService.getResourceStock(resourceId,stockId);
    }

}
