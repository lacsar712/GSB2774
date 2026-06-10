package com.gym.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.booking.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
