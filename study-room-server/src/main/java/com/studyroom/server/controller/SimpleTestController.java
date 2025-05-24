package com.studyroom.server.controller;

import com.studyroom.server.entity.User;
import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.service.UserService;
import com.studyroom.server.service.StudyRoomService;
import com.studyroom.server.service.SeatService;
import com.studyroom.server.service.ReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 简单测试控制器 - 扩展版
 * 用于验证基本功能和Service层测试
 */
@RestController
@RequestMapping("/simple-test")
public class SimpleTestController {

    @Autowired(required = false)
    private UserService userService;
    
    @Autowired(required = false)
    private StudyRoomService studyRoomService;
    
    @Autowired(required = false)
    private SeatService seatService;
    
    @Autowired(required = false)
    private ReservationService reservationService;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Study Room Management System!");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("userServiceExists", userService != null);
        response.put("studyRoomServiceExists", studyRoomService != null);
        response.put("seatServiceExists", seatService != null);
        response.put("reservationServiceExists", reservationService != null);
        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("serverStatus", "运行中");
        
        if (userService != null) {
            try {
                List<User> activeUsers = userService.findActiveUsers();
                response.put("userService", "OK - 找到 " + activeUsers.size() + " 个活跃用户");
            } catch (Exception e) {
                response.put("userService", "ERROR: " + e.getMessage());
            }
        } else {
            response.put("userService", "NOT_AVAILABLE");
        }
        
        if (studyRoomService != null) {
            try {
                List<StudyRoom> availableRooms = studyRoomService.findAvailableRooms();
                response.put("studyRoomService", "OK - 找到 " + availableRooms.size() + " 个可用自习室");
            } catch (Exception e) {
                response.put("studyRoomService", "ERROR: " + e.getMessage());
            }
        } else {
            response.put("studyRoomService", "NOT_AVAILABLE");
        }
        
        if (seatService != null) {
            try {
                List<Seat> availableSeats = seatService.findAvailableSeats();
                response.put("seatService", "OK - 找到 " + availableSeats.size() + " 个可用座位");
            } catch (Exception e) {
                response.put("seatService", "ERROR: " + e.getMessage());
            }
        } else {
            response.put("seatService", "NOT_AVAILABLE");
        }
        
        if (reservationService != null) {
            try {
                List<Reservation> activeReservations = reservationService.findActiveReservations();
                response.put("reservationService", "OK - 找到 " + activeReservations.size() + " 个活跃预订");
            } catch (Exception e) {
                response.put("reservationService", "ERROR: " + e.getMessage());
            }
        } else {
            response.put("reservationService", "NOT_AVAILABLE");
        }
        
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        
        boolean allServicesReady = userService != null && studyRoomService != null 
                                 && seatService != null && reservationService != null;
        
        response.put("allServicesReady", allServicesReady);
        response.put("userServiceReady", userService != null);
        response.put("studyRoomServiceReady", studyRoomService != null);
        response.put("seatServiceReady", seatService != null);
        response.put("reservationServiceReady", reservationService != null);
        response.put("status", allServicesReady ? "SUCCESS" : "PARTIAL");
        
