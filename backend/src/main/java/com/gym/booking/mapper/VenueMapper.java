package com.gym.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.booking.entity.Venue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 球馆 Mapper
 */
@Mapper
public interface VenueMapper extends BaseMapper<Venue> {
}
