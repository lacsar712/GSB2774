package com.gym.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.booking.dto.CreateOrderRequest;
import com.gym.booking.entity.Slot;
import com.gym.booking.mapper.SlotMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AppController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AppControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SlotMapper slotMapper;

    private Long createAvailableSlot() {
        Slot slot = new Slot();
        slot.setCourtId(1L);
        slot.setSlotDate(LocalDate.now().plusDays(7));
        slot.setStartTime(LocalTime.now().withSecond(0).withNano(0));
        slot.setEndTime(LocalTime.now().withSecond(0).withNano(0).plusHours(1));
        slot.setPrice(BigDecimal.valueOf(80));
        slot.setStatus("available");
        slotMapper.insert(slot);
        return slot.getId();
    }

    private Long createOrderAndGetId(Long slotId) throws Exception {
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setSlotId(slotId);

        String createResponse = mockMvc.perform(post("/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(createResponse).path("data").path("id").asLong();
    }

    /**
     * 测试获取球馆列表
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetVenues() throws Exception {
        mockMvc.perform(get("/app/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取球馆列表 - 带关键字搜索
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetVenuesWithKeyword() throws Exception {
        mockMvc.perform(get("/app/venues")
                        .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取球馆详情
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetVenueDetail() throws Exception {
        mockMvc.perform(get("/app/venues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").exists());
    }

    /**
     * 测试获取球馆详情 - 球馆不存在
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetVenueDetail_NotFound() throws Exception {
        mockMvc.perform(get("/app/venues/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.message").value("球馆不存在"));
    }

    /**
     * 测试获取球馆的场地列表
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetVenueCourts() throws Exception {
        mockMvc.perform(get("/app/venues/1/courts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取场地的可预约 Slot
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetCourtSlots() throws Exception {
        mockMvc.perform(get("/app/courts/1/slots")
                        .param("date", "2024-02-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试创建订单
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testCreateOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSlotId(createAvailableSlot());

        mockMvc.perform(post("/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING_PAY"));
    }

    /**
     * 测试创建订单 - Slot 不存在
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testCreateOrder_SlotNotFound() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSlotId(999999L);

        mockMvc.perform(post("/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("Slot不存在"));
    }

    /**
     * 测试创建订单 - 参数验证失败
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testCreateOrder_ValidationFailed() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        // slotId 为 null

        mockMvc.perform(post("/app/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试支付订单
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testPayOrder() throws Exception {
        Long orderId = createOrderAndGetId(createAvailableSlot());

        // 支付订单
        mockMvc.perform(post("/app/orders/" + orderId + "/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试取消订单
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testCancelOrder() throws Exception {
        Long orderId = createOrderAndGetId(createAvailableSlot());

        // 取消订单
        mockMvc.perform(post("/app/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取我的订单
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetMyOrders() throws Exception {
        mockMvc.perform(get("/app/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取我的订单 - 按状态筛选
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetMyOrdersWithStatus() throws Exception {
        mockMvc.perform(get("/app/orders")
                        .param("status", "PENDING_PAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取订单详情
     */
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetOrderDetail() throws Exception {
        Long orderId = createOrderAndGetId(createAvailableSlot());

        mockMvc.perform(get("/app/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    /**
     * 测试未认证访问
     */
    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/app/venues"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 测试错误角色访问
     */
    @Test
    @WithMockUser(username = "1", roles = "VENUE_ADMIN")
    public void testWrongRoleAccess() throws Exception {
        mockMvc.perform(get("/app/venues"))
                .andExpect(status().isForbidden());
    }
}
