package com.tomorrowmust.system.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ResourceStockDTO {
    private Long id;

    private Long resourceId;

    private LocalDate stockDate;

    private LocalTime slotStart;

    private LocalTime slotEnd;

    private Integer totalStock;

    private Integer remainStock;
    
    private Integer stockNum;
}
