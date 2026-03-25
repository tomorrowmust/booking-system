package com.tomottowmust.system.service.impl;


import com.tomottowmust.system.domain.po.TStockChangeLog;
import com.tomottowmust.system.mapper.TStockChangeLogMapper;
import com.tomottowmust.system.service.ITStockChangeLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Service
public class TStockChangeLogServiceImpl extends ServiceImpl<TStockChangeLogMapper, TStockChangeLog> implements ITStockChangeLogService {

    @Override
    public com.tomottowmust.system.domain.dto.Result queryStockChangeLogPage(String orderNo, Long stockId, Integer current) {
        int pageNum = (current == null || current < 1) ? 1 : current;
        int pageSize = com.tomottowmust.system.domain.constant.SystemConstants.MAX_PAGE_SIZE;

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TStockChangeLog> page = query()
                .like(cn.hutool.core.util.StrUtil.isNotBlank(orderNo), "order_no", orderNo)
                .eq(stockId != null, "stock_id", stockId)
                .page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize));

        return com.tomottowmust.system.domain.dto.Result.ok(page.getRecords(), page.getTotal());
    }
}
