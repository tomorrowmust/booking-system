package com.tomottowmust.system.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ResourceDTO {

    private Long id;

    private String name;

    private Integer type;

}
