package com.studyroom.server.controller;

import com.studyroom.server.entity.User;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户管理相关的API请求
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户详细信息
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
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
                .body(createErrorResponse("获取用户信息失败: " + e.getMessage(), "GET_USER_ERROR"));
        }
    }
    
    /**
     * 更新用户资料信息
     * PUT /api/users/{userId}/profile
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, String> profileRequest) {
        try {
            String email = profileRequest.get("email");
            String phone = profileRequest.get("phone");
            String realName = profileRequest.get("realName");
            
            User updatedUser = userService.updateUserProfile(userId, email, phone, realName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户资料更新成功");
            response.put("user", createUserResponse(updatedUser));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新用户资料失败: " + e.getMessage(), "UPDATE_PROFILE_ERROR"));
        }
    }
    
    /**
     * 获取用户预订历史
     * GET /api/users/{userId}/reservations
     */
    @GetMapping("/{userId}/reservations")
    public ResponseEntity<Map<String, Object>> getUserReservations(@PathVariable Long userId) {
        try {
            List<Reservation> reservations = userService.getUserReservationHistory(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reservations", reservations);
            response.put("count", reservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订历史失败: " + e.getMessage(), "GET_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取用户活跃预订
     * GET /api/users/{userId}/active-reservations
     */
    @GetMapping("/{userId}/active-reservations")
    public ResponseEntity<Map<String, Object>> getUserActiveReservations(@PathVariable Long userId) {
        try {
            List<Reservation> activeReservations = userService.getUserActiveReservations(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeReservations", activeReservations);
            response.put("count", activeReservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取活跃预订失败: " + e.getMessage(), "GET_ACTIVE_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取用户统计信息
     * GET /api/users/{userId}/statistics
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> statistics = userService.getUserStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户统计信息失败: " + e.getMessage(), "GET_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 获取所有活跃用户（管理员功能）
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveUsers() {
        try {
            List<User> activeUsers = userService.findActiveUsers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", activeUsers.stream().map(this::createUserResponse).toList());
            response.put("count", activeUsers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取活跃用户失败: " + e.getMessage(), "GET_ACTIVE_USERS_ERROR"));
        }
    }
    
    /**
     * 分页查询用户（管理员功能）
     * GET /api/users?page=0&size=10&role=USER&status=ACTIVE
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        try {
            User.UserRole userRole = null;
            User.UserStatus userStatus = null;
            
            if (role != null && !role.isEmpty()) {
                userRole = User.UserRole.valueOf(role.toUpperCase());
            }
            if (status != null && !status.isEmpty()) {
                userStatus = User.UserStatus.valueOf(status.toUpperCase());
            }
            
            Page<User> userPage = userService.findUsersWithPagination(page, size, userRole, userStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", userPage.getContent().stream().map(this::createUserResponse).toList());
            response.put("totalElements", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", userPage.hasNext());
            response.put("hasPrevious", userPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), "INVALID_PARAMETER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户列表失败: " + e.getMessage(), "GET_USERS_ERROR"));
        }
    }
    
    /**
     * 更新用户状态（管理员功能）
     * PUT /api/users/{userId}/status
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("状态参数不能为空", "MISSING_STATUS"));
            }
            
            User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
            User updatedUser = userService.updateUserStatus(userId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户状态更新成功");
            response.put("user", createUserResponse(updatedUser));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("无效的状态值", "INVALID_STATUS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新用户状态失败: " + e.getMessage(), "UPDATE_STATUS_ERROR"));
        }
    }
    
    /**
     * 软删除用户（管理员功能）
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("删除用户失败: " + e.getMessage(), "DELETE_USER_ERROR"));
        }
    }
    
    /**
     * 根据用户名查找用户
     * GET /api/users/search/{username}
     */
    @GetMapping("/search/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        try {
            var userOpt = userService.findByUsername(username);
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
                .body(createErrorResponse("查找用户失败: " + e.getMessage(), "SEARCH_USER_ERROR"));
        }
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
        userResponse.put("updatedAt", user.getUpdatedAt());
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