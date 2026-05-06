package com.tomorrowmust.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tomorrowmust.system.ai.config.SystemPromptConfig;
import com.tomorrowmust.system.ai.tool.ToolResultHolder;
import com.tomorrowmust.system.domain.Enum.ChatEventTypeEnum;
import com.tomorrowmust.system.domain.vo.ChatEventVO;
import com.tomorrowmust.system.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final SystemPromptConfig systemPromptConfig;
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();
    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        String conversationId = ChatService.getConversationId(sessionId);
        // 大模型输出内容的缓存器，用于在输出中断后的数据存储
        StringBuilder outputBuilder = new StringBuilder();
        // 生成请求id
        var requestId = IdUtil.fastSimpleUUID();

        return chatClient.prompt()
                .system(promptSystem -> promptSystem
                        .text(systemPromptConfig.getSystemPrompt()) // 设置系统提示语
                        .param("now", DateUtil.now()) // 设置当前时间的参数
                )
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .toolContext(Map.of("requestId", requestId,"userId", StpUtil.getLoginIdAsLong()))
                .stream()
                .chatResponse()
                .doFirst(() -> GENERATE_STATUS.put(sessionId, true)) // 第一次输出内容时执行
                .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 出现异常时，删除标识
                .doOnComplete(() -> GENERATE_STATUS.remove(sessionId)) // 完成时执行，删除标识
                .takeWhile(response -> { // 通过返回值来控制Flux流是否继续，true：继续，false：终止
                    return GENERATE_STATUS.getOrDefault(sessionId, false);
                })
                .map(chatResponse -> {
                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals("STOP", finishReason)) {
                        var messageId = chatResponse.getMetadata().getId();
                        ToolResultHolder.put(messageId, "requestId", requestId);
                    }
                    // 获取大模型的输出的内容
                    String text = chatResponse.getResult().getOutput().getText();
                    // 追加到输出内容中
                    outputBuilder.append(text);
                    // 封装响应对象
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        ToolResultHolder.remove(requestId); // 清除参数列表

                        // 响应给前端的参数数据
                        var chatEventVO = ChatEventVO.builder()
                                .eventData(map)
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build();
                        return Flux.just(chatEventVO, STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }

    @Override
    public void stop(String sessionId) {
        // 移除标记
        GENERATE_STATUS.remove(sessionId);
    }

}