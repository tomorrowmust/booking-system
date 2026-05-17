package com.tomorrowmust.system.ai.agent;

import com.tomorrowmust.system.ai.config.SystemPromptConfig;
import com.tomorrowmust.system.domain.Enum.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 路由智能体
 */
@Component
@RequiredArgsConstructor
public class RouteAgent extends AbstractAgent{

    private final SystemPromptConfig systemPromptConfig;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.ROUTE;
    }

    @Override
    public String systemMessage() {
        return systemPromptConfig.getRouteAgentPrompt();
    }

}
