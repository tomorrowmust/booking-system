package com.tomorrowmust.system.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.tomorrowmust.system.ai.agent.Agent;
import com.tomorrowmust.system.domain.Enum.AgentTypeEnum;
import com.tomorrowmust.system.domain.Enum.ChatEventTypeEnum;
import com.tomorrowmust.system.domain.vo.ChatEventVO;
import com.tomorrowmust.system.service.ChatService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "booking.ai",name = "chat-type",havingValue = "ROUTE")
public class AgentServiceImpl implements ChatService {

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        //使用路由智能体处理问题
        Agent routeAgent = findAgentByType(AgentTypeEnum.ROUTE);
        String routeResult = routeAgent != null ? routeAgent.process(question, sessionId) : null;

        //将结果转化为AgentTypeEnum 如果能够成功转化 需要路由到其他的智能体执行 否则返回原始结果
        AgentTypeEnum agentType = AgentTypeEnum.getAgentTypeEnum(routeResult);
        Agent agent = findAgentByType(agentType);
        if(null == agent){
            return Flux.just(ChatEventVO.builder()
                    .eventData(routeResult)
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .build());
        }
        // 调用下一个智能体执行
        return agent.processStream(question, sessionId);
    }

    @Override
    public void stop(String sessionId) {
        Agent routeAgent = findAgentByType(AgentTypeEnum.ROUTE);
        routeAgent.stop(sessionId);
    }

    private Agent findAgentByType(AgentTypeEnum agentTypeEnum){
        if(null == agentTypeEnum){
            return null;
        }
        //查找spring容器中所有的Agent实例
        Map<String, Agent> agentMap = SpringUtil.getBeansOfType(Agent.class);
        //遍历所有实例 找到匹配的Agent实例
        for (Agent agent : agentMap.values()) {
            if (agent.getAgentType().equals(agentTypeEnum)) {
                return agent;
            }
        }
        //返回匹配的Agent实例
        return null;
    }

}
