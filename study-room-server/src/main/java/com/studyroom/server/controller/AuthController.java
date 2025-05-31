package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
import com.studyroom.server.entity.User;
import com.studyroom.server.service.UserService;
import com.studyroom.server.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户认证相关的API请求
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || password == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "用户名和密码不能为空"));
            }

            User user = userService.authenticateUser(username, password);
            if (user != null) {
                // 更新最后登录时间
                userService.updateLastLoginTime(user.getId());

                String token = jwtUtil.generateToken(user);
                Map<String, Object> userInfoMap = createUserResponse(user);

                Map<String, Object> tokenResponseData = new HashMap<>();
                tokenResponseData.put("token", token);
                tokenResponseData.put("userInfo", userInfoMap);

                return ResponseEntity.ok(ApiResponse.success(tokenResponseData, "登录成功"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "登录失败: " + e.getMessage()));
        }
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String email = registerRequest.get("email");
            String phone = registerRequest.get("phone");
            String realName = registerRequest.get("realName");
            
            // 验证必填字段
            if (username == null || password == null || email == null || realName == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("用户名、密码、邮箱和真实姓名不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            // 检查用户名是否可用
            if (!userService.isUsernameAvailable(username)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("用户名已存在", HttpStatus.BAD_REQUEST.value()));
            }

            // 检查邮箱是否可用
            if (!userService.isEmailAvailable(email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("邮箱已被注册", HttpStatus.BAD_REQUEST.value()));
            }

            User newUser = userService.registerUser(username, password, email, phone, realName);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", createUserResponse(newUser));

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseData, "注册成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("注册失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 修改密码
     * PUT /api/auth/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordRequest) {
        try {
            // JwtRequestFilter is expected to have validated the token already.
            // We still need to extract userId from the token.
            String authorizationHeader = request.getHeader("Authorization");
            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }

            if (token == null) {
                // This case should ideally be caught by the filter, but as a fallback:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("缺少Authorization Header", HttpStatus.UNAUTHORIZED.value()));
            }

            Long userId;
            try {
                // Extract userId. If token is malformed or unparseable here, it's an issue.
                // The filter should have already validated its signature and expiry.
                userId = jwtUtil.extractUserId(token);
            } catch (Exception e) {
                // This might happen if the token is grossly malformed,
                // though signature/expiry should be caught by filter.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("无效Token格式: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
            }

            String oldPassword = passwordRequest.get("oldPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("旧密码和新密码不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            // Long userId = Long.parseLong(userIdStr); // No longer needed
            boolean success = userService.changePassword(userId, oldPassword, newPassword);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(null, "密码修改成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("旧密码不正确", HttpStatus.BAD_REQUEST.value()));
            }

        } catch (Exception e) { // Catch other exceptions like userService failures
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("密码修改失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 检查用户名可用性
     * GET /api/auth/check-username/{username}
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@PathVariable String username) {
        try {
            boolean available = userService.isUsernameAvailable(username);
            Map<String, Boolean> responseData = new HashMap<>();
            responseData.put("available", available);
            return ResponseEntity.ok(ApiResponse.success(responseData, available ? "用户名可用" : "用户名已存在"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "检查失败: " + e.getMessage()));
        }
    }

    /**
     * 检查邮箱可用性
     * GET /api/auth/check-email/{email}
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@PathVariable String email) {
        try {
            boolean available = userService.isEmailAvailable(email);
            Map<String, Boolean> responseData = new HashMap<>();
            responseData.put("available", available);
            return ResponseEntity.ok(ApiResponse.success(responseData, available ? "邮箱可用" : "邮箱已被注册"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "检查失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息（通过用户ID）
     * GET /api/auth/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@PathVariable Long userId) {
        try {
            var userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("user", createUserResponse(userOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "获取用户信息成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 登出（客户端处理，服务端仅返回成功响应）
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null, "登出成功"));
    }

    // 工具方法：创建用户响应对象（不包含敏感信息）
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("phone", user.getPhone());
        userResponse.put("realName", user.getRealName());
        userResponse.put("role", user.getRole().toString());
        userResponse.put("status", user.getStatus().toString());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("lastLoginAt", user.getLastLoginAt());
        return userResponse;
    }
    
    // 工具方法：创建错误响应 (now returns ApiResponse<Object>)
    // This method might need further adjustments or be replaced if specific error codes per field are needed inside data.
    // For now, it's simplified to match the direct usage of ApiResponse.error in the login method.
    private ApiResponse<Object> createErrorResponse(String message, int httpStatusCode) {
        // Map<String, Object> errorData = new HashMap<>();
        // errorData.put("errorCode", errorCode); // Potentially add error code to data if needed
        return ApiResponse.error(httpStatusCode, message);
    }

    // Example of how createErrorResponse would be used if still needed by other methods,
    // though the login method was refactored to use ApiResponse.error() directly.
    // Other methods using createErrorResponse will need to be updated like this:
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse("用户名已存在", HttpStatus.BAD_REQUEST.value()));
    // For checkUsername, checkEmail, getProfile, direct usage of ApiResponse.error is cleaner.
}