package com.gym.booking.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 生成 Slot 请求 DTO
 */
@Data
public class GenerateSlotsRequest {

    @NotNull(message = "场地ID不能为空")
    private Long courtId;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
}
