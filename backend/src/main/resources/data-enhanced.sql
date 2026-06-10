-- 球馆预约管理系统 - 增强版测试数据
-- 包含：7个用户、3个球馆、10个场地、未来7天的Slot、示例订单
-- 所有密码都是 "123456"，使用 BCrypt 加密

USE gym_booking;

-- 强制当前会话使用 UTF-8，避免导入中文出现乱码
SET NAMES utf8mb4;

-- ============================================
-- 1. 用户数据
-- ============================================

-- 超级管理员
-- 用户名: admin, 密码: 123456
INSERT INTO users (username, password_hash, role, status, created_at) VALUES
('admin', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'SUPER_ADMIN', 'enabled', NOW());

-- 球馆管理员（3个）
-- 用户名: venue_admin1/2/3, 密码: 123456
INSERT INTO users (username, password_hash, role, phone, status, created_at) VALUES
('venue_admin1', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'VENUE_ADMIN', '13800138001', 'enabled', NOW()),
('venue_admin2', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'VENUE_ADMIN', '13800138002', 'enabled', NOW()),
('venue_admin3', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'VENUE_ADMIN', '13800138003', 'enabled', NOW());

-- 普通用户（5个）
-- 用户名: user1/2/3/4/5, 密码: 123456
INSERT INTO users (username, password_hash, role, phone, status, created_at) VALUES
('user1', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139001', 'enabled', NOW()),
('user2', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139002', 'enabled', NOW()),
('user3', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139003', 'enabled', NOW()),
('user4', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139004', 'enabled', NOW()),
('user5', '$2a$10$9RLYXVWK7/zVA3wSSITRiuL0GiFg8YnWqYwSxE22xYxbHuqIM1FJS', 'USER', '13900139005', 'enabled', NOW());

-- ============================================
-- 2. 球馆数据
-- ============================================

INSERT INTO venues (name, address, phone, open_time, close_time, owner_user_id, status, created_at) VALUES
('阳光羽毛球馆', '北京市朝阳区建国路88号SOHO现代城', '010-12345678', '09:00:00', '22:00:00', 2, 'enabled', NOW()),
('星空篮球馆', '上海市浦东新区世纪大道100号环球金融中心', '021-87654321', '08:00:00', '23:00:00', 3, 'enabled', NOW()),
('蓝天网球中心', '广州市天河区体育西路200号天河体育中心', '020-11223344', '07:00:00', '21:00:00', 4, 'enabled', NOW());

-- ============================================
-- 3. 场地数据
-- ============================================

-- 阳光羽毛球馆（4个场地）
INSERT INTO courts (venue_id, name, type, price_per_slot, status, created_at) VALUES
(1, '1号场地', '羽毛球', 80.00, 'enabled', NOW()),
(1, '2号场地', '羽毛球', 80.00, 'enabled', NOW()),
(1, '3号场地', '羽毛球', 100.00, 'enabled', NOW()),
(1, 'VIP场地', '羽毛球', 150.00, 'enabled', NOW());

-- 星空篮球馆（3个场地）
INSERT INTO courts (venue_id, name, type, price_per_slot, status, created_at) VALUES
(2, 'A场', '篮球', 200.00, 'enabled', NOW()),
(2, 'B场', '篮球', 200.00, 'enabled', NOW()),
(2, 'C场', '篮球', 250.00, 'enabled', NOW());

-- 蓝天网球中心（3个场地）
INSERT INTO courts (venue_id, name, type, price_per_slot, status, created_at) VALUES
(3, '1号网球场', '网球', 120.00, 'enabled', NOW()),
(3, '2号网球场', '网球', 120.00, 'enabled', NOW()),
(3, '3号网球场', '网球', 150.00, 'enabled', NOW());

-- ============================================
-- 4. 时段数据（未来7天）
-- ============================================

-- 为阳光羽毛球馆的1号场地生成未来7天的时段（09:00-22:00，每小时一个）
DELIMITER $$

CREATE PROCEDURE generate_slots_for_court_1()
BEGIN
    DECLARE day_offset INT DEFAULT 0;
    DECLARE current_date DATE;
    DECLARE hour INT;

    WHILE day_offset < 7 DO
        SET current_date = DATE_ADD(CURDATE(), INTERVAL day_offset DAY);
        SET hour = 9;

        WHILE hour < 22 DO
            INSERT INTO slots (court_id, slot_date, start_time, end_time, price, status, created_at)
            VALUES (
                1,
                current_date,
                MAKETIME(hour, 0, 0),
                MAKETIME(hour + 1, 0, 0),
                80.00,
                'available',
                NOW()
            );
            SET hour = hour + 1;
        END WHILE;

        SET day_offset = day_offset + 1;
    END WHILE;
END$$

DELIMITER ;

CALL generate_slots_for_court_1();
DROP PROCEDURE generate_slots_for_court_1;

-- 为星空篮球馆的A场生成未来7天的时段（08:00-23:00，每小时一个）
DELIMITER $$

CREATE PROCEDURE generate_slots_for_court_5()
BEGIN
    DECLARE day_offset INT DEFAULT 0;
    DECLARE current_date DATE;
    DECLARE hour INT;

    WHILE day_offset < 7 DO
        SET current_date = DATE_ADD(CURDATE(), INTERVAL day_offset DAY);
        SET hour = 8;

        WHILE hour < 23 DO
            INSERT INTO slots (court_id, slot_date, start_time, end_time, price, status, created_at)
            VALUES (
                5,
                current_date,
                MAKETIME(hour, 0, 0),
                MAKETIME(hour + 1, 0, 0),
                200.00,
                'available',
                NOW()
            );
            SET hour = hour + 1;
        END WHILE;

        SET day_offset = day_offset + 1;
    END WHILE;
END$$

DELIMITER ;

CALL generate_slots_for_court_5();
DROP PROCEDURE generate_slots_for_court_5;

-- 为蓝天网球中心的1号网球场生成未来7天的时段（07:00-21:00，每小时一个）
DELIMITER $$

CREATE PROCEDURE generate_slots_for_court_8()
BEGIN
    DECLARE day_offset INT DEFAULT 0;
    DECLARE current_date DATE;
    DECLARE hour INT;

    WHILE day_offset < 7 DO
        SET current_date = DATE_ADD(CURDATE(), INTERVAL day_offset DAY);
        SET hour = 7;

        WHILE hour < 21 DO
            INSERT INTO slots (court_id, slot_date, start_time, end_time, price, status, created_at)
            VALUES (
                8,
                current_date,
                MAKETIME(hour, 0, 0),
                MAKETIME(hour + 1, 0, 0),
                120.00,
                'available',
                NOW()
            );
            SET hour = hour + 1;
        END WHILE;

        SET day_offset = day_offset + 1;
    END WHILE;
END$$

DELIMITER ;

CALL generate_slots_for_court_8();
DROP PROCEDURE generate_slots_for_court_8;

-- ============================================
-- 5. 示例订单数据
-- ============================================

-- 订单1：user1 预约了今天 10:00-11:00 的时段，已支付
INSERT INTO orders (order_no, user_id, venue_id, court_id, slot_id, amount, status, paid_at, created_at)
SELECT
    'ORD20240207100001',
    5,
    1,
    1,
    id,
    80.00,
    'PAID',
    NOW(),
    NOW()
FROM slots
WHERE court_id = 1 AND slot_date = CURDATE() AND start_time = '10:00:00'
LIMIT 1;

-- 插入占用记录
INSERT INTO slot_reservations (slot_id, order_id, status, created_at)
SELECT
    id,
    1,
    'confirmed',
    NOW()
FROM slots
WHERE court_id = 1 AND slot_date = CURDATE() AND start_time = '10:00:00'
LIMIT 1;

-- 订单2：user2 预约了明天 14:00-15:00 的时段，待支付
INSERT INTO orders (order_no, user_id, venue_id, court_id, slot_id, amount, status, created_at)
SELECT
    'ORD20240207100002',
    6,
    1,
    1,
    id,
    80.00,
    'PENDING_PAY',
    NOW()
FROM slots
WHERE court_id = 1 AND slot_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND start_time = '14:00:00'
LIMIT 1;

-- 插入占用记录
INSERT INTO slot_reservations (slot_id, order_id, status, created_at)
SELECT
    id,
    2,
    'locked',
    NOW()
FROM slots
WHERE court_id = 1 AND slot_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND start_time = '14:00:00'
LIMIT 1;

-- 订单3：user3 预约了后天 18:00-19:00 的时段，已支付已完成
INSERT INTO orders (order_no, user_id, venue_id, court_id, slot_id, amount, status, paid_at, completed_at, created_at)
SELECT
    'ORD20240207100003',
    7,
    2,
    5,
    id,
    200.00,
    'COMPLETED',
    NOW(),
    NOW(),
    NOW()
FROM slots
WHERE court_id = 5 AND slot_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY) AND start_time = '18:00:00'
LIMIT 1;

-- 插入占用记录
INSERT INTO slot_reservations (slot_id, order_id, status, created_at)
SELECT
    id,
    3,
    'confirmed',
    NOW()
FROM slots
WHERE court_id = 5 AND slot_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY) AND start_time = '18:00:00'
LIMIT 1;

-- ============================================
-- 6. 数据验证查询
-- ============================================

SELECT '========================================' AS '';
SELECT '数据初始化完成！' AS '';
SELECT '========================================' AS '';

SELECT '=== 用户统计 ===' AS '';
SELECT
    role AS '角色',
    COUNT(*) AS '数量'
FROM users
GROUP BY role;

SELECT '=== 球馆列表 ===' AS '';
SELECT
    id AS 'ID',
    name AS '名称',
    address AS '地址',
    CONCAT(open_time, ' - ', close_time) AS '营业时间',
    status AS '状态'
FROM venues;

SELECT '=== 场地统计 ===' AS '';
SELECT
    v.name AS '球馆',
    COUNT(c.id) AS '场地数量'
FROM venues v
LEFT JOIN courts c ON v.id = c.venue_id
GROUP BY v.id, v.name;

SELECT '=== 时段统计 ===' AS '';
SELECT
    c.name AS '场地',
    COUNT(s.id) AS '时段数量',
    SUM(CASE WHEN s.status = 'available' THEN 1 ELSE 0 END) AS '可用',
    SUM(CASE WHEN s.id IN (SELECT slot_id FROM slot_reservations) THEN 1 ELSE 0 END) AS '已预约'
FROM courts c
LEFT JOIN slots s ON c.id = s.court_id
GROUP BY c.id, c.name;

SELECT '=== 订单统计 ===' AS '';
SELECT
    status AS '状态',
    COUNT(*) AS '数量',
    SUM(amount) AS '总金额'
FROM orders
GROUP BY status;

SELECT '========================================' AS '';
SELECT '测试账号信息：' AS '';
SELECT '超级管理员: admin / 123456' AS '';
SELECT '球馆管理员: venue_admin1 / 123456' AS '';
SELECT '普通用户: user1 / 123456' AS '';
SELECT '========================================' AS '';
