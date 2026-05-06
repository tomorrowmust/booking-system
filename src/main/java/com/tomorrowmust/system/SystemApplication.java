package com.tomorrowmust.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {
        RedisVectorStoreAutoConfiguration.class
})
@MapperScan("com.tomorrowmust.system.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }

}