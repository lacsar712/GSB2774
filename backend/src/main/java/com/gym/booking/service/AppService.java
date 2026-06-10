package com.gym.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.booking.constant.OrderStatus;
import com.gym.booking.dto.CreateOrderRequest;
import com.gym.booking.entity.*;
import com.gym.booking.exception.BusinessException;
import com.gym.booking.mapper.*;
import com.gym.booking.vo.OrderVO;
import com.gym.booking.vo.SlotVO;
import com.gym.booking.vo.VenueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 用户前台服务
 */
@Service
@RequiredArgsConstructor
public class AppService {

    private final VenueMapper venueMapper;
    private final CourtMapper courtMapper;
    private final SlotMapper slotMapper;
    private final OrderMapper orderMapper;
    private final SlotReservationMapper slotReservationMapper;
    private final UserMapper userMapper;

    /**
     * 获取球馆列表
     */
    public List<VenueVO> getVenues(String keyword) {
        LambdaQueryWrapper<Venue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Venue::getStatus, "enabled");

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Venue::getName, keyword)
                    .or()
                    .like(Venue::getAddress, keyword));
        }

        queryWrapper.orderByDesc(Venue::getCreatedAt);
        List<Venue> venues = venueMapper.selectList(queryWrapper);

        // 查询管理员信息
        List<Long> ownerIds = venues.stream()
                .map(Venue::getOwnerUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> ownerMap = ownerIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(ownerIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUsername));

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

            if (venue.getOwnerUserId() != null) {
                vo.setOwnerUsername(ownerMap.get(venue.getOwnerUserId()));
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 获取球馆详情
     */
    public VenueVO getVenueDetail(Long venueId) {
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new BusinessException(1003, "球馆不存在");
        }

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
     * 获取球馆的场地列表
     */
    public List<Court> getVenueCourts(Long venueId) {
        LambdaQueryWrapper<Court> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Court::getVenueId, venueId);
        queryWrapper.eq(Court::getStatus, "enabled");
        queryWrapper.orderByAsc(Court::getName);

        return courtMapper.selectList(queryWrapper);
    }

    /**
     * 获取场地的可预约 Slot
     */
    public List<SlotVO> getCourtSlots(Long courtId, LocalDate date) {
        Court court = courtMapper.selectById(courtId);
        if (court == null) {
            throw new BusinessException(1007, "场地不存在");
        }

        // 查询 Slot
        LambdaQueryWrapper<Slot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Slot::getCourtId, courtId);
        queryWrapper.eq(Slot::getSlotDate, date);
        queryWrapper.eq(Slot::getStatus, "available");
        queryWrapper.orderByAsc(Slot::getStartTime);
        List<Slot> slots = slotMapper.selectList(queryWrapper);

        // 查询占用情况
        List<Long> slotIds = slots.stream().map(Slot::getId).collect(Collectors.toList());
        Map<Long, SlotReservation> reservationMap = Map.of();

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
            vo.setReserved(reservationMap.containsKey(slot.getId()));
            vo.setCreatedAt(slot.getCreatedAt());
            result.add(vo);
        }

        return result;
    }

    /**
     * 创建订单（核心业务逻辑）
     * 使用事务 + 唯一约束防止并发冲突
     */
    @Transactional
    public Order createOrder(Long userId, CreateOrderRequest request) {
        // 1. 验证 Slot 状态
        Slot slot = slotMapper.selectById(request.getSlotId());
        if (slot == null) {
            throw new BusinessException(3001, "Slot不存在");
        }

        if (!"available".equals(slot.getStatus())) {
            throw new BusinessException(3001, "Slot不可用");
        }

        // 2. 获取关联信息
        Court court = courtMapper.selectById(slot.getCourtId());
        if (court == null) {
            throw new BusinessException(1007, "场地不存在");
        }

        Venue venue = venueMapper.selectById(court.getVenueId());
        if (venue == null) {
            throw new BusinessException(1003, "球馆不存在");
        }

        // 3. 生成订单号
        String orderNo = generateOrderNo();

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setVenueId(venue.getId());
        order.setCourtId(court.getId());
        order.setSlotId(slot.getId());
        order.setAmount(slot.getPrice());
        order.setStatus(OrderStatus.PENDING_PAY);

        orderMapper.insert(order);

        // 5. 插入占用表（slot_id UNIQUE 约束防止并发冲突）
        try {
            SlotReservation reservation = new SlotReservation();
            reservation.setSlotId(slot.getId());
            reservation.setOrderId(order.getId());
            reservation.setStatus("locked");

            slotReservationMapper.insert(reservation);
        } catch (DuplicateKeyException e) {
            // 唯一约束冲突，说明 Slot 已被预约
            throw new BusinessException(3002, "Slot已被预约");
        }

        return order;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "ORD" + timestamp + random;
    }

    /**
     * 模拟支付
     */
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        // 验证订单归属
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(3003, "订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(2002, "无权限访问该订单");
        }

        // 验证订单状态
        if (!OrderStatus.PENDING_PAY.equals(order.getStatus())) {
            throw new BusinessException(3003, "订单状态不允许该操作");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新占用表状态
        LambdaQueryWrapper<SlotReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SlotReservation::getOrderId, orderId);
        SlotReservation reservation = slotReservationMapper.selectOne(queryWrapper);

        if (reservation != null) {
            reservation.setStatus("confirmed");
            slotReservationMapper.updateById(reservation);
        }
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        // 验证订单归属
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(3003, "订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(2002, "无权限访问该订单");
        }

        // 验证订单状态（仅未支付可取消）
        if (!OrderStatus.PENDING_PAY.equals(order.getStatus())) {
            throw new BusinessException(3003, "订单状态不允许该操作");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 释放占用（删除占用记录）
        LambdaQueryWrapper<SlotReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SlotReservation::getOrderId, orderId);
        slotReservationMapper.delete(queryWrapper);
    }

    /**
     * 获取我的订单
     */
    public List<OrderVO> getMyOrders(Long userId, String status) {
        // 查询订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            orderQuery.eq(Order::getStatus, status);
        }

        orderQuery.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(orderQuery);

        // 查询关联数据
        List<Long> venueIds = orders.stream().map(Order::getVenueId).distinct().collect(Collectors.toList());
        List<Long> courtIds = orders.stream().map(Order::getCourtId).distinct().collect(Collectors.toList());
        List<Long> slotIds = orders.stream().map(Order::getSlotId).distinct().collect(Collectors.toList());

        Map<Long, Venue> venueMap = venueIds.isEmpty() ? Map.of() :
                venueMapper.selectBatchIds(venueIds).stream()
                        .collect(Collectors.toMap(Venue::getId, v -> v));

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
            vo.setCourtId(order.getCourtId());
            vo.setSlotId(order.getSlotId());
            vo.setAmount(order.getAmount());
            vo.setStatus(order.getStatus());
            vo.setCreatedAt(order.getCreatedAt());
            vo.setPaidAt(order.getPaidAt());
            vo.setCompletedAt(order.getCompletedAt());
            vo.setCanceledAt(order.getCanceledAt());

            // 设置球馆名
            Venue venue = venueMap.get(order.getVenueId());
            if (venue != null) {
                vo.setVenueName(venue.getName());
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
     * 获取订单详情
     */
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(3003, "订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(2002, "无权限访问该订单");
        }

        // 查询关联数据
        Venue venue = venueMapper.selectById(order.getVenueId());
        Court court = courtMapper.selectById(order.getCourtId());
        Slot slot = slotMapper.selectById(order.getSlotId());

        // 转换为 VO
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setVenueId(order.getVenueId());
        vo.setCourtId(order.getCourtId());
        vo.setSlotId(order.getSlotId());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setPaidAt(order.getPaidAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setCanceledAt(order.getCanceledAt());

        if (venue != null) {
            vo.setVenueName(venue.getName());
        }
        if (court != null) {
            vo.setCourtName(court.getName());
        }
        if (slot != null) {
            vo.setSlotDate(slot.getSlotDate());
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());
        }

        return vo;
    }
}
