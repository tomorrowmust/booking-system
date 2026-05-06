package com.tomorrowmust.system.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ResourceStockVO {
    private Long id;

    private Long resourceId;

    private LocalDate stockDate;

    private LocalTime slotStart;

    private LocalTime slotEnd;

    private Integer totalStock;

    private Integer remainStock;
}
