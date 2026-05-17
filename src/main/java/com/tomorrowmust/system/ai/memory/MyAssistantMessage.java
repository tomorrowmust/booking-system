package com.tomorrowmust.system.ai.memory;

import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MyAssistantMessage extends AssistantMessage {

    private Map<String, Object> parms;

    public MyAssistantMessage(String content, Map<String, Object> metadata,
                              List<ToolCall> toolCalls, List<Media> media) {
        super(content, metadata, toolCalls, media);
    }
}
