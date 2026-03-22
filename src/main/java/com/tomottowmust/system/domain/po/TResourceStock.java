package com.tomottowmust.system.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author tomorrowmust
 * @since 2026-03-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_resource_stock")

public class TResourceStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long resourceId;

    private LocalDate stockDate;

    private LocalTime slotStart;

    private LocalTime slotEnd;

    private Integer totalStock;

    private Integer remainStock;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
