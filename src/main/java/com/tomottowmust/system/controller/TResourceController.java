package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.service.ITResourceService;
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

@Tag(name = "资源有关接口")
@RestController
@RequestMapping("/user/resource")
public class TResourceController {

    @Resource
    private ITResourceService resourceService;

    @Operation(description = "分页查询资源 支持名字查询")
    @GetMapping("/page")
    public Result queryResourcePage(
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "current", defaultValue = "1") Integer current){
        return resourceService.queryResourceUserPage(type,current);
    }

    @Operation(description = "根据 id查询资源")
    @GetMapping("/{id}")
    public Result queryResourceById(@PathVariable Long id){
        return resourceService.queryResourceStockById(id);
    }

}
