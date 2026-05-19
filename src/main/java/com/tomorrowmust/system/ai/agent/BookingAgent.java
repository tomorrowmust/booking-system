package com.tomorrowmust.system.ai.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.tomorrowmust.system.ai.config.SystemPromptConfig;
import com.tomorrowmust.system.ai.tool.OrderTools;
import com.tomorrowmust.system.ai.tool.ResourceTools;
import com.tomorrowmust.system.domain.Enum.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BookingAgent extends AbstractAgent{

    private final SystemPromptConfig systemPromptConfig;
    private final OrderTools orderTools;
    private final ResourceTools resourceTools;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.BOOKING;
    }

    @Override
    public String systemMessage() {
        return systemPromptConfig.getBookingAgentPrompt();
    }

    @Override
    public Object[] tools() {
        return new Object[]{orderTools, resourceTools};
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        return Map.of(
                "requestId", requestId,
                "userId", StpUtil.getLoginIdAsLong()
        );
    }
}
