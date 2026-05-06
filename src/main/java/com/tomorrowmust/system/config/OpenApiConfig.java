package com.tomorrowmust.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

//配置api 文档
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("高并发预约系统 API 文档")
                        .description("基于 Spring Boot 3 + Redisson + RocketMQ 的高并发预约平台")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("tomorrowmust")
                                .email("203108696@qq.com")
                                .url("https://github.com/tomorrowmust"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(new ArrayList<>() {{
                    add(new Server().url("http://localhost:8080").description("本地环境"));
                    add(new Server().url("http://8.136.215.55:8080").description("生产环境"));
                }})
                // 配置 Sa-Token 认证方案（使用 JWT 模式，token 名称为 satoken）
                .addSecurityItem(new SecurityRequirement().addList("satoken"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("satoken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("satoken")
                                .description("请输入 Sa-Token，登录成功后从响应头 satoken 中获取")));
    }

    /**
     * 用户端业务接口分组
     * 包含：预约下单、用户资源查询、用户库存查询
     * 路径：/booking/**、/user/**
     */
    @Bean
    public GroupedOpenApi bookingApi() {
        return GroupedOpenApi.builder()
                .group("用户端业务")
                .pathsToMatch("/booking/**", "/user/**")
                .build();
    }

    /**
     * 管理员业务接口分组
     * 包含：资源管理、库存管理、库存变更日志
     * 路径：/admin/**
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理员业务")
                .pathsToMatch("/admin/**")
                .build();
    }

    /**
     * 用户认证接口分组
     * 包含：验证码发送、登录、注册、登出
     * 路径：/auth/**
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户认证")
                .pathsToMatch("/auth/**")
                .build();
    }
}
