package com.studyroom.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.client.model.ApiResponse;
import com.studyroom.client.model.PageData;
import com.studyroom.client.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 用户API服务类
 * 负责与后端用户相关API的通信
 * 
 * @author Developer
 * @version 1.0.0
 */
public class UserApiService {

    private static final Logger logger = LoggerFactory.getLogger(UserApiService.class);
    
    // 单例实例
    private static UserApiService instance;
    
    // HTTP客户端服务
    private final HttpClientService httpClient;
    
    // JSON处理器
    private final ObjectMapper objectMapper;

    /**
     * 私有构造函数 - 单例模式
     */
    private UserApiService() {
        this.httpClient = HttpClientService.getInstance();
        this.objectMapper = httpClient.getObjectMapper();
        logger.info("🧑 用户API服务初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized UserApiService getInstance() {
        if (instance == null) {
            instance = new UserApiService();
        }
        return instance;
    }

    /**
     * 用户登录
     */
    public CompletableFuture<User> login(String username, String password) {
        logger.info("🔐 尝试用户登录: {}", username);
        
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);
        
        return httpClient.post("/auth/login", loginData)
            .thenApply(this::parseUserResponse)
            .whenComplete((user, throwable) -> {
                if (throwable == null && user != null) {
                    logger.info("✅ 用户登录成功: {} ({})", user.getUsername(), user.getRole().getDisplayName());
                } else {
                    logger.error("❌ 用户登录失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 用户注册
     */
    public CompletableFuture<User> register(User user, String password) {
        logger.info("📝 尝试用户注册: {}", user.getUsername());
        
        Map<String, Object> registerData = new HashMap<>();
        registerData.put("username", user.getUsername());
        registerData.put("password", password);
        registerData.put("email", user.getEmail());
        registerData.put("realName", user.getRealName());
        registerData.put("phone", user.getPhone());
        
        return httpClient.post("/auth/register", registerData)
            .thenApply(this::parseUserResponse)
            .whenComplete((newUser, throwable) -> {
                if (throwable == null && newUser != null) {
                    logger.info("✅ 用户注册成功: {}", newUser.getUsername());
                } else {
                    logger.error("❌ 用户注册失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 根据ID获取用户信息
     */
    public CompletableFuture<User> getUserById(Long userId) {
        logger.debug("🔍 获取用户信息: ID={}", userId);
        
        return httpClient.get("/users/" + userId)
            .thenApply(this::parseUserResponse)
            .whenComplete((user, throwable) -> {
                if (throwable == null && user != null) {
                    logger.debug("✅ 获取用户信息成功: {}", user.getUsername());
                } else {
                    logger.warn("❌ 获取用户信息失败: ID={}, 错误={}", userId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取当前用户信息
     */
    public CompletableFuture<User> getCurrentUser() {
        logger.debug("🔍 获取当前用户信息");
        
        return httpClient.get("/users/me")
            .thenApply(this::parseUserResponse)
            .whenComplete((user, throwable) -> {
                if (throwable == null && user != null) {
                    logger.debug("✅ 获取当前用户信息成功: {}", user.getUsername());
                } else {
                    logger.warn("❌ 获取当前用户信息失败: {}", 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 更新用户信息
     */
    public CompletableFuture<User> updateUser(User user) {
        logger.info("📝 更新用户信息: {}", user.getUsername());
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email", user.getEmail());
        updateData.put("realName", user.getRealName());
        updateData.put("phone", user.getPhone());
        
        return httpClient.put("/users/" + user.getId(), updateData)
            .thenApply(this::parseUserResponse)
            .whenComplete((updatedUser, throwable) -> {
                if (throwable == null && updatedUser != null) {
                    logger.info("✅ 用户信息更新成功: {}", updatedUser.getUsername());
                } else {
                    logger.error("❌ 用户信息更新失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 修改密码
     */
    public CompletableFuture<Boolean> changePassword(String oldPassword, String newPassword) {
        logger.info("🔑 尝试修改密码");
        
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("oldPassword", oldPassword);
        passwordData.put("newPassword", newPassword);
        
        return httpClient.put("/users/change-password", passwordData)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((success, throwable) -> {
                if (throwable == null && success) {
                    logger.info("✅ 密码修改成功");
                } else {
                    logger.error("❌ 密码修改失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 分页查询用户列表（管理员功能）
     */
    public CompletableFuture<PageData<User>> getUsers(int page, int size, String keyword, User.Role role, User.Status status) {
        logger.debug("📋 查询用户列表: page={}, size={}, keyword={}, role={}, status={}", 
            page, size, keyword, role, status);
        
        StringBuilder url = new StringBuilder("/users?page=" + page + "&size=" + size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            url.append("&keyword=").append(keyword.trim());
        }
        if (role != null) {
            url.append("&role=").append(role.name());
        }
        if (status != null) {
            url.append("&status=").append(status.name());
        }
        
        return httpClient.get(url.toString())
            .thenApply(this::parseUserPageResponse)
            .whenComplete((pageData, throwable) -> {
                if (throwable == null && pageData != null) {
                    logger.debug("✅ 查询用户列表成功: 共{}条记录", pageData.getTotalElements());
                } else {
                    logger.warn("❌ 查询用户列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取所有用户列表（不分页）
     */
    public CompletableFuture<List<User>> getAllUsers() {
        logger.debug("📋 获取所有用户列表");
        
        return httpClient.get("/users/all")
            .thenApply(this::parseUserListResponse)
            .whenComplete((users, throwable) -> {
                if (throwable == null && users != null) {
                    logger.debug("✅ 获取用户列表成功: 共{}个用户", users.size());
                } else {
                    logger.warn("❌ 获取用户列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 删除用户（管理员功能）
     */
    public CompletableFuture<Boolean> deleteUser(Long userId) {
        logger.info("🗑️ 删除用户: ID={}", userId);
        
        return httpClient.delete("/users/" + userId)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((success, throwable) -> {
                if (throwable == null && success) {
                    logger.info("✅ 用户删除成功: ID={}", userId);
                } else {
                    logger.error("❌ 用户删除失败: ID={}, 错误={}", userId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 检查用户名是否存在
     */
    public CompletableFuture<Boolean> checkUsernameExists(String username) {
        logger.debug("🔍 检查用户名是否存在: {}", username);
        
        return httpClient.get("/users/check-username?username=" + username)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((exists, throwable) -> {
                if (throwable == null) {
                    logger.debug("✅ 用户名检查完成: {} {}", username, exists ? "已存在" : "可用");
                } else {
                    logger.warn("❌ 用户名检查失败: {}", throwable.getMessage());
                }
            });
    }

    /**
     * 检查邮箱是否存在
     */
    public CompletableFuture<Boolean> checkEmailExists(String email) {
        logger.debug("🔍 检查邮箱是否存在: {}", email);
        
        return httpClient.get("/users/check-email?email=" + email)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((exists, throwable) -> {
                if (throwable == null) {
                    logger.debug("✅ 邮箱检查完成: {} {}", email, exists ? "已存在" : "可用");
                } else {
                    logger.warn("❌ 邮箱检查失败: {}", throwable.getMessage());
                }
            });
    }

    /**
     * 更新用户个人资料
     */
    public CompletableFuture<Boolean> updateUserProfile(Long userId, String email, String phone, String realName) {
        logger.info("📝 更新用户个人资料: ID={}", userId);
        
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("email", email);
        profileData.put("phone", phone);
        profileData.put("realName", realName);
        
        return httpClient.put("/users/" + userId + "/profile", profileData)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((success, throwable) -> {
                if (throwable == null && success) {
                    logger.info("✅ 用户个人资料更新成功: ID={}", userId);
                } else {
                    logger.error("❌ 用户个人资料更新失败: ID={}, 错误={}", userId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 更新用户状态
     */
    public CompletableFuture<Boolean> updateUserStatus(Long userId, User.Status status) {
        logger.info("🔄 更新用户状态: ID={}, 状态={}", userId, status);
        
        Map<String, String> statusData = new HashMap<>();
        statusData.put("status", status.name());
        
        return httpClient.put("/users/" + userId + "/status", statusData)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((success, throwable) -> {
                if (throwable == null && success) {
                    logger.info("✅ 用户状态更新成功: ID={}, 状态={}", userId, status);
                } else {
                    logger.error("❌ 用户状态更新失败: ID={}, 状态={}, 错误={}", userId, status, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    // 私有辅助方法

    /**
     * 解析用户响应
     */
    private User parseUserResponse(String jsonResponse) {
        try {
            ApiResponse<User> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<User>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析用户列表响应
     */
    private List<User> parseUserListResponse(String jsonResponse) {
        try {
            ApiResponse<List<User>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<List<User>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户列表响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析用户分页响应
     */
    private PageData<User> parseUserPageResponse(String jsonResponse) {
        try {
            ApiResponse<PageData<User>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<PageData<User>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户分页响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析布尔响应
     */
    private Boolean parseBooleanResponse(String jsonResponse) {
        try {
            ApiResponse<Boolean> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<Boolean>>() {});
            
            if (response.isSuccess()) {
                return response.getData() != null ? response.getData() : false;
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析布尔响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }
} 