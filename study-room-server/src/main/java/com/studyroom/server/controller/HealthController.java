package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthDataMap = new HashMap<>();
        healthDataMap.put("status", "UP");
        healthDataMap.put("application", "Study Room Management Server");
        healthDataMap.put("version", "1.0.0");
        //ApiResponse already adds a timestamp, so this one can be removed if redundant
        //healthDataMap.put("timestamp", LocalDateTime.now());
        //The message is now part of ApiResponse
        //healthDataMap.put("message", "🎉 服务器运行正常！");

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(healthDataMap, "系统正常");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, String>>> test() {
        Map<String, String> testDataMap = new HashMap<>();
        testDataMap.put("message", "测试接口调用成功");
        //ApiResponse already adds a timestamp, so this one can be removed if redundant
        //testDataMap.put("timestamp", LocalDateTime.now().toString());

        ApiResponse<Map<String, String>> apiResponse = ApiResponse.success(testDataMap, "测试接口调用成功");
        return ResponseEntity.ok(apiResponse);
    }
}