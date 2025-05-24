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
 * Service层测试控制器
 */
@RestController
@RequestMapping("/api/service-test")
public class ServiceTestController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private StudyRoomService studyRoomService;

    /**
     * Service层健康检查
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

    /**
     * 测试用户服务基本功能
     */
    @GetMapping("/users/basic")
    public Map<String, Object> testUserServiceBasic() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<User> activeUsers = userService.findActiveUsers();
            result.put("activeUsersCount", activeUsers.size());
            
            boolean adminExists = !userService.isUsernameAvailable("admin");
            result.put("adminUserExists", adminExists);
            
            boolean emailExists = !userService.isEmailAvailable("admin@studyroom.com");
            result.put("adminEmailExists", emailExists);
            
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
        
        try {
            List<StudyRoom> availableRooms = studyRoomService.findAvailableRooms();
            result.put("availableRoomsCount", availableRooms.size());
            
            List<StudyRoom> roomsByPrice = studyRoomService.findRoomsOrderByPrice(true);
            result.put("roomsByPriceCount", roomsByPrice.size());
            
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
} 