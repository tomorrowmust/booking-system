package com.tomorrowmust.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

@Data
public class ResourceVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    private Integer type;

    //全部天数总库存
    private Integer totalStock;

    //全部天数剩余库存

    private Integer remainStock;

    private List<ResourceStockVO>stockList;
}