        return response;
    }

    @GetMapping("/users/basic")
    public Map<String, Object> testUserServiceBasic() {
        Map<String, Object> response = new HashMap<>();
        
        if (userService == null) {
            response.put("error", "UserService 不可用");
            return response;
        }
        
        try {
            // 测试基本功能
            List<User> activeUsers = userService.findActiveUsers();
            response.put("activeUsersCount", activeUsers.size());
            
            // 测试认证
            User authenticatedUser = userService.authenticateUser("admin", "admin123");
            response.put("adminAuthentication", authenticatedUser != null ? "成功" : "失败");
            
            // 测试邮箱检查
            boolean emailAvailable = userService.isEmailAvailable("test@example.com");
            response.put("testEmailAvailable", emailAvailable);
            
            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/users/{userId}/statistics")
    public Map<String, Object> testUserStatistics(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        if (userService == null) {
            response.put("error", "UserService 不可用");
            return response;
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString(),
                    "status", user.getStatus().toString()
                ));
                
                Map<String, Object> stats = userService.getUserStatistics(userId);
                response.put("statistics", stats);
                response.put("status", "SUCCESS");
            } else {
                response.put("error", "用户不存在");
                response.put("status", "NOT_FOUND");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/studyrooms/basic")
    public Map<String, Object> testStudyRoomServiceBasic() {
        Map<String, Object> response = new HashMap<>();
        
        if (studyRoomService == null) {
            response.put("error", "StudyRoomService 不可用");
            return response;
        }
        
        try {
            // 测试基本功能
            List<StudyRoom> availableRooms = studyRoomService.findAvailableRooms();
            response.put("availableRoomsCount", availableRooms.size());
            
            // 测试排序功能
            List<StudyRoom> roomsByPrice = studyRoomService.findRoomsOrderByPrice(true);
            response.put("roomsByPriceCount", roomsByPrice.size());
            
            List<StudyRoom> roomsByCapacity = studyRoomService.findRoomsOrderByCapacity(false);
            response.put("roomsByCapacityCount", roomsByCapacity.size());
            
            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/studyrooms/{roomId}/statistics")
    public Map<String, Object> testStudyRoomStatistics(@PathVariable Long roomId) {
        Map<String, Object> response = new HashMap<>();
        
        if (studyRoomService == null) {
            response.put("error", "StudyRoomService 不可用");
            return response;
        }
        
        try {
            Optional<StudyRoom> roomOpt = studyRoomService.findById(roomId);
            if (roomOpt.isPresent()) {
                StudyRoom room = roomOpt.get();
                response.put("studyRoom", Map.of(
                    "id", room.getId(),
                    "name", room.getName(),
                    "capacity", room.getCapacity(),
                    "hourlyRate", room.getHourlyRate(),
                    "status", room.getStatus().toString()
                ));
                
                Map<String, Object> stats = studyRoomService.getRoomStatistics(roomId);
                response.put("statistics", stats);
                response.put("status", "SUCCESS");
            } else {
                response.put("error", "自习室不存在");
                response.put("status", "NOT_FOUND");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/studyrooms/utilization")
    public Map<String, Object> testStudyRoomUtilization() {
        Map<String, Object> response = new HashMap<>();
        
        if (studyRoomService == null) {
            response.put("error", "StudyRoomService 不可用");
            return response;
        }
        
        try {
            List<Map<String, Object>> utilization = studyRoomService.getRoomsUtilizationStats();
            response.put("utilization", utilization);
            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/seats/basic")
    public Map<String, Object> testSeatServiceBasic() {
        Map<String, Object> response = new HashMap<>();
        
        if (seatService == null) {
            response.put("error", "SeatService 不可用");
            return response;
        }
        
        try {
            // 测试基本功能
            List<Seat> availableSeats = seatService.findAvailableSeats();
            response.put("availableSeatsCount", availableSeats.size());
            
            // 测试特征筛选
            List<Seat> windowSeats = seatService.findSeatsWithWindow();
            response.put("windowSeatsCount", windowSeats.size());
            
            // 测试按类型查找
            List<Seat> vipSeats = seatService.findSeatsByType(Seat.SeatType.VIP);
            response.put("vipSeatsCount", vipSeats.size());
            
            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/seats/{seatId}/statistics")
    public Map<String, Object> testSeatStatistics(@PathVariable Long seatId) {
        Map<String, Object> response = new HashMap<>();
        
        if (seatService == null) {
            response.put("error", "SeatService 不可用");
            return response;
        }
        
        try {
            Optional<Seat> seatOpt = seatService.findById(seatId);
            if (seatOpt.isPresent()) {
                Seat seat = seatOpt.get();
                response.put("seat", Map.of(
                    "id", seat.getId(),
                    "seatNumber", seat.getSeatNumber(),
                    "type", seat.getType().toString(),
                    "status", seat.getStatus().toString(),
                    "hasWindow", seat.getHasWindow(),
                    "hasPowerOutlet", seat.getHasPowerOutlet(),
                    "hasLamp", seat.getHasLamp()
                ));
                
                Map<String, Object> stats = seatService.getSeatStatistics(seatId);
                response.put("statistics", stats);
                response.put("status", "SUCCESS");
            } else {
                response.put("error", "座位不存在");
                response.put("status", "NOT_FOUND");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/reservations/basic")
    public Map<String, Object> testReservationServiceBasic() {
        Map<String, Object> response = new HashMap<>();
        
        if (reservationService == null) {
            response.put("error", "ReservationService 不可用");
            return response;
        }
        
        try {
            // 测试基本功能
            List<Reservation> activeReservations = reservationService.findActiveReservations();
            response.put("activeReservationsCount", activeReservations.size());
            
            // 测试今日预订
            List<Reservation> todayReservations = reservationService.findTodayReservations();
            response.put("todayReservationsCount", todayReservations.size());
            
            // 测试统计功能
            Map<String, Object> systemStats = reservationService.getSystemReservationStatistics();
            response.put("systemStatistics", systemStats);
            
            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }

    @GetMapping("/reservations/{reservationId}/details")
    public Map<String, Object> testReservationDetails(@PathVariable Long reservationId) {
        Map<String, Object> response = new HashMap<>();
        
        if (reservationService == null) {
            response.put("error", "ReservationService 不可用");
            return response;
        }
        
        try {
            Optional<Reservation> reservationOpt = reservationService.findById(reservationId);
            if (reservationOpt.isPresent()) {
                Reservation reservation = reservationOpt.get();
                response.put("reservation", Map.of(
                    "id", reservation.getId(),
                    "reservationCode", reservation.getReservationCode(),
                    "status", reservation.getStatus().toString(),
                    "paymentStatus", reservation.getPaymentStatus().toString(),
                    "startTime", reservation.getStartTime(),
                    "endTime", reservation.getEndTime(),
                    "totalAmount", reservation.getTotalAmount()
                ));
                response.put("status", "SUCCESS");
            } else {
                response.put("error", "预订不存在");
                response.put("status", "NOT_FOUND");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return response;
    }
} 