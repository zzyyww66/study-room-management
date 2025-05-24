package com.studyroom.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP客户端服务类
 * 负责与后端服务器的API通信
 * 
 * @author Developer
 * @version 1.0.0
 */
public class HttpClientService {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);
    
    // 单例实例
    private static HttpClientService instance;
    
    // HTTP客户端
    private final CloseableHttpClient httpClient;
    
    // JSON处理器
    private final ObjectMapper objectMapper;
    
    // 服务器配置
    private String baseUrl = "http://localhost:8080";
    private String apiPrefix = "/api";
    
    // 认证令牌
    private String authToken;

    /**
     * 私有构造函数 - 单例模式
     */
    private HttpClientService() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        logger.info("🔧 HTTP客户端服务初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized HttpClientService getInstance() {
        if (instance == null) {
            instance = new HttpClientService();
        }
        return instance;
    }

    /**
     * 设置服务器基础URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        logger.info("🌐 设置服务器地址: {}", baseUrl);
    }

    /**
     * 设置认证令牌
     */
    public void setAuthToken(String token) {
        this.authToken = token;
        logger.debug("🔑 设置认证令牌");
    }

    /**
     * 清除认证令牌
     */
    public void clearAuthToken() {
        this.authToken = null;
        logger.debug("🗑️ 清除认证令牌");
    }

    /**
     * GET请求
     */
    public CompletableFuture<String> get(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            String url = buildUrl(endpoint);
            HttpGet request = new HttpGet(url);
            setupHeaders(request);
            
            logger.debug("📡 发送GET请求: {}", url);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                int statusCode = response.getStatusLine().getStatusCode();
                
                logger.debug("📨 收到响应: {} - {}", statusCode, response.getStatusLine().getReasonPhrase());
                
                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("HTTP错误: " + statusCode + " - " + responseBody);
                }
                
            } catch (IOException e) {
                logger.error("❌ GET请求失败: {}", url, e);
                throw new RuntimeException("网络请求失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * POST请求
     */
    public CompletableFuture<String> post(String endpoint, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            String url = buildUrl(endpoint);
            HttpPost request = new HttpPost(url);
            setupHeaders(request);
            
            try {
                if (data != null) {
                    String jsonData = objectMapper.writeValueAsString(data);
                    request.setEntity(new StringEntity(jsonData, StandardCharsets.UTF_8));
                    request.setHeader("Content-Type", "application/json");
                }
                
                logger.debug("📡 发送POST请求: {}", url);
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    
                    logger.debug("📨 收到响应: {} - {}", statusCode, response.getStatusLine().getReasonPhrase());
                    
                    if (statusCode >= 200 && statusCode < 300) {
                        return responseBody;
                    } else {
                        throw new RuntimeException("HTTP错误: " + statusCode + " - " + responseBody);
                    }
                }
                
            } catch (IOException e) {
                logger.error("❌ POST请求失败: {}", url, e);
                throw new RuntimeException("网络请求失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * PUT请求
     */
    public CompletableFuture<String> put(String endpoint, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            String url = buildUrl(endpoint);
            HttpPut request = new HttpPut(url);
            setupHeaders(request);
            
            try {
                if (data != null) {
                    String jsonData = objectMapper.writeValueAsString(data);
                    request.setEntity(new StringEntity(jsonData, StandardCharsets.UTF_8));
                    request.setHeader("Content-Type", "application/json");
                }
                
                logger.debug("📡 发送PUT请求: {}", url);
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    
                    logger.debug("📨 收到响应: {} - {}", statusCode, response.getStatusLine().getReasonPhrase());
                    
                    if (statusCode >= 200 && statusCode < 300) {
                        return responseBody;
                    } else {
                        throw new RuntimeException("HTTP错误: " + statusCode + " - " + responseBody);
                    }
                }
                
            } catch (IOException e) {
                logger.error("❌ PUT请求失败: {}", url, e);
                throw new RuntimeException("网络请求失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * DELETE请求
     */
    public CompletableFuture<String> delete(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            String url = buildUrl(endpoint);
            HttpDelete request = new HttpDelete(url);
            setupHeaders(request);
            
            logger.debug("📡 发送DELETE请求: {}", url);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                int statusCode = response.getStatusLine().getStatusCode();
                
                logger.debug("📨 收到响应: {} - {}", statusCode, response.getStatusLine().getReasonPhrase());
                
                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("HTTP错误: " + statusCode + " - " + responseBody);
                }
                
            } catch (IOException e) {
                logger.error("❌ DELETE请求失败: {}", url, e);
                throw new RuntimeException("网络请求失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 测试服务器连接
     */
    public CompletableFuture<Boolean> testConnection() {
        return get("/health")
            .thenApply(response -> {
                logger.info("✅ 服务器连接测试成功");
                return true;
            })
            .exceptionally(throwable -> {
                logger.warn("❌ 服务器连接测试失败: {}", throwable.getMessage());
                return false;
            });
    }

    /**
     * 构建完整URL
     */
    private String buildUrl(String endpoint) {
        // 确保endpoint以/开始
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        
        // 如果endpoint不以/api开始，则添加API前缀
        if (!endpoint.startsWith(apiPrefix)) {
            endpoint = apiPrefix + endpoint;
        }
        
        return baseUrl + endpoint;
    }

    /**
     * 设置请求头
     */
    private void setupHeaders(HttpRequestBase request) {
        // 设置通用头部
        request.setHeader("User-Agent", "StudyRoomClient/1.0.0");
        request.setHeader("Accept", "application/json");
        request.setHeader("Accept-Charset", "UTF-8");
        
        // 添加认证头部
        if (authToken != null && !authToken.trim().isEmpty()) {
            request.setHeader("Authorization", "Bearer " + authToken);
        }
    }

    /**
     * 获取ObjectMapper实例（用于JSON处理）
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
                logger.info("🔌 HTTP客户端已关闭");
            }
        } catch (IOException e) {
            logger.error("❌ 关闭HTTP客户端时发生错误", e);
        }
    }
} 