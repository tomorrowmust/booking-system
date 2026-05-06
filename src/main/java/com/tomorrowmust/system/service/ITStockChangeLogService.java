package com.tomorrowmust.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TStockChangeLog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
public interface ITStockChangeLogService extends IService<TStockChangeLog> {

    Result queryStockChangeLogPage(String orderNo, Long stockId, Integer current);
}
