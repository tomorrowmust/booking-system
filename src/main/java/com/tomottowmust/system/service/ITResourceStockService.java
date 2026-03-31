package com.tomottowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomottowmust.system.domain.dto.ResourceStockDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TResourceStock;

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
}
