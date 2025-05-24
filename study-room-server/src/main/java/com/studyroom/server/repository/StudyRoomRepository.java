package com.studyroom.server.repository;

import com.studyroom.server.entity.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 自习室数据访问接口
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    /**
     * 根据自习室名称查找
     * @param name 自习室名称
     * @return 自习室信息
     */
    Optional<StudyRoom> findByName(String name);

    /**
     * 根据状态查找自习室列表
     * @param status 房间状态
     * @return 自习室列表
     */
    List<StudyRoom> findByStatus(StudyRoom.RoomStatus status);

    /**
     * 查找可用的自习室
     * @return 可用自习室列表
     */
    @Query("SELECT r FROM StudyRoom r WHERE r.status = 'AVAILABLE'")
    List<StudyRoom> findAvailableRooms();

    /**
     * 根据容量范围查找自习室
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 自习室列表
     */
    @Query("SELECT r FROM StudyRoom r WHERE r.capacity BETWEEN :minCapacity AND :maxCapacity")
    List<StudyRoom> findByCapacityBetween(@Param("minCapacity") Integer minCapacity, 
                                          @Param("maxCapacity") Integer maxCapacity);

    /**
     * 根据价格范围查找自习室
     * @param minRate 最低小时费率
     * @param maxRate 最高小时费率
     * @return 自习室列表
     */
    List<StudyRoom> findByHourlyRateBetween(BigDecimal minRate, BigDecimal maxRate);

    /**
     * 根据位置模糊查询
     * @param location 位置关键字
     * @return 自习室列表
     */
    List<StudyRoom> findByLocationContaining(String location);

    /**
     * 根据名称模糊查询
     * @param name 名称关键字
     * @return 自习室列表
     */
    List<StudyRoom> findByNameContaining(String name);

    /**
     * 根据容量查找自习室（大于等于指定容量）
     * @param capacity 最小容量
     * @return 自习室列表
     */
    List<StudyRoom> findByCapacityGreaterThanEqual(Integer capacity);

    /**
     * 根据小时费率查找（小于等于指定价格）
     * @param rate 最高费率
     * @return 自习室列表
     */
    List<StudyRoom> findByHourlyRateLessThanEqual(BigDecimal rate);

    /**
     * 查找指定时间段开放的自习室
     * @param time 查询时间
     * @return 自习室列表
     */
    @Query("SELECT r FROM StudyRoom r WHERE r.openTime <= :time AND r.closeTime > :time AND r.status = 'AVAILABLE'")
    List<StudyRoom> findOpenAtTime(@Param("time") LocalTime time);

    /**
     * 根据设施关键字查找
     * @param facility 设施关键字
     * @return 自习室列表
     */
    @Query("SELECT r FROM StudyRoom r WHERE r.facilities LIKE %:facility%")
    List<StudyRoom> findByFacility(@Param("facility") String facility);

    /**
     * 查找有可用座位的自习室
     * @return 自习室列表
     */
    @Query("SELECT DISTINCT r FROM StudyRoom r JOIN r.seats s WHERE s.status = 'AVAILABLE' AND r.status = 'AVAILABLE'")
    List<StudyRoom> findRoomsWithAvailableSeats();

    /**
     * 统计可用自习室数量
     * @return 可用自习室数量
     */
    @Query("SELECT COUNT(r) FROM StudyRoom r WHERE r.status = 'AVAILABLE'")
    long countAvailableRooms();

    /**
     * 按价格排序查找自习室（价格从低到高）
     * @return 自习室列表
     */
    List<StudyRoom> findAllByOrderByHourlyRateAsc();

    /**
     * 按容量排序查找自习室（容量从大到小）
     * @return 自习室列表
     */
    List<StudyRoom> findAllByOrderByCapacityDesc();

    /**
     * 查找指定容量和状态的自习室
     * @param capacity 容量
     * @param status 状态
     * @return 自习室列表
     */
    List<StudyRoom> findByCapacityAndStatus(Integer capacity, StudyRoom.RoomStatus status);
} 