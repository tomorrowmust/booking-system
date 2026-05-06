package com.tomorrowmust.system.ai.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ToolRegistration {

    private final OrderToolsByUser orderTools;
    private final CommonTools commonTools;
    private final ResourceTools resourceTools;
    private final StockTools stockTools;

    // 注册工具
    @Bean
    public ToolCallback[] userTools() {
        return ToolCallbacks.from(
                commonTools,
                orderTools
        );
    }
    @Bean
    public ToolCallback[] adminTools() {
        return ToolCallbacks.from(
                commonTools,
                stockTools,
                resourceTools
        );
    }
}
