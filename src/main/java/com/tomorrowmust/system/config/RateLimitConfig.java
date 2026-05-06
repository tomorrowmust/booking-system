package com.tomorrowmust.system.config;

import com.tomorrowmust.system.ratelimit.DeviceUserKeyResolver;
import com.tomorrowmust.system.ratelimit.RateLimitKeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitKeyResolver deviceUserKeyResolver() {
        return new DeviceUserKeyResolver();
    }

}