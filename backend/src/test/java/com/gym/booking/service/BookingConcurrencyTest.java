package com.gym.booking.service;

import com.gym.booking.dto.CreateOrderRequest;
import com.gym.booking.entity.Order;
import com.gym.booking.entity.Slot;
import com.gym.booking.exception.BusinessException;
import com.gym.booking.mapper.SlotMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 并发预约测试
 * 测试目标：验证同一 Slot 不会被重复预约
 */
@SpringBootTest
@ActiveProfiles("test")
public class BookingConcurrencyTest {

    @Autowired
    private AppService appService;

    @Autowired
    private SlotMapper slotMapper;

    private Long createAvailableSlot() {
        Slot slot = new Slot();
        slot.setCourtId(1L);
        slot.setSlotDate(LocalDate.now().plusDays(14));
        slot.setStartTime(LocalTime.now().withSecond(0).withNano(0));
        slot.setEndTime(LocalTime.now().withSecond(0).withNano(0).plusHours(1));
        slot.setPrice(BigDecimal.valueOf(80));
        slot.setStatus("available");
        slotMapper.insert(slot);
        return slot.getId();
    }

    /**
     * 测试并发预约同一 Slot
     * 预期结果：只有 1 个请求成功，其他请求返回错误码 3002
     */
    @Test
    public void testConcurrentBookingSameSlot() throws InterruptedException {
        // 准备测试数据
        Long slotId = createAvailableSlot();
        int concurrentUsers = 10; // 10 个并发用户

        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1); // 确保所有线程同时开始
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers); // 等待所有线程完成

        // 统计结果
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // 创建并发任务
        for (int i = 0; i < concurrentUsers; i++) {
            final long userId = 5L; // 使用已存在的测试用户，避免外键约束失败
            executorService.submit(() -> {
                try {
                    // 等待所有线程准备就绪
                    startLatch.await();

                    // 尝试预约
                    CreateOrderRequest request = new CreateOrderRequest();
                    request.setSlotId(slotId);
                    Order order = appService.createOrder(userId, request);

                    // 预约成功
                    successCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 预约成功，订单号：" + order.getOrderNo());

                } catch (BusinessException e) {
                    // 预约失败
                    if (e.getCode() == 3002) {
                        failCount.incrementAndGet();
                        System.out.println("用户 " + userId + " 预约失败：Slot已被预约");
                    } else {
                        exceptions.add(e);
                        System.err.println("用户 " + userId + " 预约失败：" + e.getMessage());
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                    System.err.println("用户 " + userId + " 发生异常：" + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 开始并发测试
        System.out.println("开始并发测试：" + concurrentUsers + " 个用户同时预约 Slot " + slotId);
        startLatch.countDown(); // 释放所有线程

        // 等待所有线程完成（最多等待 10 秒）
        boolean finished = endLatch.await(10, TimeUnit.SECONDS);
        assertTrue(finished, "测试超时");

        // 关闭线程池
        executorService.shutdown();

        // 验证结果
        System.out.println("测试结果：");
        System.out.println("  成功：" + successCount.get());
        System.out.println("  失败（3002）：" + failCount.get());
        System.out.println("  异常：" + exceptions.size());

        // 断言
        assertEquals(1, successCount.get(), "应该只有 1 个用户预约成功");
        assertEquals(concurrentUsers - 1, failCount.get(), "应该有 " + (concurrentUsers - 1) + " 个用户预约失败");
        assertTrue(exceptions.isEmpty(), "不应该有其他异常");
    }

    /**
     * 测试并发预约不同 Slot
     * 预期结果：所有请求都成功
     */
    @Test
    public void testConcurrentBookingDifferentSlots() throws InterruptedException {
        int concurrentUsers = 5;
        List<Long> slotIds = new ArrayList<>();
        for (int i = 0; i < concurrentUsers; i++) {
            slotIds.add(createAvailableSlot());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // 每个用户预约不同的 Slot
        for (int i = 0; i < concurrentUsers; i++) {
            final long userId = 5L;
            final long slotId = slotIds.get(i); // 不同的 Slot ID

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    CreateOrderRequest request = new CreateOrderRequest();
                    request.setSlotId(slotId);
                    Order order = appService.createOrder(userId, request);

                    successCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 预约 Slot " + slotId + " 成功");

                } catch (Exception e) {
                    exceptions.add(e);
                    System.err.println("用户 " + userId + " 预约 Slot " + slotId + " 失败：" + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        System.out.println("开始并发测试：" + concurrentUsers + " 个用户预约不同 Slot");
        startLatch.countDown();

        boolean finished = endLatch.await(10, TimeUnit.SECONDS);
        assertTrue(finished, "测试超时");

        executorService.shutdown();

        System.out.println("测试结果：");
        System.out.println("  成功：" + successCount.get());
        System.out.println("  异常：" + exceptions.size());

        assertEquals(concurrentUsers, successCount.get(), "所有用户都应该预约成功");
        assertTrue(exceptions.isEmpty(), "不应该有异常");
    }

    /**
     * 测试高并发场景（100 个并发）
     */
    @Test
    public void testHighConcurrencyBooking() throws InterruptedException {
        Long slotId = createAvailableSlot();
        int concurrentUsers = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < concurrentUsers; i++) {
            final long userId = 5L;
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    CreateOrderRequest request = new CreateOrderRequest();
                    request.setSlotId(slotId);
                    appService.createOrder(userId, request);

                    successCount.incrementAndGet();

                } catch (BusinessException e) {
                    if (e.getCode() == 3002) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("异常：" + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        System.out.println("开始高并发测试：" + concurrentUsers + " 个用户同时预约");
        long startTime = System.currentTimeMillis();
        startLatch.countDown();

        boolean finished = endLatch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        assertTrue(finished, "测试超时");
        executorService.shutdown();

        System.out.println("高并发测试结果：");
        System.out.println("  成功：" + successCount.get());
        System.out.println("  失败：" + failCount.get());
        System.out.println("  耗时：" + (endTime - startTime) + " ms");
        System.out.println("  平均响应时间：" + (endTime - startTime) / concurrentUsers + " ms");

        assertEquals(1, successCount.get(), "应该只有 1 个用户预约成功");
        assertEquals(concurrentUsers - 1, failCount.get(), "应该有 " + (concurrentUsers - 1) + " 个用户预约失败");
    }
}
