package com.tomottowmust.system.common;

public class UserContext {
    private static final ThreadLocal<UserContext>user=new ThreadLocal<>();

    public static void saveUser(UserContext userDTO){
        user.set(userDTO);
    }

    public static void removeUser(){
        user.remove();
    }

    public static UserContext getUser(){
        return user.get();
    }

}
