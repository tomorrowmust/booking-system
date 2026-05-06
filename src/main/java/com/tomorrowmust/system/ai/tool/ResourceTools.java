package com.tomorrowmust.system.ai.tool;

import com.tomorrowmust.system.domain.dto.ResourceDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.vo.ResourceVO;
import com.tomorrowmust.system.service.ITResourceService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceTools {
    @Resource
    private ITResourceService resourceService;

    @Tool(name = "saveResource", description = "新增资源到系统。当用户需要创建新资源时调用，传入资源名称等必要信息。")
    public String saveResource(@ToolParam(description = "资源信息，包含名称、类型等必填字段") ResourceDTO resourceDTO) {
        Result result = resourceService.saveResource(resourceDTO);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return "资源保存成功";
    }

    @Tool(name = "updateResource", description = "修改已有资源的信息。当用户要求更新、编辑某个资源时调用，必须传入资源ID。")
    public String updateResource(@ToolParam(description = "资源信息，必须包含资源ID") ResourceDTO resourceDTO) {
        Result result = resourceService.updateResource(resourceDTO);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return "资源更新成功";
    }

    @Tool(name = "deleteResource", description = "根据ID删除指定资源。当用户要求删除资源时调用。")
    public String deleteResource(@ToolParam(description = "要删除的资源ID") Long id) {
        Result result = resourceService.deleteResource(id);
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            throw new RuntimeException(result.getErrorMsg());
        }
        return "资源删除成功";
    }

    @Tool(name = "queryResourcePage", description = "分页查询资源列表。当用户需要查看资源列表、搜索资源或翻页时调用。")
    @SuppressWarnings("unchecked")
    public List<ResourceVO> queryResourcePage(
            @ToolParam(description = "资源名称关键字，支持模糊查询，不传则查全部") String name,
            @ToolParam(description = "当前页码，从1开始") Integer current) {
        Result result = resourceService.queryResourceAdminPage(name, current);
        Object data = result.getData();
        if (data == null) {
            return null;
        }
        return (List<ResourceVO>) data;
    }
}
