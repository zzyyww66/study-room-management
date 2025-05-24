# 📡 API 接口文档

## 接口概述

本文档描述了共享自习室管理系统的 RESTful API 接口。所有接口都基于 HTTP 协议，数据格式为 JSON。

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **Character Encoding**: `UTF-8`

### 响应格式

#### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T12:00:00"
}
```

#### 状态码说明

| 状态码 | 说明               |
| ------ | ------------------ |
| 200    | 请求成功           |
| 400    | 请求参数错误       |
| 401    | 未授权，需要登录   |
| 403    | 禁止访问，权限不足 |
| 404    | 资源不存在         |
| 500    | 服务器内部错误     |

## 🔐 认证接口

### 1. 用户登录

**POST** `/auth/login`

**请求参数：**

```json
{
  "username": "string",
  "password": "string"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "userId": 1,
      "username": "user001",
      "realName": "张三",
      "email": "user001@example.com",
      "role": "USER",
      "status": "ACTIVE"
    }
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 用户注册

**POST** `/auth/register`

**请求参数：**

```json
{
  "username": "string",
  "password": "string",
  "realName": "string",
  "email": "string",
  "phone": "string"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 3. 登出

**POST** `/auth/logout`

**请求头：**

```
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

## 👤 用户管理接口

### 1. 获取用户信息

**GET** `/users/{userId}`

**请求头：**

