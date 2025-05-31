package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
import com.studyroom.server.entity.Seat;
import com.studyroom.server.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 座位管理控制器
 * 处理座位管理相关的API请求
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/seats")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    /**
     * 创建新座位（管理员功能）
     * POST /api/seats
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSeat(@RequestBody Map<String, Object> seatRequest) {
        try {
            String seatNumber = (String) seatRequest.get("seatNumber");
            Long studyRoomId = Long.valueOf(seatRequest.get("studyRoomId").toString());
            String typeStr = (String) seatRequest.get("type");
            Boolean hasWindow = (Boolean) seatRequest.get("hasWindow");
            Boolean hasPowerOutlet = (Boolean) seatRequest.get("hasPowerOutlet");
            Boolean hasLamp = (Boolean) seatRequest.get("hasLamp");
            String description = (String) seatRequest.get("description");
            String equipment = (String) seatRequest.get("equipment");
            Integer rowNum = seatRequest.get("rowNum") != null ? Integer.parseInt(seatRequest.get("rowNum").toString()) : null;
            Integer colNum = seatRequest.get("colNum") != null ? Integer.parseInt(seatRequest.get("colNum").toString()) : null;

            if (seatNumber == null || studyRoomId == null || typeStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("座位号、自习室ID和座位类型不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            Seat.SeatType type = Seat.SeatType.valueOf(typeStr.toUpperCase());

            Seat seat = seatService.createSeat(seatNumber, studyRoomId, type, hasWindow,
                                             hasPowerOutlet, hasLamp, description, equipment, rowNum, colNum);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seat", createSeatResponse(seat)); // Use helper for consistent response

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseData, "座位创建成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("座位类型无效", HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("创建座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取座位详细信息
     * GET /api/seats/{seatId}
     */
    @GetMapping("/{seatId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatById(@PathVariable Long seatId) {
        try {
            var seatOpt = seatService.findById(seatId);
            if (seatOpt.isPresent()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("seat", createSeatResponse(seatOpt.get()));
                return ResponseEntity.ok(ApiResponse.success(responseData, "获取座位信息成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("座位不存在", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取所有可用座位
     * GET /api/seats/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableSeats() {
        try {
            List<Seat> availableSeats = seatService.findAvailableSeats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", availableSeats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", availableSeats.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取可用座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取可用座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据自习室查找座位
     * GET /api/seats/study-room/{studyRoomId}
     */
    @GetMapping("/study-room/{studyRoomId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatsByStudyRoom(@PathVariable Long studyRoomId) {
        try {
            List<Seat> seats = seatService.findSeatsByStudyRoom(studyRoomId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", seats.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取自习室座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据自习室查找可用座位
     * GET /api/seats/study-room/{studyRoomId}/available
     */
    @GetMapping("/study-room/{studyRoomId}/available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableSeatsByStudyRoom(@PathVariable Long studyRoomId) {
        try {
            List<Seat> availableSeats = seatService.findAvailableSeatsByStudyRoom(studyRoomId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", availableSeats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", availableSeats.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取自习室可用座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室可用座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据座位类型查找座位
     * GET /api/seats/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatsByType(@PathVariable String type) {
        try {
            Seat.SeatType seatType = Seat.SeatType.valueOf(type.toUpperCase());
            List<Seat> seats = seatService.findSeatsByType(seatType);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", seats.size());
            responseData.put("type", type);
            return ResponseEntity.ok(ApiResponse.success(responseData, "按类型查找座位成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("无效的座位类型", HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按类型查找座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取有窗户的座位
     * GET /api/seats/with-window
     */
    @GetMapping("/with-window")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatsWithWindow() {
        try {
            List<Seat> seats = seatService.findSeatsWithWindow();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", seats.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取窗户座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取窗户座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据特征查找可用座位
     * GET /api/seats/available/features?hasWindow=true&hasPowerOutlet=true&hasLamp=false
     */
    @GetMapping("/available/features")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableSeatsWithFeatures(
            @RequestParam(required = false) Boolean hasWindow,
            @RequestParam(required = false) Boolean hasPowerOutlet,
            @RequestParam(required = false) Boolean hasLamp) {
        try {
            List<Seat> seats = seatService.findAvailableSeatsWithFeatures(hasWindow, hasPowerOutlet, hasLamp);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", seats.size());
            responseData.put("filters", Map.of(
                "hasWindow", hasWindow != null && hasWindow, // Ensure boolean, not null
                "hasPowerOutlet", hasPowerOutlet != null && hasPowerOutlet,
                "hasLamp", hasLamp != null && hasLamp
            ));
            return ResponseEntity.ok(ApiResponse.success(responseData, "按特征查找座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按特征查找座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 分页查询座位
     * GET /api/seats?page=0&size=10&studyRoomId=1&status=AVAILABLE&type=STANDARD
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long studyRoomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        try {
            Seat.SeatStatus seatStatus = null;
            Seat.SeatType seatType = null;

            if (status != null && !status.isEmpty()) {
                seatStatus = Seat.SeatStatus.valueOf(status.toUpperCase());
            }
            if (type != null && !type.isEmpty()) {
                seatType = Seat.SeatType.valueOf(type.toUpperCase());
            }

            Page<Seat> seatPage = seatService.findSeatsWithPagination(page, size, studyRoomId, seatStatus, seatType);

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("seats", seatPage.getContent().stream().map(this::createSeatResponse).toList());
            pageData.put("totalElements", seatPage.getTotalElements());
            pageData.put("totalPages", seatPage.getTotalPages());
            pageData.put("currentPage", page);
            pageData.put("pageSize", size);
            pageData.put("hasNext", seatPage.hasNext());
            pageData.put("hasPrevious", seatPage.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success(pageData, "获取座位列表成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新座位信息（管理员功能）
     * PUT /api/seats/{seatId}
     */
    @PutMapping("/{seatId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSeat(
            @PathVariable Long seatId,
            @RequestBody Map<String, Object> seatRequest) {
        try {
            String seatNumber = (String) seatRequest.get("seatNumber");
            String typeStr = (String) seatRequest.get("type");
            Boolean hasWindow = (Boolean) seatRequest.get("hasWindow");
            Boolean hasPowerOutlet = (Boolean) seatRequest.get("hasPowerOutlet");
            Boolean hasLamp = (Boolean) seatRequest.get("hasLamp");
            String description = (String) seatRequest.get("description");
            String equipment = (String) seatRequest.get("equipment");

            Seat.SeatType type = null;
            if (typeStr != null && !typeStr.isEmpty()) {
                type = Seat.SeatType.valueOf(typeStr.toUpperCase());
            }

            Seat updatedSeat = seatService.updateSeat(seatId, seatNumber, type, hasWindow,
                                                    hasPowerOutlet, hasLamp, description, equipment);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seat", createSeatResponse(updatedSeat));
            return ResponseEntity.ok(ApiResponse.success(responseData, "座位信息更新成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新座位信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新座位状态
     * PUT /api/seats/{seatId}/status
     */
    @PutMapping("/{seatId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSeatStatus(
            @PathVariable Long seatId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("状态参数不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            Seat.SeatStatus status = Seat.SeatStatus.valueOf(statusStr.toUpperCase());
            Seat updatedSeat = seatService.updateSeatStatus(seatId, status);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seat", createSeatResponse(updatedSeat));
            return ResponseEntity.ok(ApiResponse.success(responseData, "座位状态更新成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("无效的状态值", HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新座位状态失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 软删除座位（管理员功能）
     * DELETE /api/seats/{seatId}
     */
    @DeleteMapping("/{seatId}")
    public ResponseEntity<ApiResponse<Object>> deleteSeat(@PathVariable Long seatId) {
        try {
            seatService.deleteSeat(seatId);
            return ResponseEntity.ok(ApiResponse.success(null, "座位删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("删除座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 检查座位可用性
     * GET /api/seats/{seatId}/available
      * GET /api/seats/{seatId}/availability (alias for API.md alignment)
     */
     @GetMapping({"/{seatId}/available", "/{seatId}/availability"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSeatAvailability(@PathVariable Long seatId) {
        try {
            boolean available = seatService.isSeatAvailable(seatId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seatId", seatId);
            responseData.put("available", available);
            return ResponseEntity.ok(ApiResponse.success(responseData, "检查座位可用性成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查座位可用性失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 占用座位
     * POST /api/seats/{seatId}/occupy
     */
    @PostMapping("/{seatId}/occupy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> occupySeat(@PathVariable Long seatId) {
        try {
            boolean success = seatService.occupySeat(seatId);
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("seatId", seatId);
                return ResponseEntity.ok(ApiResponse.success(responseData, "座位占用成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("座位不可用或已被占用", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("占用座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 释放座位
     * POST /api/seats/{seatId}/release
     */
    @PostMapping("/{seatId}/release")
    public ResponseEntity<ApiResponse<Map<String, Object>>> releaseSeat(@PathVariable Long seatId) {
        try {
            boolean success = seatService.releaseSeat(seatId);
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("seatId", seatId);
                return ResponseEntity.ok(ApiResponse.success(responseData, "座位释放成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("座位未被占用或释放失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("释放座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 预订座位
     * POST /api/seats/{seatId}/reserve
     */
    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reserveSeat(@PathVariable Long seatId) {
        try {
            boolean success = seatService.reserveSeat(seatId);
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("seatId", seatId);
                return ResponseEntity.ok(ApiResponse.success(responseData, "座位预订成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("座位不可用或预订失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("预订座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 取消座位预订
     * POST /api/seats/{seatId}/cancel-reservation
     */
    @PostMapping("/{seatId}/cancel-reservation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelSeatReservation(@PathVariable Long seatId) {
        try {
            boolean success = seatService.cancelSeatReservation(seatId);
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("seatId", seatId);
                return ResponseEntity.ok(ApiResponse.success(responseData, "座位预订取消成功"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("座位未被预订或取消失败", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("取消座位预订失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取座位统计信息
     * GET /api/seats/{seatId}/statistics
     */
    @GetMapping("/{seatId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatStatistics(@PathVariable Long seatId) {
        try {
            Map<String, Object> statistics = seatService.getSeatStatistics(seatId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取座位统计信息成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位统计信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取自习室座位统计
     * GET /api/seats/study-room/{studyRoomId}/statistics
     */
    @GetMapping("/study-room/{studyRoomId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudyRoomSeatStatistics(@PathVariable Long studyRoomId) {
        try {
            Map<String, Object> statistics = seatService.getStudyRoomSeatStatistics(studyRoomId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取自习室座位统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室座位统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 按座位号排序查找座位
     * GET /api/seats/study-room/{studyRoomId}/sorted
     */
    @GetMapping("/study-room/{studyRoomId}/sorted")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatsSortedBySeatNumber(@PathVariable Long studyRoomId) {
        try {
            List<Seat> seats = seatService.findSeatsOrderBySeatNumber(studyRoomId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats.stream().map(this::createSeatResponse).toList());
            responseData.put("count", seats.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "按座位号排序查找座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按座位号排序失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // 工具方法：创建座位响应对象（避免Hibernate序列化问题）
    private Map<String, Object> createSeatResponse(Seat seat) {
        Map<String, Object> seatResponse = new HashMap<>();
        seatResponse.put("id", seat.getId());
        seatResponse.put("seatNumber", seat.getSeatNumber());
        seatResponse.put("type", seat.getType().toString());
        seatResponse.put("status", seat.getStatus().toString());
        seatResponse.put("description", seat.getDescription());
        seatResponse.put("equipment", seat.getEquipment());
        seatResponse.put("hasWindow", seat.getHasWindow());
        seatResponse.put("hasPowerOutlet", seat.getHasPowerOutlet());
        seatResponse.put("hasLamp", seat.getHasLamp());
        seatResponse.put("createdAt", seat.getCreatedAt());
        seatResponse.put("updatedAt", seat.getUpdatedAt());

        // 手动添加自习室信息，避免Hibernate代理问题
        if (seat.getStudyRoom() != null) {
            Map<String, Object> studyRoomInfo = new HashMap<>();
            studyRoomInfo.put("id", seat.getStudyRoom().getId());
            studyRoomInfo.put("name", seat.getStudyRoom().getName());
            studyRoomInfo.put("location", seat.getStudyRoom().getLocation());
            seatResponse.put("studyRoom", studyRoomInfo);
        }

        return seatResponse;
    }

    // 工具方法：创建错误响应
    private ApiResponse<Object> createErrorResponse(String message, int httpStatusCode) {
        return ApiResponse.error(httpStatusCode, message);
    }
}