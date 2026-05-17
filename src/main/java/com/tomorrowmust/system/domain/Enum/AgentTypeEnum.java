package com.tomorrowmust.system.domain.Enum;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;

@Getter
public enum AgentTypeEnum {

    ROUTE("ROUTE","路由智能体"),
    CONSULT("CONSULT","咨询智能体"),
    RECOMMEND("RECOMMEND","推荐智能体"),
    BOOKING("BOOK","预约智能体");


    private final String agentName;
    private final String desc;

    AgentTypeEnum(String agentName, String desc){
        this.agentName = agentName;
        this.desc = desc;
    }
    @Override
    public String toString() {
        return this.agentName;
    }

    //根据名称查询枚举
    public static AgentTypeEnum getAgentTypeEnum(String agentName) {
        return EnumUtil.getBy(AgentTypeEnum::getAgentName, agentName);
    }

}
