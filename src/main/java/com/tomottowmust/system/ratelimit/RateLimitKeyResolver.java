package com.tomottowmust.system.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;

public interface RateLimitKeyResolver {
    /**
     * 生成限流 Key
     * @param point 切点信息（可获取方法、参数）
     * @param rateLimit 注解配置
     * @param request HTTP 请求
     * @return 限流 Key（将作为 Redis 的 Key）
     */
    String resolve(ProceedingJoinPoint point, RateLimit rateLimit, HttpServletRequest request);

}
