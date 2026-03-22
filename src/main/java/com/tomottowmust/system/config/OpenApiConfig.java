package com.tomottowmust.system.config;

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
                    // 部署后添加云服务器地址
                    // add(new Server().url("http://your-server-ip:8080").description("生产环境"));
                }})
                // 配置 JWT 安全认证方案
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Authorization", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入 JWT Token，格式：Bearer {token}")));
    }

    @Bean
    public GroupedOpenApi bookingApi() {
        return GroupedOpenApi.builder()
                .group("预约核心业务")
                .pathsToMatch("/booking/**", "/stock/**")
                .build();
    }
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理员管理业务")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户认证业务")
                .pathsToMatch("/user/**", "/auth/**")
                .build();
    }
}
