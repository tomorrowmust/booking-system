package com.tomorrowmust.system.ai.agent;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tomorrowmust.system.ai.tool.ToolResultHolder;
import com.tomorrowmust.system.domain.Enum.ChatEventTypeEnum;
import com.tomorrowmust.system.domain.vo.ChatEventVO;
import com.tomorrowmust.system.service.ChatService;
import com.tomorrowmust.system.service.IChatSessionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractAgent implements Agent{

    @Resource
    private ChatClient chatClient;
    @Resource
    private ChatMemory chatMemory;
    @Resource
    private ChatModel chatModel;
    @Resource
    private IChatSessionService chatSessionService;


    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();
    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    @Override
    public Flux<ChatEventVO> processStream(String question, String sessionId) {
        // 生成请求ID
        String requestId = generateRequestId();
        // 大模型输出内容的缓存器，用于在输出中断后的数据存储
        StringBuilder outputBuilder = new StringBuilder();
        // 获取对话id
        String conversationId = ChatService.getConversationId(sessionId);
        // 生成标题
        String title = chatModel.call("根据" + question + "生成一个标题，要求标题能够概括问题核心，不超过20个字");
        // 更新会话信息
        chatSessionService.update(sessionId, title, StpUtil.getLoginIdAsLong());

        return getChatClientRequest(question, sessionId, requestId)
                .stream()
                .chatResponse()
                .doFirst(() -> GENERATE_STATUS.put(sessionId, true)) // 第一次输出内容时执行
                .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 出现异常时，删除标识
                .doOnComplete(() -> GENERATE_STATUS.remove(sessionId)) // 完成时执行，删除标识
                .doOnCancel(() -> {
                    // 当输出被取消时，保存输出的内容到历史记录中
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
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
    /**
     * 保存停止输出的记录
     *
     * @param conversationId 对话id
     * @param content   大模型输出的内容
     */
    private void saveStopHistoryRecord(String conversationId, String content) {
        this.chatMemory.add(conversationId, new AssistantMessage(content));
    }

    @Override
    public String process(String question, String sessionId) {
        // 生成请求ID
        String requestId = generateRequestId();

        return getChatClientRequest(question, sessionId, requestId)
                .call()
                .content();
    }

    @NotNull
    public ChatClient.ChatClientRequestSpec getChatClientRequest(String question, String sessionId, String requestId) {
        return chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec
                        .text(systemMessage())
                        .params(systemMessageParams()))
                .advisors(advisorSpec -> advisorSpec
                        .advisors(advisors())
                        .params(advisorParams(sessionId, requestId)))
                .tools(tools())
                .toolContext(toolContext(sessionId, requestId))
                .user(question);
    }

    private String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public void stop(String sessionId) {
        // 移除标记
        GENERATE_STATUS.remove(sessionId);
    }

    @Override
    public Map<String, Object> advisorParams(String sessionId, String requestId) {
        return Map.of(ChatMemory.CONVERSATION_ID, ChatService.getConversationId(sessionId));
    }
}
