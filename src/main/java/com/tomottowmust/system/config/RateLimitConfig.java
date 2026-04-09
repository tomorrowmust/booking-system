package com.tomottowmust.system.config;

import com.tomottowmust.system.ratelimit.DeviceUserKeyResolver;
import com.tomottowmust.system.ratelimit.RateLimitKeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitKeyResolver deviceUserKeyResolver() {
        return new DeviceUserKeyResolver();
    }

}