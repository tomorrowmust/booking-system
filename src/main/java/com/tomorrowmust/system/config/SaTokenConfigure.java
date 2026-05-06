package com.tomorrowmust.system.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // Sa-Token 整合 jwt
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验
            SaRouter.match("/**")
                    .notMatch("/auth/**","/doc.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/webjars/**",
                            "/user/ai/stream")
                    .check(r -> StpUtil.checkLogin());

            // 角色校验
            SaRouter.match("/admin/**", r -> StpUtil.checkRole("Admin"));

        }) {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 跳过异步转发，避免 SaTokenContext 上下文未初始化
                if (request.getDispatcherType() == DispatcherType.ASYNC) {
                    return true;
                }
                return super.preHandle(request, response, handler);
            }
        });
    }
}
