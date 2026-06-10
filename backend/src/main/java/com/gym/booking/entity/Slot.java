package com.gym.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时段实体类
 */
@Data
@TableName("slots")
public class Slot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属场地ID
     */
    private Long courtId;

    /**
     * 日期
     */
    private LocalDate slotDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 状态: available/disabled
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
