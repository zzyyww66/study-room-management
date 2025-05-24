-- 共享自习室管理系统 - 测试数据初始化脚本
-- =====================================================

-- =====================================================
-- 插入用户测试数据
-- =====================================================

-- 管理员用户
INSERT INTO users (username, password, email, phone, real_name, role, status, created_at, updated_at, last_login_at) VALUES 
('admin', 'admin123', 'admin@studyroom.com', '13800000001', '系统管理员', 'ADMIN', 'ACTIVE', NOW(), NOW(), NOW());

-- 普通用户
INSERT INTO users (username, password, email, phone, real_name, role, status, created_at, updated_at, last_login_at) VALUES 
('user001', 'password123', 'user001@example.com', '13800000002', '张三', 'USER', 'ACTIVE', NOW(), NOW(), NOW()),
('user002', 'password123', 'user002@example.com', '13800000003', '李四', 'USER', 'ACTIVE', NOW(), NOW(), NULL),
('user003', 'password123', 'user003@example.com', '13800000004', '王五', 'USER', 'ACTIVE', NOW(), NOW(), NULL),
('user004', 'password123', 'user004@example.com', '13800000005', '赵六', 'USER', 'INACTIVE', NOW(), NOW(), NULL),
('student01', 'student123', 'student01@university.edu', '13800000006', '陈小明', 'USER', 'ACTIVE', NOW(), NOW(), NOW()),
('student02', 'student123', 'student02@university.edu', '13800000007', '林小红', 'USER', 'ACTIVE', NOW(), NOW(), NULL);

-- =====================================================
-- 插入自习室测试数据
-- =====================================================

INSERT INTO study_rooms (name, description, capacity, hourly_rate, location, facilities, status, open_time, close_time, created_at, updated_at) VALUES 
('静音自习室A', '专为需要绝对安静环境的学习者设计，配备隔音设施', 30, 15.00, '图书馆2楼东区', 'WiFi,空调,饮水机,隔音墙,个人储物柜', 'AVAILABLE', '08:00:00', '22:00:00', NOW(), NOW()),
('讨论自习室B', '适合小组讨论和协作学习，配备白板和投影设备', 20, 20.00, '图书馆2楼西区', 'WiFi,空调,白板,投影仪,讨论桌椅', 'AVAILABLE', '08:00:00', '22:00:00', NOW(), NOW()),
('24小时自习室C', '全天候开放的自习空间，配备完善的安保设施', 40, 12.00, '图书馆3楼', 'WiFi,空调,饮水机,安保监控,应急照明', 'AVAILABLE', '00:00:00', '23:59:59', NOW(), NOW()),
('研究生专用室D', '专为研究生提供的高端学习环境，配备独立书架', 15, 25.00, '研究生院1楼', 'WiFi,空调,个人书架,台灯,打印机', 'AVAILABLE', '09:00:00', '21:00:00', NOW(), NOW()),
('考试复习室E', '考试期间专用，提供安静舒适的复习环境', 25, 18.00, '教学楼5楼', 'WiFi,空调,护眼台灯,静音键盘', 'MAINTENANCE', '08:00:00', '22:00:00', NOW(), NOW());

-- =====================================================
-- 插入座位测试数据
-- =====================================================

-- 静音自习室A的座位 (30个座位，编号A001-A030)
-- 窗边座位 (前5个，有窗户)
INSERT INTO seats (seat_number, type, status, description, has_window, has_power_outlet, has_lamp, equipment, study_room_id, created_at, updated_at) VALUES 
('A001', 'REGULAR', 'AVAILABLE', '靠窗座位，采光良好', true, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A002', 'REGULAR', 'AVAILABLE', '靠窗座位，采光良好', true, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A003', 'REGULAR', 'OCCUPIED', '靠窗座位，采光良好', true, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A004', 'REGULAR', 'AVAILABLE', '靠窗座位，采光良好', true, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A005', 'REGULAR', 'AVAILABLE', '靠窗座位，采光良好', true, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW());

