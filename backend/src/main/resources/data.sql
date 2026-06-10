-- 球馆预约管理系统 - 测试数据
-- 注意：密码都是 "123456"，使用 BCrypt 加密

USE gym_booking;

-- 强制当前会话使用 UTF-8，避免导入中文出现乱码
SET NAMES utf8mb4;

-- 清空现有数据（谨慎使用）
-- TRUNCATE TABLE slot_reservations;
-- TRUNCATE TABLE orders;
-- TRUNCATE TABLE slots;
-- TRUNCATE TABLE courts;
-- TRUNCATE TABLE venues;
-- TRUNCATE TABLE users;

-- 1. 插入超级管理员
-- 用户名: admin, 密码: 123456
INSERT INTO users (username, password_hash, role, status) VALUES
('admin', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'SUPER_ADMIN', 'enabled');

-- 2. 插入球馆管理员
-- 用户名: venue_admin1, 密码: 123456
-- 用户名: venue_admin2, 密码: 123456
-- 用户名: venue_admin3, 密码: 123456
INSERT INTO users (username, password_hash, role, phone, status) VALUES
('venue_admin1', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'VENUE_ADMIN', '13800138001', 'enabled'),
('venue_admin2', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'VENUE_ADMIN', '13800138002', 'enabled'),
('venue_admin3', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'VENUE_ADMIN', '13800138003', 'enabled');

-- 3. 插入普通用户
-- 用户名: user1, 密码: 123456
-- 用户名: user2, 密码: 123456
-- 用户名: user3, 密码: 123456
INSERT INTO users (username, password_hash, role, phone, status) VALUES
('user1', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139001', 'enabled'),
('user2', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139002', 'enabled'),
('user3', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139003', 'enabled');

-- 4. 插入球馆
-- 假设 venue_admin1 的 ID 是 2, venue_admin2 的 ID 是 3, venue_admin3 的 ID 是 4
INSERT INTO venues (name, address, phone, open_time, close_time, owner_user_id, status) VALUES
('阳光羽毛球馆', '北京市朝阳区建国路88号', '010-12345678', '09:00:00', '22:00:00', 2, 'enabled'),
('星空篮球馆', '上海市浦东新区世纪大道100号', '021-87654321', '08:00:00', '23:00:00', 3, 'enabled'),
('蓝天网球中心', '广州市天河区体育西路200号', '020-11223344', '07:00:00', '21:00:00', 4, 'enabled');

-- 5. 插入场地
-- 假设阳光羽毛球馆的 ID 是 1
INSERT INTO courts (venue_id, name, type, price_per_slot, status) VALUES
(1, '1号场地', '羽毛球', 80.00, 'enabled'),
(1, '2号场地', '羽毛球', 80.00, 'enabled'),
(1, '3号场地', '羽毛球', 100.00, 'enabled'),
(1, 'VIP场地', '羽毛球', 150.00, 'enabled');

-- 假设星空篮球馆的 ID 是 2
INSERT INTO courts (venue_id, name, type, price_per_slot, status) VALUES
(2, 'A场', '篮球', 200.00, 'enabled'),
(2, 'B场', '篮球', 200.00, 'enabled'),
(2, 'C场', '篮球', 250.00, 'enabled');

-- 假设蓝天网球中心的 ID 是 3
INSERT INTO courts (venue_id, name, type, price_per_slot, status) VALUES
(3, '1号网球场', '网球', 120.00, 'enabled'),
(3, '2号网球场', '网球', 120.00, 'enabled'),
(3, '3号网球场', '网球', 150.00, 'enabled');

-- 6. 插入时段（示例：为第一个场地生成今天和明天的时段）
-- 注意：实际使用时应该通过后端 API 生成时段
-- 这里只是示例数据

-- 今天的日期（需要根据实际情况调整）
SET @today = CURDATE();
SET @tomorrow = DATE_ADD(CURDATE(), INTERVAL 1 DAY);

-- 为 1 号场地生成今天 9:00-22:00 的时段
INSERT INTO slots (court_id, slot_date, start_time, end_time, price, status) VALUES
(1, @today, '09:00:00', '10:00:00', 80.00, 'available'),
(1, @today, '10:00:00', '11:00:00', 80.00, 'available'),
(1, @today, '11:00:00', '12:00:00', 80.00, 'available'),
(1, @today, '12:00:00', '13:00:00', 80.00, 'available'),
(1, @today, '13:00:00', '14:00:00', 80.00, 'available'),
(1, @today, '14:00:00', '15:00:00', 80.00, 'available'),
(1, @today, '15:00:00', '16:00:00', 80.00, 'available'),
(1, @today, '16:00:00', '17:00:00', 80.00, 'available'),
(1, @today, '17:00:00', '18:00:00', 80.00, 'available'),
(1, @today, '18:00:00', '19:00:00', 80.00, 'available'),
(1, @today, '19:00:00', '20:00:00', 80.00, 'available'),
(1, @today, '20:00:00', '21:00:00', 80.00, 'available'),
(1, @today, '21:00:00', '22:00:00', 80.00, 'available');

-- 为 1 号场地生成明天 9:00-22:00 的时段
INSERT INTO slots (court_id, slot_date, start_time, end_time, price, status) VALUES
(1, @tomorrow, '09:00:00', '10:00:00', 80.00, 'available'),
(1, @tomorrow, '10:00:00', '11:00:00', 80.00, 'available'),
(1, @tomorrow, '11:00:00', '12:00:00', 80.00, 'available'),
(1, @tomorrow, '12:00:00', '13:00:00', 80.00, 'available'),
(1, @tomorrow, '13:00:00', '14:00:00', 80.00, 'available'),
(1, @tomorrow, '14:00:00', '15:00:00', 80.00, 'available'),
(1, @tomorrow, '15:00:00', '16:00:00', 80.00, 'available'),
(1, @tomorrow, '16:00:00', '17:00:00', 80.00, 'available'),
(1, @tomorrow, '17:00:00', '18:00:00', 80.00, 'available'),
(1, @tomorrow, '18:00:00', '19:00:00', 80.00, 'available'),
(1, @tomorrow, '19:00:00', '20:00:00', 80.00, 'available'),
(1, @tomorrow, '20:00:00', '21:00:00', 80.00, 'available'),
(1, @tomorrow, '21:00:00', '22:00:00', 80.00, 'available');

-- 7. 插入示例订单
-- 假设 user1 的 ID 是 5
-- 假设第一个 slot 的 ID 是 1
INSERT INTO orders (order_no, user_id, venue_id, court_id, slot_id, amount, status, paid_at) VALUES
('ORD20240207001', 5, 1, 1, 1, 80.00, 'PAID', NOW());

-- 插入占用记录
INSERT INTO slot_reservations (slot_id, order_id, status) VALUES
(1, 1, 'confirmed');

-- 查询验证
SELECT '=== 用户列表 ===' AS '';
SELECT id, username, role, status FROM users;

SELECT '=== 球馆列表 ===' AS '';
SELECT id, name, address, owner_user_id FROM venues;

SELECT '=== 场地列表 ===' AS '';
SELECT id, venue_id, name, type, price_per_slot FROM courts;

SELECT '=== 时段列表（前10条）===' AS '';
SELECT id, court_id, slot_date, start_time, end_time, price, status FROM slots LIMIT 10;

SELECT '=== 订单列表 ===' AS '';
SELECT id, order_no, user_id, venue_id, court_id, slot_id, amount, status FROM orders;
