package com.tomorrowmust.system.common;

import com.tomorrowmust.system.ratelimit.RateLimit;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RateLimitStrategy {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 令牌桶算法 Lua 脚本
     * KEYS[1]: 令牌桶 key
     * KEYS[2]: 上次更新时间 key
     * ARGV[1]: 速率（每秒产生令牌数）
     * ARGV[2]: 桶容量
     * ARGV[3]: 当前时间戳（毫秒）
     * ARGV[4]: 请求令牌数（通常为1）
     */
    private static final String TOKEN_BUCKET_SCRIPT =
            "local key = KEYS[1] " +
                    "local time_key = KEYS[2] " +
                    "local rate = tonumber(ARGV[1]) " +
                    "local capacity = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +
                    "local requested = tonumber(ARGV[4]) " +

                    "local last_tokens = tonumber(redis.call('get', key) or capacity) " +
                    "local last_updated = tonumber(redis.call('get', time_key) or 0) " +

                    "local delta = math.max(0, now - last_updated) / 1000.0 " +
                    "local filled_tokens = math.min(capacity, last_tokens + (delta * rate)) " +
                    "local allowed = filled_tokens >= requested " +
                    "local new_tokens = filled_tokens " +

                    "if allowed then " +
                    "    new_tokens = filled_tokens - requested " +
                    "end " +

                    "redis.call('setex', key, 60, new_tokens) " +
                    "redis.call('setex', time_key, 60, now) " +
                    "return allowed and 1 or 0";

    /**
     * 滑动窗口算法 Lua 脚本
     * KEYS[1]: 窗口 key
     * ARGV[1]: 窗口大小（毫秒）
     * ARGV[2]: 当前时间戳（毫秒）
     * ARGV[3]: 限流阈值
     */
    private static final String SLIDE_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
                    "local window = tonumber(ARGV[1]) " +
                    "local now = tonumber(ARGV[2]) " +
                    "local limit = tonumber(ARGV[3]) " +

                    "redis.call('zremrangeByScore', key, 0, now - window) " +
                    "local current = redis.call('zcard', key) " +

                    "if current < limit then " +
                    "    redis.call('zadd', key, now, now .. '_' .. math.random()) " +
                    "    redis.call('pexpire', key, window) " +
                    "    return 1 " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * 固定窗口算法 Lua 脚本
     * KEYS[1]: 计数器 key
     * ARGV[1]: 限流阈值
     * ARGV[2]: 窗口大小（秒）
     */
    private static final String FIXED_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
                    "local limit = tonumber(ARGV[1]) " +
                    "local expire = tonumber(ARGV[2]) " +

                    "local current = tonumber(redis.call('get', key) or '0') " +
                    "if current + 1 > limit then " +
                    "    return 0 " +
                    "else " +
                    "    redis.call('incr', key) " +
                    "    if current == 0 then redis.call('expire', key, expire) end " +
                    "    return 1 " +
                    "end";
    /**
     * 滑动窗口算法
     */
    public boolean trySlideWindow(String key, RateLimit rateLimit) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SLIDE_WINDOW_SCRIPT);
        script.setResultType(Long.class);

        int windowMs = rateLimit.windowSeconds() * 1000;
        int limit = (int) (rateLimit.qps() * rateLimit.windowSeconds());

        Long result = stringRedisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(windowMs),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(limit)
        );

        return result != null && result == 1;
    }

    /**
     * 固定窗口算法
     */
    public boolean tryFixedWindow(String key, RateLimit rateLimit) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(FIXED_WINDOW_SCRIPT);
        script.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf((int) rateLimit.qps()),
                String.valueOf(rateLimit.windowSeconds())
        );

        return result != null && result == 1;
    }
    /**
     * 令牌桶算法
     */
    public boolean tryTokenBucket(String key, RateLimit rateLimit) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(TOKEN_BUCKET_SCRIPT);
        script.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                script,
                List.of(key, key + ":time"),
                String.valueOf(rateLimit.qps()),
                String.valueOf(rateLimit.burstCapacity()),
                String.valueOf(System.currentTimeMillis()),
                "1"
        );

        return result != null && result == 1;
    }
}
