package com.gym.booking.controller;

import com.gym.booking.dto.CreateOrderRequest;
import com.gym.booking.entity.Court;
import com.gym.booking.entity.Order;
import com.gym.booking.service.AppService;
import com.gym.booking.vo.OrderVO;
import com.gym.booking.vo.Result;
import com.gym.booking.vo.SlotVO;
import com.gym.booking.vo.VenueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户前台控制器
 */
@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class AppController {

    private final AppService appService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    /**
     * 获取球馆列表
     */
    @GetMapping("/venues")
    public Result<List<VenueVO>> getVenues(@RequestParam(required = false) String keyword) {
        List<VenueVO> venues = appService.getVenues(keyword);
        return Result.success(venues);
    }

    /**
     * 获取球馆详情
     */
    @GetMapping("/venues/{id}")
    public Result<VenueVO> getVenueDetail(@PathVariable Long id) {
        VenueVO venue = appService.getVenueDetail(id);
        return Result.success(venue);
    }

    /**
     * 获取球馆的场地列表
     */
    @GetMapping("/venues/{id}/courts")
    public Result<List<Court>> getVenueCourts(@PathVariable Long id) {
        List<Court> courts = appService.getVenueCourts(id);
        return Result.success(courts);
    }

    /**
     * 获取场地的可预约 Slot
     */
    @GetMapping("/courts/{id}/slots")
    public Result<List<SlotVO>> getCourtSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SlotVO> slots = appService.getCourtSlots(id, date);
        return Result.success(slots);
    }

    /**
     * 创建订单
     */
    @PostMapping("/orders")
    public Result<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        Order order = appService.createOrder(userId, request);
        return Result.success(order);
    }

    /**
     * 模拟支付
     */
    @PostMapping("/orders/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        appService.payOrder(userId, id);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/orders/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        appService.cancelOrder(userId, id);
        return Result.success();
    }

    /**
     * 获取我的订单
     */
    @GetMapping("/orders")
    public Result<List<OrderVO>> getMyOrders(@RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        List<OrderVO> orders = appService.getMyOrders(userId, status);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        OrderVO order = appService.getOrderDetail(userId, id);
        return Result.success(order);
    }
}
