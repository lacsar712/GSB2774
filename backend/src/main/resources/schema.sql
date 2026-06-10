-- 球馆预约管理系统数据库初始化脚本
-- MySQL 8.0

-- 创建数据库
CREATE DATABASE IF NOT EXISTS gym_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gym_booking;

-- 强制当前会话使用 UTF-8，避免导入中文出现乱码
SET NAMES utf8mb4;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    role VARCHAR(32) NOT NULL COMMENT '角色: SUPER_ADMIN/VENUE_ADMIN/USER',
    phone VARCHAR(32) NULL COMMENT '手机号',
    status VARCHAR(16) NOT NULL DEFAULT 'enabled' COMMENT '状态: enabled/disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 球馆表
CREATE TABLE venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '球馆名称',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    phone VARCHAR(32) NULL COMMENT '联系电话',
    open_time TIME NOT NULL COMMENT '营业开始时间',
    close_time TIME NOT NULL COMMENT '营业结束时间',
    owner_user_id BIGINT NULL COMMENT '球馆管理员ID',
    status VARCHAR(16) NOT NULL DEFAULT 'enabled' COMMENT '状态: enabled/disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_owner (owner_user_id),
    UNIQUE KEY uk_owner_user_id (owner_user_id),
    INDEX idx_status (status),
    CONSTRAINT fk_venue_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='球馆表';

-- 场地表
CREATE TABLE courts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL COMMENT '所属球馆ID',
    name VARCHAR(128) NOT NULL COMMENT '场地名称',
    type VARCHAR(32) NOT NULL COMMENT '场地类型: 羽毛球/篮球/网球等',
    price_per_slot DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '每时段价格',
    status VARCHAR(16) NOT NULL DEFAULT 'enabled' COMMENT '状态: enabled/disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_venue (venue_id),
    INDEX idx_status (status),
    CONSTRAINT fk_court_venue FOREIGN KEY (venue_id) REFERENCES venues(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地表';

-- 时段表
CREATE TABLE slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    court_id BIGINT NOT NULL COMMENT '所属场地ID',
    slot_date DATE NOT NULL COMMENT '日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    status VARCHAR(16) NOT NULL DEFAULT 'available' COMMENT '状态: available/disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_court_date (court_id, slot_date),
    INDEX idx_status (status),
    CONSTRAINT fk_slot_court FOREIGN KEY (court_id) REFERENCES courts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时段表';

-- 订单表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    venue_id BIGINT NOT NULL COMMENT '球馆ID',
    court_id BIGINT NOT NULL COMMENT '场地ID',
    slot_id BIGINT NOT NULL COMMENT '时段ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    status VARCHAR(16) NOT NULL COMMENT '状态: PENDING_PAY/PAID/COMPLETED/CANCELED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    paid_at DATETIME NULL COMMENT '支付时间',
    completed_at DATETIME NULL COMMENT '完成时间',
    canceled_at DATETIME NULL COMMENT '取消时间',
    INDEX idx_user (user_id),
    INDEX idx_venue (venue_id),
    INDEX idx_slot (slot_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_order_venue FOREIGN KEY (venue_id) REFERENCES venues(id),
    CONSTRAINT fk_order_court FOREIGN KEY (court_id) REFERENCES courts(id),
    CONSTRAINT fk_order_slot FOREIGN KEY (slot_id) REFERENCES slots(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 时段占用表（防止冲突）
CREATE TABLE slot_reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slot_id BIGINT NOT NULL UNIQUE COMMENT '时段ID（唯一约束，防止重复预约）',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    status VARCHAR(16) NOT NULL DEFAULT 'locked' COMMENT '状态: locked/confirmed/released',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order (order_id),
    INDEX idx_status (status),
    CONSTRAINT fk_sr_slot FOREIGN KEY (slot_id) REFERENCES slots(id),
    CONSTRAINT fk_sr_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时段占用表';
