package com.tomorrowmust.system.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;
import com.tomorrowmust.system.ai.model.AIProperties;

import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    private final AIProperties aiProperties;

    private String systemPrompt;

    @PostConstruct
    public String getSystem() {
        if (systemPrompt == null) {
            try {
                Resource resource = new ClassPathResource(aiProperties.getSystem());
                systemPrompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Failed to load system prompt from {}", aiProperties.getSystem(), e);
                systemPrompt = "";
            }
        }
        return systemPrompt;
    }
}
