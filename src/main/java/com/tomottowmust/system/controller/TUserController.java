package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.LoginFormDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.ratelimit.RateLimit;
import com.tomottowmust.system.service.ITUserService;

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
@Tag(name = "用户验证有关接口")
@RestController
@RequestMapping("/auth")
public class TUserController {

    @Resource
    private ITUserService userService;

    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 3,
            windowSeconds = 60,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW,
            message = "发送过于频繁，请1分钟后再试"
    )
    @Operation(description = "发送验证码")
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone){
        return userService.sendCode(phone);
    }

    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 5,
            windowSeconds = 300,          // 5分钟
            algorithm = RateLimit.Algorithm.SLIDE_WINDOW,
            message = "登录尝试过多，请5分钟后再试"
    )
    @Operation(description = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }

}
