package com.gym.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时段占用实体类
 * 用于防止同一时段被重复预约
 */
@Data
@TableName("slot_reservations")
public class SlotReservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 时段ID（唯一约束）
     */
    private Long slotId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 状态: locked/confirmed/released
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
