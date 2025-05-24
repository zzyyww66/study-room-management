package com.studyroom.server.service.impl;

import com.studyroom.server.entity.Reservation;
import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.User;
import com.studyroom.server.repository.ReservationRepository;
import com.studyroom.server.repository.SeatRepository;
import com.studyroom.server.repository.UserRepository;
import com.studyroom.server.service.ReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预订服务实现类
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeatRepository seatRepository;

    @Override
    public Reservation createReservation(Long userId, Long seatId, LocalDateTime startTime, 
                                       LocalDateTime endTime, String notes) {
        // 验证用户和座位存在
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        if (!seatOpt.isPresent()) {
            throw new RuntimeException("座位不存在");
        }
        
        User user = userOpt.get();
        Seat seat = seatOpt.get();
        
        // 检查时间有效性
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("开始时间不能早于当前时间");
        }
        
        // 检查时间冲突
        if (hasTimeConflict(seatId, startTime, endTime, null)) {
            throw new RuntimeException("预订时间与现有预订冲突");
        }
        
        // 计算费用
        BigDecimal totalAmount = calculateReservationCost(seatId, startTime, endTime);
        
        // 创建预订
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSeat(seat);
        reservation.setReservationCode("R" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        reservation.setPaymentStatus(Reservation.PaymentStatus.PENDING);
        reservation.setNotes(notes);
        
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findByReservationCode(String reservationCode) {
        return reservationRepository.findByReservationCode(reservationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findActiveReservationsByUser(Long userId) {
        return reservationRepository.findActiveReservationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsBySeat(Long seatId) {
        return reservationRepository.findBySeatId(seatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findTodayReservations() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return reservationRepository.findTodayReservations(startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findActiveReservations() {
        return reservationRepository.findActiveReservations();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTimeConflict(Long seatId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId) {
        List<Reservation> conflictingReservations = reservationRepository.findConflictingReservations(
            seatId, startTime, endTime);
        
        if (excludeReservationId != null) {
            conflictingReservations = conflictingReservations.stream()
                .filter(r -> !r.getId().equals(excludeReservationId))
                .collect(Collectors.toList());
        }
        
        return !conflictingReservations.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateReservationCost(Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (!seatOpt.isPresent()) {
            return BigDecimal.ZERO;
        }
        
        // 计算时长（小时）
        long hours = Duration.between(startTime, endTime).toHours();
        if (hours < 1) {
            hours = 1; // 最少按1小时计费
        }
        
        // 根据自习室的每小时价格计算
        Seat seat = seatOpt.get();
        BigDecimal hourlyRate = seat.getStudyRoom().getHourlyRate();
        
        // 根据座位类型调整价格
        BigDecimal multiplier = BigDecimal.ONE;
        switch (seat.getType()) {
            case VIP:
                multiplier = new BigDecimal("1.5");
                break;
            case QUIET:
                multiplier = new BigDecimal("1.2");
                break;
            case GROUP:
                multiplier = new BigDecimal("1.3");
                break;
            default:
                multiplier = BigDecimal.ONE;
        }
        
        return hourlyRate.multiply(BigDecimal.valueOf(hours)).multiply(multiplier);
    }

    @Override
    public Reservation updateReservation(Long reservationId, LocalDateTime startTime, 
                                       LocalDateTime endTime, String notes) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (!reservationOpt.isPresent()) {
            throw new RuntimeException("预订不存在");
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 检查预订状态
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            throw new RuntimeException("只能修改有效的预订");
        }
        
        // 检查时间有效性
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }
        
        // 检查时间冲突（排除当前预订）
        if (hasTimeConflict(reservation.getSeat().getId(), startTime, endTime, reservationId)) {
            throw new RuntimeException("新的预订时间与其他预订冲突");
        }
        
        // 重新计算费用
        BigDecimal newAmount = calculateReservationCost(
            reservation.getSeat().getId(), startTime, endTime);
        
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setTotalAmount(newAmount);
        reservation.setNotes(notes);
        
        return reservationRepository.save(reservation);
    }

    @Override
    public boolean cancelReservation(Long reservationId, String cancelReason) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (!reservationOpt.isPresent()) {
            return false;
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 检查是否可以取消
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
            reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            return false;
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setNotes(reservation.getNotes() + "\n取消原因: " + cancelReason);
        
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public boolean payForReservation(Long reservationId, String paymentMethod) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (!reservationOpt.isPresent()) {
            return false;
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 检查预订状态
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE ||
            reservation.getPaymentStatus() == Reservation.PaymentStatus.PAID) {
            return false;
        }
        
        reservation.setPaymentStatus(Reservation.PaymentStatus.PAID);
        
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public boolean checkIn(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (!reservationOpt.isPresent()) {
            return false;
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 检查预订状态和支付状态
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE ||
            reservation.getPaymentStatus() != Reservation.PaymentStatus.PAID) {
            return false;
        }
        
        // 检查是否在预订时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(reservation.getStartTime()) || now.isAfter(reservation.getEndTime())) {
            return false;
        }
        
        reservation.setCheckInTime(now);
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public boolean checkOut(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (!reservationOpt.isPresent()) {
            return false;
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 检查是否已签到
        if (reservation.getCheckInTime() == null) {
            return false;
        }
        
        reservation.setCheckOutTime(LocalDateTime.now());
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public boolean extendReservation(Long reservationId, LocalDateTime newEndTime) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (!reservationOpt.isPresent()) {
            return false;
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 检查预订状态
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            return false;
        }
        
        // 检查新结束时间是否合理
        if (newEndTime.isBefore(reservation.getEndTime())) {
            return false;
        }
        
        // 检查延长时间是否有冲突
        if (hasTimeConflict(reservation.getSeat().getId(), 
                           reservation.getStartTime(), newEndTime, reservationId)) {
            return false;
        }
        
        // 计算额外费用
        BigDecimal additionalCost = calculateReservationCost(
            reservation.getSeat().getId(), reservation.getEndTime(), newEndTime);
        
        reservation.setEndTime(newEndTime);
        reservation.setTotalAmount(reservation.getTotalAmount().add(additionalCost));
        
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findExpiredUnpaidReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(now);
        return expiredReservations.stream()
            .filter(r -> r.getPaymentStatus() == Reservation.PaymentStatus.PENDING)
            .collect(Collectors.toList());
    }

    @Override
    public int cancelExpiredReservations() {
        List<Reservation> expiredReservations = findExpiredUnpaidReservations();
        int canceledCount = 0;
        
        for (Reservation reservation : expiredReservations) {
            if (cancelReservation(reservation.getId(), "系统自动取消：超时未支付")) {
                canceledCount++;
            }
        }
        
        return canceledCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsExpiringWithin(int minutesBefore) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.plusMinutes(minutesBefore);
        return reservationRepository.findUpcomingExpirations(now, cutoffTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserReservationStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Reservation> userReservations = findReservationsByUser(userId);
        stats.put("totalReservations", userReservations.size());
        
        // 按状态统计
        Map<String, Long> statusStats = userReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStatus().toString(),
                Collectors.counting()
            ));
        stats.put("statusStats", statusStats);
        
        // 计算总消费
        BigDecimal totalSpent = userReservations.stream()
            .filter(r -> r.getPaymentStatus() == Reservation.PaymentStatus.PAID)
            .map(Reservation::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalSpent", totalSpent);
        
        // 最近的预订
        Optional<Reservation> latestReservation = userReservations.stream()
            .max((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
        stats.put("latestReservation", latestReservation.orElse(null));
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSeatReservationStatistics(Long seatId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Reservation> seatReservations = findReservationsBySeat(seatId);
        stats.put("totalReservations", seatReservations.size());
        
        // 计算利用率（最近30天）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Reservation> recentReservations = seatReservations.stream()
            .filter(r -> r.getCreatedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.toList());
        
        // 计算总使用时长
        long totalHours = recentReservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
            .mapToLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toHours())
            .sum();
        
        stats.put("recentReservations", recentReservations.size());
        stats.put("totalUsageHours", totalHours);
        
        // 平均使用时长
        double avgHours = recentReservations.isEmpty() ? 0 : 
            (double) totalHours / recentReservations.size();
        stats.put("averageUsageHours", avgHours);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemReservationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Reservation> allReservations = reservationRepository.findAll();
        stats.put("totalReservations", allReservations.size());
        
        // 按状态统计
        Map<String, Long> statusStats = allReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStatus().toString(),
                Collectors.counting()
            ));
        stats.put("statusStats", statusStats);
        
        // 今日预订
        List<Reservation> todayReservations = findTodayReservations();
        stats.put("todayReservations", todayReservations.size());
        
        // 活跃预订
        List<Reservation> activeReservations = findActiveReservations();
        stats.put("activeReservations", activeReservations.size());
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // 使用现有方法获取指定时间范围内的收入
        BigDecimal totalRevenue = reservationRepository.calculateRevenueInDateRange(startDate, endDate);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // 获取时间范围内的所有预订来计算其他统计信息
        List<Reservation> periodReservations = reservationRepository.findReservationsBetween(startDate, endDate);
        
        // 待收收入
        BigDecimal pendingRevenue = periodReservations.stream()
            .filter(r -> r.getPaymentStatus() == Reservation.PaymentStatus.PENDING)
            .map(Reservation::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingRevenue", pendingRevenue);
        
        // 预订数量
        stats.put("totalReservations", periodReservations.size());
        
        // 平均订单金额
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (!periodReservations.isEmpty()) {
            BigDecimal totalAmount = periodReservations.stream()
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgOrderValue = totalAmount.divide(BigDecimal.valueOf(periodReservations.size()), 2, BigDecimal.ROUND_HALF_UP);
        }
        stats.put("averageOrderValue", avgOrderValue);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findReservationsWithPagination(int page, int size, Long userId, 
                                                           Reservation.ReservationStatus status, 
                                                           Reservation.PaymentStatus paymentStatus) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // 获取所有预订，然后在内存中过滤
        List<Reservation> allReservations = reservationRepository.findAll();
        
        // 应用过滤条件
        List<Reservation> filteredReservations = allReservations.stream()
            .filter(r -> userId == null || r.getUser().getId().equals(userId))
            .filter(r -> status == null || r.getStatus() == status)
            .filter(r -> paymentStatus == null || r.getPaymentStatus() == paymentStatus)
            .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
            .collect(Collectors.toList());
        
        // 手动分页
        int start = page * size;
        int end = Math.min(start + size, filteredReservations.size());
        List<Reservation> pageContent = filteredReservations.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, filteredReservations.size());
    }
} 