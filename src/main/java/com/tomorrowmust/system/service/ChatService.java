package com.tomorrowmust.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.tomorrowmust.system.domain.vo.ChatEventVO;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 聊天
     *
     * @param question  问题
     * @param sessionId 会话id
     * @return 回答内容
     */
    Flux<ChatEventVO> chat(String question, String sessionId);

    void stop(String sessionId);

    /**
     * 获取对话id，规则：用户id_会话id
     *
     * @param sessionId 会话id
     * @return 对话id
     */
    static String getConversationId(String sessionId) {
        return StpUtil.getLoginIdAsLong() + "_" + sessionId;
    }
}