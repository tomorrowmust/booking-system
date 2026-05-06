package com.tomorrowmust.system.common;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tomorrowmust.system.domain.Enum.PermissionEnum;
import com.tomorrowmust.system.domain.po.TUser;
import com.tomorrowmust.system.mapper.TUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private TUserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String userId = loginId.toString();
        TUser user = userMapper.selectById(Long.valueOf(userId));

        List<String> permissionList = new ArrayList<>();
        if("Admin".equals(user.getRole())) {
            permissionList.add(PermissionEnum.ADD.getCode());
            permissionList.add(PermissionEnum.DELETE.getCode());
            permissionList.add(PermissionEnum.UPDATE.getCode());
            permissionList.add(PermissionEnum.VIEW.getCode());
        }
        return permissionList;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        TUser user = userMapper.selectOne(new QueryWrapper<TUser>()
                .eq("id", loginId).select("role"));
        return new ArrayList<>(Collections.singletonList(user.getRole()));
    }

}
