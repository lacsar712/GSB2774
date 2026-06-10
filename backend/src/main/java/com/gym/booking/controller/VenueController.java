package com.gym.booking.controller;

import com.gym.booking.dto.CreateCourtRequest;
import com.gym.booking.dto.GenerateSlotsRequest;
import com.gym.booking.entity.Court;
import com.gym.booking.entity.Slot;
import com.gym.booking.entity.Venue;
import com.gym.booking.service.VenueService;
import com.gym.booking.vo.*;
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
 * 球馆管理员控制器
 */
@RestController
@RequestMapping("/venue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENUE_ADMIN')")
public class VenueController {

    private final VenueService venueService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    /**
     * 获取球馆仪表盘统计
     */
    @GetMapping("/dashboard")
    public Result<VenueDashboardVO> getDashboard() {
        Long userId = getCurrentUserId();
        VenueDashboardVO dashboard = venueService.getDashboard(userId);
        return Result.success(dashboard);
    }

    /**
     * 获取球馆信息
     */
    @GetMapping("/profile")
    public Result<VenueVO> getVenueProfile() {
        Long userId = getCurrentUserId();
        VenueVO venue = venueService.getVenueProfile(userId);
        return Result.success(venue);
    }

    /**
     * 更新球馆信息
     */
    @PutMapping("/profile")
    public Result<Void> updateVenueProfile(@RequestBody Venue venue) {
        Long userId = getCurrentUserId();
        venueService.updateVenueProfile(userId, venue);
        return Result.success();
    }

    /**
     * 获取场地列表
     */
    @GetMapping("/courts")
    public Result<List<CourtVO>> getCourts() {
        Long userId = getCurrentUserId();
        List<CourtVO> courts = venueService.getCourts(userId);
        return Result.success(courts);
    }

    /**
     * 创建场地
     */
    @PostMapping("/courts")
    public Result<Void> createCourt(@Valid @RequestBody CreateCourtRequest request) {
        Long userId = getCurrentUserId();
        venueService.createCourt(userId, request);
        return Result.success();
    }

    /**
     * 更新场地
     */
    @PutMapping("/courts/{id}")
    public Result<Void> updateCourt(@PathVariable Long id, @RequestBody Court court) {
        Long userId = getCurrentUserId();
        venueService.updateCourt(userId, id, court);
        return Result.success();
    }

    /**
     * 删除场地
     */
    @DeleteMapping("/courts/{id}")
    public Result<Void> deleteCourt(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        venueService.deleteCourt(userId, id);
        return Result.success();
    }

    /**
     * 生成 Slot
     */
    @PostMapping("/slots/generate")
    public Result<Void> generateSlots(@Valid @RequestBody GenerateSlotsRequest request) {
        Long userId = getCurrentUserId();
        venueService.generateSlots(userId, request);
        return Result.success();
    }

    /**
     * 获取 Slot 列表
     */
    @GetMapping("/slots")
    public Result<List<SlotVO>> getSlots(
            @RequestParam Long courtId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = getCurrentUserId();
        List<SlotVO> slots = venueService.getSlots(userId, courtId, date);
        return Result.success(slots);
    }

    /**
     * 更新 Slot
     */
    @PutMapping("/slots/{id}")
    public Result<Void> updateSlot(@PathVariable Long id, @RequestBody Slot slot) {
        Long userId = getCurrentUserId();
        venueService.updateSlot(userId, id, slot);
        return Result.success();
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public Result<List<OrderVO>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long courtId) {
        Long userId = getCurrentUserId();
        List<OrderVO> orders = venueService.getOrders(userId, status, date, courtId);
        return Result.success(orders);
    }

    /**
     * 核销订单
     */
    @PostMapping("/orders/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        venueService.completeOrder(userId, id);
        return Result.success();
    }
}
