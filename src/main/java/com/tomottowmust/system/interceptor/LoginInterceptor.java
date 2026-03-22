package com.tomottowmust.system.interceptor;

import com.tomottowmust.system.common.UserContext;
import com.tomottowmust.system.domain.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = UserContext.getUser();
        if(user==null){
            response.setStatus(401);
            return false;
        }
        return true;
    }

}
