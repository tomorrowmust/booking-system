package com.tomottowmust.system.controller;


import com.tomottowmust.system.domain.dto.LoginFormDTO;
import com.tomottowmust.system.domain.dto.Result;
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
@Tag(name = "用户有关接口")
@RestController
@RequestMapping("/user")
public class TUserController {

    @Resource
    private ITUserService userService;

    @Operation(description = "发送验证码")
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone){
        return userService.sendCode(phone);
    }

    @Operation(description = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }
}
