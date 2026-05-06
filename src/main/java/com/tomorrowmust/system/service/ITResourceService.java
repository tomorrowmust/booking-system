package com.tomorrowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomorrowmust.system.domain.dto.ResourceDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TResource;
import com.tomorrowmust.system.domain.po.TResourceStock;
import com.tomorrowmust.system.domain.vo.ResourceVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
public interface ITResourceService extends IService<TResource> {

    /**
     * 管理员分页查询资源
     */
    Result queryResourceAdminPage(String name, Integer current);

    /**
     * 用户端分页查询资源（带缓存）
     */
    Result queryResourceUserPage(String name, Integer type, Integer current);

    /**
     * 查询资源库存明细
     */
    Result queryResourceStockById(Long id);


    /**
     * 删除资源（逻辑删除）
     */
    Result deleteResource(Long id);

    /**
     * 更新单条库存到缓存
     */
    void updateStock(Long resourceId, TResourceStock stock);

    /**
     * 删除单条库存缓存
     */
    void deleteStock(Long resourceId, Long stockId);

    /**
     * 获取单条库存缓存
     */
    TResourceStock getStock(Long resourceId, Long stockId);

    /**
     * 更新分页缓存中的单条资源
     */
    void updateResourceInPageCache(Integer type, Integer current, ResourceVO vo);

    /**
     * 从分页缓存中删除单条资源
     */
    void removeResourceFromPageCache(Integer type, Integer current, Long resourceId);

    /**
     * 清空某类型的所有分页缓存
     */
    void clearTypeCache(Integer type);

    Result updateResource(ResourceDTO resourceDTO);

    Result saveResource(ResourceDTO resourceDTO);
}
