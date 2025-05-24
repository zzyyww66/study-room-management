package com.studyroom.server.repository;

import com.studyroom.server.entity.Reservation;
import com.studyroom.server.entity.User;
import com.studyroom.server.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 预订数据访问接口
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 根据预订码查找预订
     * @param reservationCode 预订码
     * @return 预订信息
     */
    Optional<Reservation> findByReservationCode(String reservationCode);

    /**
     * 根据用户查找预订列表
     * @param user 用户
     * @return 预订列表
     */
    List<Reservation> findByUser(User user);

    /**
     * 根据用户ID查找预订列表
     * @param userId 用户ID
     * @return 预订列表
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * 根据座位查找预订列表
     * @param seat 座位
     * @return 预订列表
     */
    List<Reservation> findBySeat(Seat seat);

    /**
     * 根据座位ID查找预订列表
     * @param seatId 座位ID
     * @return 预订列表
     */
    List<Reservation> findBySeatId(Long seatId);

    /**
     * 根据预订状态查找预订列表
     * @param status 预订状态
     * @return 预订列表
     */
    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    /**
     * 根据支付状态查找预订列表
     * @param paymentStatus 支付状态
     * @return 预订列表
     */
    List<Reservation> findByPaymentStatus(Reservation.PaymentStatus paymentStatus);

    /**
     * 查找有效的预订
     * @return 有效预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE'")
    List<Reservation> findActiveReservations();

    /**
     * 根据用户查找有效预订
     * @param userId 用户ID
     * @return 有效预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status = 'ACTIVE'")
    List<Reservation> findActiveReservationsByUserId(@Param("userId") Long userId);

    /**
     * 查找指定时间段内的预订
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :startTime AND r.endTime <= :endTime")
    List<Reservation> findReservationsBetween(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定座位在指定时间段内的预订（检查冲突）
     * @param seatId 座位ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.seat.id = :seatId AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime)) AND " +
           "r.status IN ('ACTIVE', 'RESERVED')")
    List<Reservation> findConflictingReservations(@Param("seatId") Long seatId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查找今日预订
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 今日预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :startOfDay AND r.startTime < :endOfDay")
    List<Reservation> findTodayReservations(@Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找用户今日预订
     * @param userId 用户ID
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 用户今日预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND " +
           "r.startTime >= :startOfDay AND r.startTime < :endOfDay")
    List<Reservation> findUserTodayReservations(@Param("userId") Long userId,
                                                @Param("startOfDay") LocalDateTime startOfDay,
                                                @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找已过期的预订
     * @param currentTime 当前时间
     * @return 过期预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.endTime < :currentTime AND r.status = 'ACTIVE'")
    List<Reservation> findExpiredReservations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找即将到期的预订（指定时间内）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 即将到期的预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.endTime >= :startTime AND r.endTime <= :endTime AND r.status = 'ACTIVE'")
    List<Reservation> findUpcomingExpirations(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查找未支付的预订
     * @return 未支付预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.paymentStatus = 'PENDING'")
    List<Reservation> findUnpaidReservations();

    /**
     * 统计用户预订总数
     * @param userId 用户ID
     * @return 预订总数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户有效预订数
     * @param userId 用户ID
     * @return 有效预订数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.status = 'ACTIVE'")
    long countActiveReservationsByUserId(@Param("userId") Long userId);

    /**
     * 统计座位预订总数
     * @param seatId 座位ID
     * @return 预订总数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.seat.id = :seatId")
    long countBySeatId(@Param("seatId") Long seatId);

    /**
     * 计算用户总消费金额
     * @param userId 用户ID
     * @return 总消费金额
     */
    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.user.id = :userId AND r.paymentStatus = 'PAID'")
    BigDecimal calculateTotalAmountByUserId(@Param("userId") Long userId);

    /**
     * 查找指定日期范围内的收入
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收入
     */
    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE " +
           "r.createdAt >= :startDate AND r.createdAt <= :endDate AND r.paymentStatus = 'PAID'")
    BigDecimal calculateRevenueInDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 查找用户在指定时间段的预订历史
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预订历史列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND " +
           "r.createdAt >= :startDate AND r.createdAt <= :endDate ORDER BY r.createdAt DESC")
    List<Reservation> findUserReservationHistory(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * 按创建时间倒序查找用户最近的预订
     * @param userId 用户ID
     * @return 预订列表（按时间倒序）
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Reservation> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 检查用户在指定时间段是否有冲突预订
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否有冲突
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.user.id = :userId AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime)) AND " +
           "r.status IN ('ACTIVE', 'RESERVED')")
    boolean hasConflictingReservation(@Param("userId") Long userId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);
} 