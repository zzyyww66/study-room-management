package com.studyroom.server.controller;

import com.studyroom.server.util.ResponseUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    /**
     * 基本健康检查
     */
    @GetMapping
    public Map<String, Object> health() {
        logger.info("🔍 执行健康检查");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "study-room-server");
        healthInfo.put("version", "1.0.0");
        
        return ResponseUtil.success("系统健康检查通过", healthInfo);
    }

    /**
     * 版本信息
     */
    @GetMapping("/version")
    public Map<String, Object> version() {
        logger.info("📋 获取版本信息");
        
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("application", "共享自习室管理系统");
        versionInfo.put("version", "1.0.0");
        versionInfo.put("build-time", LocalDateTime.now());
        versionInfo.put("java-version", System.getProperty("java.version"));
        versionInfo.put("spring-boot", "2.7.14");
        
        return ResponseUtil.success("获取版本信息成功", versionInfo);
    }
} 