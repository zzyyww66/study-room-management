-- 共享自习室管理系统 - 测试数据插入脚本
-- 用于开发和测试环境的示例数据

-- ==========================================
-- 1. 插入用户数据
-- ==========================================

-- 管理员账户
INSERT INTO t_user (username, password, real_name, email, phone, role, status) VALUES
('admin', '$2a$10$example.hashed.password.admin', '系统管理员', 'admin@studyroom.com', '13800000000', 'ADMIN', 'ACTIVE');

-- 普通用户账户
INSERT INTO t_user (username, password, real_name, email, phone, role, status) VALUES
('user001', '$2a$10$example.hashed.password.user001', '张三', 'zhangsan@example.com', '13800000001', 'USER', 'ACTIVE'),
('user002', '$2a$10$example.hashed.password.user002', '李四', 'lisi@example.com', '13800000002', 'USER', 'ACTIVE'),
('user003', '$2a$10$example.hashed.password.user003', '王五', 'wangwu@example.com', '13800000003', 'USER', 'ACTIVE'),
('user004', '$2a$10$example.hashed.password.user004', '赵六', 'zhaoliu@example.com', '13800000004', 'USER', 'ACTIVE'),
('user005', '$2a$10$example.hashed.password.user005', '钱七', 'qianqi@example.com', '13800000005', 'USER', 'ACTIVE'),
('user006', '$2a$10$example.hashed.password.user006', '孙八', 'sunba@example.com', '13800000006', 'USER', 'INACTIVE'),
('user007', '$2a$10$example.hashed.password.user007', '周九', 'zhoujiu@example.com', '13800000007', 'USER', 'ACTIVE'),
('user008', '$2a$10$example.hashed.password.user008', '吴十', 'wushi@example.com', '13800000008', 'USER', 'ACTIVE'),
('user009', '$2a$10$example.hashed.password.user009', '郑一', 'zhengyi@example.com', '13800000009', 'USER', 'ACTIVE'),
('user010', '$2a$10$example.hashed.password.user010', '冯二', 'feng2@example.com', '13800000010', 'USER', 'ACTIVE');

-- ==========================================
-- 2. 插入自习室数据
-- ==========================================

INSERT INTO t_study_room (room_name, location, capacity, description, status) VALUES
('第一自习室', '图书馆2楼东区', 120, '安静的学习环境，配备空调和免费WiFi', 'OPEN'),
('第二自习室', '图书馆2楼西区', 100, '24小时开放，适合夜间学习', 'OPEN'),
('第三自习室', '图书馆3楼', 80, 'VIP自习室，环境更加舒适', 'OPEN'),
('第四自习室', '教学楼A座5楼', 60, '配备投影设备，适合小组学习', 'OPEN'),
('第五自习室', '教学楼B座3楼', 40, '小型安静自习室', 'CLOSED');

-- ==========================================
-- 3. 插入座位数据
-- ==========================================

-- 第一自习室座位 (12行 x 10列 = 120个座位)
INSERT INTO t_seat (room_id, seat_number, seat_type, status, row_num, col_num) 
SELECT 
    1 as room_id,
    'A' || CASE WHEN r < 10 THEN '0' || r ELSE CAST(r as TEXT) END || 
    CASE WHEN c < 10 THEN '0' || c ELSE CAST(c as TEXT) END as seat_number,
    'NORMAL' as seat_type,
    'AVAILABLE' as status,
    r as row_num,
    c as col_num
FROM (
    SELECT DISTINCT 
        (a.num - 1) / 10 + 1 as r,
        (a.num - 1) % 10 + 1 as c
    FROM (
        SELECT 1 as num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
        SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION
        SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
        SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20 UNION
        SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION
        SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30 UNION
        SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35 UNION
        SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40 UNION
        SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION SELECT 45 UNION
        SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION SELECT 50 UNION
        SELECT 51 UNION SELECT 52 UNION SELECT 53 UNION SELECT 54 UNION SELECT 55 UNION
        SELECT 56 UNION SELECT 57 UNION SELECT 58 UNION SELECT 59 UNION SELECT 60 UNION
        SELECT 61 UNION SELECT 62 UNION SELECT 63 UNION SELECT 64 UNION SELECT 65 UNION
        SELECT 66 UNION SELECT 67 UNION SELECT 68 UNION SELECT 69 UNION SELECT 70 UNION
        SELECT 71 UNION SELECT 72 UNION SELECT 73 UNION SELECT 74 UNION SELECT 75 UNION
        SELECT 76 UNION SELECT 77 UNION SELECT 78 UNION SELECT 79 UNION SELECT 80 UNION
        SELECT 81 UNION SELECT 82 UNION SELECT 83 UNION SELECT 84 UNION SELECT 85 UNION
        SELECT 86 UNION SELECT 87 UNION SELECT 88 UNION SELECT 89 UNION SELECT 90 UNION
        SELECT 91 UNION SELECT 92 UNION SELECT 93 UNION SELECT 94 UNION SELECT 95 UNION
        SELECT 96 UNION SELECT 97 UNION SELECT 98 UNION SELECT 99 UNION SELECT 100 UNION
        SELECT 101 UNION SELECT 102 UNION SELECT 103 UNION SELECT 104 UNION SELECT 105 UNION
        SELECT 106 UNION SELECT 107 UNION SELECT 108 UNION SELECT 109 UNION SELECT 110 UNION
        SELECT 111 UNION SELECT 112 UNION SELECT 113 UNION SELECT 114 UNION SELECT 115 UNION
        SELECT 116 UNION SELECT 117 UNION SELECT 118 UNION SELECT 119 UNION SELECT 120
    ) a
);

