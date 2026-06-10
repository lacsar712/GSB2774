package com.gym.booking.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 球馆管理员 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueAdminVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态
     */
    private String status;

    /**
     * 绑定的球馆ID
     */
    private Long venueId;

    /**
     * 绑定的球馆名称
     */
    private String venueName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
