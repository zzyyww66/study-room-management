package com.studyroom.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 座位模型类
 * 
 * @author Developer
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Seat {

    /**
     * 座位类型枚举
     */
    public enum Type {
        NORMAL("普通座位"),
        VIP("VIP座位"),
        COUPLE("情侣座位"),
        PRIVATE("私人座位");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 座位状态枚举
     */
    public enum Status {
        AVAILABLE("可用"),
        OCCUPIED("占用"),
        RESERVED("预订"),
        MAINTENANCE("维护");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 基本属性
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("seatNumber")
    private String seatNumber;
    
    @JsonProperty("type")
    private Type type;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("hourlyRate")
    private BigDecimal hourlyRate;
    
    @JsonProperty("hasWindow")
    private Boolean hasWindow;
    
    @JsonProperty("hasPowerOutlet")
    private Boolean hasPowerOutlet;
    
    @JsonProperty("hasLamp")
    private Boolean hasLamp;
    
    @JsonProperty("studyRoomId")
    private Long studyRoomId;
    
    @JsonProperty("studyRoom")
    private StudyRoom studyRoom;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("rowNum")
    private Integer rowNum;

    @JsonProperty("colNum")
    private Integer colNum;

    // 默认构造函数
    public Seat() {
        this.type = Type.NORMAL;
        this.status = Status.AVAILABLE;
        this.hasWindow = false;
        this.hasPowerOutlet = true;
        this.hasLamp = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 构造函数
    public Seat(String seatNumber, Type type, BigDecimal hourlyRate) {
        this();
        this.seatNumber = seatNumber;
        this.type = type;
        this.hourlyRate = hourlyRate;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Boolean getHasWindow() {
        return hasWindow;
    }

    public void setHasWindow(Boolean hasWindow) {
        this.hasWindow = hasWindow;
    }

    public Boolean getHasPowerOutlet() {
        return hasPowerOutlet;
    }

    public void setHasPowerOutlet(Boolean hasPowerOutlet) {
        this.hasPowerOutlet = hasPowerOutlet;
    }

    public Boolean getHasLamp() {
        return hasLamp;
    }

    public void setHasLamp(Boolean hasLamp) {
        this.hasLamp = hasLamp;
    }

    public Long getStudyRoomId() {
        return studyRoomId;
    }

    public void setStudyRoomId(Long studyRoomId) {
        this.studyRoomId = studyRoomId;
    }

    public StudyRoom getStudyRoom() {
        return studyRoom;
    }

    public void setStudyRoom(StudyRoom studyRoom) {
        this.studyRoom = studyRoom;
        if (studyRoom != null) {
            this.studyRoomId = studyRoom.getId();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getColNum() {
        return colNum;
    }

    public void setColNum(Integer colNum) {
        this.colNum = colNum;
    }

    // 工具方法
    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == Status.OCCUPIED;
    }

    public boolean isReserved() {
        return status == Status.RESERVED;
    }

    public boolean isUnderMaintenance() {
        return status == Status.MAINTENANCE;
    }

    public String getFeatures() {
        StringBuilder features = new StringBuilder();
        if (hasWindow != null && hasWindow) {
            features.append("靠窗 ");
        }
        if (hasPowerOutlet != null && hasPowerOutlet) {
            features.append("电源 ");
        }
        if (hasLamp != null && hasLamp) {
            features.append("台灯 ");
        }
        return features.toString().trim();
    }

    public String getDisplayName() {
        return seatNumber + " (" + type.getDisplayName() + ")";
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", seatNumber='" + seatNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", hourlyRate=" + hourlyRate +
                ", studyRoomId=" + studyRoomId +
                '}';
    }
} 