-- 第二自习室座位 (10行 x 10列 = 100个座位)
INSERT INTO t_seat (room_id, seat_number, seat_type, status, row_num, col_num) VALUES
-- 第1行
(2, 'B0101', 'NORMAL', 'AVAILABLE', 1, 1), (2, 'B0102', 'NORMAL', 'AVAILABLE', 1, 2), (2, 'B0103', 'NORMAL', 'AVAILABLE', 1, 3), (2, 'B0104', 'NORMAL', 'AVAILABLE', 1, 4), (2, 'B0105', 'NORMAL', 'AVAILABLE', 1, 5),
(2, 'B0106', 'NORMAL', 'AVAILABLE', 1, 6), (2, 'B0107', 'NORMAL', 'AVAILABLE', 1, 7), (2, 'B0108', 'NORMAL', 'AVAILABLE', 1, 8), (2, 'B0109', 'NORMAL', 'AVAILABLE', 1, 9), (2, 'B0110', 'NORMAL', 'AVAILABLE', 1, 10),
-- 第2行
(2, 'B0201', 'NORMAL', 'AVAILABLE', 2, 1), (2, 'B0202', 'NORMAL', 'AVAILABLE', 2, 2), (2, 'B0203', 'NORMAL', 'OCCUPIED', 2, 3), (2, 'B0204', 'NORMAL', 'AVAILABLE', 2, 4), (2, 'B0205', 'NORMAL', 'AVAILABLE', 2, 5),
(2, 'B0206', 'NORMAL', 'AVAILABLE', 2, 6), (2, 'B0207', 'NORMAL', 'OCCUPIED', 2, 7), (2, 'B0208', 'NORMAL', 'AVAILABLE', 2, 8), (2, 'B0209', 'NORMAL', 'AVAILABLE', 2, 9), (2, 'B0210', 'NORMAL', 'AVAILABLE', 2, 10),
-- 第3行
(2, 'B0301', 'NORMAL', 'AVAILABLE', 3, 1), (2, 'B0302', 'NORMAL', 'AVAILABLE', 3, 2), (2, 'B0303', 'NORMAL', 'AVAILABLE', 3, 3), (2, 'B0304', 'NORMAL', 'AVAILABLE', 3, 4), (2, 'B0305', 'NORMAL', 'MAINTENANCE', 3, 5),
(2, 'B0306', 'NORMAL', 'AVAILABLE', 3, 6), (2, 'B0307', 'NORMAL', 'AVAILABLE', 3, 7), (2, 'B0308', 'NORMAL', 'AVAILABLE', 3, 8), (2, 'B0309', 'NORMAL', 'AVAILABLE', 3, 9), (2, 'B0310', 'NORMAL', 'AVAILABLE', 3, 10),
-- 第4-10行 (简化插入)
(2, 'B0401', 'NORMAL', 'AVAILABLE', 4, 1), (2, 'B0402', 'NORMAL', 'AVAILABLE', 4, 2), (2, 'B0403', 'NORMAL', 'AVAILABLE', 4, 3), (2, 'B0404', 'NORMAL', 'AVAILABLE', 4, 4), (2, 'B0405', 'NORMAL', 'AVAILABLE', 4, 5),
(2, 'B0406', 'NORMAL', 'AVAILABLE', 4, 6), (2, 'B0407', 'NORMAL', 'AVAILABLE', 4, 7), (2, 'B0408', 'NORMAL', 'AVAILABLE', 4, 8), (2, 'B0409', 'NORMAL', 'AVAILABLE', 4, 9), (2, 'B0410', 'NORMAL', 'AVAILABLE', 4, 10);

