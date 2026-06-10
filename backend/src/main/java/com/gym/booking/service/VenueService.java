package com.gym.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.booking.constant.OrderStatus;
import com.gym.booking.dto.CreateCourtRequest;
import com.gym.booking.dto.GenerateSlotsRequest;
import com.gym.booking.entity.*;
import com.gym.booking.exception.BusinessException;
import com.gym.booking.mapper.*;
import com.gym.booking.vo.CourtVO;
import com.gym.booking.vo.OrderVO;
import com.gym.booking.vo.SlotVO;
import com.gym.booking.vo.VenueDashboardVO;
import com.gym.booking.vo.VenueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 球馆管理员服务
 */
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueMapper venueMapper;
    private final CourtMapper courtMapper;
    private final SlotMapper slotMapper;
    private final OrderMapper orderMapper;
    private final SlotReservationMapper slotReservationMapper;
    private final UserMapper userMapper;

    private Venue getOwnedVenue(Long userId) {
        LambdaQueryWrapper<Venue> venueQuery = new LambdaQueryWrapper<>();
        venueQuery.eq(Venue::getOwnerUserId, userId);
        Venue venue = venueMapper.selectOne(venueQuery);
        if (venue == null) {
            throw new BusinessException(1006, "未绑定球馆");
        }
        return venue;
    }

    private BigDecimal sumOrderAmount(LambdaQueryWrapper<Order> queryWrapper) {
        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(Order::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 验证球馆归属
     */
    private void verifyVenueOwnership(Long venueId, Long userId) {
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new BusinessException(1003, "球馆不存在");
        }
        if (!venue.getOwnerUserId().equals(userId)) {
            throw new BusinessException(2002, "无权限访问该球馆");
        }
    }

    /**
     * 获取当前管理员的球馆信息
     */
    public VenueVO getVenueProfile(Long userId) {
        Venue venue = getOwnedVenue(userId);

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

        return vo;
    }

    /**
     * 更新球馆信息
     */
    @Transactional
    public void updateVenueProfile(Long userId, Venue venueData) {
        Venue venue = getOwnedVenue(userId);

        // 更新允许修改的字段
        if (venueData.getName() != null) {
            venue.setName(venueData.getName());
        }
        if (venueData.getAddress() != null) {
            venue.setAddress(venueData.getAddress());
        }
        if (venueData.getPhone() != null) {
            venue.setPhone(venueData.getPhone());
        }
        if (venueData.getOpenTime() != null) {
            venue.setOpenTime(venueData.getOpenTime());
        }
        if (venueData.getCloseTime() != null) {
            venue.setCloseTime(venueData.getCloseTime());
        }

        venueMapper.updateById(venue);
    }

    /**
     * 获取场地列表
     */
    public List<CourtVO> getCourts(Long userId) {
        Venue venue = getOwnedVenue(userId);

        // 查询场地
        LambdaQueryWrapper<Court> courtQuery = new LambdaQueryWrapper<>();
        courtQuery.eq(Court::getVenueId, venue.getId());
        courtQuery.orderByDesc(Court::getCreatedAt);
        List<Court> courts = courtMapper.selectList(courtQuery);

        // 转换为 VO
        List<CourtVO> result = new ArrayList<>();
        for (Court court : courts) {
            CourtVO vo = new CourtVO();
            vo.setId(court.getId());
            vo.setVenueId(court.getVenueId());
            vo.setName(court.getName());
            vo.setType(court.getType());
            vo.setPricePerSlot(court.getPricePerSlot());
            vo.setStatus(court.getStatus());
            vo.setCreatedAt(court.getCreatedAt());
            result.add(vo);
        }

        return result;
    }

    /**
     * 创建场地
     */
    @Transactional
    public void createCourt(Long userId, CreateCourtRequest request) {
        Venue venue = getOwnedVenue(userId);

        // 创建场地
        Court court = new Court();
        court.setVenueId(venue.getId());
        court.setName(request.getName());
        court.setType(request.getType());
        court.setPricePerSlot(request.getPricePerSlot());
        court.setStatus("enabled");

        courtMapper.insert(court);
    }

    /**
     * 更新场地
     */
    @Transactional
    public void updateCourt(Long userId, Long courtId, Court courtData) {
        // 验证场地归属
        Court court = courtMapper.selectById(courtId);
        if (court == null) {
            throw new BusinessException(1007, "场地不存在");
        }

        verifyVenueOwnership(court.getVenueId(), userId);

        // 更新字段
        if (courtData.getName() != null) {
            court.setName(courtData.getName());
        }
        if (courtData.getType() != null) {
            court.setType(courtData.getType());
        }
        if (courtData.getPricePerSlot() != null) {
            court.setPricePerSlot(courtData.getPricePerSlot());
        }
        if (courtData.getStatus() != null) {
            court.setStatus(courtData.getStatus());
        }

        courtMapper.updateById(court);
    }

    /**
     * 删除场地
     */
    @Transactional
    public void deleteCourt(Long userId, Long courtId) {
        // 验证场地归属
        Court court = courtMapper.selectById(courtId);
        if (court == null) {
            throw new BusinessException(1007, "场地不存在");
        }

        verifyVenueOwnership(court.getVenueId(), userId);

        // 检查是否有未完成的订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getCourtId, courtId);
        orderQuery.in(Order::getStatus, OrderStatus.PENDING_PAY, OrderStatus.PAID);
        long count = orderMapper.selectCount(orderQuery);

        if (count > 0) {
            throw new BusinessException(1008, "该场地有未完成的订单，无法删除");
        }

        courtMapper.deleteById(courtId);
    }

    /**
     * 生成 Slot
     */
    @Transactional
    public void generateSlots(Long userId, GenerateSlotsRequest request) {
        // 验证场地归属
        Court court = courtMapper.selectById(request.getCourtId());
        if (court == null) {
            throw new BusinessException(1007, "场地不存在");
        }

        verifyVenueOwnership(court.getVenueId(), userId);

        // 获取球馆营业时间
        Venue venue = venueMapper.selectById(court.getVenueId());
        LocalTime openTime = venue.getOpenTime();
        LocalTime closeTime = venue.getCloseTime();

        // 生成 Slot
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            LocalTime currentTime = openTime;

            while (currentTime.isBefore(closeTime)) {
                LocalTime endTime = currentTime.plusHours(1);

                // 检查是否已存在
                LambdaQueryWrapper<Slot> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Slot::getCourtId, request.getCourtId());
                queryWrapper.eq(Slot::getSlotDate, currentDate);
                queryWrapper.eq(Slot::getStartTime, currentTime);
                Slot existingSlot = slotMapper.selectOne(queryWrapper);

                if (existingSlot == null) {
                    // 创建新 Slot
                    Slot slot = new Slot();
                    slot.setCourtId(request.getCourtId());
                    slot.setSlotDate(currentDate);
                    slot.setStartTime(currentTime);
                    slot.setEndTime(endTime);
                    slot.setPrice(court.getPricePerSlot());
                    slot.setStatus("available");
                    slotMapper.insert(slot);
                }

                currentTime = endTime;
            }

            currentDate = currentDate.plusDays(1);
        }
    }

    /**
     * 获取 Slot 列表
     */
    public List<SlotVO> getSlots(Long userId, Long courtId, LocalDate date) {
        // 验证场地归属
        Court court = courtMapper.selectById(courtId);
        if (court == null) {
            throw new BusinessException(1007, "场地不存在");
        }

        verifyVenueOwnership(court.getVenueId(), userId);

        // 查询 Slot
        LambdaQueryWrapper<Slot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Slot::getCourtId, courtId);
        if (date != null) {
            queryWrapper.eq(Slot::getSlotDate, date);
        }
        queryWrapper.orderByAsc(Slot::getSlotDate, Slot::getStartTime);
        List<Slot> slots = slotMapper.selectList(queryWrapper);

        // 查询占用情况
        List<Long> slotIds = slots.stream().map(Slot::getId).collect(Collectors.toList());
        Map<Long, SlotReservation> reservationMap = null;
        if (!slotIds.isEmpty()) {
            LambdaQueryWrapper<SlotReservation> reservationQuery = new LambdaQueryWrapper<>();
            reservationQuery.in(SlotReservation::getSlotId, slotIds);
            List<SlotReservation> reservations = slotReservationMapper.selectList(reservationQuery);
            reservationMap = reservations.stream()
                    .collect(Collectors.toMap(SlotReservation::getSlotId, r -> r, (r1, r2) -> r1));
        }

        // 转换为 VO
        List<SlotVO> result = new ArrayList<>();
        for (Slot slot : slots) {
            SlotVO vo = new SlotVO();
            vo.setId(slot.getId());
            vo.setCourtId(slot.getCourtId());
            vo.setCourtName(court.getName());
            vo.setSlotDate(slot.getSlotDate());
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());
            vo.setPrice(slot.getPrice());
            vo.setStatus(slot.getStatus());
            vo.setReserved(reservationMap != null && reservationMap.containsKey(slot.getId()));
            vo.setCreatedAt(slot.getCreatedAt());
            result.add(vo);
        }

        return result;
    }

    /**
     * 更新 Slot
     */
    @Transactional
    public void updateSlot(Long userId, Long slotId, Slot slotData) {
        // 验证 Slot 归属
        Slot slot = slotMapper.selectById(slotId);
        if (slot == null) {
            throw new BusinessException(3001, "Slot不存在");
        }

        Court court = courtMapper.selectById(slot.getCourtId());
        verifyVenueOwnership(court.getVenueId(), userId);

        // 更新字段
        if (slotData.getStatus() != null) {
            slot.setStatus(slotData.getStatus());
        }
        if (slotData.getPrice() != null) {
            slot.setPrice(slotData.getPrice());
        }

        slotMapper.updateById(slot);
    }

    /**
     * 获取订单列表
     */
    public List<OrderVO> getOrders(Long userId, String status, LocalDate date, Long courtId) {
        Venue venue = getOwnedVenue(userId);

        // 查询订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getVenueId, venue.getId());
        if (status != null && !status.isEmpty()) {
            orderQuery.eq(Order::getStatus, status);
        }
        if (courtId != null) {
            orderQuery.eq(Order::getCourtId, courtId);
        }
        if (date != null) {
            LambdaQueryWrapper<Slot> slotQuery = new LambdaQueryWrapper<>();
            slotQuery.select(Slot::getId);
            slotQuery.eq(Slot::getSlotDate, date);
            if (courtId != null) {
                slotQuery.eq(Slot::getCourtId, courtId);
            }
            List<Long> matchedSlotIds = slotMapper.selectList(slotQuery).stream()
                    .map(Slot::getId)
                    .collect(Collectors.toList());
            if (matchedSlotIds.isEmpty()) {
                return List.of();
            }
            orderQuery.in(Order::getSlotId, matchedSlotIds);
        }
        orderQuery.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(orderQuery);

        // 查询关联数据
        List<Long> userIds = orders.stream().map(Order::getUserId).distinct().collect(Collectors.toList());
        List<Long> courtIds = orders.stream().map(Order::getCourtId).distinct().collect(Collectors.toList());
        List<Long> slotIds = orders.stream().map(Order::getSlotId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, Court> courtMap = courtIds.isEmpty() ? Map.of() :
                courtMapper.selectBatchIds(courtIds).stream()
                        .collect(Collectors.toMap(Court::getId, c -> c));

        Map<Long, Slot> slotMap = slotIds.isEmpty() ? Map.of() :
                slotMapper.selectBatchIds(slotIds).stream()
                        .collect(Collectors.toMap(Slot::getId, s -> s));

        // 转换为 VO
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            OrderVO vo = new OrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setVenueId(order.getVenueId());
            vo.setVenueName(venue.getName());
            vo.setCourtId(order.getCourtId());
            vo.setSlotId(order.getSlotId());
            vo.setAmount(order.getAmount());
            vo.setStatus(order.getStatus());
            vo.setCreatedAt(order.getCreatedAt());
            vo.setPaidAt(order.getPaidAt());
            vo.setCompletedAt(order.getCompletedAt());
            vo.setCanceledAt(order.getCanceledAt());

            // 设置用户名
            User user = userMap.get(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }

            // 设置场地名
            Court court = courtMap.get(order.getCourtId());
            if (court != null) {
                vo.setCourtName(court.getName());
            }

            // 设置时间信息
            Slot slot = slotMap.get(order.getSlotId());
            if (slot != null) {
                vo.setSlotDate(slot.getSlotDate());
                vo.setStartTime(slot.getStartTime());
                vo.setEndTime(slot.getEndTime());
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 核销订单
     */
    @Transactional
    public void completeOrder(Long userId, Long orderId) {
        // 验证订单归属
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(3003, "订单不存在");
        }

        verifyVenueOwnership(order.getVenueId(), userId);

        // 验证订单状态
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw new BusinessException(3003, "订单状态不允许该操作");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 获取球馆管理员仪表盘统计
     */
    public VenueDashboardVO getDashboard(Long userId) {
        Venue venue = getOwnedVenue(userId);
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        VenueDashboardVO vo = new VenueDashboardVO();
        vo.setVenueName(venue.getName());

        LambdaQueryWrapper<Court> courtsQuery = new LambdaQueryWrapper<>();
        courtsQuery.eq(Court::getVenueId, venue.getId());

        LambdaQueryWrapper<Slot> slotsQuery = new LambdaQueryWrapper<>();
        slotsQuery.inSql(Slot::getCourtId, "SELECT id FROM courts WHERE venue_id = " + venue.getId());

        LambdaQueryWrapper<Slot> todaySlotsQuery = new LambdaQueryWrapper<>();
        todaySlotsQuery.inSql(Slot::getCourtId, "SELECT id FROM courts WHERE venue_id = " + venue.getId());
        todaySlotsQuery.eq(Slot::getSlotDate, today);

        LambdaQueryWrapper<Order> venueOrdersQuery = new LambdaQueryWrapper<>();
        venueOrdersQuery.eq(Order::getVenueId, venue.getId());

        LambdaQueryWrapper<Order> todayOrdersQuery = new LambdaQueryWrapper<>();
        todayOrdersQuery.eq(Order::getVenueId, venue.getId())
                .ge(Order::getCreatedAt, startOfToday)
                .lt(Order::getCreatedAt, startOfTomorrow);

        LambdaQueryWrapper<Order> pendingPayOrdersQuery = new LambdaQueryWrapper<>();
        pendingPayOrdersQuery.eq(Order::getVenueId, venue.getId())
                .eq(Order::getStatus, OrderStatus.PENDING_PAY);

        LambdaQueryWrapper<Order> pendingVerificationOrdersQuery = new LambdaQueryWrapper<>();
        pendingVerificationOrdersQuery.eq(Order::getVenueId, venue.getId())
                .eq(Order::getStatus, OrderStatus.PAID);

        LambdaQueryWrapper<Order> completedOrdersQuery = new LambdaQueryWrapper<>();
        completedOrdersQuery.eq(Order::getVenueId, venue.getId())
                .eq(Order::getStatus, OrderStatus.COMPLETED);

        LambdaQueryWrapper<Order> totalRevenueQuery = new LambdaQueryWrapper<>();
        totalRevenueQuery.eq(Order::getVenueId, venue.getId())
                .in(Order::getStatus, OrderStatus.PAID, OrderStatus.COMPLETED);

        LambdaQueryWrapper<Order> todayRevenueQuery = new LambdaQueryWrapper<>();
        todayRevenueQuery.eq(Order::getVenueId, venue.getId())
                .in(Order::getStatus, OrderStatus.PAID, OrderStatus.COMPLETED)
                .ge(Order::getPaidAt, startOfToday)
                .lt(Order::getPaidAt, startOfTomorrow);

        vo.setTotalCourts(courtMapper.selectCount(courtsQuery));
        vo.setTotalSlots(slotMapper.selectCount(slotsQuery));
        vo.setTodaySlots(slotMapper.selectCount(todaySlotsQuery));
        vo.setTotalOrders(orderMapper.selectCount(venueOrdersQuery));
        vo.setTodayOrders(orderMapper.selectCount(todayOrdersQuery));
        vo.setPendingPayOrders(orderMapper.selectCount(pendingPayOrdersQuery));
        vo.setPendingVerificationOrders(orderMapper.selectCount(pendingVerificationOrdersQuery));
        vo.setCompletedOrders(orderMapper.selectCount(completedOrdersQuery));
        vo.setTotalRevenue(sumOrderAmount(totalRevenueQuery));
        vo.setTodayRevenue(sumOrderAmount(todayRevenueQuery));

        return vo;
    }
}
