package com.tomottowmust.system.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.tomottowmust.system.common.RegexUtils;
import com.tomottowmust.system.domain.dto.LoginFormDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.dto.UserDTO;
import com.tomottowmust.system.domain.po.TUser;
import com.tomottowmust.system.mapper.TUserMapper;
import com.tomottowmust.system.service.ITUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tomottowmust.system.domain.constant.RedisConstant.*;

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
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号不合法！");
        }
        String code = RandomUtil.randomNumbers(6);
        //发送验证码
        log.info("验证码为 {}",code);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号不合法！");
        }
        //查询验证码是否合法
        String code = loginFormDTO.getCode();
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if(cacheCode==null||!cacheCode.equals(code)){
            return Result.fail("验证码错误！");
        }
        //查询用户是否存在
        TUser user = query().eq("status", 1)
                .eq("phone", phone)
                .eq("is_deleted", 0)
                .one();
        if(user==null){
            user = createUser(phone);
        }
        String token= UUID.randomUUID().toString();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)           // 忽略 null 字段
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())  // 所有值转为 String
        );
        String key=LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(key,userMap);
        stringRedisTemplate.expire(key,LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    private TUser createUser(String phone) {
        TUser user = new TUser();
        user.setPhone(phone);
        user.setUsername("user_"+RandomUtil.randomString(8));
        save(user);
        return user;
    }

}
