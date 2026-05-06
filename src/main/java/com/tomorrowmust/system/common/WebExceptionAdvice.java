package com.tomorrowmust.system.common;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.tomorrowmust.system.domain.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitException e) {
        log.warn("触发限流: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("code", 429);
        response.put("message", e.getMessage());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body(response);
    }

    @ExceptionHandler(NotLoginException.class)
    public SaResult handlerException(NotLoginException e) {
        return SaResult.error(e.getMessage());
    }


    @ExceptionHandler(NotRoleException.class)
    public SaResult handlerException(NotRoleException e) {
        return SaResult.error(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return Result.fail("服务器异常:" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统繁忙，请稍后重试");
    }
    @ExceptionHandler(GraphRunnerException.class)
    public Result handleGraphRunnerException(GraphRunnerException e) {
        log.error("ai调用异常: {}", e.getMessage(), e);
        return Result.fail("ai调用异常: " + e.getMessage());
    }

}