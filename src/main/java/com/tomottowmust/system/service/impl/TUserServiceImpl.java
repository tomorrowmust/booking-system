package com.tomottowmust.system.service.impl;


import com.tomottowmust.system.domain.po.TUser;
import com.tomottowmust.system.mapper.TUserMapper;
import com.tomottowmust.system.service.ITUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Service
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements ITUserService {

}
