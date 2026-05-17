package com.tomorrowmust.system.ai.advisor;

import cn.hutool.core.map.MapUtil;
import com.tomorrowmust.system.ai.memory.MyChatMemoryRepository;
import com.tomorrowmust.system.domain.Enum.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

/**
 * 记录优化
 */
public class RecordOptimizationAdvisor implements BaseAdvisor {

    private final MyChatMemoryRepository myChatMemoryRepository;

    public RecordOptimizationAdvisor(MyChatMemoryRepository myChatMemoryRepository) {
        this.myChatMemoryRepository = myChatMemoryRepository;
    }

    @NotNull
    @Override
    public ChatClientRequest before(@NotNull ChatClientRequest chatClientRequest, @NotNull AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @NotNull
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, @NotNull AdvisorChain advisorChain) {
        ChatResponse response = chatClientResponse.chatResponse();
        assert response != null;
        String text = response.getResult().getOutput().getText();
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.getAgentTypeEnum(text);
        if (agentTypeEnum != null) {
            String conversationId = MapUtil.getStr(chatClientResponse.context(), ChatMemory.CONVERSATION_ID);
            myChatMemoryRepository.optimization(conversationId);
        }
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER-100;
    }
}
