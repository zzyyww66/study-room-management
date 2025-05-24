# 共享自习室管理系统 - 项目规划文档

## 📋 项目概述

### 项目信息

- **项目名称**：共享自习室管理系统
- **开发模式**：面向对象分析与设计
- **前端技术**：JavaFX
- **后端技术**：Spring Boot
- **数据库**：SQLite
- **构建工具**：Maven 3.9.9
- **Java 版本**：JDK 17

## 🎯 需求分析

### 用户角色

1. **普通用户（学生）**

   - 账户注册和登录
   - 浏览自习室信息
   - 预订和取消座位
   - 查看个人预订记录
   - 修改个人信息

2. **管理员**
   - 系统管理员登录
   - 自习室管理（CRUD 操作）
   - 座位管理（CRUD 操作）
   - 用户管理和权限控制
   - 预订记录管理
   - 数据统计和报表

### 核心功能模块

#### 1. 用户管理模块

- 用户注册/登录
- 用户信息管理
- 权限控制

#### 2. 自习室管理模块

- 自习室信息管理
- 座位布局管理
- 座位状态监控

#### 3. 预订管理模块

- 座位预订/取消
- 预订时间管理
- 预订历史查询

#### 4. 系统管理模块

- 数据统计
- 系统配置
- 日志管理

## 🏗️ 系统架构

### 架构模式

采用经典的三层架构模式：

```
表示层 (Presentation Layer) - JavaFX Client
  ↕ HTTP/REST API
业务逻辑层 (Business Layer) - Spring Boot
  ↕ JDBC
数据访问层 (Data Layer) - SQLite Database
```

### 技术栈

- **前端框架**：JavaFX + Scene Builder
- **后端框架**：Spring Boot + Spring Web + Spring Data JPA
- **数据库**：SQLite (生产) + H2 (开发测试)
- **序列化**：Jackson JSON
- **构建工具**：Maven
- **开发工具**：IntelliJ IDEA / Eclipse

## 🗄️ 数据库设计

### 核心数据表

#### 1. 用户表 (t_user)

```sql
- user_id (主键)
- username (用户名，唯一)
- password (密码，加密存储)
- real_name (真实姓名)
- email (邮箱)
- phone (电话)
- role (角色：USER/ADMIN)
- status (状态：ACTIVE/INACTIVE)
- created_at (创建时间)
- updated_at (更新时间)
```

#### 2. 自习室表 (t_study_room)

```sql
- room_id (主键)
- room_name (自习室名称)
- location (位置)
- capacity (容量)
- description (描述)
- status (状态：OPEN/CLOSED)
- created_at (创建时间)
- updated_at (更新时间)
```

#### 3. 座位表 (t_seat)

```sql
- seat_id (主键)
- room_id (外键，关联自习室)
- seat_number (座位号)
- seat_type (座位类型：NORMAL/VIP)
- status (状态：AVAILABLE/OCCUPIED/MAINTENANCE)
- row_num (行号)
- col_num (列号)
- created_at (创建时间)
- updated_at (更新时间)
```

#### 4. 预订表 (t_reservation)

```sql
- reservation_id (主键)
- user_id (外键，关联用户)
- seat_id (外键，关联座位)
- start_time (开始时间)
- end_time (结束时间)
- status (状态：ACTIVE/COMPLETED/CANCELLED)
- created_at (创建时间)
- updated_at (更新时间)
```

## 📁 项目结构

### 整体目录结构

```
study-room-management/
├── study-room-server/          # 后端项目
├── study-room-client/          # 前端项目
├── docs/                       # 项目文档
├── sql/                        # 数据库脚本
├── README.md                   # 项目说明
└── pom.xml                     # 父级POM文件
```

### 后端项目结构 (study-room-server)

