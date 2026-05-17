package com.tomorrowmust.system.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.tomorrowmust.system.domain.dto.ResourceStockDTO;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TResourceStock;
import com.tomorrowmust.system.domain.vo.ResourceStockVO;
import com.tomorrowmust.system.mapper.TResourceStockMapper;
import com.tomorrowmust.system.service.ITResourceService;
import com.tomorrowmust.system.service.ITResourceStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.tomorrowmust.system.domain.constant.RedisConstant.ORDER_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Service
public class TResourceStockServiceImpl extends ServiceImpl<TResourceStockMapper, TResourceStock> implements ITResourceStockService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ITResourceService resourceService;

    @PostConstruct
    private void saveStockToRedis(){
        List<TResourceStock> stocks = query().list();
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (TResourceStock stock : stocks) {
                String key = ORDER_STOCK_KEY + stock.getId();
                String value = String.valueOf(stock.getRemainStock());

                connection.stringCommands().set(
                        key.getBytes(),
                        value.getBytes(),
                        Expiration.persistent(),
                        RedisStringCommands.SetOption.SET_IF_ABSENT
                );
            }
            return null;
        });
    }


    @Override
    public Result saveResourceStock(ResourceStockDTO resourceStockDTO) {
        TResourceStock stock = BeanUtil.copyProperties(resourceStockDTO, TResourceStock.class);
        stock.setId(null);
        
        // 使用stockNum设置库存（前端传的是stockNum）
        if (resourceStockDTO.getStockNum() != null) {
            stock.setTotalStock(resourceStockDTO.getStockNum());
            stock.setRemainStock(resourceStockDTO.getStockNum());
        } else if (resourceStockDTO.getTotalStock() != null) {
            stock.setRemainStock(resourceStockDTO.getTotalStock());
        }
        
        save(stock);
        // 处理订单库存缓存
        String key = ORDER_STOCK_KEY + stock.getId();
        stringRedisTemplate.opsForValue().set(key, stock.getRemainStock().toString());
        resourceService.deleteStock(stock.getResourceId(),stock.getId());
        return Result.ok();
    }

    @Override
    public Result updateResourceStock(ResourceStockDTO resourceStockDTO) {
        TResourceStock stock = BeanUtil.copyProperties(resourceStockDTO, TResourceStock.class);
        stock.setRemainStock(stock.getTotalStock());
        updateById(stock);
        // 处理订单库存缓存
        String key = ORDER_STOCK_KEY + stock.getId();
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForValue().set(key, stock.getRemainStock().toString());
        resourceService.deleteStock(stock.getResourceId(),stock.getId());
        return Result.ok();
    }

    @Override
    public Result deleteResourceStock(Long resourceId,Long stockId) {
        //判断数据是否存在
        Long count = query().eq("resource_id", resourceId)
                .eq("id", stockId).count();
        if(count==null){
            return Result.fail("数据不存在！");
        }
        removeById(stockId);
        String key = ORDER_STOCK_KEY + stockId;
        stringRedisTemplate.delete(key);
        resourceService.deleteStock(resourceId,stockId);
        return Result.ok();
    }

    @Override
    public Result getResourceStock(Long resourceId,Long stockId) {
        //判断是否存在
        TResourceStock stock = resourceService.getStock(resourceId, stockId);
        if(stock==null){
            return Result.fail("数据不存在");
        }
        return Result.ok(stock);
    }

    @Override
    public List<ResourceStockVO> getStockByResourceId(Long resourceId) {
        List<TResourceStock> stocks = lambdaQuery().eq(TResourceStock::getResourceId, resourceId).list();
        return stocks.stream()
                .map(stock -> BeanUtil.copyProperties(stock, ResourceStockVO.class))
                .collect(Collectors.toList());
    }

}
