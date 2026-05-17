package com.tomorrowmust.system.ai.tool;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TResource;
import com.tomorrowmust.system.domain.vo.ResourceStockVO;
import com.tomorrowmust.system.domain.vo.ResourceVO;
import com.tomorrowmust.system.service.ITResourceService;
import com.tomorrowmust.system.service.ITResourceStockService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ResourceTools {

    @Resource
    private ITResourceService resourceService;
    @Resource
    private ITResourceStockService stockService;

    private static final String FIELD_NAME_FORMAT = "{}_{}";  // 提取格式字符串常量

    @Tool(name = "getResourceData", description = "获取资源数据")
    @SuppressWarnings("unchecked")
    public List<ResourceVO> getResourceData(
            @ToolParam(required = false, description = "资源名称关键词，如'篮球场'、'羽毛球'、'会议室'") String resourceName,
            @ToolParam(required = false, description = "资源类型：1-羽毛球场地，2-篮球场地，3-医生诊室，4-会议室。不传则查所有类型") Integer type,
            @ToolParam(required = true, description = "页码，从1开始") Integer page) {
        Result result = resourceService.queryResourceUserPage(resourceName, type, page);
        Object data = result.getData();
        if(data == null){
            return null;
        }
        return (List<ResourceVO>) data;
    }
    @Tool(name = "getStockData", description = "获取资源库存数据")
    public List<ResourceStockVO> getStockData(@ToolParam(required = true, description = "资源ID") Long resourceId){
        return stockService.getStockByResourceId(resourceId);
    }
    @Tool(name = "getResourceById", description = "根据id查询资源")
    public TResource getResourceById(@ToolParam(required = true, description = "资源ID") Long resourceId,
                                     ToolContext toolContext){
        return Optional.ofNullable(resourceId)
                .map(id -> this.resourceService.getById(id))
                .map(resource -> {
                    // 存储数据的字段名
                    String field = StrUtil.format(FIELD_NAME_FORMAT,
                            StrUtil.lowerFirst(TResource.class.getSimpleName()),
                            resource.getId());
                    // 存储的key
                    var requestId = Convert.toStr(toolContext.getContext().get("requestId"));
                    ToolResultHolder.put(requestId, field, resource);
                    return resource;
                })
                .orElse(null);
    }
}
