package com.tomottowmust.system.common;

import com.tomottowmust.system.domain.dto.UserDTO;

public class UserContext {
    private static final ThreadLocal<UserDTO>user=new ThreadLocal<>();

    public static void saveUser(UserDTO userDTO){
        user.set(userDTO);
    }

    public static void removeUser(){
        user.remove();
    }

    public static UserDTO getUser(){
        return user.get();
    }

}
