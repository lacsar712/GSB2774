package com.gym.booking.controller;

import com.gym.booking.constant.OrderStatus;
import com.gym.booking.entity.Order;
import com.gym.booking.entity.Slot;
import com.gym.booking.mapper.OrderMapper;
import com.gym.booking.mapper.SlotMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VenueController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VenueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SlotMapper slotMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    @WithMockUser(username = "2", roles = "VENUE_ADMIN")
    void testGetDashboardShouldReturnStats() throws Exception {
        mockMvc.perform(get("/venue/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.venueName").isString())
                .andExpect(jsonPath("$.data.totalCourts").isNumber())
                .andExpect(jsonPath("$.data.totalSlots").isNumber())
                .andExpect(jsonPath("$.data.totalOrders").isNumber())
                .andExpect(jsonPath("$.data.todayOrders").isNumber())
                .andExpect(jsonPath("$.data.pendingVerificationOrders").isNumber())
                .andExpect(jsonPath("$.data.totalRevenue").isNumber());
    }

    private Long createSlot(Long courtId, LocalDate date, int hour, BigDecimal price) {
        Slot slot = new Slot();
        slot.setCourtId(courtId);
        slot.setSlotDate(date);
        slot.setStartTime(LocalTime.of(hour, 0));
        slot.setEndTime(LocalTime.of(hour + 1, 0));
        slot.setPrice(price);
        slot.setStatus("available");
        slotMapper.insert(slot);
        return slot.getId();
    }

    private Long createOrder(Long slotId, Long venueId, Long courtId, String status) {
        Order order = new Order();
        order.setOrderNo("ORD_IT_" + System.nanoTime());
        order.setUserId(5L);
        order.setVenueId(venueId);
        order.setCourtId(courtId);
        order.setSlotId(slotId);
        order.setAmount(BigDecimal.valueOf(88));
        order.setStatus(status);
        orderMapper.insert(order);
        return order.getId();
    }

    @Test
    @WithMockUser(username = "2", roles = "VENUE_ADMIN")
    void testGetOrdersWithDateFilterShouldReturnOnlyTargetDate() throws Exception {
        LocalDate targetDate = LocalDate.of(2099, 1, 1);
        Long slotOnTargetDate = createSlot(1L, targetDate, 10, BigDecimal.valueOf(80));
        Long slotOnAnotherDate = createSlot(1L, targetDate.plusDays(1), 11, BigDecimal.valueOf(80));

        createOrder(slotOnTargetDate, 1L, 1L, OrderStatus.PENDING_PAY);
        createOrder(slotOnAnotherDate, 1L, 1L, OrderStatus.PENDING_PAY);

        mockMvc.perform(get("/venue/orders")
                        .param("date", targetDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].slotDate").value(targetDate.toString()));
    }

    @Test
    @WithMockUser(username = "2", roles = "VENUE_ADMIN")
    void testGetOrdersWithDateFilterShouldIgnoreOtherVenue() throws Exception {
        LocalDate targetDate = LocalDate.of(2099, 2, 1);
        Long venue1Slot = createSlot(1L, targetDate, 14, BigDecimal.valueOf(80));
        Long venue2Slot = createSlot(3L, targetDate, 15, BigDecimal.valueOf(200));

        createOrder(venue1Slot, 1L, 1L, OrderStatus.PENDING_PAY);
        createOrder(venue2Slot, 2L, 3L, OrderStatus.PENDING_PAY);

        mockMvc.perform(get("/venue/orders")
                        .param("date", targetDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].venueId").value(1));
    }

    @Test
    @WithMockUser(username = "2", roles = "VENUE_ADMIN")
    void testCompletePaidOrderShouldSucceed() throws Exception {
        Long slotId = createSlot(1L, LocalDate.of(2099, 3, 1), 16, BigDecimal.valueOf(80));
        Long orderId = createOrder(slotId, 1L, 1L, OrderStatus.PAID);

        mockMvc.perform(post("/venue/orders/" + orderId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Order updated = orderMapper.selectById(orderId);
        assertNotNull(updated);
        assertEquals(OrderStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());
    }
}