```
src/main/java/com/studyroom/
├── StudyRoomServerApplication.java     # 启动类
├── controller/                         # 控制器层
│   ├── UserController.java
│   ├── StudyRoomController.java
│   ├── SeatController.java
│   └── ReservationController.java
├── service/                           # 服务层
│   ├── UserService.java
│   ├── StudyRoomService.java
│   ├── SeatService.java
│   └── ReservationService.java
├── repository/                        # 数据访问层
│   ├── UserRepository.java
│   ├── StudyRoomRepository.java
│   ├── SeatRepository.java
│   └── ReservationRepository.java
├── entity/                           # 实体类
│   ├── User.java
│   ├── StudyRoom.java
│   ├── Seat.java
│   └── Reservation.java
├── dto/                             # 数据传输对象
│   ├── request/
│   └── response/
├── config/                          # 配置类
│   ├── DatabaseConfig.java
│   ├── WebConfig.java
│   └── SecurityConfig.java
└── util/                           # 工具类
    ├── DateUtil.java
    ├── PasswordUtil.java
    └── ResponseUtil.java
```

### 前端项目结构 (study-room-client)

```
src/main/java/com/studyroom/client/
├── StudyRoomClientApplication.java    # 启动类
├── controller/                        # FXML控制器
│   ├── LoginController.java
│   ├── MainController.java
│   ├── UserDashboardController.java
│   ├── AdminDashboardController.java
│   ├── RoomListController.java
│   └── ReservationController.java
├── service/                          # 客户端服务
│   ├── ApiService.java
│   ├── UserService.java
│   └── ReservationService.java
├── model/                           # 客户端模型
│   ├── User.java
│   ├── StudyRoom.java
│   ├── Seat.java
│   └── Reservation.java
├── util/                           # 工具类
│   ├── HttpUtil.java
│   ├── SceneUtil.java
│   └── AlertUtil.java
└── config/
    └── AppConfig.java

src/main/resources/
├── fxml/                          # FXML界面文件
│   ├── login.fxml
│   ├── main.fxml
│   ├── user-dashboard.fxml
│   ├── admin-dashboard.fxml
│   ├── room-list.fxml
│   └── reservation.fxml
├── css/                          # 样式文件
│   ├── main.css
│   └── theme.css
├── images/                       # 图片资源
└── application.properties        # 配置文件
```

## 🔧 开发步骤规划

### 第一阶段：项目搭建（1-2 天）

1. **创建 Maven 多模块项目**

   - 创建父级项目
   - 创建后端子模块
   - 创建前端子模块
   - 配置依赖管理

2. **后端基础搭建**

   - Spring Boot 项目初始化
   - 数据库连接配置
   - 创建实体类
   - 配置 JPA 和 SQLite

3. **前端基础搭建**
   - JavaFX 项目初始化
   - 创建基本界面框架
   - 配置 CSS 样式

### 第二阶段：核心功能开发（3-5 天）

1. **用户管理模块**

   - 用户注册/登录功能
   - 用户信息管理
   - 权限控制

2. **自习室管理模块**

   - 自习室 CRUD 操作
   - 座位管理功能

3. **预订管理模块**
   - 座位预订功能
   - 预订取消功能
   - 预订查询功能

### 第三阶段：界面优化和功能完善（2-3 天）

1. **前端界面优化**

   - 美化界面设计
   - 添加动画效果
   - 响应式布局

2. **功能完善**
   - 数据统计功能
   - 异常处理
   - 日志记录

### 第四阶段：测试和部署（1-2 天）

1. **功能测试**

   - 单元测试
   - 集成测试
   - 用户体验测试

2. **文档整理**
   - 用户手册
   - 开发文档
   - 部署说明

## 📝 注意事项

### 开发规范

1. **代码规范**

   - 遵循 Java 编码规范
   - 使用统一的命名约定
   - 添加必要的注释

2. **版本控制**

   - 使用 Git 进行版本管理
   - 合理的提交信息
   - 分支管理策略

3. **异常处理**
   - 统一的异常处理机制
   - 用户友好的错误提示
   - 完整的日志记录

### 性能考虑

1. **数据库优化**

   - 合理的索引设计
   - 避免 N+1 查询问题
   - 数据分页处理

2. **前端性能**
   - 避免界面阻塞
   - 异步数据加载
   - 内存管理

## 🎯 项目目标

### 功能目标

- 完整的自习室管理功能
- 用户友好的操作界面
- 稳定可靠的系统运行

### 技术目标

- 掌握 JavaFX 开发技术
- 熟练使用 Spring Boot 框架
- 理解面向对象设计原则
- 掌握前后端分离架构

### 学习目标

- 软件工程实践能力
- 项目管理和团队协作
- 问题分析和解决能力
- 文档编写和表达能力
