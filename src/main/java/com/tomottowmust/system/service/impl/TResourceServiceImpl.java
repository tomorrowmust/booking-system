package com.tomottowmust.system.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomottowmust.system.domain.dto.Result;
import com.tomottowmust.system.domain.po.TResource;
import com.tomottowmust.system.domain.po.TResourceStock;
import com.tomottowmust.system.domain.vo.ResourceStockVO;
import com.tomottowmust.system.domain.vo.ResourceVO;
import com.tomottowmust.system.mapper.TResourceMapper;
import com.tomottowmust.system.service.ITResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomottowmust.system.service.ITResourceStockService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tomottowmust.system.domain.constant.SystemConstants.MAX_PAGE_SIZE;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Service
public class TResourceServiceImpl extends ServiceImpl<TResourceMapper, TResource> implements ITResourceService {

    @Resource
    private ITResourceStockService stockService;

    @Override
    public Result queryResourcePage(String name, Integer current) {
        Page<TResource> page = query().like(StrUtil.isNotBlank(name), "name", name)
                .eq("status",1)
                .page(new Page<>(current, MAX_PAGE_SIZE));
        List<TResource> records = page.getRecords();
        if(records==null){
            return Result.ok(Collections.emptyList());
        }
        List<ResourceVO> vos = BeanUtil.copyToList(records, ResourceVO.class);
        List<Long> ids = records.stream().map(TResource::getId).toList();
        List<TResourceStock> stocks = stockService.query()
                .in("resource_id", ids)
                .list();
        List<ResourceStockVO> stockVOList = BeanUtil.copyToList(stocks, ResourceStockVO.class);
        Map<Long, List<ResourceStockVO>> stockMap = stockVOList.stream()
                .collect(Collectors.groupingBy(ResourceStockVO::getResourceId));
        for (ResourceVO vo : vos) {
            List<ResourceStockVO> stockList = stockMap.get(vo.getId());
            if (!stockList.isEmpty()) {
                vo.setStockList(stockList);
            }
        }
        return Result.ok(vos);
    }

    @Override
    public Result queryResourceById(Long id) {
        TResource resource = query().eq("id", id)
                .eq("status",1)
                .one();
        return Result.ok(resource);
    }
}
