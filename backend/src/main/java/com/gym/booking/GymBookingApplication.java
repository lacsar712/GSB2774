package com.gym.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 球馆预约管理系统主应用类
 */
@SpringBootApplication
@MapperScan("com.gym.booking.mapper")
public class GymBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymBookingApplication.class, args);
    }
}