```
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userId": 1,
    "username": "user001",
    "realName": "张三",
    "email": "user001@example.com",
    "phone": "13800138000",
    "role": "USER",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 更新用户信息

**PUT** `/users/{userId}`

**请求参数：**

```json
{
  "realName": "string",
  "email": "string",
  "phone": "string"
}
```

### 3. 获取用户列表（管理员）

**GET** `/users`

**查询参数：**

- `page`: 页码（默认 1）
- `size`: 每页大小（默认 10）
- `keyword`: 搜索关键词
- `role`: 用户角色
- `status`: 用户状态

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "userId": 1,
        "username": "user001",
        "realName": "张三",
        "email": "user001@example.com",
        "role": "USER",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 1,
    "size": 10
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## 🏫 自习室管理接口

### 1. 获取自习室列表

**GET** `/study-rooms`

**查询参数：**

- `page`: 页码（默认 1）
- `size`: 每页大小（默认 10）
- `status`: 自习室状态（OPEN/CLOSED）

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "roomId": 1,
        "roomName": "第一自习室",
        "location": "图书馆2楼",
        "capacity": 100,
        "description": "安静的学习环境",
        "status": "OPEN",
        "availableSeats": 80,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 1,
    "size": 10
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 获取自习室详情

**GET** `/study-rooms/{roomId}`

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "roomId": 1,
    "roomName": "第一自习室",
    "location": "图书馆2楼",
    "capacity": 100,
    "description": "安静的学习环境",
    "status": "OPEN",
    "seats": [
      {
        "seatId": 1,
        "seatNumber": "A01",
        "seatType": "NORMAL",
        "status": "AVAILABLE",
        "rowNum": 1,
        "colNum": 1
      }
    ],
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 3. 创建自习室（管理员）

**POST** `/study-rooms`

**请求参数：**

```json
{
  "roomName": "string",
  "location": "string",
  "capacity": 100,
  "description": "string"
}
```

### 4. 更新自习室（管理员）

**PUT** `/study-rooms/{roomId}`

**请求参数：**

```json
{
  "roomName": "string",
  "location": "string",
  "capacity": 100,
  "description": "string",
  "status": "OPEN"
}
```

### 5. 删除自习室（管理员）

**DELETE** `/study-rooms/{roomId}`

## 🪑 座位管理接口

### 1. 获取座位列表

**GET** `/seats`

**查询参数：**

- `roomId`: 自习室 ID
- `status`: 座位状态（AVAILABLE/OCCUPIED/MAINTENANCE）
- `seatType`: 座位类型（NORMAL/VIP）

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "seatId": 1,
      "roomId": 1,
      "seatNumber": "A01",
      "seatType": "NORMAL",
      "status": "AVAILABLE",
      "rowNum": 1,
      "colNum": 1,
      "currentReservation": null
    }
  ],
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 获取座位详情

**GET** `/seats/{seatId}`

### 3. 创建座位（管理员）

**POST** `/seats`

**请求参数：**

```json
{
  "roomId": 1,
  "seatNumber": "string",
  "seatType": "NORMAL",
  "rowNum": 1,
  "colNum": 1
}
```

### 4. 更新座位状态（管理员）

**PUT** `/seats/{seatId}/status`

**请求参数：**

```json
{
  "status": "MAINTENANCE"
}
```

## 📅 预订管理接口

### 1. 创建预订

**POST** `/reservations`

**请求参数：**

```json
{
  "seatId": 1,
  "startTime": "2024-01-01T09:00:00",
  "endTime": "2024-01-01T12:00:00"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "预订成功",
  "data": {
    "reservationId": 1,
    "seatId": 1,
    "userId": 1,
    "startTime": "2024-01-01T09:00:00",
    "endTime": "2024-01-01T12:00:00",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T08:30:00"
  },
  "timestamp": "2024-01-01T08:30:00"
}
```

### 2. 获取预订列表

**GET** `/reservations`

**查询参数：**

- `userId`: 用户 ID（可选，管理员可查看所有用户）
- `seatId`: 座位 ID
- `status`: 预订状态
- `startDate`: 开始日期
- `endDate`: 结束日期
- `page`: 页码
- `size`: 每页大小

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "reservationId": 1,
        "userId": 1,
        "userName": "张三",
        "seatId": 1,
        "seatNumber": "A01",
        "roomName": "第一自习室",
        "startTime": "2024-01-01T09:00:00",
        "endTime": "2024-01-01T12:00:00",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T08:30:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 1,
    "size": 10
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 3. 获取预订详情

**GET** `/reservations/{reservationId}`

### 4. 取消预订

**PUT** `/reservations/{reservationId}/cancel`

**响应示例：**

```json
{
  "code": 200,
  "message": "取消预订成功",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 5. 获取用户预订历史

**GET** `/users/{userId}/reservations`

**查询参数：**

- `status`: 预订状态
- `page`: 页码
- `size`: 每页大小

## 📊 统计接口（管理员）

### 1. 获取系统统计

**GET** `/statistics/overview`

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalUsers": 1000,
    "totalRooms": 5,
    "totalSeats": 500,
    "activeReservations": 80,
    "todayReservations": 150,
    "occupancyRate": 0.75,
    "popularRooms": [
      {
        "roomId": 1,
        "roomName": "第一自习室",
        "reservationCount": 50
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 获取使用统计

**GET** `/statistics/usage`

**查询参数：**

- `startDate`: 开始日期
- `endDate`: 结束日期
- `roomId`: 自习室 ID（可选）

**响应示例：**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "dailyStatistics": [
      {
        "date": "2024-01-01",
        "totalReservations": 100,
        "completedReservations": 90,
        "cancelledReservations": 10,
        "occupancyRate": 0.8
      }
    ],
    "roomStatistics": [
      {
        "roomId": 1,
        "roomName": "第一自习室",
        "totalReservations": 200,
        "averageOccupancy": 0.75
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## 🔧 工具接口

### 1. 检查座位可用性

**GET** `/seats/{seatId}/availability`

**查询参数：**

- `startTime`: 开始时间
- `endTime`: 结束时间

**响应示例：**

```json
{
  "code": 200,
  "message": "检查完成",
  "data": {
    "seatId": 1,
    "available": true,
    "conflicts": []
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 批量检查座位可用性

**POST** `/seats/batch-availability`

**请求参数：**

```json
{
  "seatIds": [1, 2, 3],
  "startTime": "2024-01-01T09:00:00",
  "endTime": "2024-01-01T12:00:00"
}
```

### 3. 系统健康检查

**GET** `/health`

**响应示例：**

```json
{
  "code": 200,
  "message": "系统正常",
  "data": {
    "status": "UP",
    "database": "UP",
    "diskSpace": "UP",
    "timestamp": "2024-01-01T12:00:00"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## 📝 错误码说明

### 业务错误码

| 错误码 | 说明               |
| ------ | ------------------ |
| 1001   | 用户名或密码错误   |
| 1002   | 用户不存在         |
| 1003   | 用户已存在         |
| 1004   | Token 无效或已过期 |
| 1005   | 权限不足           |
| 2001   | 自习室不存在       |
| 2002   | 自习室已关闭       |
| 3001   | 座位不存在         |
| 3002   | 座位不可用         |
| 3003   | 座位正在维护       |
| 4001   | 预订不存在         |
| 4002   | 预订时间冲突       |
| 4003   | 预订已取消         |
| 4004   | 预订时间无效       |
| 5001   | 参数验证失败       |
| 5002   | 数据格式错误       |

## 🧪 接口测试

### Postman 集合

建议创建 Postman 集合来测试所有接口：

1. **环境变量设置**

   ```
   baseUrl: http://localhost:8080/api
   token: {{login_token}}
   ```

2. **认证流程测试**

   - 注册新用户
   - 用户登录获取 token
   - 使用 token 访问受保护接口

3. **业务流程测试**
   - 查看自习室列表
   - 查看座位信息
   - 创建预订
   - 查看预订记录
   - 取消预订

### 接口调用示例

```javascript
// JavaScript 调用示例
const API_BASE = "http://localhost:8080/api";

// 登录
async function login(username, password) {
  const response = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });
  return response.json();
}

// 获取自习室列表
async function getStudyRooms(token) {
  const response = await fetch(`${API_BASE}/study-rooms`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.json();
}

// 创建预订
async function createReservation(token, seatId, startTime, endTime) {
  const response = await fetch(`${API_BASE}/reservations`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ seatId, startTime, endTime }),
  });
  return response.json();
}
```

---

💡 **提示**：所有需要认证的接口都需要在请求头中携带有效的 Bearer Token。
