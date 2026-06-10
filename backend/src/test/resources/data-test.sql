INSERT INTO users (id, username, password_hash, role, phone, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SUPER_ADMIN', '13800000001', 'enabled'),
(2, 'venue_admin1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'VENUE_ADMIN', '13800000002', 'enabled'),
(3, 'venue_admin2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'VENUE_ADMIN', '13800000003', 'enabled'),
(4, 'venue_admin3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'VENUE_ADMIN', '13800000004', 'enabled'),
(5, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', '13900000001', 'enabled'),
(6, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', '13900000002', 'enabled');

INSERT INTO venues (id, name, address, phone, open_time, close_time, owner_user_id, status) VALUES
(1, '阳光羽毛球馆', '北京市朝阳区建国路88号', '010-12345678', '09:00:00', '22:00:00', 2, 'enabled'),
(2, '星空篮球馆', '上海市浦东新区世纪大道100号', '021-87654321', '08:00:00', '23:00:00', 3, 'enabled'),
(3, '蓝天网球中心', '广州市天河区体育西路200号', '020-11223344', '07:00:00', '21:00:00', 4, 'enabled');

INSERT INTO courts (id, venue_id, name, type, price_per_slot, status) VALUES
(1, 1, '1号场地', '羽毛球', 80.00, 'enabled'),
(2, 1, '2号场地', '羽毛球', 100.00, 'enabled'),
(3, 2, 'A场', '篮球', 200.00, 'enabled');

INSERT INTO slots (id, court_id, slot_date, start_time, end_time, price, status) VALUES
(1, 1, DATE '2024-02-10', '09:00:00', '10:00:00', 80.00, 'available');

ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE venues ALTER COLUMN id RESTART WITH 100;
ALTER TABLE courts ALTER COLUMN id RESTART WITH 100;
ALTER TABLE slots ALTER COLUMN id RESTART WITH 100;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 100;
ALTER TABLE slot_reservations ALTER COLUMN id RESTART WITH 100;
