package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.ResourceDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.service.ITResourceService;
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
@Tag(name = "管理员操作资源相关接口")
@RestController
@RequestMapping("/admin/resource")
public class TResourceAdminController {

    @Resource
    private ITResourceService resourceService;

    @Operation(description = "新增资源")
    @PostMapping
    public Result saveResource(@RequestBody ResourceDTO resourceDTO){
        return resourceService.saveResource(resourceDTO);
    }

    @Operation(description = "修改资源")
    @PutMapping
    public Result updateResource(@RequestBody ResourceDTO resourceDTO){
        return resourceService.updateResource(resourceDTO);
    }

    @Operation(description = "删除资源")
    @DeleteMapping("/{id}")
    public Result deleteResource(@PathVariable Long id){
        return resourceService.deleteResource(id);
    }

    @Operation(description = "查询资源")
    @GetMapping
    public Result getResourcePage(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current){
        return resourceService.queryResourceAdminPage(name,current);
    }

}