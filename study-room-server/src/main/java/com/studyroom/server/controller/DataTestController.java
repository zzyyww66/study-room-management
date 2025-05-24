package com.studyroom.server.controller;

import com.studyroom.server.entity.User;
import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.repository.UserRepository;
import com.studyroom.server.repository.StudyRoomRepository;
import com.studyroom.server.repository.SeatRepository;
import com.studyroom.server.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库测试控制器
 * 用于验证数据初始化和Repository层功能
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/test")
public class DataTestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 获取数据库统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各表数据量
        stats.put("totalUsers", userRepository.count());
        stats.put("totalStudyRooms", studyRoomRepository.count());
        stats.put("totalSeats", seatRepository.count());
        stats.put("totalReservations", reservationRepository.count());
        
        // 按状态统计
        stats.put("activeUsers", userRepository.findActiveUsers().size());
        stats.put("availableRooms", studyRoomRepository.findAvailableRooms().size());
        stats.put("availableSeats", seatRepository.findAvailableSeats().size());
        stats.put("activeReservations", reservationRepository.findActiveReservations().size());
        
        // 按角色统计用户
        stats.put("adminUsers", userRepository.countByRole(User.UserRole.ADMIN));
        stats.put("regularUsers", userRepository.countByRole(User.UserRole.USER));
        
        return stats;
    }

    /**
     * 获取所有用户信息
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据用户名查找用户
     */
    @GetMapping("/users/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 获取所有自习室信息
     */
    @GetMapping("/study-rooms")
    public List<StudyRoom> getAllStudyRooms() {
        return studyRoomRepository.findAll();
    }

    /**
     * 获取可用的自习室
     */
    @GetMapping("/study-rooms/available")
    public List<StudyRoom> getAvailableStudyRooms() {
        return studyRoomRepository.findAvailableRooms();
    }

    /**
     * 根据自习室ID获取座位
     */
    @GetMapping("/study-rooms/{roomId}/seats")
    public List<Seat> getSeatsByStudyRoom(@PathVariable Long roomId) {
        return seatRepository.findByStudyRoomId(roomId);
    }

    /**
     * 获取可用座位
     */
    @GetMapping("/seats/available")
    public List<Seat> getAvailableSeats() {
        return seatRepository.findAvailableSeats();
    }

    /**
     * 根据自习室ID获取可用座位
     */
    @GetMapping("/study-rooms/{roomId}/seats/available")
    public List<Seat> getAvailableSeatsByRoom(@PathVariable Long roomId) {
        return seatRepository.findAvailableSeatsByStudyRoomId(roomId);
    }

    /**
     * 获取所有预订信息
     */
    @GetMapping("/reservations")
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * 获取有效预订
     */
    @GetMapping("/reservations/active")
    public List<Reservation> getActiveReservations() {
        return reservationRepository.findActiveReservations();
    }

    /**
     * 根据用户ID获取预订
     */
    @GetMapping("/users/{userId}/reservations")
    public List<Reservation> getReservationsByUser(@PathVariable Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * 获取今日预订
     */
    @GetMapping("/reservations/today")
    public List<Reservation> getTodayReservations() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return reservationRepository.findTodayReservations(startOfDay, endOfDay);
    }

    /**
     * 测试复杂查询 - 查找有窗户和电源的可用座位
     */
    @GetMapping("/seats/features")
    public List<Seat> getSeatsWithWindowAndPower() {
        return seatRepository.findAvailableSeatsWithFeatures(true, true, null);
    }

    /**
     * 测试Repository的自定义查询方法
     */
    @GetMapping("/test-queries")
    public Map<String, Object> testCustomQueries() {
        Map<String, Object> results = new HashMap<>();
        
        // 测试用户相关查询
        results.put("usersByRole_ADMIN", userRepository.findByRole(User.UserRole.ADMIN));
        results.put("usersByStatus_ACTIVE", userRepository.findByStatus(User.UserStatus.ACTIVE));
        results.put("usernameExists_admin", userRepository.existsByUsername("admin"));
        
        // 测试自习室相关查询
        results.put("roomsByName_contains_自习", studyRoomRepository.findByNameContaining("自习"));
        results.put("roomsOrderByRate", studyRoomRepository.findAllByOrderByHourlyRateAsc());
        
        // 测试座位相关查询
        results.put("windowSeats", seatRepository.findByHasWindowTrue());
        results.put("vipSeats", seatRepository.findByType(Seat.SeatType.VIP));
        results.put("seatsInRoom1", seatRepository.countByStudyRoomId(1L));
        
        // 测试预订相关查询
        results.put("paidReservations", reservationRepository.findByPaymentStatus(Reservation.PaymentStatus.PAID));
        results.put("pendingReservations", reservationRepository.findByPaymentStatus(Reservation.PaymentStatus.PENDING));
        
        return results;
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 简单的数据库连接测试
            long userCount = userRepository.count();
            result.put("status", "SUCCESS");
            result.put("message", "数据库连接正常");
            result.put("userCount", userCount);
            result.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "数据库连接失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * 简单的数据量统计
     */
    @GetMapping("/simple-stats")
    public Map<String, Object> getSimpleStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各表数据量
        stats.put("totalUsers", userRepository.count());
        stats.put("totalStudyRooms", studyRoomRepository.count());
        stats.put("totalSeats", seatRepository.count());
        stats.put("totalReservations", reservationRepository.count());
        
        return stats;
    }
} 