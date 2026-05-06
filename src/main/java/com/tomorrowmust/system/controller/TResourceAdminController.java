package com.tomorrowmust.system.controller;


import com.tomorrowmust.system.domain.dto.ResourceDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.service.ITResourceService;
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

    @Operation(summary = "新增资源")
    @PostMapping
    public Result saveResource(@RequestBody ResourceDTO resourceDTO){
        return resourceService.saveResource(resourceDTO);
    }

    @Operation(summary = "修改资源")
    @PutMapping
    public Result updateResource(@RequestBody ResourceDTO resourceDTO){
        return resourceService.updateResource(resourceDTO);
    }

    @Operation(summary = "删除资源")
    @DeleteMapping("/{id}")
    public Result deleteResource(@PathVariable Long id){
        return resourceService.deleteResource(id);
    }


    @Operation(summary = "查询资源")
    @GetMapping
    public Result getResourcePage(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current){
        return resourceService.queryResourceAdminPage(name,current);
    }

}