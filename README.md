# 🏫 共享自习室管理系统

> 基于 JavaFX + Spring Boot + SQLite 的现代化自习室管理解决方案

## 📖 项目简介

共享自习室管理系统是一个面向学校、图书馆或共享学习空间的座位预订和管理系统。该系统采用前后端分离的架构设计，为普通用户和管理员提供直观易用的操作界面。

### ✨ 主要特性

- 🎯 **双角色支持**：普通用户和管理员不同权限
- 🪑 **智能座位管理**：实时座位状态监控和预订
- 📅 **灵活预订系统**：支持时间段预订和取消
- 📊 **数据统计分析**：详细的使用统计和报表
- 🎨 **现代化界面**：基于 JavaFX 的美观用户界面
- 🔐 **安全可靠**：完整的用户认证和权限控制

## 🛠️ 技术栈

### 前端

- **JavaFX 17+**：桌面应用程序框架
- **FXML**：界面设计和布局
- **CSS**：样式和主题定制

### 后端

- **Spring Boot 2.7+**：微服务框架
- **Spring Data JPA**：数据持久化
- **Spring Web**：RESTful API
- **Jackson**：JSON 序列化

### 数据库

- **SQLite**：轻量级关系数据库
- **H2**：开发测试数据库

### 构建工具

- **Maven 3.9.9**：项目管理和构建
- **JDK 17**：Java 开发环境

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Git

### 安装步骤

1. **克隆项目**

   ```bash
   git clone <repository-url>
   cd study-room-management
   ```

2. **构建项目**

   ```bash
   mvn clean install
   ```

3. **启动后端服务**

   ```bash
   cd study-room-server
   mvn spring-boot:run
   ```

4. **启动前端客户端**
   ```bash
   cd study-room-client
   mvn javafx:run
   ```

### 默认账户

- **管理员**：admin / admin123
- **测试用户**：user001 / user123

## 📁 项目结构

```
study-room-management/
├── study-room-server/      # Spring Boot 后端
├── study-room-client/      # JavaFX 前端
├── docs/                   # 项目文档
├── sql/                    # 数据库脚本
├── PROJECT_PLAN.md         # 详细项目规划
└── README.md              # 项目说明
```

## 🔗 相关文档

- [📋 项目规划文档](PROJECT_PLAN.md)
- [🔧 开发环境配置](docs/DEVELOPMENT.md)
- [📡 API 接口文档](docs/API.md)
- [🎨 界面设计指南](docs/UI_DESIGN.md)

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📝 开发日志

- [x] 项目架构设计
- [x] 数据库设计
- [ ] 后端 API 开发
- [ ] 前端界面开发
- [ ] 功能测试
- [ ] 系统部署

## 📄 许可证

本项目仅用于学习和教育目的。

## 👨‍💻 开发团队

- **项目负责人**：朱禹阌 2022217610

---

💡 **提示**：查看 [PROJECT_PLAN.md](PROJECT_PLAN.md) 获取完整的项目规划和开发指南。
