package com.tomorrowmust.system.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomorrowmust.system.domain.constant.SystemConstants;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TStockChangeLog;
import com.tomorrowmust.system.mapper.TStockChangeLogMapper;
import com.tomorrowmust.system.service.ITStockChangeLogService;
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
    public Result queryStockChangeLogPage(String orderNo, Long stockId, Integer current) {
        int pageNum = (current == null || current < 1) ? 1 : current;
        int pageSize = SystemConstants.MAX_PAGE_SIZE;

        Page<TStockChangeLog> page = query()
                .like(StrUtil.isNotBlank(orderNo), "order_no", orderNo)
                .eq(stockId != null, "stock_id", stockId)
                .page(new Page<>(pageNum, pageSize));

        return Result.ok(page.getRecords(), page.getTotal());
    }
}
