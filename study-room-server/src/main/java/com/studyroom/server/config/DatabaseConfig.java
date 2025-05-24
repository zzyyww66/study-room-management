package com.studyroom.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * 数据库配置类
 * 
 * @author Developer
 * @version 1.0.0
 */
@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    /**
     * 开发环境数据源配置
     */
    @Bean
    @Profile("dev")
    @ConfigurationProperties("spring.datasource")
    public DataSource devDataSource() {
        logger.info("🔧 配置开发环境数据源 (H2)");
        return DataSourceBuilder.create().build();
    }

    /**
     * 生产环境数据源配置
     */
    @Bean
    @Profile("prod")
    @ConfigurationProperties("spring.datasource")
    public DataSource prodDataSource() {
        logger.info("🔧 配置生产环境数据源 (SQLite)");
        return DataSourceBuilder.create().build();
    }
} 