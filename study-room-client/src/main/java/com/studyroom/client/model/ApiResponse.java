package com.studyroom.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API响应统一封装类
 * 用于封装后端返回的所有响应数据
 * 
 * @param <T> 响应数据类型
 * @author Developer
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
    
    /**
     * 响应是否成功
     */
    @JsonProperty("success")
    private boolean success;
    
    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * 响应数据
     */
    @JsonProperty("data")
    private T data;
    
    /**
     * 错误代码（可选）
     */
    @JsonProperty("errorCode")
    private String errorCode;

    // 默认构造函数
    public ApiResponse() {
    }

    // 构造函数
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 判断请求是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 获取错误信息（如果失败的话）
     */
    public String getErrorMessage() {
        return isError() ? message : null;
    }

    // Getter 和 Setter 方法
    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
} 