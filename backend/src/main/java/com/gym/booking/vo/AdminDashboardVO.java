package com.gym.booking.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 超级管理员仪表盘统计
 */
@Data
public class AdminDashboardVO {

    private Long totalVenues;
    private Long totalVenueAdmins;
    private Long totalUsers;

    private Long totalOrders;
    private Long pendingPayOrders;
    private Long paidOrders;
    private Long completedOrders;

    private Long todayOrders;

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
}
