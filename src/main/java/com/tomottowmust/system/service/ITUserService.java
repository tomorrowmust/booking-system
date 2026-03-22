package com.tomottowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomottowmust.system.domain.dto.LoginFormDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TUser;
import org.springframework.stereotype.Service;

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

    Result login(LoginFormDTO loginFormDTO);
}
