package com.tomorrowmust.system.controller;


import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.ratelimit.RateLimit;
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

@Tag(name = "用户操作资源有关接口")
@RestController
@RequestMapping("/user/resource")
public class TResourceController {

    @Resource
    private ITResourceService resourceService;

    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 20,
            windowSeconds = 1,
            algorithm = RateLimit.Algorithm.SLIDE_WINDOW,
            message = "查询过于频繁，请稍后再试"
    )
    @Operation(summary = "分页查询资源", description = "支持名字查询")
    @GetMapping("/page")
    public Result queryResourcePage(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "current", defaultValue = "1") Integer current){
        return resourceService.queryResourceUserPage(name,type,current);
    }

    @RateLimit(
            limitType = RateLimit.LimitType.CUSTOM,
            keyExpression = "#id",
            qps = 500,
            burstCapacity = 800,
            algorithm = RateLimit.Algorithm.TOKEN_BUCKET,
            message = "该资源访问火爆，请稍后再试"
    )
    @Operation(summary = "根据 id查询资源")
    @GetMapping("/{id}")
    public Result queryResourceById(@PathVariable Long id){
        return resourceService.queryResourceStockById(id);
    }

}
