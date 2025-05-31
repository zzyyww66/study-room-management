package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
import com.studyroom.server.service.UserService;
import com.studyroom.server.service.StudyRoomService;
import com.studyroom.server.service.SeatService;
import com.studyroom.server.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计信息控制器
 * 提供系统级别的数据分析和统计信息API
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 获取系统总体统计信息
     * GET /api/statistics/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 获取各类统计数据
            Map<String, Object> userStats = userService.getUserStatistics(null); // 系统级用户统计
            Map<String, Object> reservationStats = reservationService.getSystemReservationStatistics();
            List<Map<String, Object>> roomUtilization = studyRoomService.getRoomsUtilizationStats();
            
            // 汇总数据
            overview.put("userStatistics", userStats);
            overview.put("reservationStatistics", reservationStats);
            overview.put("roomUtilization", roomUtilization);
            overview.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(overview, "获取系统概览成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取系统概览失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户相关统计
     * GET /api/statistics/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        try {
            // 这里可以调用UserService的系统级统计方法
            // 由于UserService.getUserStatistics(userId)是针对特定用户的，
            // 我们需要实现系统级的用户统计
            
            Map<String, Object> userStats = new HashMap<>();
            // 可以通过UserService获取活跃用户等基础数据
            var activeUsers = userService.findActiveUsers();
            userStats.put("totalActiveUsers", activeUsers.size());
            userStats.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(userStats, "获取用户统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取自习室利用率统计
     * GET /api/statistics/rooms/utilization
     */
    @GetMapping("/rooms/utilization")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomsUtilization() {
        try {
            List<Map<String, Object>> utilizationStatsList = studyRoomService.getRoomsUtilizationStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("roomUtilization", utilizationStatsList);
            responseData.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(responseData, "获取自习室利用率统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室利用率统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取座位使用统计
     * GET /api/statistics/seats
     */
    @GetMapping("/seats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatsStatistics() {
        try {
            // 获取所有可用座位
            var availableSeats = seatService.findAvailableSeats();
            var windowSeats = seatService.findSeatsWithWindow();

            Map<String, Object> seatStats = new HashMap<>();
            seatStats.put("totalAvailableSeats", availableSeats.size());
            seatStats.put("windowSeats", windowSeats.size());
            seatStats.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(seatStats, "获取座位统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取预订统计
     * GET /api/statistics/reservations
     */
    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservationStatistics() {
        try {
            Map<String, Object> reservationStats = reservationService.getSystemReservationStatistics();
            // reservationStats already contains generatedAt, if needed, or rely on ApiResponse timestamp
            return ResponseEntity.ok(ApiResponse.success(reservationStats, "获取预订统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取收入统计报告
     * GET /api/statistics/revenue?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate);
            LocalDateTime endDateTime = LocalDateTime.parse(endDate);

            Map<String, Object> revenueStatsData = reservationService.getRevenueStatistics(startDateTime, endDateTime);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("revenueReport", revenueStatsData);
            responseData.put("period", Map.of(
                "startDate", startDate,
                "endDate", endDate
            ));
            responseData.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(responseData, "获取收入报告成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取收入报告失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取今日运营数据
     * GET /api/statistics/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayStatistics() {
        try {
            Map<String, Object> todayStats = new HashMap<>();
            
            // 今日预订
            var todayReservations = reservationService.findTodayReservations();
            todayStats.put("todayReservationsCount", todayReservations.size());
            
            // 活跃预订
            var activeReservations = reservationService.findActiveReservations();
            todayStats.put("activeReservationsCount", activeReservations.size());
            
            // 可用座位
            var availableSeats = seatService.findAvailableSeats();
            todayStats.put("availableSeatsCount", availableSeats.size());
            
            // 可用自习室
            var availableRooms = studyRoomService.findAvailableRooms();
            todayStats.put("availableRoomsCount", availableRooms.size());

            todayStats.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(todayStats, "获取今日统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取今日统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取实时状态统计
     * GET /api/statistics/realtime
     */
    @GetMapping("/realtime")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealtimeStatistics() {
        try {
            Map<String, Object> realtimeStats = new HashMap<>();
            
            // 当前活跃预订
            var activeReservations = reservationService.findActiveReservations();
            realtimeStats.put("currentActiveReservations", activeReservations.size());
            
            // 当前可用座位
            var availableSeats = seatService.findAvailableSeats();
            realtimeStats.put("currentAvailableSeats", availableSeats.size());
            
            // 即将到期的预订（30分钟内）
            var expiringReservations = reservationService.findReservationsExpiringWithin(30);
            realtimeStats.put("reservationsExpiringWithin30Minutes", expiringReservations.size());
            
            // 过期未支付预订
            var expiredUnpaidReservations = reservationService.findExpiredUnpaidReservations();
            realtimeStats.put("expiredUnpaidReservations", expiredUnpaidReservations.size());

            realtimeStats.put("timestamp", LocalDateTime.now()); // This is fine, or use ApiResponse's timestamp

            return ResponseEntity.ok(ApiResponse.success(realtimeStats, "获取实时统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取实时统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取详细的系统健康状况
     * GET /api/statistics/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        try {
            Map<String, Object> healthStats = new HashMap<>();
            
            // 检查各个服务的可用性
            healthStats.put("userServiceAvailable", userService != null);
            healthStats.put("studyRoomServiceAvailable", studyRoomService != null);
            healthStats.put("seatServiceAvailable", seatService != null);
            healthStats.put("reservationServiceAvailable", reservationService != null);
            
            // 获取基础数据量
            try {
                var activeUsers = userService.findActiveUsers();
                healthStats.put("activeUsersCount", activeUsers.size());
            } catch (Exception e) {
                healthStats.put("activeUsersCount", "N/A");
                healthStats.put("userServiceError", e.getMessage());
            }
            
            try {
                var availableRooms = studyRoomService.findAvailableRooms();
                healthStats.put("availableRoomsCount", availableRooms.size());
            } catch (Exception e) {
                healthStats.put("availableRoomsCount", "N/A");
                healthStats.put("studyRoomServiceError", e.getMessage());
            }
            
            try {
                var availableSeats = seatService.findAvailableSeats();
                healthStats.put("availableSeatsCount", availableSeats.size());
            } catch (Exception e) {
                healthStats.put("availableSeatsCount", "N/A");
                healthStats.put("seatServiceError", e.getMessage());
            }
            
            try {
                var activeReservations = reservationService.findActiveReservations();
                healthStats.put("activeReservationsCount", activeReservations.size());
            } catch (Exception e) {
                healthStats.put("activeReservationsCount", "N/A");
                healthStats.put("reservationServiceError", e.getMessage());
            }

            healthStats.put("systemStatus", "OPERATIONAL");
            healthStats.put("checkTime", LocalDateTime.now()); // This is fine, or use ApiResponse's timestamp

            return ResponseEntity.ok(ApiResponse.success(healthStats, "获取系统健康状况成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取系统健康状况失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取特定自习室的详细统计
     * GET /api/statistics/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomDetailedStatistics(@PathVariable Long roomId) {
        try {
            Map<String, Object> roomStats = studyRoomService.getRoomStatistics(roomId);
            Map<String, Object> seatStats = seatService.getStudyRoomSeatStatistics(roomId);

            Map<String, Object> detailedStats = new HashMap<>();
            detailedStats.put("roomStatistics", roomStats);
            detailedStats.put("seatStatistics", seatStats);
            detailedStats.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(detailedStats, "获取自习室详细统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室详细统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取特定用户的详细统计
     * GET /api/statistics/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetailedStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> userStats = userService.getUserStatistics(userId);
            Map<String, Object> reservationStats = reservationService.getUserReservationStatistics(userId);

            Map<String, Object> detailedStats = new HashMap<>();
            detailedStats.put("userStatistics", userStats);
            detailedStats.put("reservationStatistics", reservationStats);
            detailedStats.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(detailedStats, "获取用户详细统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户详细统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取特定座位的详细统计
     * GET /api/statistics/seat/{seatId}
     */
    @GetMapping("/seat/{seatId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatDetailedStatistics(@PathVariable Long seatId) {
        try {
            Map<String, Object> seatStats = seatService.getSeatStatistics(seatId);
            Map<String, Object> reservationStats = reservationService.getSeatReservationStatistics(seatId);

            Map<String, Object> detailedStats = new HashMap<>();
            detailedStats.put("seatStatistics", seatStats);
            detailedStats.put("reservationStatistics", reservationStats);
            detailedStats.put("generatedAt", LocalDateTime.now()); // This could be part of ApiResponse timestamp

            return ResponseEntity.ok(ApiResponse.success(detailedStats, "获取座位详细统计成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位详细统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // 工具方法：创建错误响应
    private ApiResponse<Object> createErrorResponse(String message, int httpStatusCode) {
        return ApiResponse.error(httpStatusCode, message);
    }
}