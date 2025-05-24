package com.studyroom.client.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 自习室模型类
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class StudyRoom {

    // 基本属性
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private String openTime;
    private String closeTime;
    private Status status;
    
    // 时间戳
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 自习室状态枚举
     */
    public enum Status {
        AVAILABLE("可用"),
        OCCUPIED("占用"),
        MAINTENANCE("维护中"),
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

    public StudyRoom(String name, String location, Integer capacity, BigDecimal pricePerHour) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.status = Status.AVAILABLE;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
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

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
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
                ", pricePerHour=" + pricePerHour +
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