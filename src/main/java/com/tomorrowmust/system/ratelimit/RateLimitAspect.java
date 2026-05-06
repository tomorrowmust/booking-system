package com.tomorrowmust.system.ratelimit;

import cn.dev33.satoken.stp.StpUtil;
import com.tomorrowmust.system.common.RateLimitException;
import com.tomorrowmust.system.common.RateLimitStrategy;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static cn.hutool.extra.servlet.JakartaServletUtil.getClientIP;

@Component
@Aspect
@Slf4j
class RateLimitAspect {

    @Resource
    private RateLimitKeyResolver keyResolver;

    @Resource
    private RateLimitStrategy rateLimitStrategy;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(rateLimit)")
    public void pointcut(RateLimit rateLimit) {}

    @Around("pointcut(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String limitKey = generateKey(point, rateLimit);
        if (limitKey == null) {
            return point.proceed(); // 白名单，直接放行
        }

        try {
            boolean allowed = checkLimit(limitKey, rateLimit);
            if (!allowed) {
                log.warn("Rate limit hit: key={}, qps={}", limitKey, rateLimit.qps());
                throw new RateLimitException(rateLimit.message());
            }
            return point.proceed();
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("Rate limit error, key={}", limitKey, e);
            // 故障降级：Redis 异常时放行
            return point.proceed();
        }
    }

    /**
     * 生成限流 Key
     */
    private String generateKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        String prefix = rateLimit.prefix();
        String method = point.getTarget().getClass().getSimpleName() + ":" +
                point.getSignature().getName();

        String dimension;
        HttpServletRequest request = getRequest();

        switch (rateLimit.limitType()) {
            case CUSTOM:
                if (keyResolver != null) {
                    String customKey = keyResolver.resolve(point, rateLimit, request);
                    return customKey == null ? null : prefix + ":" + customKey;
                }
                // fallback 到 IP
                dimension = getClientIP(request);
                break;
            case IP:
                dimension = getClientIP(request);
                break;
            case USER:
                dimension = getUserId();
                break;
            case DEFAULT:
            default:
                dimension = "global";
        }

        return String.format("%s:%s:%s", prefix, method, dimension);
    }

    /**
     * 检查是否允许通过
     */
    private boolean checkLimit(String key, RateLimit rateLimit) {
        switch (rateLimit.algorithm()) {
            case TOKEN_BUCKET:
                return rateLimitStrategy.tryTokenBucket(key, rateLimit);
            case SLIDE_WINDOW:
                return rateLimitStrategy.trySlideWindow(key, rateLimit);
            case FIXED_WINDOW:
                return rateLimitStrategy.tryFixedWindow(key, rateLimit);
            default:
                return true;
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String getUserId() {
        try {
            String loginId = StpUtil.getLoginIdAsString();
            return loginId != null ? loginId : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
