package com.tomottowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomottowmust.system.domain.dto.ResourceDTO;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TResource;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
public interface ITResourceService extends IService<TResource> {

    Result queryResourceAdminPage(String name, Integer current);

    Result queryResourceStockById(Long id);

    Result saveOrUpdateResource(ResourceDTO resourceDTO);

    Result deleteResource(Long id);

    Result queryResourceUserPage(Integer type, Integer current);
}