-- 普通座位 (A006-A025)
INSERT INTO seats (seat_number, type, status, description, has_window, has_power_outlet, has_lamp, equipment, study_room_id, created_at, updated_at) VALUES 
('A006', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A007', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A008', 'REGULAR', 'OCCUPIED', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A009', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A010', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A011', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A012', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A013', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A014', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A015', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A016', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A017', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A018', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A019', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A020', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A021', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A022', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A023', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A024', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW()),
('A025', 'REGULAR', 'AVAILABLE', '标准学习座位', false, true, true, '护眼台灯,USB充电口', 1, NOW(), NOW());

-- VIP座位 (A026-A030)
INSERT INTO seats (seat_number, type, status, description, has_window, has_power_outlet, has_lamp, equipment, study_room_id, created_at, updated_at) VALUES 
('A026', 'VIP', 'AVAILABLE', 'VIP座位，空间宽敞', false, true, true, '护眼台灯,USB充电口,个人储物柜', 1, NOW(), NOW()),
('A027', 'VIP', 'AVAILABLE', 'VIP座位，空间宽敞', false, true, true, '护眼台灯,USB充电口,个人储物柜', 1, NOW(), NOW()),
('A028', 'VIP', 'AVAILABLE', 'VIP座位，空间宽敞', false, true, true, '护眼台灯,USB充电口,个人储物柜', 1, NOW(), NOW()),
('A029', 'VIP', 'AVAILABLE', 'VIP座位，空间宽敞', false, true, true, '护眼台灯,USB充电口,个人储物柜', 1, NOW(), NOW()),
('A030', 'VIP', 'AVAILABLE', 'VIP座位，空间宽敞', false, true, true, '护眼台灯,USB充电口,个人储物柜', 1, NOW(), NOW());

-- 讨论自习室B的座位 (20个座位，编号B001-B020)
INSERT INTO seats (seat_number, type, status, description, has_window, has_power_outlet, has_lamp, equipment, study_room_id, created_at, updated_at) VALUES 
('B001', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B002', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B003', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B004', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B005', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B006', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B007', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B008', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B009', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B010', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B011', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B012', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B013', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B014', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B015', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B016', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B017', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B018', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B019', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW()),
('B020', 'REGULAR', 'AVAILABLE', '讨论室座位', false, true, true, '护眼台灯,USB充电口', 2, NOW(), NOW());

