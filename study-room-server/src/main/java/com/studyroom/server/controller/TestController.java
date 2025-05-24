package com.studyroom.server.controller;

import com.studyroom.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 验证Spring Boot Controller扫描是否正常
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Study Room Server!");
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/service-test")
    public Map<String, Object> testService() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 测试Service是否正确注入
            response.put("userServiceInjected", userService != null);
            
            // 测试一个最简单的查询
            var activeUsers = userService.findActiveUsers();
            response.put("activeUsersCount", activeUsers.size());
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getSimpleName());
        }
        return response;
    }
    
    @GetMapping("/username-test/{username}")
    public Map<String, Object> testUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 直接测试isUsernameAvailable方法
            boolean available = userService.isUsernameAvailable(username);
            response.put("username", username);
            response.put("available", available);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getSimpleName());
            e.printStackTrace(); // 在控制台打印详细错误
        }
        return response;
    }
} 