package com.tomottowmust.system.config;

import com.tomottowmust.system.domain.po.TResourceStock;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.tomottowmust.system.domain.constant.RedisConstant.BLOOM_FILTER_RESOURCE;

@Configuration
public class BloomFilterManager {
    @Resource
    private RedissonClient redissonClient;

    @Bean
    public RBloomFilter<String> resourceStockBloomFilter(){
        RBloomFilter<String> resourceBloomFilter = redissonClient
                .getBloomFilter(BLOOM_FILTER_RESOURCE);
        resourceBloomFilter.tryInit(1_000_000, 0.01);
        return resourceBloomFilter;
    }

}
