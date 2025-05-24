package com.studyroom.server.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 座位实体类
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatType type = SeatType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String equipment;

    @Column
    private Boolean hasWindow = false;

    @Column
    private Boolean hasPowerOutlet = true;

    @Column
    private Boolean hasLamp = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 多个座位属于一个自习室
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_room_id", nullable = false)
    private StudyRoom studyRoom;

    // 一个座位可以有多个预订记录
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // 避免循环引用
    private List<Reservation> reservations = new ArrayList<>();

    /**
     * 座位类型枚举
     */
    public enum SeatType {
        REGULAR("普通座位"),
        VIP("VIP座位"),
        QUIET("安静座位"),
        GROUP("团体座位");

        private final String description;

        SeatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 座位状态枚举
     */
    public enum SeatStatus {
        AVAILABLE("可用"),
        OCCUPIED("占用中"),
        RESERVED("已预订"),
        MAINTENANCE("维护中"),
        OUT_OF_ORDER("故障");

        private final String description;

        SeatStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造方法
    public Seat() {
    }

    public Seat(String seatNumber, StudyRoom studyRoom) {
        this.seatNumber = seatNumber;
        this.studyRoom = studyRoom;
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

    public SeatType getType() {
        return type;
    }

    public void setType(SeatType type) {
        this.type = type;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
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

    public StudyRoom getStudyRoom() {
        return studyRoom;
    }

    public void setStudyRoom(StudyRoom studyRoom) {
        this.studyRoom = studyRoom;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", seatNumber='" + seatNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", hasWindow=" + hasWindow +
                ", hasPowerOutlet=" + hasPowerOutlet +
                ", hasLamp=" + hasLamp +
                ", createdAt=" + createdAt +
                '}';
    }
} 