package com.gym.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.booking.entity.Slot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 时段 Mapper
 */
@Mapper
public interface SlotMapper extends BaseMapper<Slot> {
}
