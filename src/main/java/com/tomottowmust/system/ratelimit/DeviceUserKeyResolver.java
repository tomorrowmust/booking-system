package com.tomottowmust.system.ratelimit;

import com.tomottowmust.system.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import static cn.hutool.extra.servlet.JakartaServletUtil.getClientIP;

public class DeviceUserKeyResolver implements RateLimitKeyResolver{
    @Override
    public String resolve(ProceedingJoinPoint point, RateLimit rateLimit, HttpServletRequest request) {

        Long userId = UserContext.getUser().getId();
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
