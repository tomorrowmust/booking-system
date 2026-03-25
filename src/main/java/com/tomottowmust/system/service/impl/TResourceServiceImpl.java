package com.tomottowmust.system.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomottowmust.system.domain.dto.ResourceDTO;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tomottowmust.system.domain.constant.RedisConstant.*;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryResourceAdminPage(String name, Integer current) {
        List<ResourceVO> vos = getResourceAllVOS(name, current);
        if (vos.isEmpty()) {
            return Result.fail("数据不存在！");
        }
        return Result.ok(vos);
    }

    private @NonNull List<ResourceVO> getResourceAllVOS(String name, Integer current) {
        Page<TResource> page = query().like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, MAX_PAGE_SIZE));
        List<TResource> records = page.getRecords();
        if(CollUtil.isEmpty(records)){
            return Collections.emptyList();
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
        return vos;
    }

    @Override
    public Result queryResourceStockById(Long id) {
        String key=CACHE_RESOURCE_KEY+id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(json==null){
            List<TResourceStock> stocks = stockService.query().eq("resource_id", id)
                    .list();
            //redis 保存空值防止缓存穿透//TODO 布隆过滤器
            String  jsonStr = CollUtil.isEmpty(stocks)? "" : JSONUtil.toJsonStr(stocks);
            stringRedisTemplate.opsForValue().set(key,jsonStr);
            return Result.ok(stocks);
        }
        List<TResourceStock> stocks = JSONUtil.toList(json, TResourceStock.class);
        return Result.ok(stocks);
    }

    @Override
    @Transactional
    public Result saveOrUpdateResource(ResourceDTO resourceDTO) {
        TResource resource = BeanUtil.copyProperties(resourceDTO, TResource.class);
        saveOrUpdate(resource);
        TResourceStock stock = BeanUtil.copyProperties(resourceDTO, TResourceStock.class);
        stock.setResourceId(resource.getId());
        Long stockId = resourceDTO.getStockId();
        if(stockId !=null){
            stock.setId(stockId);
        }
        stockService.saveOrUpdate(stock);
        String key=ORDER_STOCK_KEY+stock.getId();
        //保证一致性先修改数据库 再删除redis
        //清空key 里的数据
        stringRedisTemplate.delete(key);
        //把库存数据缓存到redis
        stringRedisTemplate.opsForValue().set(key,stock.getRemainStock().toString());
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteResource(Long id) {
        update().eq("id",id)
                .set("is_deleted",1)
                .update();
        return Result.ok();
    }


    @Override
    public Result queryResourceUserPage(Integer type, Integer current) {
        //查询热点数据到缓存
        String key=CACHE_RESOURCE_TYPE_KEY+ type +":"+CACHE_PAGE_KEY+current;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(json)){
            return Result.ok(JSONUtil.toList(json,ResourceVO.class));
        }
        List<ResourceVO> vos = getResourceVOS(type, current);
        if (vos.isEmpty()) {
            return Result.fail("数据不存在！");
        }
        //保存数据到缓存
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(vos));
        return Result.ok(vos);
    }

    private List<ResourceVO> getResourceVOS(Integer type, Integer current) {
        Page<TResource> page = query()
                .eq(type!=null,"type",type)
                .eq("status",1)
                .page(new Page<>(current, MAX_PAGE_SIZE));
        List<TResource> records = page.getRecords();
        if(CollUtil.isEmpty(records)){
            return Collections.emptyList();
        }
        List<ResourceVO> vos = BeanUtil.copyToList(records, ResourceVO.class);
        List<Long> ids = records.stream().map(TResource::getId).toList();
        List<TResourceStock> stocks = stockService.query()
                .in("resource_id", ids)
                .list();
        Map<Long, List<TResourceStock>> stockMap = stocks.stream()
                .collect(Collectors.groupingBy(TResourceStock::getResourceId));
        for (ResourceVO vo : vos) {
            List<TResourceStock> stockList = stockMap.get(vo.getId());
            vo.setTotalStock(stockList.stream().mapToInt(TResourceStock::getTotalStock).sum());
            vo.setRemainStock(stockList.stream().mapToInt(TResourceStock::getRemainStock).sum());
        }
        return vos;
    }
}
