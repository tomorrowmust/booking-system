package com.tomorrowmust.system.controller;


import com.tomorrowmust.system.domain.dto.LoginFormDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.ratelimit.RateLimit;
import com.tomorrowmust.system.service.ITUserService;

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
    @Operation(summary = "发送登录验证码")
    @PostMapping("/code/login")
    public Result sendCode(@RequestParam("phone") String phone){
        return userService.sendCode(phone);
    }

    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 3,
            windowSeconds = 60,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW,
            message = "发送过于频繁，请1分钟后再试"
    )
    @Operation(summary = "发送注册验证码")
    @PostMapping("/code/register")
    public Result sendRegisterCode(@RequestParam("phone") String phone){
        return userService.sendRegisterCode(phone);
    }

    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 5,
            windowSeconds = 300,          // 5分钟
            algorithm = RateLimit.Algorithm.SLIDE_WINDOW,
            message = "登录尝试过多，请5分钟后再试"
    )
    @Operation(summary = "用户用验证码登录")
    @PostMapping("/login/code")
    public Result login(@RequestBody LoginFormDTO loginFormDTO){
        return userService.loginCode(loginFormDTO);
    }
    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 3,
            windowSeconds = 60,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW,
            message = "登录过于频繁，请1分钟后再试"
    )
    @Operation(summary = "用户用密码登录")
    @PostMapping("/login/password")
    public Result loginPassword(@RequestBody LoginFormDTO loginFormDTO){
        return userService.loginPassword(loginFormDTO);
    }

    @RateLimit(
            limitType = RateLimit.LimitType.IP,
            qps = 3,
            windowSeconds = 60,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW,
            message = "注册过于频繁，请1分钟后再试"
    )
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody LoginFormDTO loginFormDTO){
        return userService.register(loginFormDTO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result logout(){
        return userService.logout();
    }

}
