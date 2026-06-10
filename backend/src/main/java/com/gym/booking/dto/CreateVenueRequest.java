package com.gym.booking.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建球馆请求 DTO
 */
@Data
public class CreateVenueRequest {

    @NotBlank(message = "球馆名称不能为空")
    private String name;

    @NotBlank(message = "地址不能为空")
    private String address;

    private String phone;

    @NotBlank(message = "营业开始时间不能为空")
    private String openTime;  // 格式: "09:00"

    @NotBlank(message = "营业结束时间不能为空")
    private String closeTime; // 格式: "22:00"

    @NotNull(message = "管理员ID不能为空")
    private Long ownerUserId;
}
