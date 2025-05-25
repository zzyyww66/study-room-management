package com.studyroom.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自习室模型类
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyRoom {

    // 基本属性
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer capacity;
    private BigDecimal hourlyRate;  // 修改字段名以匹配后端和控制器
    private BigDecimal pricePerHour; // 保留兼容性
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openTime;  // 修改为LocalTime类型
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closeTime; // 修改为LocalTime类型
    
    private Status status;
    
    // 时间戳
    private LocalDateTime createdAt;  // 修改字段名以匹配后端
    private LocalDateTime updatedAt;  // 修改字段名以匹配后端
    private LocalDateTime createTime; // 保留兼容性
    private LocalDateTime updateTime; // 保留兼容性

    /**
     * 自习室状态枚举
     */
    public enum Status {
        @com.fasterxml.jackson.annotation.JsonProperty("AVAILABLE")
        AVAILABLE("可用"),
        @com.fasterxml.jackson.annotation.JsonProperty("OCCUPIED")
        OCCUPIED("占用"),
        @com.fasterxml.jackson.annotation.JsonProperty("MAINTENANCE")
        MAINTENANCE("维护中"),
        @com.fasterxml.jackson.annotation.JsonProperty("CLOSED")
        CLOSED("已关闭");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 构造方法
    public StudyRoom() {
    }

    public StudyRoom(String name, String location, Integer capacity, BigDecimal hourlyRate) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.hourlyRate = hourlyRate;
        this.pricePerHour = hourlyRate; // 同步设置
        this.status = Status.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createTime = this.createdAt; // 兼容性
        this.updateTime = this.updatedAt; // 兼容性
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
        this.pricePerHour = hourlyRate; // 保持同步
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour != null ? pricePerHour : hourlyRate;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
        this.hourlyRate = pricePerHour; // 保持同步
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        this.updateTime = this.updatedAt; // 兼容性
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.createTime = createdAt; // 兼容性
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        this.updateTime = updatedAt; // 兼容性
    }

    public LocalDateTime getCreateTime() {
        return createTime != null ? createTime : createdAt;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        this.createdAt = createTime; // 保持同步
    }

    public LocalDateTime getUpdateTime() {
        return updateTime != null ? updateTime : updatedAt;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        this.updatedAt = updateTime; // 保持同步
    }

    // 业务方法
    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    public boolean isOpen() {
        return status != Status.CLOSED && status != Status.MAINTENANCE;
    }

    public String getDisplayName() {
        return name + " (" + location + ")";
    }

    // toString 方法
    @Override
    public String toString() {
        return "StudyRoom{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", hourlyRate=" + hourlyRate +
                ", status=" + status +
                '}';
    }

    // equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyRoom)) return false;
        
        StudyRoom studyRoom = (StudyRoom) o;
        return id != null ? id.equals(studyRoom.id) : studyRoom.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 