package com.studyroom.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

/**
 * 配置服务类
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    
    // 配置文件路径
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".studyroom";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "client.properties";
    
    // 配置键名
    private static final String KEY_SERVER_URL = "server.url";
    private static final String KEY_SAVED_USERNAME = "login.username";
    private static final String KEY_SAVED_PASSWORD = "login.password";
    private static final String KEY_AUTO_LOGIN = "login.auto";
    private static final String KEY_REMEMBER_PASSWORD = "login.remember";
    private static final String KEY_THEME = "ui.theme";
    private static final String KEY_LANGUAGE = "ui.language";
    
    // 配置属性
    private Properties properties;

    public ConfigService() {
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    private void loadConfig() {
        properties = new Properties();
        
        try {
            // 确保配置目录存在
            ensureConfigDirectory();
            
            // 加载配置文件
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                    logger.info("✅ 配置文件加载成功: {}", CONFIG_FILE);
                }
            } else {
                // 创建默认配置
                createDefaultConfig();
                logger.info("📝 创建默认配置文件: {}", CONFIG_FILE);
            }
            
        } catch (IOException e) {
            logger.error("❌ 配置文件加载失败", e);
            // 使用默认配置
            setDefaultValues();
        }
    }

    /**
     * 确保配置目录存在
     */
    private void ensureConfigDirectory() throws IOException {
        Path configDir = Paths.get(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
            logger.info("📁 创建配置目录: {}", CONFIG_DIR);
        }
    }

    /**
     * 创建默认配置
     */
    private void createDefaultConfig() {
        setDefaultValues();
        saveConfig();
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues() {
        properties.setProperty(KEY_SERVER_URL, "http://localhost:8080");
        properties.setProperty(KEY_AUTO_LOGIN, "false");
        properties.setProperty(KEY_REMEMBER_PASSWORD, "false");
        properties.setProperty(KEY_THEME, "default");
        properties.setProperty(KEY_LANGUAGE, "zh_CN");
    }

    /**
     * 保存配置文件
     */
    private void saveConfig() {
        try {
            ensureConfigDirectory();
            
            try (OutputStream output = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
                properties.store(output, "Study Room Client Configuration");
                logger.debug("💾 配置文件保存成功");
            }
            
        } catch (IOException e) {
            logger.error("❌ 配置文件保存失败", e);
        }
    }

    // 服务器配置
    public String getServerUrl() {
        return properties.getProperty(KEY_SERVER_URL, "http://localhost:8080");
    }

    public void setServerUrl(String serverUrl) {
        properties.setProperty(KEY_SERVER_URL, serverUrl);
        saveConfig();
    }

    // 登录凭据管理
    public String getSavedUsername() {
        return properties.getProperty(KEY_SAVED_USERNAME, "");
    }

    public String getSavedPassword() {
        String encodedPassword = properties.getProperty(KEY_SAVED_PASSWORD, "");
        if (encodedPassword.isEmpty()) {
            return "";
        }
        
        try {
            // 简单的Base64解码（注意：这不是安全的加密方式，仅用于演示）
            byte[] decodedBytes = Base64.getDecoder().decode(encodedPassword);
            return new String(decodedBytes);
        } catch (Exception e) {
            logger.warn("⚠️ 密码解码失败", e);
            return "";
        }
    }

    public void saveCredentials(String username, String password) {
        properties.setProperty(KEY_SAVED_USERNAME, username);
        
        if (password != null && !password.isEmpty()) {
            // 简单的Base64编码（注意：这不是安全的加密方式，仅用于演示）
            String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
            properties.setProperty(KEY_SAVED_PASSWORD, encodedPassword);
        }
        
        properties.setProperty(KEY_REMEMBER_PASSWORD, "true");
        saveConfig();
        logger.info("💾 用户凭据已保存");
    }

    public void clearSavedCredentials() {
        properties.remove(KEY_SAVED_USERNAME);
        properties.remove(KEY_SAVED_PASSWORD);
        properties.setProperty(KEY_REMEMBER_PASSWORD, "false");
        saveConfig();
        logger.info("🗑️ 已清除保存的凭据");
    }

    public boolean isRememberPasswordEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_REMEMBER_PASSWORD, "false"));
    }

    // 自动登录设置
    public boolean isAutoLoginEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_AUTO_LOGIN, "false"));
    }

    public void setAutoLoginEnabled(boolean enabled) {
        properties.setProperty(KEY_AUTO_LOGIN, String.valueOf(enabled));
        saveConfig();
    }

    // 界面配置
    public String getTheme() {
        return properties.getProperty(KEY_THEME, "default");
    }

    public void setTheme(String theme) {
        properties.setProperty(KEY_THEME, theme);
        saveConfig();
    }

    public String getLanguage() {
        return properties.getProperty(KEY_LANGUAGE, "zh_CN");
    }

    public void setLanguage(String language) {
        properties.setProperty(KEY_LANGUAGE, language);
        saveConfig();
    }

    // 窗口配置
    public double getWindowWidth() {
        return Double.parseDouble(properties.getProperty("window.width", "1000"));
    }

    public void setWindowWidth(double width) {
        properties.setProperty("window.width", String.valueOf(width));
        saveConfig();
    }

    public double getWindowHeight() {
        return Double.parseDouble(properties.getProperty("window.height", "700"));
    }

    public void setWindowHeight(double height) {
        properties.setProperty("window.height", String.valueOf(height));
        saveConfig();
    }

    public double getWindowX() {
        return Double.parseDouble(properties.getProperty("window.x", "-1"));
    }

    public void setWindowX(double x) {
        properties.setProperty("window.x", String.valueOf(x));
        saveConfig();
    }

    public double getWindowY() {
        return Double.parseDouble(properties.getProperty("window.y", "-1"));
    }

    public void setWindowY(double y) {
        properties.setProperty("window.y", String.valueOf(y));
        saveConfig();
    }

    public boolean isWindowMaximized() {
        return Boolean.parseBoolean(properties.getProperty("window.maximized", "false"));
    }

    public void setWindowMaximized(boolean maximized) {
        properties.setProperty("window.maximized", String.valueOf(maximized));
        saveConfig();
    }

    // 其他设置
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }

    public void removeProperty(String key) {
        properties.remove(key);
        saveConfig();
    }

    /**
     * 重置所有配置到默认值
     */
    public void resetToDefaults() {
        properties.clear();
        setDefaultValues();
        saveConfig();
        logger.info("🔄 配置已重置为默认值");
    }

    /**
     * 获取配置文件路径
     */
    public String getConfigFilePath() {
        return CONFIG_FILE;
    }
} 