-- 简化其余座位插入（实际项目中可以用程序生成）
INSERT INTO t_seat (room_id, seat_number, seat_type, status, row_num, col_num) VALUES
-- 第三自习室 VIP座位
(3, 'C001', 'VIP', 'AVAILABLE', 1, 1), (3, 'C002', 'VIP', 'AVAILABLE', 1, 2), (3, 'C003', 'VIP', 'OCCUPIED', 1, 3), (3, 'C004', 'VIP', 'AVAILABLE', 1, 4),
(3, 'C005', 'VIP', 'AVAILABLE', 1, 5), (3, 'C006', 'VIP', 'AVAILABLE', 1, 6), (3, 'C007', 'VIP', 'AVAILABLE', 1, 7), (3, 'C008', 'VIP', 'AVAILABLE', 1, 8),
-- 第四自习室座位
(4, 'D001', 'NORMAL', 'AVAILABLE', 1, 1), (4, 'D002', 'NORMAL', 'AVAILABLE', 1, 2), (4, 'D003', 'NORMAL', 'AVAILABLE', 1, 3), (4, 'D004', 'NORMAL', 'AVAILABLE', 1, 4),
(4, 'D005', 'NORMAL', 'AVAILABLE', 1, 5), (4, 'D006', 'NORMAL', 'AVAILABLE', 1, 6), (4, 'D007', 'NORMAL', 'AVAILABLE', 1, 7), (4, 'D008', 'NORMAL', 'AVAILABLE', 1, 8),
-- 第五自习室座位（关闭状态）
(5, 'E001', 'NORMAL', 'MAINTENANCE', 1, 1), (5, 'E002', 'NORMAL', 'MAINTENANCE', 1, 2), (5, 'E003', 'NORMAL', 'MAINTENANCE', 1, 3), (5, 'E004', 'NORMAL', 'MAINTENANCE', 1, 4);

-- ==========================================
-- 4. 插入预订数据
-- ==========================================

-- 当前活跃预订
INSERT INTO t_reservation (user_id, seat_id, start_time, end_time, status) VALUES
-- 今天的预订
(2, 23, '2024-01-01 09:00:00', '2024-01-01 12:00:00', 'ACTIVE'),
(3, 27, '2024-01-01 14:00:00', '2024-01-01 17:00:00', 'ACTIVE'),
(4, 123, '2024-01-01 09:30:00', '2024-01-01 11:30:00', 'ACTIVE'),
-- 明天的预订
(5, 1, '2024-01-02 08:00:00', '2024-01-02 10:00:00', 'ACTIVE'),
(6, 15, '2024-01-02 10:30:00', '2024-01-02 12:30:00', 'ACTIVE'),
(7, 45, '2024-01-02 14:00:00', '2024-01-02 16:00:00', 'ACTIVE'),
-- 历史预订（已完成）
(2, 10, '2023-12-30 09:00:00', '2023-12-30 12:00:00', 'COMPLETED'),
(3, 20, '2023-12-30 14:00:00', '2023-12-30 17:00:00', 'COMPLETED'),
(4, 30, '2023-12-31 08:00:00', '2023-12-31 11:00:00', 'COMPLETED'),
-- 取消的预订
(5, 40, '2024-01-01 16:00:00', '2024-01-01 18:00:00', 'CANCELLED'),
(6, 50, '2024-01-01 19:00:00', '2024-01-01 21:00:00', 'CANCELLED');

-- ==========================================
-- 5. 更新座位状态（根据预订情况）
-- ==========================================

-- 将有活跃预订的座位状态设置为 OCCUPIED
UPDATE t_seat 
SET status = 'OCCUPIED' 
WHERE seat_id IN (
    SELECT DISTINCT seat_id 
    FROM t_reservation 
    WHERE status = 'ACTIVE' 
    AND start_time <= datetime('now') 
    AND end_time > datetime('now')
);

-- ==========================================
-- 6. 验证数据插入
-- ==========================================

-- 查看用户统计
SELECT 
    'Users' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as admin_count,
    COUNT(CASE WHEN role = 'USER' THEN 1 END) as user_count,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count
FROM t_user

UNION ALL

-- 查看自习室统计
SELECT 
    'Study Rooms' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN status = 'OPEN' THEN 1 END) as open_count,
    COUNT(CASE WHEN status = 'CLOSED' THEN 1 END) as closed_count,
    SUM(capacity) as total_capacity
FROM t_study_room

UNION ALL

-- 查看座位统计
SELECT 
    'Seats' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN status = 'AVAILABLE' THEN 1 END) as available_count,
    COUNT(CASE WHEN status = 'OCCUPIED' THEN 1 END) as occupied_count,
    COUNT(CASE WHEN status = 'MAINTENANCE' THEN 1 END) as maintenance_count
FROM t_seat

UNION ALL

-- 查看预订统计
SELECT 
    'Reservations' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_count
FROM t_reservation;

-- 输出插入完成信息
SELECT 'Test data inserted successfully!' as message; 