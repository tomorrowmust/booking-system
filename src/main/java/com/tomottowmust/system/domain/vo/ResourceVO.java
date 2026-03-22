package com.tomottowmust.system.domain.vo;

import com.tomottowmust.system.domain.po.TResourceStock;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class ResourceVO {
    private Long id;

    private String name;

    private List<ResourceStockVO>stockList;
}
