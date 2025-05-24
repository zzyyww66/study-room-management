# 🔧 开发环境配置指南

## 环境要求

### 基础环境

- **操作系统**：Windows 10+, macOS 10.14+, Ubuntu 18.04+
- **JDK**：Oracle JDK 17+ 或 OpenJDK 17+
- **Maven**：3.6.0+
- **IDE**：IntelliJ IDEA 2022+ 或 Eclipse 2022+
- **Git**：2.20+

### 开发工具推荐

- **数据库工具**：DB Browser for SQLite
- **API 测试**：Postman 或 Apifox
- **界面设计**：Scene Builder (JavaFX)

## 🛠️ 环境安装

### 1. JDK 17 安装

#### Windows

```bash
# 下载并安装 OpenJDK 17
# 访问 https://adoptium.net/ 下载安装包

# 验证安装
java -version
javac -version
```

#### macOS

```bash
# 使用 Homebrew 安装
brew install openjdk@17

# 配置环境变量
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Ubuntu

```bash
# 安装 OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk

# 验证安装
java -version
```

### 2. Maven 安装

#### Windows

```bash
# 下载 Maven 3.9.9
# 解压到 C:\Program Files\Apache\maven

# 设置环境变量
# MAVEN_HOME=C:\Program Files\Apache\maven
# PATH=%MAVEN_HOME%\bin;%PATH%

# 验证安装
mvn -version
```

#### macOS

```bash
# 使用 Homebrew 安装
brew install maven

# 验证安装
mvn -version
```

#### Ubuntu

```bash
# 安装 Maven
sudo apt install maven

# 验证安装
mvn -version
```

### 3. IDE 配置

#### IntelliJ IDEA 配置

1. **安装必要插件**

   - Maven Helper
   - Spring Boot Helper
   - JavaFX Runtime for Plugins

2. **项目导入**

   ```
   File -> Open -> 选择项目根目录
   Import as Maven project
   ```

3. **JDK 配置**

   ```
   File -> Project Structure -> Project -> Project SDK -> 选择 JDK 17
   ```

4. **Maven 配置**
   ```
   File -> Settings -> Build Tools -> Maven
   Maven home directory: 选择 Maven 安装目录
   User settings file: 选择 settings.xml
   ```

#### Eclipse 配置

1. **安装 Spring Tools Suite**
2. **导入 Maven 项目**
   ```
   File -> Import -> Existing Maven Projects
   ```

## 📦 项目依赖配置

### Maven 仓库配置

创建 `~/.m2/settings.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <mirrors>
    <!-- 阿里云 Maven 仓库镜像 -->
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>

  <profiles>
    <profile>
      <id>jdk-17</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>17</jdk>
      </activation>
      <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
      </properties>
    </profile>
  </profiles>
</settings>
```

### JavaFX 配置

由于 JDK 11+ 不再包含 JavaFX，需要单独配置：

```xml
<!-- 在 pom.xml 中添加 JavaFX 依赖 -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>17.0.2</version>
</dependency>
```

## 🚀 项目启动步骤

### 1. 克隆项目

```bash
git clone <repository-url>
cd study-room-management
```

### 2. 构建项目

```bash
# 编译整个项目
mvn clean compile

# 打包项目
mvn clean package

# 安装到本地仓库
mvn clean install
```

### 3. 启动后端服务

```bash
cd study-room-server

# 开发环境启动
mvn spring-boot:run

# 或者使用 IDE 运行 StudyRoomServerApplication.java
```

后端服务启动后访问：

- API 接口：http://localhost:8080
- 数据库控制台：http://localhost:8080/h2-console (开发环境)

### 4. 启动前端客户端

```bash
cd study-room-client

# 使用 Maven 插件启动
mvn javafx:run

# 或者使用 IDE 运行 StudyRoomClientApplication.java
```

## 🔍 开发调试

### 后端调试

1. **查看日志**

   ```bash
   # 查看应用日志
   tail -f logs/app.log

   # 查看 Spring Boot 启动日志
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug"
   ```

2. **数据库调试**

   ```sql
   -- 连接 H2 数据库控制台
   -- URL: jdbc:h2:mem:testdb
   -- Username: sa
   -- Password: (空)
   ```

3. **API 调试**
   - 使用 Postman 测试 API 接口
   - 查看 Swagger 文档：http://localhost:8080/swagger-ui.html

### 前端调试

1. **JavaFX Scene Builder**

   - 下载并安装 Scene Builder
   - 用于可视化设计 FXML 界面

2. **日志调试**

   ```java
   // 在代码中添加日志
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;

   private static final Logger logger = LoggerFactory.getLogger(YourClass.class);
   logger.info("调试信息");
   ```

## 🧪 测试环境

### 单元测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 生成测试报告
mvn surefire-report:report
```

### 集成测试

```bash
# 运行集成测试
mvn verify

# 使用测试配置文件
mvn test -Dspring.profiles.active=test
```

## 📝 代码规范

### Java 编码规范

- 使用驼峰命名法
- 类名首字母大写
- 方法名和变量名首字母小写
- 常量全大写，用下划线分隔

### 项目规范

- 每个类都要有类注释
- 重要方法要有注释
- 使用有意义的变量名
- 保持代码简洁清晰

### Git 提交规范

```bash
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 🔧 常见问题解决

### 1. Maven 依赖下载失败

```bash
# 清理本地仓库
mvn dependency:purge-local-repository

# 强制更新依赖
mvn clean install -U
```

### 2. JavaFX 运行错误

```bash
# 检查 JDK 版本
java -version

# 确保 JavaFX 模块路径正确
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
```

### 3. 数据库连接问题

```properties
# 检查数据库配置 application.properties
spring.datasource.url=jdbc:sqlite:./data/study_room.db
spring.datasource.driver-class-name=org.sqlite.JDBC
```

### 4. 端口冲突

```bash
# 查看端口占用
netstat -ano | findstr :8080

# 修改端口配置
server.port=8081
```

## 📚 参考资料

### 官方文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [JavaFX 官方文档](https://openjfx.io/)
- [Maven 官方文档](https://maven.apache.org/)

### 学习资源

- [Spring Boot 教程](https://www.baeldung.com/spring-boot)
- [JavaFX 教程](https://docs.oracle.com/javafx/2/)
- [SQLite 教程](https://www.sqlite.org/docs.html)

---

💡 **提示**：遇到问题时，请先查看日志文件，大部分问题都可以通过日志信息定位和解决。
