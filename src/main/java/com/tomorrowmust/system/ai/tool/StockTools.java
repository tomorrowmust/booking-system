package com.tomorrowmust.system.ai.tool;

import com.tomorrowmust.system.domain.dto.ResourceStockDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TResourceStock;
import com.tomorrowmust.system.service.ITResourceStockService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class StockTools {
    @Resource
    private ITResourceStockService stockService;

    @Tool(name = "saveResourceStock", description = "新增库存数据")
    public String saveResourceStock(@ToolParam(description = "库存信息") ResourceStockDTO resourceStockDTO) {
        Result result = stockService.saveResourceStock(resourceStockDTO);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return "新增库存成功";
    }

    @Tool(name = "updateResourceStock", description = "修改库存数据")
    public String updateResourceStock(@ToolParam(description = "库存信息") ResourceStockDTO resourceStockDTO) {
        Result result = stockService.updateResourceStock(resourceStockDTO);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return "修改库存成功";
    }

    @Tool(name = "deleteResourceStock", description = "删除库存数据")
    public String deleteResourceStock(
            @ToolParam(description = "资源ID") Long resourceId,
            @ToolParam(description = "库存ID") Long stockId) {
        Result result = stockService.deleteResourceStock(resourceId, stockId);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return "删除库存成功";
    }

    @Tool(name = "getResourceStock", description = "根据ID查询库存数据")
    public TResourceStock getResourceStock(
            @ToolParam(description = "资源ID") Long resourceId,
            @ToolParam(description = "库存ID") Long stockId) {
        Result result = stockService.getResourceStock(resourceId, stockId);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return (TResourceStock) result.getData();
    }
}
