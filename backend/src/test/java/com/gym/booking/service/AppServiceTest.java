package com.gym.booking.service;

import com.gym.booking.constant.OrderStatus;
import com.gym.booking.dto.CreateOrderRequest;
import com.gym.booking.entity.*;
import com.gym.booking.exception.BusinessException;
import com.gym.booking.mapper.*;
import com.gym.booking.vo.OrderVO;
import com.gym.booking.vo.SlotVO;
import com.gym.booking.vo.VenueVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AppService 单元测试
 */
@ExtendWith(MockitoExtension.class)
public class AppServiceTest {

    @Mock
    private VenueMapper venueMapper;

    @Mock
    private CourtMapper courtMapper;

    @Mock
    private SlotMapper slotMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SlotReservationMapper slotReservationMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AppService appService;

    private Venue testVenue;
    private Court testCourt;
    private Slot testSlot;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // 准备测试数据
        testVenue = new Venue();
        testVenue.setId(1L);
        testVenue.setName("测试球馆");
        testVenue.setAddress("测试地址");
        testVenue.setPhone("13800138000");
        testVenue.setOpenTime(LocalTime.of(8, 0));
        testVenue.setCloseTime(LocalTime.of(22, 0));
        testVenue.setStatus("enabled");

        testCourt = new Court();
        testCourt.setId(1L);
        testCourt.setVenueId(1L);
        testCourt.setName("1号场地");
        testCourt.setType("羽毛球");
        testCourt.setPricePerSlot(BigDecimal.valueOf(100));
        testCourt.setStatus("enabled");

        testSlot = new Slot();
        testSlot.setId(1L);
        testSlot.setCourtId(1L);
        testSlot.setSlotDate(LocalDate.now().plusDays(1));
        testSlot.setStartTime(LocalTime.of(10, 0));
        testSlot.setEndTime(LocalTime.of(11, 0));
        testSlot.setPrice(BigDecimal.valueOf(100));
        testSlot.setStatus("available");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    /**
     * 测试获取球馆列表
     */
    @Test
    public void testGetVenues() {
        // Mock 数据
        when(venueMapper.selectList(any())).thenReturn(Arrays.asList(testVenue));

        // 执行
        List<VenueVO> venues = appService.getVenues(null);

        // 验证
        assertNotNull(venues);
        assertEquals(1, venues.size());
        assertEquals("测试球馆", venues.get(0).getName());

        verify(venueMapper, times(1)).selectList(any());
    }

    /**
     * 测试获取球馆详情
     */
    @Test
    public void testGetVenueDetail() {
        // Mock 数据
        when(venueMapper.selectById(1L)).thenReturn(testVenue);

        // 执行
        VenueVO venue = appService.getVenueDetail(1L);

        // 验证
        assertNotNull(venue);
        assertEquals("测试球馆", venue.getName());
        assertEquals("测试地址", venue.getAddress());

        verify(venueMapper, times(1)).selectById(1L);
    }

