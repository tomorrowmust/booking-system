package com.tomorrowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomorrowmust.system.domain.dto.LoginFormDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */

public interface ITUserService extends IService<TUser> {

    Result sendCode(String phone);

    Result register(LoginFormDTO loginFormDTO);

    Result sendRegisterCode(String phone);

    Result loginPassword(LoginFormDTO loginFormDTO);

    Result loginCode(LoginFormDTO loginFormDTO);

    Result logout();
}
