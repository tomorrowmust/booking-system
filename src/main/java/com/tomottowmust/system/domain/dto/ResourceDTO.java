package com.tomottowmust.system.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ResourceDTO {

    private Long id;

    private String name;

    private Integer type;

    private Long stockId;

    private LocalDate stockDate;

    private LocalTime slotStart;

    private LocalTime slotEnd;

    private Integer totalStock;

    private Integer remainStock;
}
