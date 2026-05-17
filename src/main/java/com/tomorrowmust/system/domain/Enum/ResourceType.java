package com.tomorrowmust.system.domain.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
@Getter
public enum ResourceType {
        BADMINTON_COURT(1, "羽毛球场地"),
        BASKETBALL_COURT(2, "篮球场地"),
        PINGPONG_ROOM(3, "乒乓球室");

        @JsonValue
        @EnumValue
        int value;
        String desc;

        ResourceType(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }


}
