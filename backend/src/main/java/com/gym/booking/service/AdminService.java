package com.gym.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gym.booking.constant.RoleConstants;
import com.gym.booking.dto.CreateVenueAdminRequest;
import com.gym.booking.dto.CreateVenueRequest;
import com.gym.booking.dto.UpdateVenueAdminRequest;
import com.gym.booking.entity.Order;
import com.gym.booking.entity.User;
import com.gym.booking.entity.Venue;
import com.gym.booking.exception.BusinessException;
import com.gym.booking.mapper.OrderMapper;
import com.gym.booking.mapper.UserMapper;
import com.gym.booking.mapper.VenueMapper;
import com.gym.booking.vo.AdminDashboardVO;
import com.gym.booking.vo.VenueAdminVO;
import com.gym.booking.vo.VenueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 超级管理员服务
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final VenueMapper venueMapper;
    private final OrderMapper orderMapper;
    private final PasswordEncoder passwordEncoder;

    private BigDecimal sumOrderAmount(LambdaQueryWrapper<Order> queryWrapper) {
        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(Order::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取平台仪表盘统计
     */
    public AdminDashboardVO getDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();

        LambdaQueryWrapper<User> venueAdminQuery = new LambdaQueryWrapper<>();
        venueAdminQuery.eq(User::getRole, RoleConstants.VENUE_ADMIN);

        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getRole, RoleConstants.USER);

        LambdaQueryWrapper<Order> pendingPayQuery = new LambdaQueryWrapper<>();
        pendingPayQuery.eq(Order::getStatus, "PENDING_PAY");

        LambdaQueryWrapper<Order> paidQuery = new LambdaQueryWrapper<>();
        paidQuery.eq(Order::getStatus, "PAID");

        LambdaQueryWrapper<Order> completedQuery = new LambdaQueryWrapper<>();
        completedQuery.eq(Order::getStatus, "COMPLETED");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<Order> todayOrdersQuery = new LambdaQueryWrapper<>();
        todayOrdersQuery.ge(Order::getCreatedAt, startOfToday)
                .lt(Order::getCreatedAt, startOfTomorrow);

        LambdaQueryWrapper<Order> totalRevenueQuery = new LambdaQueryWrapper<>();
        totalRevenueQuery.in(Order::getStatus, "PAID", "COMPLETED");

        LambdaQueryWrapper<Order> todayRevenueQuery = new LambdaQueryWrapper<>();
        todayRevenueQuery.in(Order::getStatus, "PAID", "COMPLETED")
                .ge(Order::getPaidAt, startOfToday)
                .lt(Order::getPaidAt, startOfTomorrow);

        vo.setTotalVenues(venueMapper.selectCount(null));
        vo.setTotalVenueAdmins(userMapper.selectCount(venueAdminQuery));
        vo.setTotalUsers(userMapper.selectCount(userQuery));

        vo.setTotalOrders(orderMapper.selectCount(null));
        vo.setPendingPayOrders(orderMapper.selectCount(pendingPayQuery));
        vo.setPaidOrders(orderMapper.selectCount(paidQuery));
        vo.setCompletedOrders(orderMapper.selectCount(completedQuery));
        vo.setTodayOrders(orderMapper.selectCount(todayOrdersQuery));

        vo.setTotalRevenue(sumOrderAmount(totalRevenueQuery));
        vo.setTodayRevenue(sumOrderAmount(todayRevenueQuery));

        return vo;
    }

    /**
     * 获取所有球馆管理员
     */
    public List<VenueAdminVO> getVenueAdmins() {
        // 查询所有球馆管理员
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, RoleConstants.VENUE_ADMIN);
        queryWrapper.orderByDesc(User::getCreatedAt);
        List<User> admins = userMapper.selectList(queryWrapper);

        // 查询所有球馆
        List<Venue> venues = venueMapper.selectList(null);
        Map<Long, Venue> venueMap = venues.stream()
                .collect(Collectors.toMap(Venue::getOwnerUserId, v -> v, (v1, v2) -> v1));

        // 转换为 VO
        List<VenueAdminVO> result = new ArrayList<>();
        for (User admin : admins) {
            VenueAdminVO vo = new VenueAdminVO();
            vo.setId(admin.getId());
            vo.setUsername(admin.getUsername());
            vo.setPhone(admin.getPhone());
            vo.setStatus(admin.getStatus());
            vo.setCreatedAt(admin.getCreatedAt());

            // 查找绑定的球馆
            Venue venue = venueMap.get(admin.getId());
            if (venue != null) {
                vo.setVenueId(venue.getId());
                vo.setVenueName(venue.getName());
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 创建球馆管理员
     */
    @Transactional
    public void createVenueAdmin(CreateVenueAdminRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);

        if (existingUser != null) {
            throw new BusinessException(1002, "用户名已存在");
        }

        // 创建球馆管理员账号
        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRole(RoleConstants.VENUE_ADMIN);
        admin.setPhone(request.getPhone());
        admin.setStatus("enabled");

        userMapper.insert(admin);

        // 如果指定了球馆ID，更新球馆的 owner_user_id
        if (request.getVenueId() != null) {
            Venue venue = venueMapper.selectById(request.getVenueId());
            if (venue == null) {
                throw new BusinessException(1003, "球馆不存在");
            }
            if (venue.getOwnerUserId() != null) {
                throw new BusinessException(1009, "球馆已绑定管理员");
            }
            venue.setOwnerUserId(admin.getId());
            venueMapper.updateById(venue);
        }
    }

    /**
     * 更新球馆管理员
     */
    @Transactional
    public void updateVenueAdmin(Long id, UpdateVenueAdminRequest request) {
        User admin = userMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(1004, "管理员不存在");
        }

        if (!RoleConstants.VENUE_ADMIN.equals(admin.getRole())) {
            throw new BusinessException(1005, "该用户不是球馆管理员");
        }

        // 更新状态
        if (request.getStatus() != null) {
            admin.setStatus(request.getStatus());
        }

        // 重置密码
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userMapper.updateById(admin);

        // 更新球馆绑定
        if (request.getVenueId() != null) {
            Venue targetVenue = venueMapper.selectById(request.getVenueId());
            if (targetVenue == null) {
                throw new BusinessException(1003, "球馆不存在");
            }
            if (targetVenue.getOwnerUserId() != null && !id.equals(targetVenue.getOwnerUserId())) {
                throw new BusinessException(1009, "球馆已绑定其他管理员");
            }

            // 先解除旧的绑定
            LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Venue::getOwnerUserId, id);
            List<Venue> oldVenues = venueMapper.selectList(queryWrapper);
            for (Venue oldVenue : oldVenues) {
                if (!oldVenue.getId().equals(targetVenue.getId())) {
                    LambdaUpdateWrapper<Venue> clearOwnerWrapper = new LambdaUpdateWrapper<>();
                    clearOwnerWrapper.eq(Venue::getId, oldVenue.getId())
                            .set(Venue::getOwnerUserId, null);
                    venueMapper.update(null, clearOwnerWrapper);
                }
            }

            // 绑定新球馆
            if (!id.equals(targetVenue.getOwnerUserId())) {
                targetVenue.setOwnerUserId(id);
                venueMapper.updateById(targetVenue);
            }
        }
    }

    /**
     * 删除球馆管理员
     */
    @Transactional
    public void deleteVenueAdmin(Long id) {
        User admin = userMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(1004, "管理员不存在");
        }

        if (!RoleConstants.VENUE_ADMIN.equals(admin.getRole())) {
            throw new BusinessException(1005, "该用户不是球馆管理员");
        }

        // 解除球馆绑定
        LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Venue::getOwnerUserId, id);
        List<Venue> venues = venueMapper.selectList(queryWrapper);
        for (Venue venue : venues) {
            LambdaUpdateWrapper<Venue> clearOwnerWrapper = new LambdaUpdateWrapper<>();
            clearOwnerWrapper.eq(Venue::getId, venue.getId())
                    .set(Venue::getOwnerUserId, null);
            venueMapper.update(null, clearOwnerWrapper);
        }

        // 删除管理员账号
        userMapper.deleteById(id);
    }

    /**
     * 获取所有球馆
     */
    public List<VenueVO> getVenues() {
        List<Venue> venues = venueMapper.selectList(null);

        // 查询所有管理员
        List<User> admins = userMapper.selectList(null);
        Map<Long, String> adminMap = admins.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (u1, u2) -> u1));

        // 转换为 VO
        List<VenueVO> result = new ArrayList<>();
        for (Venue venue : venues) {
            VenueVO vo = new VenueVO();
            vo.setId(venue.getId());
            vo.setName(venue.getName());
            vo.setAddress(venue.getAddress());
            vo.setPhone(venue.getPhone());
            vo.setOpenTime(venue.getOpenTime());
            vo.setCloseTime(venue.getCloseTime());
            vo.setOwnerUserId(venue.getOwnerUserId());
            vo.setStatus(venue.getStatus());
            vo.setCreatedAt(venue.getCreatedAt());

            // 设置管理员用户名
            if (venue.getOwnerUserId() != null) {
                vo.setOwnerUsername(adminMap.get(venue.getOwnerUserId()));
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 创建球馆
     */
    @Transactional
    public void createVenue(CreateVenueRequest request) {
        // 验证管理员是否存在
        User admin = userMapper.selectById(request.getOwnerUserId());
        if (admin == null) {
            throw new BusinessException(1004, "管理员不存在");
        }

        if (!RoleConstants.VENUE_ADMIN.equals(admin.getRole())) {
            throw new BusinessException(1005, "该用户不是球馆管理员");
        }

        LambdaQueryWrapper<Venue> adminVenueQuery = new LambdaQueryWrapper<>();
        adminVenueQuery.eq(Venue::getOwnerUserId, request.getOwnerUserId());
        long existingVenueCount = venueMapper.selectCount(adminVenueQuery);
        if (existingVenueCount > 0) {
            throw new BusinessException(1009, "该管理员已绑定球馆");
        }

        // 创建球馆
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setPhone(request.getPhone());
        venue.setOpenTime(LocalTime.parse(request.getOpenTime(), DateTimeFormatter.ofPattern("HH:mm")));
        venue.setCloseTime(LocalTime.parse(request.getCloseTime(), DateTimeFormatter.ofPattern("HH:mm")));
        venue.setOwnerUserId(request.getOwnerUserId());
        venue.setStatus("enabled");

        venueMapper.insert(venue);
    }
}
