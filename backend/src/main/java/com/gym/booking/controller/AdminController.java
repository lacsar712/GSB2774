package com.gym.booking.controller;

import com.gym.booking.dto.CreateVenueAdminRequest;
import com.gym.booking.dto.CreateVenueRequest;
import com.gym.booking.dto.UpdateVenueAdminRequest;
import com.gym.booking.service.AdminService;
import com.gym.booking.vo.AdminDashboardVO;
import com.gym.booking.vo.Result;
import com.gym.booking.vo.VenueAdminVO;
import com.gym.booking.vo.VenueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 超级管理员控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取平台仪表盘统计
     */
    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> getDashboard() {
        AdminDashboardVO dashboard = adminService.getDashboard();
        return Result.success(dashboard);
    }

    /**
     * 获取所有球馆管理员
     */
    @GetMapping("/venue-admins")
    public Result<List<VenueAdminVO>> getVenueAdmins() {
        List<VenueAdminVO> admins = adminService.getVenueAdmins();
        return Result.success(admins);
    }

    /**
     * 创建球馆管理员
     */
    @PostMapping("/venue-admins")
    public Result<Void> createVenueAdmin(@Valid @RequestBody CreateVenueAdminRequest request) {
        adminService.createVenueAdmin(request);
        return Result.success();
    }

    /**
     * 更新球馆管理员
     */
    @PutMapping("/venue-admins/{id}")
    public Result<Void> updateVenueAdmin(@PathVariable Long id,
                                         @RequestBody UpdateVenueAdminRequest request) {
        adminService.updateVenueAdmin(id, request);
        return Result.success();
    }

    /**
     * 删除球馆管理员
     */
    @DeleteMapping("/venue-admins/{id}")
    public Result<Void> deleteVenueAdmin(@PathVariable Long id) {
        adminService.deleteVenueAdmin(id);
        return Result.success();
    }

    /**
     * 获取所有球馆
     */
    @GetMapping("/venues")
    public Result<List<VenueVO>> getVenues() {
        List<VenueVO> venues = adminService.getVenues();
        return Result.success(venues);
    }

    /**
     * 创建球馆
     */
    @PostMapping("/venues")
    public Result<Void> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        adminService.createVenue(request);
        return Result.success();
    }
}
