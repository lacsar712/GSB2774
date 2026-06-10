package com.gym.booking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 球馆 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueVO {

    /**
     * 球馆ID
     */
    private Long id;

    /**
     * 球馆名称
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 营业开始时间
     */
    private LocalTime openTime;

    /**
     * 营业结束时间
     */
    private LocalTime closeTime;

    /**
     * 管理员ID
     */
    private Long ownerUserId;

    /**
     * 管理员用户名
     */
    private String ownerUsername;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
