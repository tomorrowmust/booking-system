package com.tomorrowmust.system.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.tomorrowmust.system.common.RegexUtils;
import com.tomorrowmust.system.domain.dto.LoginFormDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TUser;
import com.tomorrowmust.system.mapper.TUserMapper;
import com.tomorrowmust.system.service.ITUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.tomorrowmust.system.domain.constant.RedisConstant.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Service
@Slf4j
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements ITUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        return sendCodeToPhone(phone, LOGIN_CODE_KEY, LOGIN_CODE_TTL);
    }

    @Override
    public Result sendRegisterCode(String phone) {
        return sendCodeToPhone(phone, REGISTER_CODE_KEY, REGISTER_CODE_TTL);
    }

    @Override
    public Result loginPassword(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();
        Result phoneError = validatePhone(phone);
        if (phoneError != null) return phoneError;

        String password = loginFormDTO.getPassword();
        if (password == null) {
            return Result.fail("密码不能为空！");
        }
        //密码匹配
        TUser user = queryActiveUserByPhone(phone);
        if (user == null || !user.getPassword().equals(DigestUtil.md5Hex(password))) {
            return Result.fail("密码错误！");
        }
        StpUtil.login(user.getId());
        StpUtil.getSession().set("phone", phone);
        return Result.ok();
    }

    @Override
    public Result loginCode(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();
        Result phoneError = validatePhone(phone);
        if (phoneError != null) return phoneError;

        //查询验证码是否合法
        Result codeError = validateCode(loginFormDTO.getCode(), phone, LOGIN_CODE_KEY);
        if (codeError != null) return codeError;

        //查询用户是否存在
        TUser user = queryActiveUserByPhone(phone);
        if (user == null) {
            return Result.fail("用户不存在！");
        }
        StpUtil.login(user.getId(),new SaLoginParameter()
                .setExtra("phone", phone)
        );

        return Result.ok();
    }

    @Override
    public Result logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @Override
    public Result register(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();
        Result phoneError = validatePhone(phone);
        if (phoneError != null) return phoneError;

        //查询验证码是否合法
        Result codeError = validateCode(loginFormDTO.getCode(), phone, REGISTER_CODE_KEY);
        if (codeError != null) return codeError;

        String password = loginFormDTO.getPassword();
        if (password == null || password.length() < 6) {
            return Result.fail("密码不能为空或长度小于6!");
        } else if (!password.equals(loginFormDTO.getConfirmPassword())) {
            return Result.fail("密码不一致!");
        }
        //查询用户是否存在
        TUser user = queryActiveUserByPhone(phone);
        if (user != null) {
            return Result.fail("用户已存在！");
        }
        createUser(phone, password);
        return Result.ok();
    }

    private void createUser(String phone, String password) {
        TUser user = new TUser();
        user.setPhone(phone);
        user.setUsername("user_" + RandomUtil.randomString(8));
        user.setPassword(DigestUtil.md5Hex(password));
        save(user);
    }

    /**
     * 校验手机号是否合法
     */
    private Result validatePhone(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号不合法！");
        }
        return null;
    }

    /**
     * 校验验证码是否正确
     */
    private Result validateCode(String code, String phone, String redisKeyPrefix) {
        if (code != null) {
            String cacheCode = stringRedisTemplate.opsForValue().get(redisKeyPrefix + phone);
            if (cacheCode == null || !cacheCode.equals(code)) {
                return Result.fail("验证码错误！");
            }
        }
        return null;
    }

    /**
     * 查询有效用户
     */
    private TUser queryActiveUserByPhone(String phone) {
        return query().eq("status", 1)
                .eq("phone", phone)
                .eq("is_deleted", 0)
                .one();
    }

    /**
     * 发送验证码到手机
     */
    private Result sendCodeToPhone(String phone, String redisKey, long ttl) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号不合法！");
        }
        String code = RandomUtil.randomNumbers(6);
        //发送验证码
        log.info("验证码为 {}", code);
        stringRedisTemplate.opsForValue().set(redisKey + phone, code, ttl, TimeUnit.MINUTES);
        return Result.ok();
    }

}
