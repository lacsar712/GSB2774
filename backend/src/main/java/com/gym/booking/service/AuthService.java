package com.gym.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.booking.constant.RoleConstants;
import com.gym.booking.dto.LoginRequest;
import com.gym.booking.dto.RegisterRequest;
import com.gym.booking.entity.User;
import com.gym.booking.exception.BusinessException;
import com.gym.booking.mapper.UserMapper;
import com.gym.booking.util.JwtUtil;
import com.gym.booking.vo.CurrentUserVO;
import com.gym.booking.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);

        if (existingUser != null) {
            throw new BusinessException(1002, "用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleConstants.USER); // 默认角色为普通用户
        user.setPhone(request.getPhone());
        user.setStatus("enabled");

        userMapper.insert(user);
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(2001, "用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(2001, "用户名或密码错误");
        }

        // 检查用户状态
        if (!"enabled".equals(user.getStatus())) {
            throw new BusinessException(2003, "账号已被禁用");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginResponse(token, user.getRole(), user.getId());
    }

    /**
     * 获取当前用户信息
     */
    public CurrentUserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(2001, "用户不存在");
        }

        return new CurrentUserVO(user.getId(), user.getUsername(), user.getRole());
    }
}
