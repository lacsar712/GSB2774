package com.gym.booking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场地 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtVO {

    /**
     * 场地ID
     */
    private Long id;

    /**
     * 球馆ID
     */
    private Long venueId;

    /**
     * 场地名称
     */
    private String name;

    /**
     * 场地类型
     */
    private String type;

    /**
     * 每时段价格
     */
    private BigDecimal pricePerSlot;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
