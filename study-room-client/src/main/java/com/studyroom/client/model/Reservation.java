package com.studyroom.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 预订模型类
 * 
 * @author Developer
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation {

    /**
     * 预订状态枚举
     */
    public enum Status {
        ACTIVE("已确认/进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        EXPIRED("已过期"),
        NO_SHOW("未到场");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 支付状态枚举
     */
    public enum PaymentStatus {
        UNPAID("未支付"),
        PAID("已支付"),
        REFUNDED("已退款"),
        PARTIAL_REFUND("部分退款");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 基本属性
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("paymentStatus")
    private PaymentStatus paymentStatus;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("actualStartTime")
    private LocalDateTime actualStartTime;
    
    @JsonProperty("actualEndTime")
    private LocalDateTime actualEndTime;
    
    @JsonProperty("note")
    private String note;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("user")
    private User user;
    
    @JsonProperty("seatId")
    private Long seatId;
    
    @JsonProperty("seat")
    private Seat seat;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("paidAt")
    private LocalDateTime paidAt;

    // 默认构造函数
    public Reservation() {
        this.status = Status.ACTIVE;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 构造函数
    public Reservation(LocalDateTime startTime, LocalDateTime endTime, BigDecimal totalAmount) {
        this();
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalAmount;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
        if (seat != null) {
            this.seatId = seat.getId();
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

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    // 工具方法
    public boolean isPending() {
        return status == Status.ACTIVE;
    }

    public boolean isConfirmed() {
        return status == Status.ACTIVE;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    public boolean isExpired() {
        return status == Status.EXPIRED;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    public boolean isUnpaid() {
        return paymentStatus == PaymentStatus.UNPAID;
    }

    /**
     * 获取预订时长（小时）
     */
    public long getDurationHours() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toHours();
        }
        return 0;
    }

    /**
     * 获取实际使用时长（小时）
     */
    public long getActualDurationHours() {
        if (actualStartTime != null && actualEndTime != null) {
            return java.time.Duration.between(actualStartTime, actualEndTime).toHours();
        }
        return 0;
    }

    /**
     * 获取格式化的时间段
     */
    public String getTimeSlot() {
        if (startTime != null && endTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            return startTime.format(formatter) + " ~ " + endTime.format(formatter);
        }
        return "";
    }

    /**
     * 获取座位和房间信息
     */
    public String getSeatInfo() {
        if (seat != null) {
            String seatInfo = seat.getSeatNumber();
            if (seat.getStudyRoom() != null) {
                seatInfo += " (" + seat.getStudyRoom().getName() + ")";
            }
            return seatInfo;
        }
        return "";
    }

    /**
     * 判断是否需要支付
     */
    public boolean needsPayment() {
        return !isCancelled() && !isExpired() && isUnpaid();
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return isPending() || isConfirmed();
    }

    /**
     * 判断是否可以签到
     */
    public boolean canCheckIn() {
        return isConfirmed() && isPaid() && 
               startTime != null && LocalDateTime.now().isAfter(startTime.minusMinutes(15));
    }

    /**
     * 判断是否可以签退
     */
    public boolean canCheckOut() {
        return isActive() && actualStartTime != null;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", paymentStatus=" + paymentStatus +
                ", totalAmount=" + totalAmount +
                ", seatId=" + seatId +
                ", userId=" + userId +
                '}';
    }
} 