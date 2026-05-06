package com.tomorrowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomorrowmust.system.domain.dto.ResourceStockDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TResourceStock;
import com.tomorrowmust.system.domain.vo.ResourceStockVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
public interface ITResourceStockService extends IService<TResourceStock> {

    Result saveResourceStock(ResourceStockDTO resourceStockDTO);

    Result updateResourceStock(ResourceStockDTO resourceStockDTO);

    Result deleteResourceStock(Long resourceId,Long stockId);

    Result getResourceStock(Long resourceId,Long stockId);

    List<ResourceStockVO> getStockByResourceId(Long resourceId);
}
