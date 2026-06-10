package com.gym.booking.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建球馆管理员请求 DTO
 */
@Data
public class CreateVenueAdminRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 绑定的球馆ID（可选，创建时可以不绑定）
     */
    private Long venueId;
}
