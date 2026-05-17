package com.tomorrowmust.system.ai.agent;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.tomorrowmust.system.ai.config.SystemPromptConfig;
import com.tomorrowmust.system.ai.tool.ResourceTools;
import com.tomorrowmust.system.domain.Enum.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConsultAgent extends AbstractAgent{

    private final SystemPromptConfig systemPromptConfig;
    private final ResourceTools resourceTools;
    private final VectorStore vectorStore;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.CONSULT;
    }

    @Override
    public String systemMessage() {
        return systemPromptConfig.getConsultAgentPrompt();
    }

    @Override
    public Map<String, Object> systemMessageParams() {
        return Map.of(
                "now", DateUtil.now()
        );
    }

    @Override
    public List<Advisor> advisors() {
        // 创建RAG增强
        var qaAdvisor = QuestionAnswerAdvisor.builder(this.vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.6d).topK(6).build())
                .build();

        return List.of(qaAdvisor);
    }

    @Override
    public Object[] tools() {
        return new  Object[]{resourceTools};
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        var userId = StpUtil.getLoginIdAsLong();
        return Map.of(
                "requestId", requestId,
                "userId", userId
        );
    }
}
