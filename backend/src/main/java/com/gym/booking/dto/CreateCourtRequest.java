package com.gym.booking.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建场地请求 DTO
 */
@Data
public class CreateCourtRequest {

    @NotBlank(message = "场地名称不能为空")
    private String name;

    @NotBlank(message = "场地类型不能为空")
    private String type;

    @NotNull(message = "价格不能为空")
    private BigDecimal pricePerSlot;
}
