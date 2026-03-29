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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
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

    private static final String NULL_TYPE = "0";

    private String buildPageCacheKey(Integer type, Integer current) {
        String typeStr = (type == null) ? NULL_TYPE : type.toString();
        return CACHE_RESOURCE_TYPE_KEY + typeStr + ":" + CACHE_PAGE_KEY + current;
    }

    // ==================== 管理员接口 ====================

    @Override
    public Result queryResourceAdminPage(String name, Integer current) {
        List<ResourceVO> vos = getResourceAllVOS(name, current);
        if (vos.isEmpty()) {
            return Result.fail("数据不存在！");
        }
        return Result.ok(vos);
    }

    private List<ResourceVO> getResourceAllVOS(String name, Integer current) {
        Page<TResource> page = query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, MAX_PAGE_SIZE));
        List<TResource> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
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
            if (stockList != null && !stockList.isEmpty()) {
                vo.setStockList(stockList);
            }
        }
        return vos;
    }

    // ==================== 用户端分页查询（Hash 存储）====================

    @Override
    public Result queryResourceUserPage(Integer type, Integer current) {
        String hashKey = buildPageCacheKey(type, current);

        // 1. 先检查是否有空值标记（防缓存穿透）
        Boolean hasEmpty = stringRedisTemplate.opsForHash().hasKey(hashKey, EMPTY_FLAG);
        if (hasEmpty) {
            return Result.fail("数据不存在！");
        }

        // 2. 尝试从 Hash 中获取分页数据
        List<ResourceVO> cachedList = getPageFromHash(hashKey);
        if (!cachedList.isEmpty()) {
            return Result.ok(cachedList);
        }

        // 3. 缓存未命中，查数据库
        List<ResourceVO> vos = getResourceVOS(type, current);

        // 4. 防缓存穿透：空数据标记
        if (CollUtil.isEmpty(vos)) {
            stringRedisTemplate.opsForHash().put(hashKey, EMPTY_FLAG, "1");
            stringRedisTemplate.expire(hashKey, Duration.ofMinutes(1));
            return Result.fail("数据不存在！");
        }

        // 5. 存入 Hash（逐条存储，便于单独更新）
        savePageToHash(hashKey, vos);
        stringRedisTemplate.expire(hashKey, Duration.ofMinutes(1));

        return Result.ok(vos);
    }

    /**
     * 从 Hash 中获取分页数据
     */
    private List<ResourceVO> getPageFromHash(String hashKey) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(hashKey);

        List<ResourceVO> list = new ArrayList<>();
        entries.forEach((field, value) -> {
            String fieldStr = field.toString();
            // 只取 item: 开头的数据，排除 meta 和空值标记
            if (fieldStr.startsWith(PAGE_ITEM_PREFIX) && !fieldStr.equals(EMPTY_FLAG)) {
                ResourceVO vo = JSONUtil.toBean(value.toString(), ResourceVO.class);
                list.add(vo);
            }
        });

        // 按 ID 排序保持顺序
        list.sort(Comparator.comparing(ResourceVO::getId));
        return list;
    }

    /**
     * 将分页数据存入 Hash
     */
    private void savePageToHash(String hashKey, List<ResourceVO> vos) {
        Map<String, String> map = new HashMap<>();

        // 逐条存储，field = item:资源ID
        for (ResourceVO vo : vos) {
            String field = PAGE_ITEM_PREFIX + vo.getId();
            map.put(field, JSONUtil.toJsonStr(vo));
        }

        // 存储元数据
        Map<String, Object> meta = new HashMap<>();
        meta.put("total", vos.size());
        meta.put("timestamp", System.currentTimeMillis());
        map.put(PAGE_META, JSONUtil.toJsonStr(meta));

        stringRedisTemplate.opsForHash().putAll(hashKey, map);
    }

    /**
     * 更新/新增单条资源到缓存
     * 支持 type 为 null
     */
    @Override
    public void updateResourceInPageCache(Integer type, Integer current, ResourceVO vo) {
        String hashKey = buildPageCacheKey(type, current);

        // 检查 key 是否存在
        Boolean exists = stringRedisTemplate.hasKey(hashKey);
        if (!exists) {
            return; // 缓存不存在，不处理
        }

        // 直接更新/新增该资源
        String field = PAGE_ITEM_PREFIX + vo.getId();
        stringRedisTemplate.opsForHash().put(hashKey, field, JSONUtil.toJsonStr(vo));
    }

    /**
     * 从分页缓存中删除单条资源
     * 支持 type 为 null
     */
    @Override
    public void removeResourceFromPageCache(Integer type, Integer current, Long resourceId) {
        String hashKey = buildPageCacheKey(type, current);
        String field = PAGE_ITEM_PREFIX + resourceId;

        stringRedisTemplate.opsForHash().delete(hashKey, field);

        // 检查是否删空了
        Long size = stringRedisTemplate.opsForHash().size(hashKey);
        if (size != null && size <= 1) { // 只剩 meta 或为空
            stringRedisTemplate.delete(hashKey);
        }
    }

    /**
     * 清空某类型的所有分页缓存
     * 支持 type 为 null
     */
    @Override
    public void clearTypeCache(Integer type) {
        String typeStr = (type == null) ? NULL_TYPE : type.toString();
        String pattern = CACHE_RESOURCE_TYPE_KEY + typeStr + ":" + CACHE_PAGE_KEY + "*";

        // 使用 scan 安全删除
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        Cursor<byte[]> cursor = stringRedisTemplate.executeWithStickyConnection(
                redisConnection -> redisConnection.scan(options)
        );

        if (cursor != null) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next(), StandardCharsets.UTF_8);
                stringRedisTemplate.delete(key);
            }
        }
    }

    // ==================== 库存明细缓存（Hash 存储）====================

    @Override
    public Result queryResourceStockById(Long id) {
        String key = CACHE_RESOURCE_KEY + id;

        // 检查是否有空值标记
        Boolean hasEmptyFlag = stringRedisTemplate.opsForHash().hasKey(key, EMPTY_FLAG);
        if (Boolean.TRUE.equals(hasEmptyFlag)) {
            return Result.ok(Collections.emptyList());
        }

        // 获取该资源下的所有库存记录
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);

        // 3. 缓存未命中，查数据库
        if (entries.isEmpty()) {
            List<TResourceStock> stocks = stockService.query()
                    .eq("resource_id", id)
                    .list();

            // 防止缓存穿透
            if (CollUtil.isEmpty(stocks)) {
                stringRedisTemplate.opsForHash().put(key, EMPTY_FLAG, "1");
                stringRedisTemplate.expire(key, Duration.ofMinutes(5));
                return Result.ok(Collections.emptyList());
            }

            // 批量存入 Hash：field=stockId, value=JSON
            Map<String, String> stockMap = stocks.stream()
                    .collect(Collectors.toMap(
                            stock -> String.valueOf(stock.getId()),
                            JSONUtil::toJsonStr
                    ));

            stringRedisTemplate.opsForHash().putAll(key, stockMap);
            stringRedisTemplate.expire(key, Duration.ofMinutes(30));
            return Result.ok(stocks);
        }

        // 4. 缓存命中，反序列化
        List<TResourceStock> stocks = entries.values().stream()
                .map(obj -> JSONUtil.toBean(obj.toString(), TResourceStock.class))
                .collect(Collectors.toList());

        return Result.ok(stocks);
    }

    /**
     * 更新单条库存到缓存
     */
    @Override
    public void updateStock(Long resourceId, TResourceStock stock) {
        String key = CACHE_RESOURCE_KEY + resourceId;
        stringRedisTemplate.opsForHash().put(
                key,
                String.valueOf(stock.getId()),
                JSONUtil.toJsonStr(stock)
        );
    }

    /**
     * 删除单条库存
     */
    @Override
    public void deleteStock(Long resourceId, Long stockId) {
        String key = CACHE_RESOURCE_KEY + resourceId;
        stringRedisTemplate.opsForHash().delete(key, String.valueOf(stockId));
    }

    /**
     * 获取单条库存（无需加载整个列表）
     */
    @Override
    public TResourceStock getStock(Long resourceId, Long stockId) {
        String key = CACHE_RESOURCE_KEY + resourceId;
        Object value = stringRedisTemplate.opsForHash().get(key, String.valueOf(stockId));
        return value != null ? JSONUtil.toBean(value.toString(), TResourceStock.class) : null;
    }

    // ==================== 资源保存/更新 ====================

    @Override
    @Transactional
    public Result saveOrUpdateResource(ResourceDTO resourceDTO) {
        TResource resource = BeanUtil.copyProperties(resourceDTO, TResource.class);
        saveOrUpdate(resource);

        TResourceStock stock = BeanUtil.copyProperties(resourceDTO, TResourceStock.class);
        stock.setResourceId(resource.getId());
        Long stockId = resourceDTO.getStockId();
        if (stockId != null) {
            stock.setId(stockId);
        }
        stockService.saveOrUpdate(stock);

        // 处理订单库存缓存
        String key = ORDER_STOCK_KEY + stock.getId();
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForValue().set(key, stock.getRemainStock().toString());

        // 清空该资源类型的分页缓存（因为资源信息可能变化）
        clearTypeCache(resource.getType());

        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteResource(Long id) {
        // 先获取资源信息（需要 type 来清空缓存）
        TResource resource = getById(id);

        update().eq("id", id)
                .set("is_deleted", 1)
                .update();

        // 清空该类型的分页缓存
        if (resource != null) {
            clearTypeCache(resource.getType());
        }

        return Result.ok();
    }

    // ==================== 私有工具方法 ====================

    private List<ResourceVO> getResourceVOS(Integer type, Integer current) {
        Page<TResource> page = query()
                .eq(type != null, "type", type)
                .eq("status", 1)
                .page(new Page<>(current, MAX_PAGE_SIZE));

        List<TResource> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
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
            if (stockList != null && !stockList.isEmpty()) {
                vo.setTotalStock(stockList.stream().mapToInt(TResourceStock::getTotalStock).sum());
                vo.setRemainStock(stockList.stream().mapToInt(TResourceStock::getRemainStock).sum());
            } else {
                vo.setTotalStock(0);
                vo.setRemainStock(0);
            }
        }
        return vos;
    }
}
