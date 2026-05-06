package com.tomorrowmust.system.ai.tool;

import com.tomorrowmust.system.domain.dto.Result;
import com.tomorrowmust.system.domain.po.TBookingOrder;
import com.tomorrowmust.system.service.ITBookingOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OrderToolsByUser {

    @Resource
    private ITBookingOrderService orderService;

    @Tool(name = "queryMyBooking", description = "查询当前用户预约")
    public List<TBookingOrder> queryMyBooking(ToolContext toolContext) {
        Long userId = getUserIdFromContext(toolContext);
        return orderService.lambdaQuery()
                .eq(TBookingOrder::getUserId, userId).list();
    }

    @Tool(name = "cancelBooking", description = "取消当前用户预约")
    public void cancelBooking(
            @ToolParam(required = true, description = "预约ID") Long stockId,
            ToolContext toolContext) {
        Long userId = getUserIdFromContext(toolContext);
        orderService.cancelBooking(stockId, userId);
    }

    @Tool(name = "createOrder", description = "创建预约,调用前需要查询该用户是否预约和库存是否充足,返回预约号")
    public long createOrder(
            @ToolParam(required = true, description = "库存ID") Long stockId,
            ToolContext toolContext) {
        Long userId = getUserIdFromContext(toolContext);
        Result result = orderService.saveBookingOrder(stockId, userId);
        return (long) result.getData();
    }

    /**
     * 从 ToolContext 中获取 userId
     */
    private Long getUserIdFromContext(ToolContext toolContext) {
        if (toolContext == null) {
            throw new RuntimeException("工具上下文为空");
        }
        
        // 从 context 中获取 metadata
        Map<String, Object> context = toolContext.getContext();
        Object userIdObj = context.get("userId");
        
        if (userIdObj == null) {
            log.error("从 ToolContext 中获取 userId 失败");
            throw new RuntimeException("用户未登录");
        }
        
        return ((Number) userIdObj).longValue();
    }

}
