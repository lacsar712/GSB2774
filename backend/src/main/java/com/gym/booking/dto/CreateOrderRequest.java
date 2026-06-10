package com.gym.booking.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 创建订单请求 DTO
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "Slot ID不能为空")
    private Long slotId;
}
