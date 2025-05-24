package com.studyroom.server.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应格式工具类
 * 
 * @author Developer
 * @version 1.0.0
 */
public class ResponseUtil {

    /**
     * 成功响应
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "操作成功");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 错误响应
     */
    public static Map<String, Object> error(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 错误响应（默认500）
     */
    public static Map<String, Object> error(String message) {
        return error(500, message);
    }

    /**
     * 参数错误响应
     */
    public static Map<String, Object> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 未授权响应
     */
    public static Map<String, Object> unauthorized(String message) {
        return error(401, message != null ? message : "未授权访问");
    }

    /**
     * 资源不存在响应
     */
    public static Map<String, Object> notFound(String message) {
        return error(404, message != null ? message : "资源不存在");
    }
} 