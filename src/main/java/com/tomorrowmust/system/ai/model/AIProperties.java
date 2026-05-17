package com.tomorrowmust.system.ai.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "booking.ai.prompt")
public class AIProperties {

    private String system;
    private String routeAgent;
    private String recommendAgent;
    private String bookingAgent;
    private String consultAgent;
}