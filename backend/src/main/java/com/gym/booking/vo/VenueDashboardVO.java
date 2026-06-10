package com.gym.booking.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 球馆管理员仪表盘统计
 */
@Data
public class VenueDashboardVO {

    private String venueName;

    private Long totalCourts;
    private Long totalSlots;
    private Long todaySlots;

    private Long totalOrders;
    private Long todayOrders;
    private Long pendingPayOrders;
    private Long pendingVerificationOrders;
    private Long completedOrders;

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
}
