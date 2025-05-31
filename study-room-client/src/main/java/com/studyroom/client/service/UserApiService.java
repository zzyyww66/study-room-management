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
            .thenApply(this::parseLoginRegisterResponse) // Use the new parser
            .whenComplete((user, throwable) -> {
                if (throwable == null && user != null) {
                    logger.info("✅ 用户登录成功: {} ({})", user.getUsername(), user.getRole() != null ? user.getRole().getDisplayName() : "N/A");
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
            .thenApply(this::parseLoginRegisterResponse) // Use the new parser
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
        
        // Endpoint is now /api/users/me as implemented on the server
        return httpClient.get("/users/me")
            .thenApply(this::parseUserFromMapResponse) // Use a parser that expects User within a Map
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
        // TODO: The changePassword endpoint on the server expects userId in the body.
        // This client method doesn't have userId. This needs to be addressed.
        // For now, I'll assume the client will need to be updated or this method is called
        // when a user is already logged in, and the server can get userId from the token/session.
        // However, the current server AuthController.changePassword expects "userId" in request body.
        // This is a mismatch.
        // For this subtask, I will only change the path as requested by the instructions.
        // A "userId" field should be added to passwordData if the server requires it.
        // Let's assume for now the server will be adapted or this is handled by token.
        return httpClient.put("/auth/change-password", passwordData)
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
        
        return httpClient.get("/auth/check-username/" + username)
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
        
        return httpClient.get("/auth/check-email/" + email)
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
            ApiResponse<User> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<User>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败 (parseUserResponse): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户响应失败 (parseUserResponse): {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a response where the User object is nested within a map in the 'data' field.
     * e.g., {"code":200, "message":"...", "data":{"user":{...}}}
     */
    private User parseUserFromMapResponse(String jsonResponse) {
        try {
            ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(jsonResponse,
                    new TypeReference<ApiResponse<Map<String, Object>>>() {});

            if (apiResponse.getCode() == 200) {
                Map<String, Object> dataMap = apiResponse.getData();
                if (dataMap != null && dataMap.containsKey("user")) {
                    return objectMapper.convertValue(dataMap.get("user"), User.class);
                } else {
                    logger.error("❌ Response data does not contain 'user' field or dataMap is null: {}", dataMap);
                    throw new RuntimeException("API错误: 响应数据格式不正确, 'user' 字段缺失");
                }
            } else {
                logger.error("❌ API请求失败 (parseUserFromMapResponse): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户(从Map)响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析用户列表响应
     */
    private List<User> parseUserListResponse(String jsonResponse) {
        try {
            ApiResponse<List<User>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<List<User>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败 (parseUserListResponse): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户列表响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析用户分页响应
     */
    private PageData<User> parseUserPageResponse(String jsonResponse) {
        try {
            ApiResponse<PageData<User>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<PageData<User>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析用户分页响应失败: {}", jsonResponse, e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析布尔响应
     */
    private Boolean parseBooleanResponse(String jsonResponse) {
        try {
            // For some cases, the data field for a boolean response might be a Map, e.g. {"available": true}
            // Or it could be directly Boolean. We need to handle this gracefully.
            // Let's first try to parse as ApiResponse<Boolean>
            try {
                ApiResponse<Boolean> apiResponse = objectMapper.readValue(jsonResponse,
                        new TypeReference<ApiResponse<Boolean>>() {});
                if (apiResponse.getCode() == 200) { // Assuming 200 is success
                    return apiResponse.getData() != null ? apiResponse.getData() : false;
                } else {
                     // Check if data is a map containing a boolean, e.g. for checkUsername/Email
                    if (apiResponse.getData() instanceof Map) {
                        Map<?, ?> dataMap = (Map<?, ?>) apiResponse.getData();
                        if (dataMap.containsKey("available") && dataMap.get("available") instanceof Boolean) {
                            logger.warn("⚠️ API returned success code but boolean data was in a map for: {}", jsonResponse);
                            return (Boolean) dataMap.get("available");
                        }
                         if (dataMap.containsKey("success") && dataMap.get("success") instanceof Boolean) {
                            logger.warn("⚠️ API returned success code but boolean data was in a map for: {}", jsonResponse);
                            return (Boolean) dataMap.get("success");
                        }
                    }
                    logger.error("❌ API请求失败 (Boolean direct): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                    throw new RuntimeException("API错误: " + apiResponse.getMessage());
                }
            } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                // If direct Boolean parsing fails, try parsing as ApiResponse<Map<String, Boolean>>
                // This is common for responses like checkUsernameExists which might return {"available": true} in data
                logger.warn("⚠️ Direct boolean parsing failed, attempting to parse as Map<String, Boolean>: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                ApiResponse<Map<String, Boolean>> apiResponseMap = objectMapper.readValue(jsonResponse,
                        new TypeReference<ApiResponse<Map<String, Boolean>>>() {});

                if (apiResponseMap.getCode() == 200) { // Assuming 200 is success
                    Map<String, Boolean> dataMap = apiResponseMap.getData();
                    if (dataMap != null) {
                        if (dataMap.containsKey("available")) return dataMap.get("available");
                        if (dataMap.containsKey("success")) return dataMap.get("success");
                        // Add other common boolean keys if necessary
                        logger.warn("⚠️ Boolean response map did not contain 'available' or 'success' key: {}", dataMap);
                        return !dataMap.isEmpty(); // Default to true if map is not empty and no specific key found
                    }
                    return false; // Default to false if data map is null
                } else {
                    logger.error("❌ API请求失败 (Boolean as Map): Code={}, Message={}", apiResponseMap.getCode(), apiResponseMap.getMessage());
                    throw new RuntimeException("API错误: " + apiResponseMap.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("❌ 解析布尔响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    // Special parser for login/register that expects User inside a Map in data
    private User parseLoginRegisterResponse(String jsonResponse) {
        try {
            ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(jsonResponse,
                    new TypeReference<ApiResponse<Map<String, Object>>>() {});

            // HTTPStatus.CREATED is 201 for registration, 200 for login
            if (apiResponse.getCode() == 200 || apiResponse.getCode() == 201) {
                Map<String, Object> dataMap = apiResponse.getData();
                if (dataMap == null) {
                    logger.error("❌ Login/Register response data map is null.");
                    throw new RuntimeException("API错误: 响应数据为空");
                }

                // For login, expect "token" and "userInfo"
                // For register, expect "user" (which contains the User object directly)
                User user = null;
                if (apiResponse.getCode() == 200) { // Login
                    if (dataMap.containsKey("token") && dataMap.containsKey("userInfo")) {
                        String token = (String) dataMap.get("token");
                        if (token == null || token.trim().isEmpty()) {
                            logger.error("❌ Login response token is missing or empty.");
                            throw new RuntimeException("API错误: Token缺失");
                        }
                        HttpClientService.getInstance().setAuthToken(token);
                        logger.info("🔑 Auth token set successfully.");

                        Object userInfoObj = dataMap.get("userInfo");
                        if (userInfoObj == null) {
                             logger.error("❌ Login response userInfo is null.");
                             throw new RuntimeException("API错误: 用户信息缺失");
                        }
                        user = objectMapper.convertValue(userInfoObj, User.class);
                    } else {
                        logger.error("❌ Login response data does not contain 'token' or 'userInfo' field: {}", dataMap);
                        throw new RuntimeException("API错误: 响应数据格式不正确 (token/userInfo)");
                    }
                } else if (apiResponse.getCode() == 201) { // Register
                    if (dataMap.containsKey("user")) {
                         Object userObj = dataMap.get("user");
                         if (userObj == null) {
                            logger.error("❌ Register response user object is null.");
                            throw new RuntimeException("API错误: 用户信息缺失");
                         }
                        user = objectMapper.convertValue(userObj, User.class);
                    } else {
                        logger.error("❌ Register response data does not contain 'user' field: {}", dataMap);
                        throw new RuntimeException("API错误: 响应数据格式不正确 (user)");
                    }
                }

                if (user == null) {
                    logger.error("❌ Failed to extract user object from response: {}", dataMap);
                    throw new RuntimeException("API错误: 用户信息解析失败");
                }
                return user;

            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析登录/注册响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }
}