package com.tomorrowmust.system.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.tomorrowmust.system.ai.advisor.RecordOptimizationAdvisor;
import com.tomorrowmust.system.ai.hook.LogAgentHook;
import com.tomorrowmust.system.ai.memory.MyChatMemoryRepository;
import com.tomorrowmust.system.ai.memory.RedisChatMemoryRepository;
import com.tomorrowmust.system.ai.tool.CommonTools;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class BookingAgentConfig {

    @Value("${booking.ai.memory.max:100}")
    private Integer maxMessages;

    // private final ToolCallbackProvider toolCallbackProvider;
    private final CommonTools commonTools;

    /**
     * 配置 ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 Advisor loggerAdvisor,
                                 Advisor messageChatMemoryAdvisor,
                                 Advisor recordOptimizationAdvisor) {  // 日志记录器
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor,messageChatMemoryAdvisor,recordOptimizationAdvisor) //添加 Advisor 功能增强
                // .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .defaultTools(commonTools)
                .build();
    }

    /**
     * 日志记录器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    /**
     * 配置 ChatMemoryRepository
     * @return
     */
    @Bean
    public ChatMemoryRepository redisChatMemoryRepository() {
        return new RedisChatMemoryRepository();
    }

    /**
     * 配置 ChatMemory
     * @param chatMemoryRepository
     * @return
     */
    @Bean
    public ChatMemory redisChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }
    /**
     * 配置 MessageChatMemoryAdvisor
     * @param chatMemory
     * @return
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
    @Bean
    public Advisor recordOptimizationAdvisor(MyChatMemoryRepository  myChatMemoryRepository) {
        return new RecordOptimizationAdvisor(myChatMemoryRepository);
    }

}
