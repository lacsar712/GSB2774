package com.gym.booking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.booking.entity.Venue;
import com.gym.booking.mapper.VenueMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Test
    @WithMockUser(username = "1", roles = "SUPER_ADMIN")
    void testGetDashboardShouldReturnStats() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalVenues").isNumber())
                .andExpect(jsonPath("$.data.totalVenueAdmins").isNumber())
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.totalOrders").isNumber())
                .andExpect(jsonPath("$.data.todayOrders").isNumber())
                .andExpect(jsonPath("$.data.totalRevenue").isNumber())
                .andExpect(jsonPath("$.data.todayRevenue").isNumber());
    }

    @Test
    @WithMockUser(username = "1", roles = "SUPER_ADMIN")
    void testCreateVenueWithBoundAdminShouldFail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "冲突绑定球馆");
        request.put("address", "测试地址");
        request.put("phone", "010-10000000");
        request.put("openTime", "09:00");
        request.put("closeTime", "22:00");
        request.put("ownerUserId", 2L);

        mockMvc.perform(post("/admin/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1009))
                .andExpect(jsonPath("$.message").value("该管理员已绑定球馆"));
    }

    @Test
    @WithMockUser(username = "1", roles = "SUPER_ADMIN")
    void testCreateVenueAdminWithBoundVenueShouldFail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("username", "bc" + (System.currentTimeMillis() % 1000000));
        request.put("password", "123456");
        request.put("phone", "13800009999");
        request.put("venueId", 2L);

        mockMvc.perform(post("/admin/venue-admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1009))
                .andExpect(jsonPath("$.message").value("球馆已绑定管理员"));
    }

    @Test
    @WithMockUser(username = "1", roles = "SUPER_ADMIN")
    void testUpdateVenueAdminBindOccupiedVenueShouldFail() throws Exception {
        Map<String, Object> request = Map.of("venueId", 2L);

        mockMvc.perform(put("/admin/venue-admins/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1009))
                .andExpect(jsonPath("$.message").value("球馆已绑定其他管理员"));
    }

    @Test
    @WithMockUser(username = "1", roles = "SUPER_ADMIN")
    void testUpdateVenueAdminBindUnassignedVenueShouldSucceed() throws Exception {
        Venue unassignedVenue = new Venue();
        unassignedVenue.setName("待绑定球馆");
        unassignedVenue.setAddress("测试地址");
        unassignedVenue.setPhone("010-20000000");
        unassignedVenue.setOpenTime(LocalTime.of(8, 0));
        unassignedVenue.setCloseTime(LocalTime.of(22, 0));
        unassignedVenue.setOwnerUserId(null);
        unassignedVenue.setStatus("enabled");
        venueMapper.insert(unassignedVenue);
        assertNotNull(unassignedVenue.getId());

        Map<String, Object> request = Map.of("venueId", unassignedVenue.getId());

        mockMvc.perform(put("/admin/venue-admins/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        LambdaQueryWrapper<Venue> query = new LambdaQueryWrapper<>();
        query.eq(Venue::getOwnerUserId, 2L);
        List<Venue> boundVenues = venueMapper.selectList(query);
        assertEquals(1, boundVenues.size());
        assertEquals(unassignedVenue.getId(), boundVenues.get(0).getId());
    }
}
