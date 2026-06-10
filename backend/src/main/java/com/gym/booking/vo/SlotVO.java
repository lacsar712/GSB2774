package com.gym.booking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Slot VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotVO {

    /**
     * Slot ID
     */
    private Long id;

    /**
     * 场地ID
     */
    private Long courtId;

    /**
     * 场地名称
     */
    private String courtName;

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
     * 状态
     */
    private String status;

    /**
     * 是否已被预约
     */
    private Boolean reserved;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
