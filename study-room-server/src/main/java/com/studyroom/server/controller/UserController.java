package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
import com.studyroom.server.entity.User;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.service.UserService;
import com.studyroom.server.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前登录用户的个人资料
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserProfile(HttpServletRequest request) {
        try {
            // JwtRequestFilter is expected to have validated the token and set 'x-user-id' attribute.
            Object userIdAttribute = request.getAttribute("x-user-id");

            if (userIdAttribute == null) {
                // This should not happen if JwtRequestFilter is correctly configured and working.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("用户未认证或用户ID未在请求中设置", HttpStatus.UNAUTHORIZED.value()));
            }

            Long currentUserId;
            if (userIdAttribute instanceof Long) {
                currentUserId = (Long) userIdAttribute;
            } else if (userIdAttribute instanceof Integer) { // GSON might parse number as Integer
                currentUserId = ((Integer) userIdAttribute).longValue();
            }
            else {
                 // Fallback or error if attribute is not of expected type, though Long is expected from JwtUtil
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("无法解析用户ID从请求属性", HttpStatus.INTERNAL_SERVER_ERROR.value()));
            }

            var userOpt = userService.findById(currentUserId);
            if (userOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("user", createUserResponse(userOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "获取当前用户信息成功"));
            } else {
                // This case should ideally not happen if token is valid, user ID in token exists,
                // and x-user-id attribute was correctly set by the filter.
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("用户不存在 (ID: " + currentUserId + ")", HttpStatus.NOT_FOUND.value()));
            }
        } catch (ClassCastException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("服务器内部错误: 无法转换用户ID类型.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取当前用户信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    
    /**
     * 获取用户详细信息
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable Long userId) {
        try {
            var userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("user", createUserResponse(userOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "获取用户信息成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("用户不存在", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新用户资料信息
     * PUT /api/users/{userId}/profile
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @PathVariable Long userId, // This is the targetUserId
            @RequestBody Map<String, String> profileRequest,
            HttpServletRequest request) {
        try {
            Object authUserIdAttr = request.getAttribute("x-user-id");
            Object authUserRoleAttr = request.getAttribute("x-user-role");

            if (authUserIdAttr == null || authUserRoleAttr == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("用户认证信息缺失", HttpStatus.UNAUTHORIZED.value()));
            }

            Long authenticatedUserId = -1L; // Default to an invalid ID
            if (authUserIdAttr instanceof Long) {
                authenticatedUserId = (Long) authUserIdAttr;
            } else if (authUserIdAttr instanceof Integer) {
                authenticatedUserId = ((Integer) authUserIdAttr).longValue();
            }

            String authenticatedUserRole = (String) authUserRoleAttr;

            // Authorization check: Admin can update any profile, User can only update their own.
            if (!"ADMIN".equals(authenticatedUserRole) && !authenticatedUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("无权修改此用户资料", HttpStatus.FORBIDDEN.value()));
            }

            // Proceed with update if authorized
            String email = profileRequest.get("email");
            String phone = profileRequest.get("phone");
            String realName = profileRequest.get("realName");

            User updatedUser = userService.updateUserProfile(userId, email, phone, realName);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", createUserResponse(updatedUser));

            return ResponseEntity.ok(ApiResponse.success(responseData, "用户资料更新成功"));

        } catch (ClassCastException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("服务器内部错误: 无法转换用户认证信息类型.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新用户资料失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户预订历史
     * GET /api/users/{userId}/reservations
     */
    @GetMapping("/{userId}/reservations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserReservations(@PathVariable Long userId) {
        try {
            List<Reservation> reservations = userService.getUserReservationHistory(userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reservations", reservations);
            responseData.put("count", reservations.size());

            return ResponseEntity.ok(ApiResponse.success(responseData, "获取用户预订历史成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订历史失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户活跃预订
     * GET /api/users/{userId}/active-reservations
     */
    @GetMapping("/{userId}/active-reservations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActiveReservations(@PathVariable Long userId) {
        try {
            List<Reservation> activeReservations = userService.getUserActiveReservations(userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("activeReservations", activeReservations);
            responseData.put("count", activeReservations.size());

            return ResponseEntity.ok(ApiResponse.success(responseData, "获取用户活跃预订成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取活跃预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户统计信息
     * GET /api/users/{userId}/statistics
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> statistics = userService.getUserStatistics(userId);
            // The statistics map itself is the data
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取用户统计信息成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户统计信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取所有活跃用户（管理员功能）
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveUsers() {
        try {
            List<User> activeUsers = userService.findActiveUsers();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("users", activeUsers.stream().map(this::createUserResponse).toList());
            responseData.put("count", activeUsers.size());

            return ResponseEntity.ok(ApiResponse.success(responseData, "获取活跃用户成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取活跃用户失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 分页查询用户（管理员功能）
     * GET /api/users?page=0&size=10&role=USER&status=ACTIVE&keyword=关键字
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
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

            // 如果有关键字搜索，先在结果中进行简单过滤（后续可以改进为数据库层面搜索）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = keyword.trim().toLowerCase();
                // 这里简化处理，实际应该在数据库层面进行搜索
                // 但为了快速验证功能，先在内存中过滤
            }

            // 创建符合前端期望的PageData结构
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("content", userPage.getContent().stream().map(this::createUserResponse).toList());
            pageData.put("totalElements", userPage.getTotalElements());
            pageData.put("totalPages", userPage.getTotalPages());
            pageData.put("pageNumber", page);
            pageData.put("pageSize", size);
            pageData.put("hasNext", userPage.hasNext());
            pageData.put("hasPrevious", userPage.hasPrevious());
            pageData.put("isFirst", userPage.isFirst());
            pageData.put("isLast", userPage.isLast());

            return ResponseEntity.ok(ApiResponse.success(pageData, "查询成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新用户状态（管理员功能）
     * PUT /api/users/{userId}/status
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("状态参数不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
            User updatedUser = userService.updateUserStatus(userId, status);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", createUserResponse(updatedUser));

            return ResponseEntity.ok(ApiResponse.success(responseData, "用户状态更新成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("无效的状态值", HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新用户状态失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 软删除用户（管理员功能）
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success(null, "用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("删除用户失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据用户名查找用户
     * GET /api/users/search/{username}
     */
    @GetMapping("/search/{username}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserByUsername(@PathVariable String username) {
        try {
            var userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("user", createUserResponse(userOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "查找用户成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("用户不存在", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("查找用户失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
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

    // 工具方法：创建错误响应 (now returns ApiResponse<Object>)
    private ApiResponse<Object> createErrorResponse(String message, int httpStatusCode) {
        // Potentially add error code to data if needed, for now it's just message and code
        return ApiResponse.error(httpStatusCode, message);
    }
}