-- 24小时自习室C的部分座位 (前10个座位，C001-C010)
INSERT INTO seats (seat_number, type, status, description, has_window, has_power_outlet, has_lamp, equipment, study_room_id, created_at, updated_at) VALUES 
('C001', 'REGULAR', 'AVAILABLE', '24小时自习室窗边座位', true, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C002', 'REGULAR', 'AVAILABLE', '24小时自习室窗边座位', true, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C003', 'REGULAR', 'AVAILABLE', '24小时自习室普通座位', false, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C004', 'REGULAR', 'AVAILABLE', '24小时自习室普通座位', false, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C005', 'REGULAR', 'AVAILABLE', '24小时自习室普通座位', false, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C006', 'REGULAR', 'AVAILABLE', '24小时自习室普通座位', false, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C007', 'REGULAR', 'AVAILABLE', '24小时自习室普通座位', false, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C008', 'REGULAR', 'AVAILABLE', '24小时自习室普通座位', false, true, true, '护眼台灯,USB充电口', 3, NOW(), NOW()),
('C009', 'VIP', 'AVAILABLE', '24小时自习室VIP座位', false, true, true, '护眼台灯,USB充电口,个人储物柜', 3, NOW(), NOW()),
('C010', 'VIP', 'AVAILABLE', '24小时自习室VIP座位', false, true, true, '护眼台灯,USB充电口,个人储物柜', 3, NOW(), NOW());

-- 研究生专用室D的座位 (15个座位，D001-D015)
INSERT INTO seats (seat_number, type, status, description, has_window, has_power_outlet, has_lamp, equipment, study_room_id, created_at, updated_at) VALUES 
('D001', 'VIP', 'AVAILABLE', '研究生专用VIP座位', true, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D002', 'VIP', 'AVAILABLE', '研究生专用VIP座位', true, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D003', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D004', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D005', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D006', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D007', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D008', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D009', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D010', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D011', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D012', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D013', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D014', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW()),
('D015', 'VIP', 'AVAILABLE', '研究生专用VIP座位', false, true, true, '护眼台灯,USB充电口,个人书架,储物柜', 4, NOW(), NOW());

-- =====================================================
-- 插入预订测试数据
-- =====================================================

-- 生成一些预订记录（包含当前时间的预订和历史预订）
-- 当前有效预订 (使用DATEADD替代INTERVAL，兼容H2数据库)
INSERT INTO reservations (reservation_code, start_time, end_time, status, payment_status, total_amount, notes, user_id, seat_id, created_at, updated_at) VALUES 
('RSV2024052401001', DATEADD('HOUR', 1, NOW()), DATEADD('HOUR', 4, NOW()), 'ACTIVE', 'PAID', 45.00, '今日学习计划', 2, 3, NOW(), NOW()),
('RSV2024052401002', DATEADD('HOUR', 2, NOW()), DATEADD('HOUR', 5, NOW()), 'ACTIVE', 'PAID', 45.00, '准备考试', 6, 8, NOW(), NOW());

-- 今日预订
INSERT INTO reservations (reservation_code, start_time, end_time, status, payment_status, total_amount, notes, user_id, seat_id, created_at, updated_at) VALUES 
('RSV2024052401003', DATEADD('HOUR', 3, NOW()), DATEADD('HOUR', 6, NOW()), 'ACTIVE', 'PENDING', 45.00, '下午学习', 3, 26, NOW(), NOW()),
('RSV2024052401004', DATEADD('HOUR', 4, NOW()), DATEADD('HOUR', 7, NOW()), 'ACTIVE', 'PENDING', 60.00, 'VIP座位学习', 7, 61, NOW(), NOW());

-- 历史预订（已完成）
INSERT INTO reservations (reservation_code, start_time, end_time, status, payment_status, total_amount, notes, user_id, seat_id, created_at, updated_at) VALUES 
('RSV2024052301001', DATEADD('DAY', -1, NOW()), DATEADD('HOUR', 3, DATEADD('DAY', -1, NOW())), 'COMPLETED', 'PAID', 45.00, '昨日学习记录', 2, 1, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('RSV2024052301002', DATEADD('HOUR', 4, DATEADD('DAY', -1, NOW())), DATEADD('HOUR', 7, DATEADD('DAY', -1, NOW())), 'COMPLETED', 'PAID', 45.00, '昨日下午学习', 6, 31, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW()));

-- 已取消的预订
INSERT INTO reservations (reservation_code, start_time, end_time, status, payment_status, total_amount, notes, user_id, seat_id, created_at, updated_at) VALUES 
('RSV2024052201001', DATEADD('DAY', -2, NOW()), DATEADD('HOUR', 2, DATEADD('DAY', -2, NOW())), 'CANCELLED', 'REFUNDED', 30.00, '临时有事取消', 3, 51, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()));

-- 过期未支付的预订
INSERT INTO reservations (reservation_code, start_time, end_time, status, payment_status, total_amount, notes, user_id, seat_id, created_at, updated_at) VALUES 
('RSV2024052101001', DATEADD('DAY', -3, NOW()), DATEADD('HOUR', 3, DATEADD('DAY', -3, NOW())), 'EXPIRED', 'PENDING', 45.00, '未及时支付', 4, 2, DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW()));

-- =====================================================
-- 数据统计信息
-- =====================================================

-- 总用户数: 7 (1个管理员 + 6个普通用户)
-- 总自习室数: 5 (4个可用 + 1个维护中)
-- 总座位数: 75 (分布在4个可用自习室中)
-- 总预订数: 8 (包含各种状态的预订)

-- 座位分布:
-- 静音自习室A: 30个座位 (5个窗边 + 20个普通 + 5个VIP)
-- 讨论自习室B: 20个座位 (全部普通座位)
-- 24小时自习室C: 10个座位 (2个窗边 + 6个普通 + 2个VIP)
-- 研究生专用室D: 15个座位 (全部VIP座位)

-- 当前占用座位: A003, A008 (通过OCCUPIED状态标记)
-- 当前预订座位: 根据reservations表中的ACTIVE状态记录 