package com.gym.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场地实体类
 */
@Data
@TableName("courts")
public class Court {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属球馆ID
     */
    private Long venueId;

    /**
     * 场地名称
     */
    private String name;

    /**
     * 场地类型: 羽毛球/篮球/网球等
     */
    private String type;

    /**
     * 每时段价格
     */
    private BigDecimal pricePerSlot;

    /**
     * 状态: enabled/disabled
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
