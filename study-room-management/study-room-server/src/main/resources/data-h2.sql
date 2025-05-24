-- H2数据库初始化测试数据
-- ===================================================

-- 注意：此文件仅在使用H2数据库时生效
-- Spring Boot会自动执行此文件来初始化测试数据

INSERT INTO users (id, username, password, email, phone, role, status, created_at, updated_at) VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKfkIyHuGGo1xT7SPz67fBZr2', 'admin@studyroom.com', '13800138000', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKfkIyHuGGo1xT7SPz67fBZr2', 'user1@studyroom.com', '13800138001', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKfkIyHuGGo1xT7SPz67fBZr2', 'user2@studyroom.com', '13800138002', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO study_rooms (id, name, description, capacity, location, status, created_at, updated_at) VALUES 
(1, 'A101自习室', '安静的学习环境，适合个人学习', 50, '图书馆A区1楼', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'B201讨论室', '小组讨论专用，配备白板和投影仪', 12, '图书馆B区2楼', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'C301阅览室', '期刊杂志阅览，环境舒适', 30, '图书馆C区3楼', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO seats (id, room_id, seat_number, status, created_at, updated_at) VALUES 
-- A101自习室的座位
(1, 1, 'A101-001', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'A101-002', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'A101-003', 'OCCUPIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- B201讨论室的座位  
(4, 2, 'B201-001', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'B201-002', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- C301阅览室的座位
(6, 3, 'C301-001', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 3, 'C301-002', 'MAINTENANCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO reservations (id, user_id, seat_id, start_time, end_time, status, created_at, updated_at) VALUES 
(1, 2, 3, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 7, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 