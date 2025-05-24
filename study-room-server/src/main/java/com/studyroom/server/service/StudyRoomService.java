package com.studyroom.server.service;

import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.entity.Seat;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 自习室服务接口
 * 定义自习室管理相关的业务逻辑
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
public interface StudyRoomService {
    
    /**
     * 创建新自习室
     * @param name 自习室名称
     * @param description 描述
     * @param capacity 容量
     * @param hourlyRate 小时费率
     * @param openTime 开放时间
     * @param closeTime 关闭时间
     * @param location 位置
     * @param facilities 设施
     * @return 创建的自习室对象
     */
    StudyRoom createStudyRoom(String name, String description, Integer capacity, 
                             BigDecimal hourlyRate, LocalTime openTime, LocalTime closeTime, 
                             String location, String facilities);
    
    /**
     * 根据ID查找自习室
     * @param roomId 自习室ID
     * @return 自习室对象
     */
    Optional<StudyRoom> findById(Long roomId);
    
    /**
     * 获取所有可用的自习室
     * @return 可用自习室列表
     */
    List<StudyRoom> findAvailableRooms();
    
    /**
     * 根据容量范围查找自习室
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 符合条件的自习室列表
     */
    List<StudyRoom> findRoomsByCapacityRange(Integer minCapacity, Integer maxCapacity);
    
    /**
     * 根据价格范围查找自习室
     * @param minRate 最低价格
     * @param maxRate 最高价格
     * @return 符合条件的自习室列表
     */
    List<StudyRoom> findRoomsByPriceRange(BigDecimal minRate, BigDecimal maxRate);
    
    /**
     * 根据名称模糊查找自习室
     * @param nameKeyword 名称关键字
     * @return 符合条件的自习室列表
     */
    List<StudyRoom> findRoomsByNameContaining(String nameKeyword);
    
    /**
     * 获取自习室的所有座位
     * @param roomId 自习室ID
     * @return 座位列表
     */
    List<Seat> getRoomSeats(Long roomId);
    
    /**
     * 获取自习室的可用座位数量
     * @param roomId 自习室ID
     * @return 可用座位数量
     */
    int getAvailableSeatsCount(Long roomId);
    
    /**
     * 更新自习室信息
     * @param roomId 自习室ID
     * @param name 新名称
     * @param description 新描述
     * @param capacity 新容量
     * @param hourlyRate 新费率
     * @param openTime 新开放时间
     * @param closeTime 新关闭时间
     * @param location 新位置
     * @param facilities 新设施
     * @return 更新后的自习室对象
     */
    StudyRoom updateStudyRoom(Long roomId, String name, String description, Integer capacity,
                             BigDecimal hourlyRate, LocalTime openTime, LocalTime closeTime,
                             String location, String facilities);
    
    /**
     * 更新自习室状态
     * @param roomId 自习室ID
     * @param status 新状态
     * @return 更新后的自习室对象
     */
    StudyRoom updateRoomStatus(Long roomId, StudyRoom.RoomStatus status);
    
    /**
     * 删除自习室（软删除，设置为CLOSED状态）
     * @param roomId 自习室ID
     */
    void deleteStudyRoom(Long roomId);
    
    /**
     * 检查自习室在指定时间是否开放
     * @param roomId 自习室ID
     * @param time 检查时间
     * @return 是否开放
     */
    boolean isRoomOpenAtTime(Long roomId, LocalTime time);
    
    /**
     * 获取自习室统计信息
     * @param roomId 自习室ID
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getRoomStatistics(Long roomId);
    
    /**
     * 获取所有自习室的利用率统计
     * @return 利用率统计列表
     */
    List<java.util.Map<String, Object>> getRoomsUtilizationStats();
    
    /**
     * 按价格排序获取自习室
     * @param ascending 是否升序
     * @return 排序后的自习室列表
     */
    List<StudyRoom> findRoomsOrderByPrice(boolean ascending);
    
    /**
     * 按容量排序获取自习室
     * @param ascending 是否升序
     * @return 排序后的自习室列表
     */
    List<StudyRoom> findRoomsOrderByCapacity(boolean ascending);
    
    /**
     * 分页查询自习室
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param status 状态筛选（可选）
     * @return 自习室分页结果
     */
    org.springframework.data.domain.Page<StudyRoom> findRoomsWithPagination(
        int page, int size, StudyRoom.RoomStatus status);
} 