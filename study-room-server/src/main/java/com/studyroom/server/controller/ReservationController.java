package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.service.ReservationService;
import com.studyroom.server.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建新预订
     * POST /api/reservations
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createReservation(HttpServletRequest request, @RequestBody Map<String, Object> reservationRequest) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("缺少或无效的Authorization Header", HttpStatus.UNAUTHORIZED.value()));
            }
            String token = authorizationHeader.substring(7);
            Long userId;
            try {
                userId = jwtUtil.extractUserId(token);
                if (!jwtUtil.validateToken(token)) {
                     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("无效或过期的Token", HttpStatus.UNAUTHORIZED.value()));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("无效或过期的Token: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
            }

            // Long userId = Long.valueOf(reservationRequest.get("userId").toString()); // Removed
            Long seatId = Long.valueOf(reservationRequest.get("seatId").toString());
            String startTimeStr = (String) reservationRequest.get("startTime");
            String endTimeStr = (String) reservationRequest.get("endTime");
            String notes = (String) reservationRequest.get("notes");

            if (seatId == null || startTimeStr == null || endTimeStr == null) { // userId is now from token
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("座位ID、开始时间和结束时间不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);

            // 检查时间冲突
            if (reservationService.hasTimeConflict(seatId, startTime, endTime, null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("预订时间与已有预订冲突", HttpStatus.BAD_REQUEST.value()));
            }

            Reservation reservation = reservationService.createReservation(userId, seatId, startTime, endTime, notes);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reservation", createReservationResponse(reservation));

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseData, "预订创建成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("创建预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取预订详细信息
     * GET /api/reservations/{reservationId}
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservationById(@PathVariable Long reservationId) {
        try {
            var reservationOpt = reservationService.findById(reservationId);
            if (reservationOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("reservation", createReservationResponse(reservationOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "获取预订信息成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("预订不存在", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据预订编码查找预订
     * GET /api/reservations/code/{reservationCode}
     */
    @GetMapping("/code/{reservationCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservationByCode(@PathVariable String reservationCode) {
        try {
            var reservationOpt = reservationService.findByReservationCode(reservationCode);
            if (reservationOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("reservation", createReservationResponse(reservationOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "查找预订成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("预订不存在", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("查找预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户的所有预订
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservationsByUser(@PathVariable Long userId) {
        try {
            List<Reservation> reservations = reservationService.findReservationsByUser(userId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reservations", reservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", reservations.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取用户预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户的有效预订
     * GET /api/reservations/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveReservationsByUser(@PathVariable Long userId) {
        try {
            List<Reservation> activeReservations = reservationService.findActiveReservationsByUser(userId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("activeReservations", activeReservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", activeReservations.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取用户有效预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户有效预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取座位的所有预订
     * GET /api/reservations/seat/{seatId}
     */
    @GetMapping("/seat/{seatId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservationsBySeat(@PathVariable Long seatId) {
        try {
            List<Reservation> reservations = reservationService.findReservationsBySeat(seatId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reservations", reservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", reservations.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取座位预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取今日预订
     * GET /api/reservations/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayReservations() {
        try {
            List<Reservation> todayReservations = reservationService.findTodayReservations();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("todayReservations", todayReservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", todayReservations.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取今日预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取今日预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取有效预订
     * GET /api/reservations/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveReservations() {
        try {
            List<Reservation> activeReservations = reservationService.findActiveReservations();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("activeReservations", activeReservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", activeReservations.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取有效预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取有效预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 分页查询预订
     * GET /api/reservations?page=0&size=10&userId=1&status=ACTIVE&paymentStatus=PAID
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long seatId, // New
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String startDate, // New
            @RequestParam(required = false) String endDate) { // New
        try {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate); // Assumes ISO_LOCAL_DATE_TIME
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate); // Assumes ISO_LOCAL_DATE_TIME
            }

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

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("reservations", reservationPage.getContent().stream().map(this::createReservationResponse).toList());
            pageData.put("totalElements", reservationPage.getTotalElements());
            pageData.put("totalPages", reservationPage.getTotalPages());
            pageData.put("currentPage", page);
            pageData.put("pageSize", size);
            pageData.put("hasNext", reservationPage.hasNext());
            pageData.put("hasPrevious", reservationPage.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success(pageData, "获取预订列表成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取预订列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 检查预订时间冲突
     * POST /api/reservations/check-conflict
     */
    @PostMapping("/check-conflict")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkTimeConflict(@RequestBody Map<String, Object> conflictRequest) {
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
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("hasConflict", hasConflict);

            return ResponseEntity.ok(ApiResponse.success(responseData, hasConflict ? "存在时间冲突" : "无时间冲突"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查时间冲突失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 计算预订费用
     * POST /api/reservations/calculate-cost
     */
    @PostMapping("/calculate-cost")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateCost(@RequestBody Map<String, Object> costRequest) {
        try {
            Long seatId = Long.valueOf(costRequest.get("seatId").toString());
            String startTimeStr = (String) costRequest.get("startTime");
            String endTimeStr = (String) costRequest.get("endTime");

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);

            BigDecimal cost = reservationService.calculateReservationCost(seatId, startTime, endTime);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("cost", cost);
            responseData.put("currency", "CNY");

            return ResponseEntity.ok(ApiResponse.success(responseData, "费用计算成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("计算费用失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新预订信息
     * PUT /api/reservations/{reservationId}
     */
    @PutMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateReservation(
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
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reservation", createReservationResponse(updatedReservation));

            return ResponseEntity.ok(ApiResponse.success(responseData, "预订信息更新成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新预订信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 取消预订
     * PUT /api/reservations/{reservationId}/cancel
     */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> cancelRequest) {
        try {
            String cancelReason = cancelRequest.get("cancelReason");
            boolean success = reservationService.cancelReservation(reservationId, cancelReason);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(null, "预订取消成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("预订取消失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("取消预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 支付预订
     * PUT /api/reservations/{reservationId}/pay
     */
    @PutMapping("/{reservationId}/pay")
    public ResponseEntity<ApiResponse<Object>> payForReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> paymentRequest) {
        try {
            String paymentMethod = paymentRequest.get("paymentMethod");
            boolean success = reservationService.payForReservation(reservationId, paymentMethod);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success(null, "支付成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("支付失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("支付处理失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 办理入住（签到）
     * PUT /api/reservations/{reservationId}/check-in
     */
    @PutMapping("/{reservationId}/check-in")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkIn(@PathVariable Long reservationId) {
        try {
            boolean success = reservationService.checkIn(reservationId);
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("checkInTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return ResponseEntity.ok(ApiResponse.success(responseData, "签到成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("签到失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("签到处理失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 办理退房（签退）
     * PUT /api/reservations/{reservationId}/check-out
     */
    @PutMapping("/{reservationId}/check-out")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkOut(@PathVariable Long reservationId) {
        try {
            boolean success = reservationService.checkOut(reservationId);
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("checkOutTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return ResponseEntity.ok(ApiResponse.success(responseData, "签退成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("签退失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("签退处理失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 延长预订时间
     * PUT /api/reservations/{reservationId}/extend
     */
    @PutMapping("/{reservationId}/extend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extendReservation(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> extendRequest) {
        try {
            String newEndTimeStr = extendRequest.get("newEndTime");
            if (newEndTimeStr == null || newEndTimeStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("新的结束时间不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            LocalDateTime newEndTime = LocalDateTime.parse(newEndTimeStr);
            boolean success = reservationService.extendReservation(reservationId, newEndTime);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("newEndTime", newEndTimeStr);
                return ResponseEntity.ok(ApiResponse.success(responseData, "预订延长成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("预订延长失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("延长预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取过期未支付的预订
     * GET /api/reservations/expired-unpaid
     */
    @GetMapping("/expired-unpaid")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExpiredUnpaidReservations() {
        try {
            List<Reservation> expiredReservations = reservationService.findExpiredUnpaidReservations();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("expiredUnpaidReservations", expiredReservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", expiredReservations.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取过期未支付预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取过期未支付预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 自动取消过期预订
     * POST /api/reservations/cancel-expired
     */
    @PostMapping("/cancel-expired")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cancelExpiredReservations() {
        try {
            int cancelledCount = reservationService.cancelExpiredReservations();
            Map<String, Integer> responseData = new HashMap<>();
            responseData.put("cancelledCount", cancelledCount);
            return ResponseEntity.ok(ApiResponse.success(responseData, "过期预订自动取消完成"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("自动取消过期预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取即将到期的预订
     * GET /api/reservations/expiring-within/{minutes}
     */
    @GetMapping("/expiring-within/{minutes}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReservationsExpiringWithin(@PathVariable int minutes) {
        try {
            List<Reservation> expiringReservations = reservationService.findReservationsExpiringWithin(minutes);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("expiringReservations", expiringReservations.stream().map(this::createReservationResponse).toList());
            responseData.put("count", expiringReservations.size());
            responseData.put("minutesBefore", minutes);
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取即将到期预订成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取即将到期预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取用户预订统计
     * GET /api/reservations/user/{userId}/statistics
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserReservationStatistics(@PathVariable Long userId) {
        try {
            Map<String, Object> statistics = reservationService.getUserReservationStatistics(userId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取用户预订统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取用户预订统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取座位预订统计
     * GET /api/reservations/seat/{seatId}/statistics
     */
    @GetMapping("/seat/{seatId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatReservationStatistics(@PathVariable Long seatId) {
        try {
            Map<String, Object> statistics = reservationService.getSeatReservationStatistics(seatId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取座位预订统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位预订统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取系统预订统计
     * GET /api/reservations/statistics/system
     */
    @GetMapping("/statistics/system")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemReservationStatistics() {
        try {
            Map<String, Object> statistics = reservationService.getSystemReservationStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取系统预订统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取系统预订统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取收入统计
     * GET /api/reservations/statistics/revenue?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/statistics/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate);
            LocalDateTime endDateTime = LocalDateTime.parse(endDate);

            Map<String, Object> statistics = reservationService.getRevenueStatistics(startDateTime, endDateTime);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("statistics", statistics);
            responseData.put("period", Map.of(
                "startDate", startDate,
                "endDate", endDate
            ));
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取收入统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取收入统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // 工具方法：创建预订响应对象（避免Hibernate序列化问题）
    private Map<String, Object> createReservationResponse(Reservation reservation) {
        Map<String, Object> reservationResponse = new HashMap<>();
        reservationResponse.put("id", reservation.getId());
        reservationResponse.put("reservationCode", reservation.getReservationCode());
        reservationResponse.put("startTime", reservation.getStartTime());
        reservationResponse.put("endTime", reservation.getEndTime());
        reservationResponse.put("status", reservation.getStatus().toString());
        reservationResponse.put("paymentStatus", reservation.getPaymentStatus().toString());
        reservationResponse.put("totalAmount", reservation.getTotalAmount());
        reservationResponse.put("notes", reservation.getNotes());
        reservationResponse.put("createdAt", reservation.getCreatedAt());
        reservationResponse.put("updatedAt", reservation.getUpdatedAt());
        reservationResponse.put("checkInTime", reservation.getCheckInTime());
        reservationResponse.put("checkOutTime", reservation.getCheckOutTime());

        // 手动添加用户信息，避免Hibernate代理问题
        if (reservation.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", reservation.getUser().getId());
            userInfo.put("username", reservation.getUser().getUsername());
            userInfo.put("realName", reservation.getUser().getRealName());
            userInfo.put("email", reservation.getUser().getEmail());
            reservationResponse.put("user", userInfo);
        }

        // 手动添加座位信息，避免Hibernate代理问题
        if (reservation.getSeat() != null) {
            Map<String, Object> seatInfo = new HashMap<>();
            seatInfo.put("id", reservation.getSeat().getId());
            seatInfo.put("seatNumber", reservation.getSeat().getSeatNumber());
            seatInfo.put("type", reservation.getSeat().getType().toString());
            seatInfo.put("status", reservation.getSeat().getStatus().toString());

            // 如果座位有自习室信息，也添加进来
            if (reservation.getSeat().getStudyRoom() != null) {
                Map<String, Object> studyRoomInfo = new HashMap<>();
                studyRoomInfo.put("id", reservation.getSeat().getStudyRoom().getId());
                studyRoomInfo.put("name", reservation.getSeat().getStudyRoom().getName());
                studyRoomInfo.put("location", reservation.getSeat().getStudyRoom().getLocation());
                seatInfo.put("studyRoom", studyRoomInfo);
            }

            reservationResponse.put("seat", seatInfo);
        }

        return reservationResponse;
    }

    // 工具方法：创建错误响应
    private ApiResponse<Object> createErrorResponse(String message, int httpStatusCode) {
        return ApiResponse.error(httpStatusCode, message);
    }
}