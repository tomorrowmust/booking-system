package com.tomorrowmust.system.ratelimit;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;

import static cn.hutool.extra.servlet.JakartaServletUtil.getClientIP;

public class DeviceUserKeyResolver implements RateLimitKeyResolver{
    @Override
    public String resolve(ProceedingJoinPoint point, RateLimit rateLimit, HttpServletRequest request) {

        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录时 userId 为 null
        }
        String deviceId = extractDeviceId(request);
        String method = point.getSignature().getName();

        if(userId==null){
            return String.format("ratelimit:device:%s:%s", deviceId, method);
        }
        return String.format("ratelimit:user:%s:device:%s:%s", userId, deviceId, method);
    }

    private String extractDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId != null) {
            return deviceId;
        }
        deviceId = request.getHeader("Fingerprint");
        if (deviceId != null) {
            return deviceId;
        }
        return getClientIP(request); // 兜底用 IP
    }
}
