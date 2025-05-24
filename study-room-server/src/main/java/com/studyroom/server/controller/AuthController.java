package com.studyroom.server.controller;

import com.studyroom.server.entity.User;
import com.studyroom.server.service.UserService;
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
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("用户名和密码不能为空", "MISSING_CREDENTIALS"));
            }
            
            User user = userService.authenticateUser(username, password);
            if (user != null) {
                // 更新最后登录时间
                userService.updateLastLoginTime(user.getId());
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "登录成功");
                response.put("user", createUserResponse(user));
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("用户名或密码错误", "INVALID_CREDENTIALS"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("登录失败: " + e.getMessage(), "LOGIN_ERROR"));
        }
    }
    
    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String email = registerRequest.get("email");
            String phone = registerRequest.get("phone");
            String realName = registerRequest.get("realName");
            
            // 验证必填字段
            if (username == null || password == null || email == null || realName == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("用户名、密码、邮箱和真实姓名不能为空", "MISSING_REQUIRED_FIELDS"));
            }
            
            // 检查用户名是否可用
            if (!userService.isUsernameAvailable(username)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("用户名已存在", "USERNAME_EXISTS"));
            }
            
            // 检查邮箱是否可用
            if (!userService.isEmailAvailable(email)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("邮箱已被注册", "EMAIL_EXISTS"));
            }
            
            User newUser = userService.registerUser(username, password, email, phone, realName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", createUserResponse(newUser));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("注册失败: " + e.getMessage(), "REGISTER_ERROR"));
        }
    }
    
    /**
     * 修改密码
     * PUT /api/auth/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> passwordRequest) {
        try {
            String userIdStr = passwordRequest.get("userId");
            String oldPassword = passwordRequest.get("oldPassword");
            String newPassword = passwordRequest.get("newPassword");
            
            if (userIdStr == null || oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("用户ID、旧密码和新密码不能为空", "MISSING_PASSWORD_FIELDS"));
            }
            
            Long userId = Long.parseLong(userIdStr);
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "密码修改成功");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("旧密码不正确", "INVALID_OLD_PASSWORD"));
            }
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("用户ID格式错误", "INVALID_USER_ID"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("密码修改失败: " + e.getMessage(), "PASSWORD_CHANGE_ERROR"));
        }
    }
    
    /**
     * 检查用户名可用性
     * GET /api/auth/check-username/{username}
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsername(@PathVariable String username) {
        try {
            boolean available = userService.isUsernameAvailable(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", available);
            response.put("message", available ? "用户名可用" : "用户名已存在");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查失败: " + e.getMessage(), "CHECK_USERNAME_ERROR"));
        }
    }
    
    /**
     * 检查邮箱可用性
     * GET /api/auth/check-email/{email}
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(@PathVariable String email) {
        try {
            boolean available = userService.isEmailAvailable(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", available);
            response.put("message", available ? "邮箱可用" : "邮箱已被注册");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查失败: " + e.getMessage(), "CHECK_EMAIL_ERROR"));
        }
    }
    
    /**
     * 获取当前用户信息（通过用户ID）
     * GET /api/auth/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long userId) {
        try {
            var userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("user", createUserResponse(userOpt.get()));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("用户不存在", "USER_NOT_FOUND"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户信息失败: " + e.getMessage(), "GET_PROFILE_ERROR"));
        }
    }
    
    /**
     * 登出（客户端处理，服务端仅返回成功响应）
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
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
    
    // 工具方法：创建错误响应
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        return errorResponse;
    }
} 