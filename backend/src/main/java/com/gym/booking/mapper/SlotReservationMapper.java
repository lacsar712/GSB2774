package com.gym.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.booking.entity.SlotReservation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 时段占用 Mapper
 */
@Mapper
public interface SlotReservationMapper extends BaseMapper<SlotReservation> {
}
