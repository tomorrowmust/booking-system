package com.tomorrowmust.system.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingMessage {
    private Long stockId;
    private Long userId;
    private Long orderNum;
}
