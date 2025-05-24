-- 共享自习室管理系统 - 数据库表结构创建脚本
-- 数据库: SQLite
-- 创建时间: 2024年

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS t_reservation;
DROP TABLE IF EXISTS t_seat;
DROP TABLE IF EXISTS t_study_room;
DROP TABLE IF EXISTS t_user;

-- ==========================================
-- 1. 用户表 (t_user)
-- ==========================================
CREATE TABLE t_user (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户表索引
CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_email ON t_user(email);
CREATE INDEX idx_user_role ON t_user(role);
CREATE INDEX idx_user_status ON t_user(status);

-- ==========================================
-- 2. 自习室表 (t_study_room)
-- ==========================================
CREATE TABLE t_study_room (
    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    capacity INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建自习室表索引
CREATE INDEX idx_study_room_name ON t_study_room(room_name);
CREATE INDEX idx_study_room_status ON t_study_room(status);

-- ==========================================
-- 3. 座位表 (t_seat)
-- ==========================================
CREATE TABLE t_seat (
    seat_id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id INTEGER NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    row_num INTEGER NOT NULL DEFAULT 1,
    col_num INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES t_study_room(room_id) ON DELETE CASCADE
);

-- 创建座位表索引
CREATE INDEX idx_seat_room_id ON t_seat(room_id);
CREATE INDEX idx_seat_number ON t_seat(seat_number);
CREATE INDEX idx_seat_status ON t_seat(status);
CREATE INDEX idx_seat_type ON t_seat(seat_type);
CREATE UNIQUE INDEX idx_seat_room_number ON t_seat(room_id, seat_number);

-- ==========================================
-- 4. 预订表 (t_reservation)
-- ==========================================
CREATE TABLE t_reservation (
    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    seat_id INTEGER NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES t_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES t_seat(seat_id) ON DELETE CASCADE
);

-- 创建预订表索引
CREATE INDEX idx_reservation_user_id ON t_reservation(user_id);
CREATE INDEX idx_reservation_seat_id ON t_reservation(seat_id);
CREATE INDEX idx_reservation_start_time ON t_reservation(start_time);
CREATE INDEX idx_reservation_end_time ON t_reservation(end_time);
CREATE INDEX idx_reservation_status ON t_reservation(status);
CREATE INDEX idx_reservation_created_at ON t_reservation(created_at);

-- ==========================================
-- 数据约束和触发器
-- ==========================================

-- 用户表更新时间触发器
CREATE TRIGGER tr_user_updated_at
    AFTER UPDATE ON t_user
BEGIN
    UPDATE t_user SET updated_at = CURRENT_TIMESTAMP WHERE user_id = NEW.user_id;
END;

-- 自习室表更新时间触发器
CREATE TRIGGER tr_study_room_updated_at
    AFTER UPDATE ON t_study_room
BEGIN
    UPDATE t_study_room SET updated_at = CURRENT_TIMESTAMP WHERE room_id = NEW.room_id;
END;

-- 座位表更新时间触发器
CREATE TRIGGER tr_seat_updated_at
    AFTER UPDATE ON t_seat
BEGIN
    UPDATE t_seat SET updated_at = CURRENT_TIMESTAMP WHERE seat_id = NEW.seat_id;
END;

-- 预订表更新时间触发器
CREATE TRIGGER tr_reservation_updated_at
    AFTER UPDATE ON t_reservation
BEGIN
    UPDATE t_reservation SET updated_at = CURRENT_TIMESTAMP WHERE reservation_id = NEW.reservation_id;
END;

-- ==========================================
-- 数据完整性约束
-- ==========================================

-- 检查约束：用户角色只能是 USER 或 ADMIN
-- SQLite 不支持 CHECK 约束的枚举值，将在应用层实现

-- 检查约束：用户状态只能是 ACTIVE 或 INACTIVE
-- SQLite 不支持 CHECK 约束的枚举值，将在应用层实现

-- 检查约束：自习室状态只能是 OPEN 或 CLOSED
-- SQLite 不支持 CHECK 约束的枚举值，将在应用层实现

-- 检查约束：座位类型只能是 NORMAL 或 VIP
-- SQLite 不支持 CHECK 约束的枚举值，将在应用层实现

-- 检查约束：座位状态只能是 AVAILABLE, OCCUPIED, MAINTENANCE
-- SQLite 不支持 CHECK 约束的枚举值，将在应用层实现

-- 检查约束：预订状态只能是 ACTIVE, COMPLETED, CANCELLED
-- SQLite 不支持 CHECK 约束的枚举值，将在应用层实现

-- ==========================================
-- 视图创建
-- ==========================================

-- 创建预订详情视图
CREATE VIEW v_reservation_detail AS
SELECT 
    r.reservation_id,
    r.start_time,
    r.end_time,
    r.status AS reservation_status,
    r.created_at AS reservation_created_at,
    u.user_id,
    u.username,
    u.real_name,
    u.email,
    s.seat_id,
    s.seat_number,
    s.seat_type,
    sr.room_id,
    sr.room_name,
    sr.location
FROM t_reservation r
LEFT JOIN t_user u ON r.user_id = u.user_id
LEFT JOIN t_seat s ON r.seat_id = s.seat_id
LEFT JOIN t_study_room sr ON s.room_id = sr.room_id;

-- 创建座位详情视图
CREATE VIEW v_seat_detail AS
SELECT 
    s.seat_id,
    s.seat_number,
    s.seat_type,
    s.status AS seat_status,
    s.row_num,
    s.col_num,
    s.created_at AS seat_created_at,
    sr.room_id,
    sr.room_name,
    sr.location,
    sr.capacity,
    sr.status AS room_status
FROM t_seat s
LEFT JOIN t_study_room sr ON s.room_id = sr.room_id;

-- 创建自习室统计视图
CREATE VIEW v_study_room_stats AS
SELECT 
    sr.room_id,
    sr.room_name,
    sr.location,
    sr.capacity,
    sr.status,
    COUNT(s.seat_id) AS total_seats,
    COUNT(CASE WHEN s.status = 'AVAILABLE' THEN 1 END) AS available_seats,
    COUNT(CASE WHEN s.status = 'OCCUPIED' THEN 1 END) AS occupied_seats,
    COUNT(CASE WHEN s.status = 'MAINTENANCE' THEN 1 END) AS maintenance_seats
FROM t_study_room sr
LEFT JOIN t_seat s ON sr.room_id = s.room_id
GROUP BY sr.room_id, sr.room_name, sr.location, sr.capacity, sr.status;

-- ==========================================
-- 表结构创建完成
-- ==========================================

-- 输出创建完成信息
SELECT 'Database tables created successfully!' AS message; 