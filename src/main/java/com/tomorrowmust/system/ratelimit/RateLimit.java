package com.tomorrowmust.system.ratelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    enum LimitType{
        DEFAULT,      // 全局限流（所有请求共享配额）
        IP,           // 按客户端 IP 限流
        USER,         // 按用户 ID 限流
        CUSTOM        // 自定义 key（配合 keyExpression）
    }

    //限流算法
    enum Algorithm {
        TOKEN_BUCKET,    // 令牌桶（允许突发）
        SLIDE_WINDOW,    // 滑动窗口（平滑）
        FIXED_WINDOW     // 固定窗口（简单）
    }

    /** 限流 key 前缀 */
    String prefix() default "rate_limit";

    /** 限流类型 */
    LimitType limitType() default LimitType.DEFAULT;

    /** 每秒允许通过的请求数（QPS） */
    double qps() default 100;

    /** 时间窗口（秒），用于窗口类算法 */
    int windowSeconds() default 1;

    /** 令牌桶容量（突发流量上限） */
    int burstCapacity() default 100;

    /** 限流算法 */
    Algorithm algorithm() default Algorithm.TOKEN_BUCKET;

    /** 自定义限流 key（SpEL 表达式，如 #userId, #request.ip） */
    String keyExpression() default "";

    /** 提示消息 */
    String message() default "请求过于频繁，请稍后重试";

    /** 是否等待获取令牌（仅令牌桶有效） */
    boolean waitForToken() default false;

    /** 等待超时时间（毫秒） */
    long waitTimeout() default 0;
}