    /**
     * 测试获取球馆详情 - 球馆不存在
     */
    @Test
    public void testGetVenueDetail_NotFound() {
        // Mock 数据
        when(venueMapper.selectById(999L)).thenReturn(null);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.getVenueDetail(999L);
        });

        assertEquals(1003, exception.getCode());
        assertEquals("球馆不存在", exception.getMessage());
    }

    /**
     * 测试获取场地列表
     */
    @Test
    public void testGetVenueCourts() {
        // Mock 数据
        when(courtMapper.selectList(any())).thenReturn(Arrays.asList(testCourt));

        // 执行
        List<Court> courts = appService.getVenueCourts(1L);

        // 验证
        assertNotNull(courts);
        assertEquals(1, courts.size());
        assertEquals("1号场地", courts.get(0).getName());

        verify(courtMapper, times(1)).selectList(any());
    }

    /**
     * 测试获取可预约 Slot
     */
    @Test
    public void testGetCourtSlots() {
        // Mock 数据
        when(courtMapper.selectById(1L)).thenReturn(testCourt);
        when(slotMapper.selectList(any())).thenReturn(Arrays.asList(testSlot));
        when(slotReservationMapper.selectList(any())).thenReturn(Arrays.asList());

        // 执行
        List<SlotVO> slots = appService.getCourtSlots(1L, LocalDate.now().plusDays(1));

        // 验证
        assertNotNull(slots);
        assertEquals(1, slots.size());
        assertEquals(false, slots.get(0).getReserved());

        verify(courtMapper, times(1)).selectById(1L);
        verify(slotMapper, times(1)).selectList(any());
    }

    /**
     * 测试创建订单 - 成功
     */
    @Test
    public void testCreateOrder_Success() {
        // Mock 数据
        when(slotMapper.selectById(1L)).thenReturn(testSlot);
        when(courtMapper.selectById(1L)).thenReturn(testCourt);
        when(venueMapper.selectById(1L)).thenReturn(testVenue);
        when(orderMapper.insert(any())).thenReturn(1);
        when(slotReservationMapper.insert(any())).thenReturn(1);

        // 执行
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSlotId(1L);
        Order order = appService.createOrder(1L, request);

        // 验证
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING_PAY, order.getStatus());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(order.getAmount()));

        verify(slotMapper, times(1)).selectById(1L);
        verify(orderMapper, times(1)).insert(any());
        verify(slotReservationMapper, times(1)).insert(any());
    }

    /**
     * 测试创建订单 - Slot 不存在
     */
    @Test
    public void testCreateOrder_SlotNotFound() {
        // Mock 数据
        when(slotMapper.selectById(999L)).thenReturn(null);

        // 执行并验证异常
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSlotId(999L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.createOrder(1L, request);
        });

        assertEquals(3001, exception.getCode());
        assertEquals("Slot不存在", exception.getMessage());
    }

    /**
     * 测试创建订单 - Slot 不可用
     */
    @Test
    public void testCreateOrder_SlotNotAvailable() {
        // Mock 数据
        testSlot.setStatus("disabled");
        when(slotMapper.selectById(1L)).thenReturn(testSlot);

        // 执行并验证异常
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSlotId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.createOrder(1L, request);
        });

        assertEquals(3001, exception.getCode());
        assertEquals("Slot不可用", exception.getMessage());
    }

    /**
     * 测试创建订单 - Slot 已被预约（并发冲突）
     */
    @Test
    public void testCreateOrder_SlotAlreadyBooked() {
        // Mock 数据
        when(slotMapper.selectById(1L)).thenReturn(testSlot);
        when(courtMapper.selectById(1L)).thenReturn(testCourt);
        when(venueMapper.selectById(1L)).thenReturn(testVenue);
        when(orderMapper.insert(any())).thenReturn(1);
        when(slotReservationMapper.insert(any())).thenThrow(new DuplicateKeyException("Duplicate entry"));

        // 执行并验证异常
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSlotId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.createOrder(1L, request);
        });

        assertEquals(3002, exception.getCode());
        assertEquals("Slot已被预约", exception.getMessage());
    }

    /**
     * 测试支付订单 - 成功
     */
    @Test
    public void testPayOrder_Success() {
        // Mock 数据
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING_PAY);

        SlotReservation reservation = new SlotReservation();
        reservation.setId(1L);
        reservation.setOrderId(1L);
        reservation.setStatus("locked");

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(slotReservationMapper.selectOne(any())).thenReturn(reservation);
        when(orderMapper.updateById(any())).thenReturn(1);
        when(slotReservationMapper.updateById(any())).thenReturn(1);

        // 执行
        appService.payOrder(1L, 1L);

        // 验证
        verify(orderMapper, times(1)).updateById(any());
        verify(slotReservationMapper, times(1)).updateById(any());
    }

    /**
     * 测试支付订单 - 订单不存在
     */
    @Test
    public void testPayOrder_OrderNotFound() {
        // Mock 数据
        when(orderMapper.selectById(999L)).thenReturn(null);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.payOrder(1L, 999L);
        });

        assertEquals(3003, exception.getCode());
        assertEquals("订单不存在", exception.getMessage());
    }

    /**
     * 测试支付订单 - 无权限
     */
    @Test
    public void testPayOrder_NoPermission() {
        // Mock 数据
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L); // 不同的用户
        order.setStatus(OrderStatus.PENDING_PAY);

        when(orderMapper.selectById(1L)).thenReturn(order);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.payOrder(1L, 1L);
        });

        assertEquals(2002, exception.getCode());
        assertEquals("无权限访问该订单", exception.getMessage());
    }

    /**
     * 测试支付订单 - 订单状态不允许
     */
    @Test
    public void testPayOrder_InvalidStatus() {
        // Mock 数据
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PAID); // 已支付

        when(orderMapper.selectById(1L)).thenReturn(order);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.payOrder(1L, 1L);
        });

        assertEquals(3003, exception.getCode());
        assertEquals("订单状态不允许该操作", exception.getMessage());
    }

    /**
     * 测试取消订单 - 成功
     */
    @Test
    public void testCancelOrder_Success() {
        // Mock 数据
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING_PAY);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.updateById(any())).thenReturn(1);
        when(slotReservationMapper.delete(any())).thenReturn(1);

        // 执行
        appService.cancelOrder(1L, 1L);

        // 验证
        verify(orderMapper, times(1)).updateById(any());
        verify(slotReservationMapper, times(1)).delete(any());
    }

    /**
     * 测试取消订单 - 订单状态不允许
     */
    @Test
    public void testCancelOrder_InvalidStatus() {
        // Mock 数据
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PAID); // 已支付，不能取消

        when(orderMapper.selectById(1L)).thenReturn(order);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appService.cancelOrder(1L, 1L);
        });

        assertEquals(3003, exception.getCode());
        assertEquals("订单状态不允许该操作", exception.getMessage());
    }

    /**
     * 测试获取我的订单
     */
    @Test
    public void testGetMyOrders() {
        // Mock 数据
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setVenueId(1L);
        order.setCourtId(1L);
        order.setSlotId(1L);
        order.setStatus(OrderStatus.PENDING_PAY);

        when(orderMapper.selectList(any())).thenReturn(Arrays.asList(order));
        when(venueMapper.selectBatchIds(any())).thenReturn(Arrays.asList(testVenue));
        when(courtMapper.selectBatchIds(any())).thenReturn(Arrays.asList(testCourt));
        when(slotMapper.selectBatchIds(any())).thenReturn(Arrays.asList(testSlot));

        // 执行
        List<OrderVO> orders = appService.getMyOrders(1L, null);

        // 验证
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals("测试球馆", orders.get(0).getVenueName());
        assertEquals("1号场地", orders.get(0).getCourtName());

        verify(orderMapper, times(1)).selectList(any());
    }
}
