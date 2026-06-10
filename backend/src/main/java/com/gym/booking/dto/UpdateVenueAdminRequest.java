package com.gym.booking.dto;

import lombok.Data;

/**
 * 更新球馆管理员请求 DTO
 */
@Data
public class UpdateVenueAdminRequest {

    /**
     * 状态: enabled/disabled
     */
    private String status;

    /**
     * 新密码（重置密码时使用）
     */
    private String newPassword;

    /**
     * 绑定的球馆ID
     */
    private Long venueId;
}
