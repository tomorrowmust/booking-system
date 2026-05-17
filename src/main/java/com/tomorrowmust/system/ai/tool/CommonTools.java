package com.tomorrowmust.system.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommonTools {

    @Tool(name="Time Tool", description="获取当前时间")
    public String getTime() {
        return LocalDateTime.now().toString();
    }
}
