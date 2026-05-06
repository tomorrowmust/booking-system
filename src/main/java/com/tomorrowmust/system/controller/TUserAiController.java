package com.tomorrowmust.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.vo.ChatEventVO;
import com.tomorrowmust.system.domain.vo.ChatSessionVO;
import com.tomorrowmust.system.service.ChatService;
import com.tomorrowmust.system.service.IChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/user/ai")
@Slf4j
@Tag(name = "用户AI", description = "用户AI相关接口")
public class TUserAiController {

    @Resource
    private ChatService chatService;

    @PostMapping(value = "/stream", produces = "text/event-stream")
    @Operation(summary = "调用AI回答用户问题")
    public Flux<ChatEventVO> stream(
            @RequestParam String question,
            @RequestParam String sessionId) throws GraphRunnerException {

        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("question cannot be null or blank");
        }
        return chatService.chat(question, sessionId);
    }
    @PostMapping("/stop")
    @Operation(summary = "停止AI回答")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.chatService.stop(sessionId);
    }

}
