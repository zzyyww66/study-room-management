package com.studyroom.server.controller;

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
    public ResponseEntity<Map<String, Object>> createSeat(@RequestBody Map<String, Object> seatRequest) {
        try {
            String seatNumber = (String) seatRequest.get("seatNumber");
            Long studyRoomId = Long.valueOf(seatRequest.get("studyRoomId").toString());
            String typeStr = (String) seatRequest.get("type");
            Boolean hasWindow = (Boolean) seatRequest.get("hasWindow");
            Boolean hasPowerOutlet = (Boolean) seatRequest.get("hasPowerOutlet");
            Boolean hasLamp = (Boolean) seatRequest.get("hasLamp");
            String description = (String) seatRequest.get("description");
            String equipment = (String) seatRequest.get("equipment");
            
            if (seatNumber == null || studyRoomId == null || typeStr == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("座位号、自习室ID和座位类型不能为空", "MISSING_REQUIRED_FIELDS"));
            }
            
            Seat.SeatType type = Seat.SeatType.valueOf(typeStr.toUpperCase());
            
            Seat seat = seatService.createSeat(seatNumber, studyRoomId, type, hasWindow, 
                                             hasPowerOutlet, hasLamp, description, equipment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "座位创建成功");
            response.put("seat", seat);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("座位类型无效", "INVALID_SEAT_TYPE"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("创建座位失败: " + e.getMessage(), "CREATE_SEAT_ERROR"));
        }
    }
    
    /**
     * 获取座位详细信息
     * GET /api/seats/{seatId}
     */
    @GetMapping("/{seatId}")
    public ResponseEntity<Map<String, Object>> getSeatById(@PathVariable Long seatId) {
        try {
            var seatOpt = seatService.findById(seatId);
            if (seatOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("seat", seatOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("座位不存在", "SEAT_NOT_FOUND"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位信息失败: " + e.getMessage(), "GET_SEAT_ERROR"));
        }
    }
    
    /**
     * 获取所有可用座位
     * GET /api/seats/available
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableSeats() {
        try {
            List<Seat> availableSeats = seatService.findAvailableSeats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", availableSeats);
            response.put("count", availableSeats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取可用座位失败: " + e.getMessage(), "GET_AVAILABLE_SEATS_ERROR"));
        }
    }
    
    /**
     * 根据自习室查找座位
     * GET /api/seats/study-room/{studyRoomId}
     */
    @GetMapping("/study-room/{studyRoomId}")
    public ResponseEntity<Map<String, Object>> getSeatsByStudyRoom(@PathVariable Long studyRoomId) {
        try {
            List<Seat> seats = seatService.findSeatsByStudyRoom(studyRoomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", seats);
            response.put("count", seats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室座位失败: " + e.getMessage(), "GET_ROOM_SEATS_ERROR"));
        }
    }
    
    /**
     * 根据自习室查找可用座位
     * GET /api/seats/study-room/{studyRoomId}/available
     */
    @GetMapping("/study-room/{studyRoomId}/available")
    public ResponseEntity<Map<String, Object>> getAvailableSeatsByStudyRoom(@PathVariable Long studyRoomId) {
        try {
            List<Seat> availableSeats = seatService.findAvailableSeatsByStudyRoom(studyRoomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", availableSeats);
            response.put("count", availableSeats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室可用座位失败: " + e.getMessage(), "GET_ROOM_AVAILABLE_SEATS_ERROR"));
        }
    }
    
    /**
     * 根据座位类型查找座位
     * GET /api/seats/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getSeatsByType(@PathVariable String type) {
        try {
            Seat.SeatType seatType = Seat.SeatType.valueOf(type.toUpperCase());
            List<Seat> seats = seatService.findSeatsByType(seatType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", seats);
            response.put("count", seats.size());
            response.put("type", type);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("无效的座位类型", "INVALID_SEAT_TYPE"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按类型查找座位失败: " + e.getMessage(), "GET_SEATS_BY_TYPE_ERROR"));
        }
    }
    
    /**
     * 获取有窗户的座位
     * GET /api/seats/with-window
     */
    @GetMapping("/with-window")
    public ResponseEntity<Map<String, Object>> getSeatsWithWindow() {
        try {
            List<Seat> seats = seatService.findSeatsWithWindow();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", seats);
            response.put("count", seats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取窗户座位失败: " + e.getMessage(), "GET_WINDOW_SEATS_ERROR"));
        }
    }
    
    /**
     * 根据特征查找可用座位
     * GET /api/seats/available/features?hasWindow=true&hasPowerOutlet=true&hasLamp=false
     */
    @GetMapping("/available/features")
    public ResponseEntity<Map<String, Object>> getAvailableSeatsWithFeatures(
            @RequestParam(required = false) Boolean hasWindow,
            @RequestParam(required = false) Boolean hasPowerOutlet,
            @RequestParam(required = false) Boolean hasLamp) {
        try {
            List<Seat> seats = seatService.findAvailableSeatsWithFeatures(hasWindow, hasPowerOutlet, hasLamp);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", seats);
            response.put("count", seats.size());
            response.put("filters", Map.of(
                "hasWindow", hasWindow,
                "hasPowerOutlet", hasPowerOutlet,
                "hasLamp", hasLamp
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按特征查找座位失败: " + e.getMessage(), "GET_SEATS_BY_FEATURES_ERROR"));
        }
    }
    
    /**
     * 分页查询座位
     * GET /api/seats?page=0&size=10&studyRoomId=1&status=AVAILABLE&type=STANDARD
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getSeats(
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", seatPage.getContent().stream().map(this::createSeatResponse).toList());
            response.put("totalElements", seatPage.getTotalElements());
            response.put("totalPages", seatPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", seatPage.hasNext());
            response.put("hasPrevious", seatPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), "INVALID_PARAMETER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位列表失败: " + e.getMessage(), "GET_SEATS_ERROR"));
        }
    }
    
    /**
     * 更新座位信息（管理员功能）
     * PUT /api/seats/{seatId}
     */
    @PutMapping("/{seatId}")
    public ResponseEntity<Map<String, Object>> updateSeat(
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "座位信息更新成功");
            response.put("seat", updatedSeat);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), "INVALID_PARAMETER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新座位信息失败: " + e.getMessage(), "UPDATE_SEAT_ERROR"));
        }
    }
    
    /**
     * 更新座位状态
     * PUT /api/seats/{seatId}/status
     */
    @PutMapping("/{seatId}/status")
    public ResponseEntity<Map<String, Object>> updateSeatStatus(
            @PathVariable Long seatId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("状态参数不能为空", "MISSING_STATUS"));
            }
            
            Seat.SeatStatus status = Seat.SeatStatus.valueOf(statusStr.toUpperCase());
            Seat updatedSeat = seatService.updateSeatStatus(seatId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "座位状态更新成功");
            response.put("seat", updatedSeat);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("无效的状态值", "INVALID_STATUS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新座位状态失败: " + e.getMessage(), "UPDATE_STATUS_ERROR"));
        }
    }
    
    /**
     * 软删除座位（管理员功能）
     * DELETE /api/seats/{seatId}
     */
    @DeleteMapping("/{seatId}")
    public ResponseEntity<Map<String, Object>> deleteSeat(@PathVariable Long seatId) {
        try {
            seatService.deleteSeat(seatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "座位删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("删除座位失败: " + e.getMessage(), "DELETE_SEAT_ERROR"));
        }
    }
    
    /**
     * 检查座位可用性
     * GET /api/seats/{seatId}/available
     */
    @GetMapping("/{seatId}/available")
    public ResponseEntity<Map<String, Object>> checkSeatAvailability(@PathVariable Long seatId) {
        try {
            boolean available = seatService.isSeatAvailable(seatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seatId", seatId);
            response.put("available", available);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查座位可用性失败: " + e.getMessage(), "CHECK_AVAILABILITY_ERROR"));
        }
    }
    
    /**
     * 占用座位
     * POST /api/seats/{seatId}/occupy
     */
    @PostMapping("/{seatId}/occupy")
    public ResponseEntity<Map<String, Object>> occupySeat(@PathVariable Long seatId) {
        try {
            boolean success = seatService.occupySeat(seatId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "座位占用成功");
                response.put("seatId", seatId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("座位不可用或已被占用", "SEAT_NOT_AVAILABLE"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("占用座位失败: " + e.getMessage(), "OCCUPY_SEAT_ERROR"));
        }
    }
    
    /**
     * 释放座位
     * POST /api/seats/{seatId}/release
     */
    @PostMapping("/{seatId}/release")
    public ResponseEntity<Map<String, Object>> releaseSeat(@PathVariable Long seatId) {
        try {
            boolean success = seatService.releaseSeat(seatId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "座位释放成功");
                response.put("seatId", seatId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("座位未被占用或释放失败", "RELEASE_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("释放座位失败: " + e.getMessage(), "RELEASE_SEAT_ERROR"));
        }
    }
    
    /**
     * 预订座位
     * POST /api/seats/{seatId}/reserve
     */
    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<Map<String, Object>> reserveSeat(@PathVariable Long seatId) {
        try {
            boolean success = seatService.reserveSeat(seatId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "座位预订成功");
                response.put("seatId", seatId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("座位不可用或预订失败", "RESERVE_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("预订座位失败: " + e.getMessage(), "RESERVE_SEAT_ERROR"));
        }
    }
    
    /**
     * 取消座位预订
     * POST /api/seats/{seatId}/cancel-reservation
     */
    @PostMapping("/{seatId}/cancel-reservation")
    public ResponseEntity<Map<String, Object>> cancelSeatReservation(@PathVariable Long seatId) {
        try {
            boolean success = seatService.cancelSeatReservation(seatId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "座位预订取消成功");
                response.put("seatId", seatId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("座位未被预订或取消失败", "CANCEL_FAILED"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("取消座位预订失败: " + e.getMessage(), "CANCEL_RESERVATION_ERROR"));
        }
    }
    
    /**
     * 获取座位统计信息
     * GET /api/seats/{seatId}/statistics
     */
    @GetMapping("/{seatId}/statistics")
    public ResponseEntity<Map<String, Object>> getSeatStatistics(@PathVariable Long seatId) {
        try {
            Map<String, Object> statistics = seatService.getSeatStatistics(seatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取座位统计信息失败: " + e.getMessage(), "GET_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 获取自习室座位统计
     * GET /api/seats/study-room/{studyRoomId}/statistics
     */
    @GetMapping("/study-room/{studyRoomId}/statistics")
    public ResponseEntity<Map<String, Object>> getStudyRoomSeatStatistics(@PathVariable Long studyRoomId) {
        try {
            Map<String, Object> statistics = seatService.getStudyRoomSeatStatistics(studyRoomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室座位统计失败: " + e.getMessage(), "GET_ROOM_SEAT_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 按座位号排序查找座位
     * GET /api/seats/study-room/{studyRoomId}/sorted
     */
    @GetMapping("/study-room/{studyRoomId}/sorted")
    public ResponseEntity<Map<String, Object>> getSeatsSortedBySeatNumber(@PathVariable Long studyRoomId) {
        try {
            List<Seat> seats = seatService.findSeatsOrderBySeatNumber(studyRoomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("seats", seats);
            response.put("count", seats.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按座位号排序失败: " + e.getMessage(), "SORT_BY_SEAT_NUMBER_ERROR"));
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
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        return errorResponse;
    }
} 