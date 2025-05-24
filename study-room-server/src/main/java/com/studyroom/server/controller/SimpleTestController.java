package com.studyroom.server.controller;

import com.studyroom.server.entity.User;
import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.service.UserService;
import com.studyroom.server.service.StudyRoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 简单测试控制器 - 扩展版
 * 用于验证基本功能和Service层测试
 */
@RestController
@RequestMapping("/simple-test")
public class SimpleTestController {

    @Autowired(required = false)
    private UserService userService;
    
    @Autowired(required = false)
    private StudyRoomService studyRoomService;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello from Study Room Server!");
        result.put("timestamp", java.time.LocalDateTime.now());
        result.put("userServiceExists", userService != null);
        result.put("studyRoomServiceExists", studyRoomService != null);
        return result;
    }
    
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("server", "running");
        result.put("services", "checking");
        
        if (userService != null) {
            try {
                int activeUsers = userService.findActiveUsers().size();
                result.put("activeUsers", activeUsers);
                result.put("userService", "OK");
            } catch (Exception e) {
                result.put("userService", "ERROR: " + e.getMessage());
            }
        } else {
            result.put("userService", "NOT_INJECTED");
        }
        
        if (studyRoomService != null) {
            try {
                int availableRooms = studyRoomService.findAvailableRooms().size();
                result.put("availableRooms", availableRooms);
                result.put("studyRoomService", "OK");
            } catch (Exception e) {
                result.put("studyRoomService", "ERROR: " + e.getMessage());
            }
        } else {
            result.put("studyRoomService", "NOT_INJECTED");
        }
        
        return result;
    }

    /**
     * 测试用户服务基本功能
     */
    @GetMapping("/users/basic")
    public Map<String, Object> testUserServiceBasic() {
        Map<String, Object> result = new HashMap<>();
        
        if (userService == null) {
            result.put("status", "ERROR");
            result.put("message", "用户服务未注入");
            return result;
        }
        
        try {
            // 测试查找活跃用户
            List<User> activeUsers = userService.findActiveUsers();
            result.put("activeUsersCount", activeUsers.size());
            
            // 测试用户名可用性检查
            boolean adminExists = !userService.isUsernameAvailable("admin");
            result.put("adminUserExists", adminExists);
            
            // 测试邮箱可用性检查
            boolean emailExists = !userService.isEmailAvailable("admin@studyroom.com");
            result.put("adminEmailExists", emailExists);
            
            // 测试用户认证
            User authenticatedUser = userService.authenticateUser("admin", "admin123");
            result.put("adminAuthenticationSuccess", authenticatedUser != null);
            
            result.put("status", "SUCCESS");
            result.put("message", "用户服务基本功能测试通过");
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "用户服务测试失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 测试用户统计功能
     */
    @GetMapping("/users/{userId}/statistics")
    public Map<String, Object> testUserStatistics(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userService == null) {
            result.put("status", "ERROR");
            result.put("message", "用户服务未注入");
            return result;
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                Map<String, Object> stats = userService.getUserStatistics(userId);
                result.put("userInfo", userOpt.get());
                result.put("statistics", stats);
                result.put("status", "SUCCESS");
            } else {
                result.put("status", "ERROR");
                result.put("message", "用户不存在");
            }
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "用户统计测试失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 测试自习室服务基本功能
     */
    @GetMapping("/studyrooms/basic")
    public Map<String, Object> testStudyRoomServiceBasic() {
        Map<String, Object> result = new HashMap<>();
        
        if (studyRoomService == null) {
            result.put("status", "ERROR");
            result.put("message", "自习室服务未注入");
            return result;
        }
        
        try {
            // 测试查找可用自习室
            List<StudyRoom> availableRooms = studyRoomService.findAvailableRooms();
            result.put("availableRoomsCount", availableRooms.size());
            
            // 测试按价格排序
            List<StudyRoom> roomsByPrice = studyRoomService.findRoomsOrderByPrice(true);
            result.put("roomsByPriceCount", roomsByPrice.size());
            
            // 测试按容量排序
            List<StudyRoom> roomsByCapacity = studyRoomService.findRoomsOrderByCapacity(false);
            result.put("roomsByCapacityCount", roomsByCapacity.size());
            
            result.put("status", "SUCCESS");
            result.put("message", "自习室服务基本功能测试通过");
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "自习室服务测试失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 测试自习室统计功能
     */
    @GetMapping("/studyrooms/{roomId}/statistics")
    public Map<String, Object> testStudyRoomStatistics(@PathVariable Long roomId) {
        Map<String, Object> result = new HashMap<>();
        
        if (studyRoomService == null) {
            result.put("status", "ERROR");
            result.put("message", "自习室服务未注入");
            return result;
        }
        
        try {
            Optional<StudyRoom> roomOpt = studyRoomService.findById(roomId);
            if (roomOpt.isPresent()) {
                Map<String, Object> stats = studyRoomService.getRoomStatistics(roomId);
                result.put("roomInfo", roomOpt.get());
                result.put("statistics", stats);
                result.put("status", "SUCCESS");
            } else {
                result.put("status", "ERROR");
                result.put("message", "自习室不存在");
            }
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "自习室统计测试失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 测试自习室利用率统计
     */
    @GetMapping("/studyrooms/utilization")
    public Map<String, Object> testStudyRoomUtilization() {
        Map<String, Object> result = new HashMap<>();
        
        if (studyRoomService == null) {
            result.put("status", "ERROR");
            result.put("message", "自习室服务未注入");
            return result;
        }
        
        try {
            List<Map<String, Object>> utilizationStats = studyRoomService.getRoomsUtilizationStats();
            result.put("utilizationStats", utilizationStats);
            result.put("roomsCount", utilizationStats.size());
            result.put("status", "SUCCESS");
            result.put("message", "自习室利用率统计测试通过");
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "自习室利用率统计测试失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Service层整体健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> serviceHealthCheck() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean userServiceReady = userService != null;
            boolean studyRoomServiceReady = studyRoomService != null;
            
            result.put("userServiceReady", userServiceReady);
            result.put("studyRoomServiceReady", studyRoomServiceReady);
            result.put("allServicesReady", userServiceReady && studyRoomServiceReady);
            result.put("status", "SUCCESS");
            result.put("message", "Service层健康检查通过");
            result.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Service层健康检查失败: " + e.getMessage());
        }
        
        return result;
    }
} 