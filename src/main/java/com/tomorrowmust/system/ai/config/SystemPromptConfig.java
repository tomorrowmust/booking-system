package com.tomorrowmust.system.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import com.tomorrowmust.system.ai.model.AIProperties;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@Getter
public class SystemPromptConfig {

    private final AIProperties aiProperties;

    private String systemPrompt;
    private String routeAgentPrompt;
    private String recommendAgentPrompt;
    private String bookingAgentPrompt;
    private String consultAgentPrompt;

    public SystemPromptConfig(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @PostConstruct
    public void init() {
        this.systemPrompt = loadConfig(aiProperties.getSystem());
        log.info("System Prompt Config loaded");
        this.routeAgentPrompt = loadConfig(aiProperties.getRouteAgent());
        log.info("Route Agent Prompt loaded");
        this.recommendAgentPrompt = loadConfig(aiProperties.getRecommendAgent());
        log.info("Recommend Agent Prompt loaded");
        this.bookingAgentPrompt = loadConfig(aiProperties.getBookingAgent());
        log.info("Booking Agent Prompt loaded");
        this.consultAgentPrompt = loadConfig(aiProperties.getConsultAgent());
        log.info("Consult Agent Prompt loaded");
    }

    private String loadConfig(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load prompt from {}", path, e);
            return "";
        }
    }

}
