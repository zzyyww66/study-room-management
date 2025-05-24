package com.studyroom.server.service;

import com.studyroom.server.entity.Reservation;
import com.studyroom.server.entity.User;
import com.studyroom.server.entity.Seat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 预订服务接口
 * 定义预订管理相关的业务逻辑
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
public interface ReservationService {
    
    /**
     * 创建预订
     * @param userId 用户ID
     * @param seatId 座位ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param notes 备注
     * @return 创建的预订对象
     */
    Reservation createReservation(Long userId, Long seatId, LocalDateTime startTime, 
                                LocalDateTime endTime, String notes);
    
    /**
     * 根据ID查找预订
     * @param reservationId 预订ID
     * @return 预订对象
     */
    Optional<Reservation> findById(Long reservationId);
    
    /**
     * 根据预订编码查找预订
     * @param reservationCode 预订编码
     * @return 预订对象
     */
    Optional<Reservation> findByReservationCode(String reservationCode);
    
    /**
     * 获取用户的所有预订
     * @param userId 用户ID
     * @return 预订列表
     */
    List<Reservation> findReservationsByUser(Long userId);
    
    /**
     * 获取用户的有效预订
     * @param userId 用户ID
     * @return 有效预订列表
     */
    List<Reservation> findActiveReservationsByUser(Long userId);
    
    /**
     * 获取座位的所有预订
     * @param seatId 座位ID
     * @return 预订列表
     */
    List<Reservation> findReservationsBySeat(Long seatId);
    
    /**
     * 获取今日预订
     * @return 今日预订列表
     */
    List<Reservation> findTodayReservations();
    
    /**
     * 获取有效预订
     * @return 有效预订列表
     */
    List<Reservation> findActiveReservations();
    
    /**
     * 检查预订时间冲突
     * @param seatId 座位ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeReservationId 排除的预订ID（用于更新时）
     * @return 是否有冲突
     */
    boolean hasTimeConflict(Long seatId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId);
    
    /**
     * 计算预订费用
     * @param seatId 座位ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 费用金额
     */
    BigDecimal calculateReservationCost(Long seatId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 更新预订信息
     * @param reservationId 预订ID
     * @param startTime 新开始时间
     * @param endTime 新结束时间
     * @param notes 新备注
     * @return 更新后的预订对象
     */
    Reservation updateReservation(Long reservationId, LocalDateTime startTime, 
                                LocalDateTime endTime, String notes);
    
    /**
     * 取消预订
     * @param reservationId 预订ID
     * @param cancelReason 取消原因
     * @return 是否成功取消
     */
    boolean cancelReservation(Long reservationId, String cancelReason);
    
    /**
     * 支付预订
     * @param reservationId 预订ID
     * @param paymentMethod 支付方式
     * @return 是否支付成功
     */
    boolean payForReservation(Long reservationId, String paymentMethod);
    
    /**
     * 办理入住（签到）
     * @param reservationId 预订ID
     * @return 是否成功办理入住
     */
    boolean checkIn(Long reservationId);
    
    /**
     * 办理退房（签退）
     * @param reservationId 预订ID
     * @return 是否成功办理退房
     */
    boolean checkOut(Long reservationId);
    
    /**
     * 延长预订时间
     * @param reservationId 预订ID
     * @param newEndTime 新的结束时间
     * @return 是否成功延长
     */
    boolean extendReservation(Long reservationId, LocalDateTime newEndTime);
    
    /**
     * 获取过期未支付的预订
     * @return 过期未支付预订列表
     */
    List<Reservation> findExpiredUnpaidReservations();
    
    /**
     * 自动取消过期预订
     * @return 取消的预订数量
     */
    int cancelExpiredReservations();
    
    /**
     * 获取即将到期的预订
     * @param minutesBefore 提前多少分钟
     * @return 即将到期的预订列表
     */
    List<Reservation> findReservationsExpiringWithin(int minutesBefore);
    
    /**
     * 获取用户预订统计
     * @param userId 用户ID
     * @return 统计信息Map
     */
    Map<String, Object> getUserReservationStatistics(Long userId);
    
    /**
     * 获取座位预订统计
     * @param seatId 座位ID
     * @return 统计信息Map
     */
    Map<String, Object> getSeatReservationStatistics(Long seatId);
    
    /**
     * 获取系统预订统计
     * @return 系统级统计信息Map
     */
    Map<String, Object> getSystemReservationStatistics();
    
    /**
     * 获取收入统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入统计信息
     */
    Map<String, Object> getRevenueStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 分页查询预订
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param userId 用户ID筛选（可选）
     * @param status 状态筛选（可选）
     * @param paymentStatus 支付状态筛选（可选）
     * @return 预订分页结果
     */
    org.springframework.data.domain.Page<Reservation> findReservationsWithPagination(
        int page, int size, Long userId, Reservation.ReservationStatus status, 
        Reservation.PaymentStatus paymentStatus);
} 