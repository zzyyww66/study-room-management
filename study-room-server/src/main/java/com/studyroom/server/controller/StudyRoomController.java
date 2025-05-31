package com.studyroom.server.controller;

import com.studyroom.server.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStudyRoom(@RequestBody Map<String, Object> roomRequest) {
        try {
            // Align with API.md: "roomName", "location", "capacity", "description"
            String roomName = (String) roomRequest.get("roomName");
            String location = (String) roomRequest.get("location");
            Integer capacity = roomRequest.get("capacity") != null ? Integer.parseInt(roomRequest.get("capacity").toString()) : null;
            String description = (String) roomRequest.get("description");

            // Optional fields (based on current server implementation, may need API.md update)
            BigDecimal hourlyRate = roomRequest.get("hourlyRate") != null ? new BigDecimal(roomRequest.get("hourlyRate").toString()) : null;
            LocalTime openTime = roomRequest.get("openTime") != null ? LocalTime.parse((String) roomRequest.get("openTime")) : null;
            LocalTime closeTime = roomRequest.get("closeTime") != null ? LocalTime.parse((String) roomRequest.get("closeTime")) : null;
            String facilities = (String) roomRequest.get("facilities");


            // Validate required fields based on API.md (or server's actual minimum requirements)
            // For this subtask, let's assume roomName, location, capacity are essential.
            if (roomName == null || roomName.trim().isEmpty() || location == null || location.trim().isEmpty() || capacity == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("房间名称、位置和容量不能为空", HttpStatus.BAD_REQUEST.value()));
            }
            if (capacity <= 0) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("容量必须为正数", HttpStatus.BAD_REQUEST.value()));
            }
            // Add more specific validation as needed (e.g. for hourlyRate if provided)

            StudyRoom studyRoom = studyRoomService.createStudyRoom(
                roomName, description, capacity, hourlyRate, openTime, closeTime, location, facilities);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRoom", studyRoom);

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseData, "自习室创建成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("创建自习室失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取自习室详细信息
     * GET /api/study-rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudyRoomById(@PathVariable Long roomId) {
        try {
            var roomOpt = studyRoomService.findById(roomId);
            if (roomOpt.isPresent()) {
                StudyRoom room = roomOpt.get();
                List<Seat> seats = studyRoomService.getRoomSeats(roomId); // Fetch seats

                Map<String, Object> roomDetailMap = new HashMap<>();
                roomDetailMap.put("id", room.getId()); // Changed from roomId to id for consistency
                roomDetailMap.put("name", room.getName()); // Assuming getName() exists
                roomDetailMap.put("description", room.getDescription());
                roomDetailMap.put("capacity", room.getCapacity());
                roomDetailMap.put("hourlyRate", room.getHourlyRate());
                roomDetailMap.put("openTime", room.getOpenTime() != null ? room.getOpenTime().toString() : null);
                roomDetailMap.put("closeTime", room.getCloseTime() != null ? room.getCloseTime().toString() : null);
                roomDetailMap.put("location", room.getLocation());
                roomDetailMap.put("facilities", room.getFacilities());
                roomDetailMap.put("status", room.getStatus() != null ? room.getStatus().toString() : null);
                roomDetailMap.put("createdAt", room.getCreatedAt() != null ? room.getCreatedAt().toString() : null);
                roomDetailMap.put("updatedAt", room.getUpdatedAt() != null ? room.getUpdatedAt().toString() : null);

                List<Map<String, Object>> seatMaps = seats.stream().map(seat -> {
                    Map<String, Object> seatMap = new HashMap<>();
                    seatMap.put("id", seat.getId()); // Changed from seatId to id
                    seatMap.put("seatNumber", seat.getSeatNumber());
                    seatMap.put("type", seat.getType() != null ? seat.getType().toString() : null); // Changed from seatType
                    seatMap.put("status", seat.getStatus() != null ? seat.getStatus().toString() : null);
                    // Add other seat fields if necessary e.g. hasWindow, hasPowerOutlet
                    seatMap.put("hasWindow", seat.getHasWindow());
                    seatMap.put("hasPowerOutlet", seat.getHasPowerOutlet());
                    seatMap.put("hasLamp", seat.getHasLamp());
                    return seatMap;
                }).collect(java.util.stream.Collectors.toList());

                roomDetailMap.put("seats", seatMaps);

                return ResponseEntity.ok(ApiResponse.success(roomDetailMap, "自习室信息获取成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("自习室不存在", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取所有可用自习室
     * GET /api/study-rooms/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableRooms() {
        try {
            List<StudyRoom> availableRooms = studyRoomService.findAvailableRooms();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRooms", availableRooms);
            responseData.put("count", availableRooms.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取可用自习室成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取可用自习室失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 分页查询自习室
     * GET /api/study-rooms?page=0&size=10&status=AVAILABLE
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudyRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            StudyRoom.RoomStatus roomStatus = null;
            if (status != null && !status.isEmpty()) {
                roomStatus = StudyRoom.RoomStatus.valueOf(status.toUpperCase());
            }

            Page<StudyRoom> roomPage = studyRoomService.findRoomsWithPagination(page, size, roomStatus);

            Map<String, Object> pageData = new HashMap<>();
            pageData.put("studyRooms", roomPage.getContent());
            pageData.put("totalElements", roomPage.getTotalElements());
            pageData.put("totalPages", roomPage.getTotalPages());
            pageData.put("currentPage", page);
            pageData.put("pageSize", size);
            pageData.put("hasNext", roomPage.hasNext());
            pageData.put("hasPrevious", roomPage.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success(pageData, "获取自习室列表成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("参数格式错误: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据容量范围查找自习室
     * GET /api/study-rooms/search/capacity?min=10&max=50
     */
    @GetMapping("/search/capacity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchRoomsByCapacity(
            @RequestParam(required = false) Integer min,
            @RequestParam(required = false) Integer max) {
        try {
            List<StudyRoom> rooms = studyRoomService.findRoomsByCapacityRange(min, max);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRooms", rooms);
            responseData.put("count", rooms.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "按容量搜索自习室成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按容量搜索自习室失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据价格范围查找自习室
     * GET /api/study-rooms/search/price?min=10.00&max=50.00
     */
    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchRoomsByPrice(
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max) {
        try {
            List<StudyRoom> rooms = studyRoomService.findRoomsByPriceRange(min, max);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRooms", rooms);
            responseData.put("count", rooms.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "按价格搜索自习室成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按价格搜索自习室失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 根据名称模糊查找自习室
     * GET /api/study-rooms/search/name/{keyword}
     */
    @GetMapping("/search/name/{keyword}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchRoomsByName(@PathVariable String keyword) {
        try {
            List<StudyRoom> rooms = studyRoomService.findRoomsByNameContaining(keyword);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRooms", rooms);
            responseData.put("count", rooms.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "按名称搜索自习室成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按名称搜索自习室失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取自习室的所有座位
     * GET /api/study-rooms/{roomId}/seats
     */
    @GetMapping("/{roomId}/seats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomSeats(@PathVariable Long roomId) {
        try {
            List<Seat> seats = studyRoomService.getRoomSeats(roomId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats);
            responseData.put("count", seats.size());
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取自习室座位成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室座位失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取自习室可用座位数量
     * GET /api/study-rooms/{roomId}/available-seats-count
     */
    @GetMapping("/{roomId}/available-seats-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getAvailableSeatsCount(@PathVariable Long roomId) {
        try {
            int count = studyRoomService.getAvailableSeatsCount(roomId);
            Map<String, Integer> responseData = new HashMap<>();
            responseData.put("availableSeatsCount", count);
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取可用座位数量成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取可用座位数量失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新自习室信息（管理员功能）
     * PUT /api/study-rooms/{roomId}
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStudyRoom(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> roomRequest) {
        try {
            // Align with API.md for field names, making non-specified ones optional
            String roomName = (String) roomRequest.get("roomName"); // Changed from "name"
            String location = (String) roomRequest.get("location");
            Integer capacity = roomRequest.get("capacity") != null ? Integer.parseInt(roomRequest.get("capacity").toString()) : null;
            String description = (String) roomRequest.get("description");

            // Optional fields based on current server impl (API.md doesn't list these for PUT /rooms/{id})
            BigDecimal hourlyRate = roomRequest.get("hourlyRate") != null ? new BigDecimal(roomRequest.get("hourlyRate").toString()) : null;
            LocalTime openTime = roomRequest.get("openTime") != null ? LocalTime.parse((String) roomRequest.get("openTime")) : null;
            LocalTime closeTime = roomRequest.get("closeTime") != null ? LocalTime.parse((String) roomRequest.get("closeTime")) : null;
            String facilities = (String) roomRequest.get("facilities");
            
            // Note: 'status' is handled by a separate endpoint PUT /{roomId}/status as per server design.
            // API.md might need update to remove 'status' from this general update PUT request.

            // Perform validation if needed, e.g., if capacity is provided, it should be positive.
            if (capacity != null && capacity <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(createErrorResponse("容量必须为正数（如果提供）", HttpStatus.BAD_REQUEST.value()));
            }

            StudyRoom updatedRoom = studyRoomService.updateStudyRoom(
                roomId, roomName, description, capacity, hourlyRate, openTime, closeTime, location, facilities);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRoom", updatedRoom);

            return ResponseEntity.ok(ApiResponse.success(responseData, "自习室信息更新成功"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新自习室信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 更新自习室状态（管理员功能）
     * PUT /api/study-rooms/{roomId}/status
     */
    @PutMapping("/{roomId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateRoomStatus(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("状态参数不能为空", HttpStatus.BAD_REQUEST.value()));
            }

            StudyRoom.RoomStatus status = StudyRoom.RoomStatus.valueOf(statusStr.toUpperCase());
            StudyRoom updatedRoom = studyRoomService.updateRoomStatus(roomId, status);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRoom", updatedRoom);

            return ResponseEntity.ok(ApiResponse.success(responseData, "自习室状态更新成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("无效的状态值", HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("更新自习室状态失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 软删除自习室（管理员功能）
     * DELETE /api/study-rooms/{roomId}
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Object>> deleteStudyRoom(@PathVariable Long roomId) {
        try {
            studyRoomService.deleteStudyRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success(null, "自习室删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("删除自习室失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 检查自习室在指定时间是否开放
     * GET /api/study-rooms/{roomId}/open-at/{time}
     */
    @GetMapping("/{roomId}/open-at/{time}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isRoomOpenAtTime(
            @PathVariable Long roomId,
            @PathVariable String time) {
        try {
            LocalTime checkTime = LocalTime.parse(time);
            boolean isOpen = studyRoomService.isRoomOpenAtTime(roomId, checkTime);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("isOpen", isOpen);
            responseData.put("time", time);

            return ResponseEntity.ok(ApiResponse.success(responseData, "检查开放时间成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("检查开放时间失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取自习室统计信息
     * GET /api/study-rooms/{roomId}/statistics
     */
    @GetMapping("/{roomId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomStatistics(@PathVariable Long roomId) {
        try {
            Map<String, Object> statistics = studyRoomService.getRoomStatistics(roomId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取自习室统计信息成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取自习室统计信息失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 获取所有自习室利用率统计
     * GET /api/study-rooms/utilization-stats
     */
    @GetMapping("/utilization-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomsUtilizationStats() {
        try {
            List<Map<String, Object>> utilizationStats = studyRoomService.getRoomsUtilizationStats();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("utilizationStats", utilizationStats);
            return ResponseEntity.ok(ApiResponse.success(responseData, "获取利用率统计成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("获取利用率统计失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 按价格排序获取自习室
     * GET /api/study-rooms/sorted/price?order=asc
     */
    @GetMapping("/sorted/price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomsSortedByPrice(
            @RequestParam(defaultValue = "asc") String order) {
        try {
            boolean ascending = "asc".equalsIgnoreCase(order);
            List<StudyRoom> rooms = studyRoomService.findRoomsOrderByPrice(ascending);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRooms", rooms);
            responseData.put("count", rooms.size());
            responseData.put("sortOrder", ascending ? "ascending" : "descending");

            return ResponseEntity.ok(ApiResponse.success(responseData, "按价格排序获取自习室成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按价格排序失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * 按容量排序获取自习室
     * GET /api/study-rooms/sorted/capacity?order=asc
     */
    @GetMapping("/sorted/capacity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomsSortedByCapacity(
            @RequestParam(defaultValue = "asc") String order) {
        try {
            boolean ascending = "asc".equalsIgnoreCase(order);
            List<StudyRoom> rooms = studyRoomService.findRoomsOrderByCapacity(ascending);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("studyRooms", rooms);
            responseData.put("count", rooms.size());
            responseData.put("sortOrder", ascending ? "ascending" : "descending");

            return ResponseEntity.ok(ApiResponse.success(responseData, "按容量排序获取自习室成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("按容量排序失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // 工具方法：创建错误响应
    private ApiResponse<Object> createErrorResponse(String message, int httpStatusCode) {
        return ApiResponse.error(httpStatusCode, message);
    }
}