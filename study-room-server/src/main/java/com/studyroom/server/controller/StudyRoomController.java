package com.studyroom.server.controller;

import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.entity.Seat;
import com.studyroom.server.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自习室管理控制器
 * 处理自习室管理相关的API请求
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@RestController
@RequestMapping("/api/study-rooms")
public class StudyRoomController {
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    /**
     * 创建新自习室（管理员功能）
     * POST /api/study-rooms
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createStudyRoom(@RequestBody Map<String, Object> roomRequest) {
        try {
            String name = (String) roomRequest.get("name");
            String description = (String) roomRequest.get("description");
            Integer capacity = (Integer) roomRequest.get("capacity");
            BigDecimal hourlyRate = new BigDecimal(roomRequest.get("hourlyRate").toString());
            LocalTime openTime = LocalTime.parse((String) roomRequest.get("openTime"));
            LocalTime closeTime = LocalTime.parse((String) roomRequest.get("closeTime"));
            String location = (String) roomRequest.get("location");
            String facilities = (String) roomRequest.get("facilities");
            
            if (name == null || capacity == null || hourlyRate == null || openTime == null || closeTime == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("必填字段不能为空", "MISSING_REQUIRED_FIELDS"));
            }
            
            StudyRoom studyRoom = studyRoomService.createStudyRoom(
                name, description, capacity, hourlyRate, openTime, closeTime, location, facilities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "自习室创建成功");
            response.put("studyRoom", studyRoom);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("创建自习室失败: " + e.getMessage(), "CREATE_ROOM_ERROR"));
        }
    }
    
    /**
     * 获取自习室详细信息
     * GET /api/study-rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> getStudyRoomById(@PathVariable Long roomId) {
        try {
            var roomOpt = studyRoomService.findById(roomId);
            if (roomOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("studyRoom", roomOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("自习室不存在", "ROOM_NOT_FOUND"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室信息失败: " + e.getMessage(), "GET_ROOM_ERROR"));
        }
    }
    
    /**
     * 获取所有可用自习室
     * GET /api/study-rooms/available
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableRooms() {
        try {
            List<StudyRoom> availableRooms = studyRoomService.findAvailableRooms();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", availableRooms);
            response.put("count", availableRooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取可用自习室失败: " + e.getMessage(), "GET_AVAILABLE_ROOMS_ERROR"));
        }
    }
    
    /**
     * 分页查询自习室
     * GET /api/study-rooms?page=0&size=10&status=AVAILABLE
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getStudyRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            StudyRoom.RoomStatus roomStatus = null;
            if (status != null && !status.isEmpty()) {
                roomStatus = StudyRoom.RoomStatus.valueOf(status.toUpperCase());
            }
            
            Page<StudyRoom> roomPage = studyRoomService.findRoomsWithPagination(page, size, roomStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", roomPage.getContent());
            response.put("totalElements", roomPage.getTotalElements());
            response.put("totalPages", roomPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", roomPage.hasNext());
            response.put("hasPrevious", roomPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), "INVALID_PARAMETER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室列表失败: " + e.getMessage(), "GET_ROOMS_ERROR"));
        }
    }
    
    /**
     * 根据容量范围查找自习室
     * GET /api/study-rooms/search/capacity?min=10&max=50
     */
    @GetMapping("/search/capacity")
    public ResponseEntity<Map<String, Object>> searchRoomsByCapacity(
            @RequestParam(required = false) Integer min,
            @RequestParam(required = false) Integer max) {
        try {
            List<StudyRoom> rooms = studyRoomService.findRoomsByCapacityRange(min, max);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", rooms);
            response.put("count", rooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按容量搜索自习室失败: " + e.getMessage(), "SEARCH_BY_CAPACITY_ERROR"));
        }
    }
    
    /**
     * 根据价格范围查找自习室
     * GET /api/study-rooms/search/price?min=10.00&max=50.00
     */
    @GetMapping("/search/price")
    public ResponseEntity<Map<String, Object>> searchRoomsByPrice(
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max) {
        try {
            List<StudyRoom> rooms = studyRoomService.findRoomsByPriceRange(min, max);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", rooms);
            response.put("count", rooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按价格搜索自习室失败: " + e.getMessage(), "SEARCH_BY_PRICE_ERROR"));
        }
    }
    
    /**
     * 根据名称模糊查找自习室
     * GET /api/study-rooms/search/name/{keyword}
     */
    @GetMapping("/search/name/{keyword}")
    public ResponseEntity<Map<String, Object>> searchRoomsByName(@PathVariable String keyword) {
        try {
            List<StudyRoom> rooms = studyRoomService.findRoomsByNameContaining(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", rooms);
            response.put("count", rooms.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按名称搜索自习室失败: " + e.getMessage(), "SEARCH_BY_NAME_ERROR"));
        }
    }
    
    /**
     * 获取自习室的所有座位
     * GET /api/study-rooms/{roomId}/seats
     */
    @GetMapping("/{roomId}/seats")
    public ResponseEntity<Map<String, Object>> getRoomSeats(@PathVariable Long roomId) {
        try {
            List<Seat> seats = studyRoomService.getRoomSeats(roomId);
            
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
     * 获取自习室可用座位数量
     * GET /api/study-rooms/{roomId}/available-seats-count
     */
    @GetMapping("/{roomId}/available-seats-count")
    public ResponseEntity<Map<String, Object>> getAvailableSeatsCount(@PathVariable Long roomId) {
        try {
            int count = studyRoomService.getAvailableSeatsCount(roomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("availableSeatsCount", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取可用座位数量失败: " + e.getMessage(), "GET_AVAILABLE_SEATS_COUNT_ERROR"));
        }
    }
    
    /**
     * 更新自习室信息（管理员功能）
     * PUT /api/study-rooms/{roomId}
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> updateStudyRoom(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> roomRequest) {
        try {
            String name = (String) roomRequest.get("name");
            String description = (String) roomRequest.get("description");
            Integer capacity = (Integer) roomRequest.get("capacity");
            BigDecimal hourlyRate = new BigDecimal(roomRequest.get("hourlyRate").toString());
            LocalTime openTime = LocalTime.parse((String) roomRequest.get("openTime"));
            LocalTime closeTime = LocalTime.parse((String) roomRequest.get("closeTime"));
            String location = (String) roomRequest.get("location");
            String facilities = (String) roomRequest.get("facilities");
            
            StudyRoom updatedRoom = studyRoomService.updateStudyRoom(
                roomId, name, description, capacity, hourlyRate, openTime, closeTime, location, facilities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "自习室信息更新成功");
            response.put("studyRoom", updatedRoom);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新自习室信息失败: " + e.getMessage(), "UPDATE_ROOM_ERROR"));
        }
    }
    
    /**
     * 更新自习室状态（管理员功能）
     * PUT /api/study-rooms/{roomId}/status
     */
    @PutMapping("/{roomId}/status")
    public ResponseEntity<Map<String, Object>> updateRoomStatus(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("状态参数不能为空", "MISSING_STATUS"));
            }
            
            StudyRoom.RoomStatus status = StudyRoom.RoomStatus.valueOf(statusStr.toUpperCase());
            StudyRoom updatedRoom = studyRoomService.updateRoomStatus(roomId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "自习室状态更新成功");
            response.put("studyRoom", updatedRoom);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("无效的状态值", "INVALID_STATUS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新自习室状态失败: " + e.getMessage(), "UPDATE_STATUS_ERROR"));
        }
    }
    
    /**
     * 软删除自习室（管理员功能）
     * DELETE /api/study-rooms/{roomId}
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> deleteStudyRoom(@PathVariable Long roomId) {
        try {
            studyRoomService.deleteStudyRoom(roomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "自习室删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("删除自习室失败: " + e.getMessage(), "DELETE_ROOM_ERROR"));
        }
    }
    
    /**
     * 检查自习室在指定时间是否开放
     * GET /api/study-rooms/{roomId}/open-at/{time}
     */
    @GetMapping("/{roomId}/open-at/{time}")
    public ResponseEntity<Map<String, Object>> isRoomOpenAtTime(
            @PathVariable Long roomId,
            @PathVariable String time) {
        try {
            LocalTime checkTime = LocalTime.parse(time);
            boolean isOpen = studyRoomService.isRoomOpenAtTime(roomId, checkTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isOpen", isOpen);
            response.put("time", time);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查开放时间失败: " + e.getMessage(), "CHECK_OPEN_TIME_ERROR"));
        }
    }
    
    /**
     * 获取自习室统计信息
     * GET /api/study-rooms/{roomId}/statistics
     */
    @GetMapping("/{roomId}/statistics")
    public ResponseEntity<Map<String, Object>> getRoomStatistics(@PathVariable Long roomId) {
        try {
            Map<String, Object> statistics = studyRoomService.getRoomStatistics(roomId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室统计信息失败: " + e.getMessage(), "GET_STATISTICS_ERROR"));
        }
    }
    
    /**
     * 获取所有自习室利用率统计
     * GET /api/study-rooms/utilization-stats
     */
    @GetMapping("/utilization-stats")
    public ResponseEntity<Map<String, Object>> getRoomsUtilizationStats() {
        try {
            List<Map<String, Object>> utilizationStats = studyRoomService.getRoomsUtilizationStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("utilizationStats", utilizationStats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取利用率统计失败: " + e.getMessage(), "GET_UTILIZATION_STATS_ERROR"));
        }
    }
    
    /**
     * 按价格排序获取自习室
     * GET /api/study-rooms/sorted/price?order=asc
     */
    @GetMapping("/sorted/price")
    public ResponseEntity<Map<String, Object>> getRoomsSortedByPrice(
            @RequestParam(defaultValue = "asc") String order) {
        try {
            boolean ascending = "asc".equalsIgnoreCase(order);
            List<StudyRoom> rooms = studyRoomService.findRoomsOrderByPrice(ascending);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", rooms);
            response.put("count", rooms.size());
            response.put("sortOrder", ascending ? "ascending" : "descending");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按价格排序失败: " + e.getMessage(), "SORT_BY_PRICE_ERROR"));
        }
    }
    
    /**
     * 按容量排序获取自习室
     * GET /api/study-rooms/sorted/capacity?order=asc
     */
    @GetMapping("/sorted/capacity")
    public ResponseEntity<Map<String, Object>> getRoomsSortedByCapacity(
            @RequestParam(defaultValue = "asc") String order) {
        try {
            boolean ascending = "asc".equalsIgnoreCase(order);
            List<StudyRoom> rooms = studyRoomService.findRoomsOrderByCapacity(ascending);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studyRooms", rooms);
            response.put("count", rooms.size());
            response.put("sortOrder", ascending ? "ascending" : "descending");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按容量排序失败: " + e.getMessage(), "SORT_BY_CAPACITY_ERROR"));
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