package com.studyroom.server.controller;

import com.studyroom.server.entity.Reservation;
import com.studyroom.server.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预订管理控制器
 * 处理预订管理相关的API请求
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 创建新预订
     * POST /api/reservations
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createReservation(@RequestBody Map<String, Object> reservationRequest) {
        try {
            Long userId = Long.valueOf(reservationRequest.get("userId").toString());
            Long seatId = Long.valueOf(reservationRequest.get("seatId").toString());
            String startTimeStr = (String) reservationRequest.get("startTime");
            String endTimeStr = (String) reservationRequest.get("endTime");
            String notes = (String) reservationRequest.get("notes");
            
            if (userId == null || seatId == null || startTimeStr == null || endTimeStr == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("用户ID、座位ID、开始时间和结束时间不能为空", "MISSING_REQUIRED_FIELDS"));
            }
            
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            
            // 检查时间冲突
            if (reservationService.hasTimeConflict(seatId, startTime, endTime, null)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("预订时间与已有预订冲突", "TIME_CONFLICT"));
            }
            
            Reservation reservation = reservationService.createReservation(userId, seatId, startTime, endTime, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预订创建成功");
            response.put("reservation", reservation);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("创建预订失败: " + e.getMessage(), "CREATE_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 获取预订详细信息
     * GET /api/reservations/{reservationId}
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> getReservationById(@PathVariable Long reservationId) {
        try {
            var reservationOpt = reservationService.findById(reservationId);
            if (reservationOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("reservation", reservationOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("预订不存在", "RESERVATION_NOT_FOUND"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订信息失败: " + e.getMessage(), "GET_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 根据预订编码查找预订
     * GET /api/reservations/code/{reservationCode}
     */
    @GetMapping("/code/{reservationCode}")
    public ResponseEntity<Map<String, Object>> getReservationByCode(@PathVariable String reservationCode) {
        try {
            var reservationOpt = reservationService.findByReservationCode(reservationCode);
            if (reservationOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("reservation", reservationOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("预订不存在", "RESERVATION_NOT_FOUND"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("查找预订失败: " + e.getMessage(), "FIND_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 获取用户的所有预订
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReservationsByUser(@PathVariable Long userId) {
        try {
            List<Reservation> reservations = reservationService.findReservationsByUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reservations", reservations);
            response.put("count", reservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户预订失败: " + e.getMessage(), "GET_USER_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取用户的有效预订
     * GET /api/reservations/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Object>> getActiveReservationsByUser(@PathVariable Long userId) {
        try {
            List<Reservation> activeReservations = reservationService.findActiveReservationsByUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeReservations", activeReservations);
            response.put("count", activeReservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户有效预订失败: " + e.getMessage(), "GET_USER_ACTIVE_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取座位的所有预订
     * GET /api/reservations/seat/{seatId}
     */
    @GetMapping("/seat/{seatId}")
    public ResponseEntity<Map<String, Object>> getReservationsBySeat(@PathVariable Long seatId) {
        try {
            List<Reservation> reservations = reservationService.findReservationsBySeat(seatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reservations", reservations);
            response.put("count", reservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位预订失败: " + e.getMessage(), "GET_SEAT_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取今日预订
     * GET /api/reservations/today
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayReservations() {
        try {
            List<Reservation> todayReservations = reservationService.findTodayReservations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("todayReservations", todayReservations);
            response.put("count", todayReservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取今日预订失败: " + e.getMessage(), "GET_TODAY_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取有效预订
     * GET /api/reservations/active
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveReservations() {
        try {
            List<Reservation> activeReservations = reservationService.findActiveReservations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeReservations", activeReservations);
            response.put("count", activeReservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取有效预订失败: " + e.getMessage(), "GET_ACTIVE_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 分页查询预订
     * GET /api/reservations?page=0&size=10&userId=1&status=ACTIVE&paymentStatus=PAID
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus) {
        try {
            Reservation.ReservationStatus reservationStatus = null;
            Reservation.PaymentStatus payStatus = null;
            
            if (status != null && !status.isEmpty()) {
                reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            }
            if (paymentStatus != null && !paymentStatus.isEmpty()) {
                payStatus = Reservation.PaymentStatus.valueOf(paymentStatus.toUpperCase());
            }
            
            Page<Reservation> reservationPage = reservationService.findReservationsWithPagination(
                page, size, userId, reservationStatus, payStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reservations", reservationPage.getContent());
            response.put("totalElements", reservationPage.getTotalElements());
            response.put("totalPages", reservationPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", reservationPage.hasNext());
            response.put("hasPrevious", reservationPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), "INVALID_PARAMETER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订列表失败: " + e.getMessage(), "GET_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 检查预订时间冲突
     * POST /api/reservations/check-conflict
     */
    @PostMapping("/check-conflict")
    public ResponseEntity<Map<String, Object>> checkTimeConflict(@RequestBody Map<String, Object> conflictRequest) {
        try {
            Long seatId = Long.valueOf(conflictRequest.get("seatId").toString());
            String startTimeStr = (String) conflictRequest.get("startTime");
            String endTimeStr = (String) conflictRequest.get("endTime");
            Long excludeReservationId = null;
            
            if (conflictRequest.get("excludeReservationId") != null) {
                excludeReservationId = Long.valueOf(conflictRequest.get("excludeReservationId").toString());
            }
            
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            
            boolean hasConflict = reservationService.hasTimeConflict(seatId, startTime, endTime, excludeReservationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasConflict", hasConflict);
            response.put("message", hasConflict ? "存在时间冲突" : "无时间冲突");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查时间冲突失败: " + e.getMessage(), "CHECK_CONFLICT_ERROR"));
        }
    }
    
    /**
     * 计算预订费用
     * POST /api/reservations/calculate-cost
     */
    @PostMapping("/calculate-cost")
    public ResponseEntity<Map<String, Object>> calculateCost(@RequestBody Map<String, Object> costRequest) {
        try {
            Long seatId = Long.valueOf(costRequest.get("seatId").toString());
            String startTimeStr = (String) costRequest.get("startTime");
            String endTimeStr = (String) costRequest.get("endTime");
            
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            
            BigDecimal cost = reservationService.calculateReservationCost(seatId, startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cost", cost);
            response.put("currency", "CNY");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("计算费用失败: " + e.getMessage(), "CALCULATE_COST_ERROR"));
        }
    }
    
    /**
     * 更新预订信息
     * PUT /api/reservations/{reservationId}
     */
    @PutMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, Object> reservationRequest) {
        try {
            String startTimeStr = (String) reservationRequest.get("startTime");
            String endTimeStr = (String) reservationRequest.get("endTime");
            String notes = (String) reservationRequest.get("notes");
            
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                startTime = LocalDateTime.parse(startTimeStr);
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                endTime = LocalDateTime.parse(endTimeStr);
            }
            
            Reservation updatedReservation = reservationService.updateReservation(
                reservationId, startTime, endTime, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预订信息更新成功");
            response.put("reservation", updatedReservation);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新预订信息失败: " + e.getMessage(), "UPDATE_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 取消预订
     * PUT /api/reservations/{reservationId}/cancel
     */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> cancelRequest) {
        try {
            String cancelReason = cancelRequest.get("cancelReason");
            
            boolean success = reservationService.cancelReservation(reservationId, cancelReason);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "预订取消成功");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("预订取消失败", "CANCEL_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("取消预订失败: " + e.getMessage(), "CANCEL_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 支付预订
     * PUT /api/reservations/{reservationId}/pay
     */
    @PutMapping("/{reservationId}/pay")
    public ResponseEntity<Map<String, Object>> payForReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> paymentRequest) {
        try {
            String paymentMethod = paymentRequest.get("paymentMethod");
            
            boolean success = reservationService.payForReservation(reservationId, paymentMethod);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "支付成功");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("支付失败", "PAYMENT_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("支付处理失败: " + e.getMessage(), "PAY_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 办理入住（签到）
     * PUT /api/reservations/{reservationId}/check-in
     */
    @PutMapping("/{reservationId}/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable Long reservationId) {
        try {
            boolean success = reservationService.checkIn(reservationId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "签到成功");
                response.put("checkInTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("签到失败", "CHECK_IN_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("签到处理失败: " + e.getMessage(), "CHECK_IN_ERROR"));
        }
    }
    
    /**
     * 办理退房（签退）
     * PUT /api/reservations/{reservationId}/check-out
     */
    @PutMapping("/{reservationId}/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Long reservationId) {
        try {
            boolean success = reservationService.checkOut(reservationId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "签退成功");
                response.put("checkOutTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("签退失败", "CHECK_OUT_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("签退处理失败: " + e.getMessage(), "CHECK_OUT_ERROR"));
        }
    }
    
    /**
     * 延长预订时间
     * PUT /api/reservations/{reservationId}/extend
     */
    @PutMapping("/{reservationId}/extend")
    public ResponseEntity<Map<String, Object>> extendReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> extendRequest) {
        try {
            String newEndTimeStr = extendRequest.get("newEndTime");
            if (newEndTimeStr == null || newEndTimeStr.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("新的结束时间不能为空", "MISSING_END_TIME"));
            }
            
            LocalDateTime newEndTime = LocalDateTime.parse(newEndTimeStr);
            boolean success = reservationService.extendReservation(reservationId, newEndTime);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "预订延长成功");
                response.put("newEndTime", newEndTimeStr);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("预订延长失败", "EXTEND_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("延长预订失败: " + e.getMessage(), "EXTEND_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 获取过期未支付的预订
     * GET /api/reservations/expired-unpaid
     */
    @GetMapping("/expired-unpaid")
    public ResponseEntity<Map<String, Object>> getExpiredUnpaidReservations() {
        try {
            List<Reservation> expiredReservations = reservationService.findExpiredUnpaidReservations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("expiredUnpaidReservations", expiredReservations);
            response.put("count", expiredReservations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取过期未支付预订失败: " + e.getMessage(), "GET_EXPIRED_UNPAID_ERROR"));
        }
    }
    
    /**
     * 自动取消过期预订
     * POST /api/reservations/cancel-expired
     */
    @PostMapping("/cancel-expired")
    public ResponseEntity<Map<String, Object>> cancelExpiredReservations() {
        try {
            int cancelledCount = reservationService.cancelExpiredReservations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "过期预订自动取消完成");
            response.put("cancelledCount", cancelledCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("自动取消过期预订失败: " + e.getMessage(), "CANCEL_EXPIRED_ERROR"));
        }
    }
    
    /**
     * 获取即将到期的预订
     * GET /api/reservations/expiring-within/{minutes}
     */
    @GetMapping("/expiring-within/{minutes}")
    public ResponseEntity<Map<String, Object>> getReservationsExpiringWithin(@PathVariable int minutes) {
        try {
            List<Reservation> expiringReservations = reservationService.findReservationsExpiringWithin(minutes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("expiringReservations", expiringReservations);
            response.put("count", expiringReservations.size());
            response.put("minutesBefore", minutes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取即将到期预订失败: " + e.getMessage(), "GET_EXPIRING_RESERVATIONS_ERROR"));
        }
    }
    
    /**
     * 获取用户预订统计
     * GET /api/reservations/user/{userId}/statistics
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getUserReservationStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> statistics = reservationService.getUserReservationStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户预订统计失败: " + e.getMessage(), "GET_USER_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 获取座位预订统计
     * GET /api/reservations/seat/{seatId}/statistics
     */
    @GetMapping("/seat/{seatId}/statistics")
    public ResponseEntity<Map<String, Object>> getSeatReservationStatistics(@PathVariable Long seatId) {
        try {
            Map<String, Object> statistics = reservationService.getSeatReservationStatistics(seatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位预订统计失败: " + e.getMessage(), "GET_SEAT_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 获取系统预订统计
     * GET /api/reservations/statistics/system
     */
    @GetMapping("/statistics/system")
    public ResponseEntity<Map<String, Object>> getSystemReservationStatistics() {
        try {
            Map<String, Object> statistics = reservationService.getSystemReservationStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取系统预订统计失败: " + e.getMessage(), "GET_SYSTEM_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 获取收入统计
     * GET /api/reservations/statistics/revenue?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/statistics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate);
            LocalDateTime endDateTime = LocalDateTime.parse(endDate);
            
            Map<String, Object> statistics = reservationService.getRevenueStatistics(startDateTime, endDateTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("period", Map.of(
                "startDate", startDate,
                "endDate", endDate
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取收入统计失败: " + e.getMessage(), "GET_REVENUE_STATISTICS_ERROR"));
        }
    }
    
    // 工具方法：创建错误响应
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        return errorResponse;
    }
} 