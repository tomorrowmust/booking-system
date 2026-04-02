package com.tomottowmust.system.config;

import com.tomottowmust.system.interceptor.AdminInterceptor;
import com.tomottowmust.system.interceptor.LoginInterceptor;
import com.tomottowmust.system.interceptor.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    //配置拦截器
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**");
        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(
                "/auth/login",
                "/auth/code",
                "/system/**",
                "/doc.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/webjars/**"
                );
        registry.addInterceptor(new AdminInterceptor()).addPathPatterns("/admin/**");
    }
}
