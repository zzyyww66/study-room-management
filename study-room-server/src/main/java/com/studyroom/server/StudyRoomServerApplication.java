package com.studyroom.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 共享自习室管理系统 - Spring Boot 服务端主应用程序
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.studyroom.server.repository")
@ComponentScan(basePackages = "com.studyroom.server")
public class StudyRoomServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(StudyRoomServerApplication.class);
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("🏫 共享自习室管理系统 v1.0.0");
        System.out.println("🚀 Spring Boot 服务端启动中...");
        System.out.println("=================================");
        
        SpringApplication.run(StudyRoomServerApplication.class, args);
        
        logger.info("✅ 服务端应用启动成功！");
    }
} 