package com.studyroom.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.client.model.ApiResponse;
import com.studyroom.client.model.PageData;
import com.studyroom.client.model.StudyRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 自习室API服务类
 * 负责与后端自习室相关API的通信
 * 
 * @author Developer
 * @version 1.0.0
 */
public class StudyRoomApiService {

    private static final Logger logger = LoggerFactory.getLogger(StudyRoomApiService.class);
    
    // 单例实例
    private static StudyRoomApiService instance;
    
    // HTTP客户端服务
    private final HttpClientService httpClient;
    
    // JSON处理器
    private final ObjectMapper objectMapper;

    /**
     * 私有构造函数 - 单例模式
     */
    private StudyRoomApiService() {
        this.httpClient = HttpClientService.getInstance();
        this.objectMapper = httpClient.getObjectMapper();
        logger.info("🏢 自习室API服务初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized StudyRoomApiService getInstance() {
        if (instance == null) {
            instance = new StudyRoomApiService();
        }
        return instance;
    }

    /**
     * 根据ID获取自习室信息
     */
    public CompletableFuture<StudyRoom> getStudyRoomById(Long roomId) {
        logger.debug("🔍 获取自习室信息: ID={}", roomId);
        
        return httpClient.get("/study-rooms/" + roomId)
            .thenApply(this::parseStudyRoomResponse)
            .whenComplete((room, throwable) -> {
                if (throwable == null && room != null) {
                    logger.debug("✅ 获取自习室信息成功: {}", room.getName());
                } else {
                    logger.warn("❌ 获取自习室信息失败: ID={}, 错误={}", roomId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取所有自习室列表
     */
    public CompletableFuture<List<StudyRoom>> getAllStudyRooms() {
        logger.warn("⚠️ 调用了 getAllStudyRooms() 方法，但其对应的服务器端点 /study-rooms/all 可能不存在或未实现。");
        logger.warn("Consider using the paginated getStudyRooms() method or ensure a non-paginated endpoint is available on the server.");
        // For now, returning an empty list or exceptional future to avoid breaking compilation if called.
        // return httpClient.get("/study-rooms/all")
        //     .thenApply(this::parseStudyRoomListResponse)
        //     .whenComplete((rooms, throwable) -> {
        //         if (throwable == null && rooms != null) {
        //             logger.debug("✅ 获取自习室列表成功: 共{}个自习室", rooms.size());
        //         } else {
        //             logger.warn("❌ 获取自习室列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
        //         }
        //     });
        return CompletableFuture.completedFuture(new java.util.ArrayList<>()); // Return empty list for now
    }

    /**
     * 分页查询自习室列表
     */
    public CompletableFuture<PageData<StudyRoom>> getStudyRooms(int page, int size, StudyRoom.Status status) {
        logger.debug("📋 查询自习室列表: page={}, size={}, status={}",
            page, size, status);
        
        StringBuilder url = new StringBuilder("/study-rooms?page=" + page + "&size=" + size);
        // Keyword parameter removed as it's not supported by the server's paginated GET /study-rooms endpoint
        // if (keyword != null && !keyword.trim().isEmpty()) {
        //     url.append("&keyword=").append(keyword.trim());
        // }
        if (status != null) {
            url.append("&status=").append(status.name());
        }
        
        return httpClient.get(url.toString())
            .thenApply(this::parseStudyRoomPageResponse)
            .whenComplete((pageData, throwable) -> {
                if (throwable == null && pageData != null) {
                    logger.debug("✅ 查询自习室列表成功: 共{}条记录", pageData.getTotalElements());
                } else {
                    logger.warn("❌ 查询自习室列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 查询可用的自习室
     */
    public CompletableFuture<List<StudyRoom>> getAvailableStudyRooms() {
        logger.debug("🔍 查询可用的自习室");
        
        return httpClient.get("/study-rooms/available")
            .thenApply(this::parseStudyRoomListResponse)
            .whenComplete((rooms, throwable) -> {
                if (throwable == null && rooms != null) {
                    logger.debug("✅ 查询可用自习室成功: 共{}个可用", rooms.size());
                } else {
                    logger.warn("❌ 查询可用自习室失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 按条件搜索自习室
     */
    public CompletableFuture<List<StudyRoom>> searchStudyRooms(String keyword, Integer minCapacity, Integer maxCapacity, 
                                                               BigDecimal minPrice, BigDecimal maxPrice, String location) {
        logger.warn("⚠️ 调用了 searchStudyRooms() 方法. This method uses a generic /study-rooms/search endpoint " +
                    "which is not standard on the server. Server has specific search paths like /search/capacity, /search/price, /search/name. " +
                    "This method needs a more detailed refactoring to align with server capabilities.");
        logger.debug("🔍 搜索自习室 (legacy): keyword={}, capacity={}-{}, price={}-{}, location={}",
            keyword, minCapacity, maxCapacity, minPrice, maxPrice, location);
        
        StringBuilder url = new StringBuilder("/study-rooms/search?");
        boolean hasParam = false;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            url.append("keyword=").append(keyword.trim());
            hasParam = true;
        }
        if (minCapacity != null) {
            if (hasParam) url.append("&");
            url.append("minCapacity=").append(minCapacity);
            hasParam = true;
        }
        if (maxCapacity != null) {
            if (hasParam) url.append("&");
            url.append("maxCapacity=").append(maxCapacity);
            hasParam = true;
        }
        if (minPrice != null) {
            if (hasParam) url.append("&");
            url.append("minPrice=").append(minPrice);
            hasParam = true;
        }
        if (maxPrice != null) {
            if (hasParam) url.append("&");
            url.append("maxPrice=").append(maxPrice);
            hasParam = true;
        }
        if (location != null && !location.trim().isEmpty()) {
            if (hasParam) url.append("&");
            url.append("location=").append(location.trim());
        }
        
        return httpClient.get(url.toString())
            .thenApply(this::parseStudyRoomListResponse)
            .whenComplete((rooms, throwable) -> {
                if (throwable == null && rooms != null) {
                    logger.debug("✅ 搜索自习室成功: 找到{}个匹配结果", rooms.size());
                } else {
                    logger.warn("❌ 搜索自习室失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 创建自习室（管理员功能）
     */
    public CompletableFuture<StudyRoom> createStudyRoom(StudyRoom room) {
        logger.info("📝 创建自习室: {}", room.getName());
        
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("roomName", room.getName()); // Changed from "name" to "roomName"
        roomData.put("location", room.getLocation());
        roomData.put("capacity", room.getCapacity());
        roomData.put("description", room.getDescription());

        // Align with server: server expects hourlyRate, openTime, closeTime, facilities as optional
        if (room.getHourlyRate() != null) { // Client model uses hourlyRate internally
            roomData.put("hourlyRate", room.getHourlyRate());
        }
        if (room.getOpenTime() != null) {
            roomData.put("openTime", room.getOpenTime().toString()); // Ensure correct format if needed by server
        }
        if (room.getCloseTime() != null) {
            roomData.put("closeTime", room.getCloseTime().toString()); // Ensure correct format
        }
        // facilities is not directly on client StudyRoom model, assume it's not sent or handled differently
        // roomData.put("facilities", room.getFacilities());

        // Status is typically not sent on create, server should handle default status
        // roomData.put("status", room.getStatus());
        
        return httpClient.post("/study-rooms", roomData)
            .thenApply(this::parseStudyRoomResponse)
            .whenComplete((newRoom, throwable) -> {
                if (throwable == null && newRoom != null) {
                    logger.info("✅ 自习室创建成功: {}", newRoom.getName());
                } else {
                    logger.error("❌ 自习室创建失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 更新自习室信息（管理员功能）
     */
    public CompletableFuture<StudyRoom> updateStudyRoom(StudyRoom room) {
        logger.info("📝 更新自习室信息: {}", room.getName());
        
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("roomName", room.getName()); // Changed from "name" to "roomName"
        roomData.put("location", room.getLocation());
        roomData.put("capacity", room.getCapacity());
        roomData.put("description", room.getDescription());

        // Align with server: server expects hourlyRate, openTime, closeTime, facilities as optional
        // for the general update. Status is handled by a separate endpoint.
        if (room.getHourlyRate() != null) {
            roomData.put("hourlyRate", room.getHourlyRate());
        }
        if (room.getOpenTime() != null) {
            roomData.put("openTime", room.getOpenTime().toString());
        }
        if (room.getCloseTime() != null) {
            roomData.put("closeTime", room.getCloseTime().toString());
        }
        // facilities is not directly on client StudyRoom model for update here, assume not sent or handled differently
        // roomData.put("facilities", room.getFacilities());

        // Status should not be sent in the general update request as per server design
        // roomData.put("status", room.getStatus());
        
        return httpClient.put("/study-rooms/" + room.getId(), roomData)
            .thenApply(this::parseStudyRoomResponse)
            .whenComplete((updatedRoom, throwable) -> {
                if (throwable == null && updatedRoom != null) {
                    logger.info("✅ 自习室信息更新成功: {}", updatedRoom.getName());
                } else {
                    logger.error("❌ 自习室信息更新失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 删除自习室（管理员功能）
     */
    public CompletableFuture<Boolean> deleteStudyRoom(Long roomId) {
        logger.info("🗑️ 删除自习室: ID={}", roomId);
        
        return httpClient.delete("/study-rooms/" + roomId)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((success, throwable) -> {
                if (throwable == null && success) {
                    logger.info("✅ 自习室删除成功: ID={}", roomId);
                } else {
                    logger.error("❌ 自习室删除失败: ID={}, 错误={}", roomId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取自习室统计信息
     */
    public CompletableFuture<Map<String, Object>> getStudyRoomStatistics(Long roomId) {
        logger.debug("📊 获取自习室统计信息: ID={}", roomId);
        
        return httpClient.get("/study-rooms/" + roomId + "/statistics")
            .thenApply(this::parseStatisticsResponse)
            .whenComplete((stats, throwable) -> {
                if (throwable == null && stats != null) {
                    logger.debug("✅ 获取自习室统计信息成功: ID={}", roomId);
                } else {
                    logger.warn("❌ 获取自习室统计信息失败: ID={}, 错误={}", roomId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取自习室整体统计信息
     */
    public CompletableFuture<Map<String, Object>> getOverallStatistics() {
        logger.warn("⚠️ 调用了 getOverallStatistics() 方法, which calls /study-rooms/statistics.");
        logger.warn("This endpoint on the server might provide per-room statistics if a roomId is expected, or it might be intended for StatisticsController.");
        logger.warn("Verify if /study-rooms/statistics is the correct endpoint for 'overall' study room statistics without a specific room ID.");
        logger.debug("📊 获取自习室整体统计信息 (current target: /study-rooms/statistics)");
        
        return httpClient.get("/study-rooms/statistics") // This endpoint might not exist or might expect a roomId
            .thenApply(this::parseStatisticsResponse)
            .whenComplete((stats, throwable) -> {
                if (throwable == null && stats != null) {
                    logger.debug("✅ 获取统计信息成功 (from /study-rooms/statistics)");
                } else {
                    logger.warn("❌ 获取统计信息失败 (from /study-rooms/statistics): {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    // 私有辅助方法

    /**
     * 解析自习室响应
     */
    private StudyRoom parseStudyRoomResponse(String jsonResponse) {
        try {
            ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<Map<String, Object>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                Map<String, Object> dataMap = apiResponse.getData();
                if (dataMap == null) {
                    logger.error("❌ 自习室响应数据为空");
                    throw new RuntimeException("API错误: 响应数据为空");
                }

                StudyRoom room = new StudyRoom();
                // Map top-level fields
                room.setId(objectMapper.convertValue(dataMap.get("id"), Long.class));
                room.setName((String) dataMap.get("name"));
                room.setDescription((String) dataMap.get("description"));
                room.setCapacity(objectMapper.convertValue(dataMap.get("capacity"), Integer.class));

                Object hourlyRateObj = dataMap.get("hourlyRate");
                if (hourlyRateObj != null) {
                    room.setHourlyRate(new BigDecimal(hourlyRateObj.toString()));
                }

                String openTimeStr = (String) dataMap.get("openTime");
                if (openTimeStr != null) {
                    room.setOpenTime(objectMapper.convertValue(openTimeStr, java.time.LocalTime.class));
                }
                String closeTimeStr = (String) dataMap.get("closeTime");
                if (closeTimeStr != null) {
                    room.setCloseTime(objectMapper.convertValue(closeTimeStr, java.time.LocalTime.class));
                }

                room.setLocation((String) dataMap.get("location"));
                room.setFacilities((String) dataMap.get("facilities")); // Assuming facilities is a simple string

                String statusStr = (String) dataMap.get("status");
                if (statusStr != null) {
                    room.setStatus(StudyRoom.Status.valueOf(statusStr));
                }

                String createdAtStr = (String) dataMap.get("createdAt");
                if (createdAtStr != null) {
                    room.setCreatedAt(objectMapper.convertValue(createdAtStr, java.time.LocalDateTime.class));
                }
                String updatedAtStr = (String) dataMap.get("updatedAt");
                if (updatedAtStr != null) {
                     room.setUpdatedAt(objectMapper.convertValue(updatedAtStr, java.time.LocalDateTime.class));
                }


                // Parse seats
                List<Map<String, Object>> seatMaps = (List<Map<String, Object>>) dataMap.get("seats");
                if (seatMaps != null) {
                    List<com.studyroom.client.model.Seat> seats = seatMaps.stream().map(seatMap -> {
                        com.studyroom.client.model.Seat seat = new com.studyroom.client.model.Seat();
                        seat.setId(objectMapper.convertValue(seatMap.get("id"), Long.class));
                        seat.setSeatNumber((String) seatMap.get("seatNumber"));

                        String seatTypeStr = (String) seatMap.get("type");
                        if (seatTypeStr != null) {
                             try {
                                seat.setType(com.studyroom.client.model.Seat.Type.valueOf(seatTypeStr));
                             } catch (IllegalArgumentException iae) {
                                 logger.warn("⚠️ 未知座位类型 '{}' (ID: {}), 将使用默认值或设为null", seatTypeStr, seat.getId());
                                 // Handle unknown enum: set to null or a default, or log and skip.
                                 // For now, let it be null if not parsable to client's enum.
                             }
                        }

                        String seatStatusStr = (String) seatMap.get("status");
                         if (seatStatusStr != null) {
                             try {
                                seat.setStatus(com.studyroom.client.model.Seat.Status.valueOf(seatStatusStr));
                             } catch (IllegalArgumentException iae) {
                                 logger.warn("⚠️ 未知座位状态 '{}' (ID: {}), 将使用默认值或设为null", seatStatusStr, seat.getId());
                             }
                        }
                        seat.setHasWindow(objectMapper.convertValue(seatMap.get("hasWindow"), Boolean.class));
                        seat.setHasPowerOutlet(objectMapper.convertValue(seatMap.get("hasPowerOutlet"), Boolean.class));
                        seat.setHasLamp(objectMapper.convertValue(seatMap.get("hasLamp"), Boolean.class));
                        // seat.setStudyRoomId(room.getId()); // Set back-reference if needed, though API sends flat list
                        return seat;
                    }).collect(Collectors.toList());
                    room.setSeats(seats);
                }
                return room;
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析自习室(含座位)响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析自习室列表响应
     */
    private List<StudyRoom> parseStudyRoomListResponse(String jsonResponse) {
        try {
            ApiResponse<List<StudyRoom>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<List<StudyRoom>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析自习室列表响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析自习室分页响应
     */
    private PageData<StudyRoom> parseStudyRoomPageResponse(String jsonResponse) {
        try {
            ApiResponse<PageData<StudyRoom>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<PageData<StudyRoom>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                PageData<StudyRoom> pageData = apiResponse.getData();
                if (pageData != null) {
                     // Ensure content is correctly deserialized if it's a list of maps initially
                    if (pageData.getContent() != null && !pageData.getContent().isEmpty() && !(pageData.getContent().get(0) instanceof StudyRoom)) {
                        List<StudyRoom> studyRooms = pageData.getContent().stream()
                            .map(item -> objectMapper.convertValue(item, StudyRoom.class))
                            .collect(Collectors.toList());
                        pageData.setContent(studyRooms);
                    }
                    logger.debug("✅ 自习室分页数据解析成功: 当前页={}, 总页数={}, 总记录数={}, 当前页记录数={}",
                        pageData.getPageNumber(), pageData.getTotalPages(), pageData.getTotalElements(), pageData.getContent().size());
                    return pageData;
                } else {
                    logger.error("❌ 自习室分页响应数据为空");
                    throw new RuntimeException("API错误: 响应数据为空");
                }
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析自习室分页响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析统计信息响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseStatisticsResponse(String jsonResponse) {
        try {
            ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<Map<String, Object>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析统计信息响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析布尔响应
     */
    private Boolean parseBooleanResponse(String jsonResponse) {
        try {
            // Attempt to parse as ApiResponse<Boolean> first
            try {
                ApiResponse<Boolean> apiResponse = objectMapper.readValue(jsonResponse,
                        new TypeReference<ApiResponse<Boolean>>() {});
                if (apiResponse.getCode() == 200) { // Assuming 200 is success
                    return apiResponse.getData() != null ? apiResponse.getData() : false;
                } else {
                     // Check if data is a map containing a boolean
                    if (apiResponse.getData() instanceof Map) {
                         Map<?, ?> dataMap = (Map<?, ?>) apiResponse.getData();
                        if (dataMap.containsKey("success") && dataMap.get("success") instanceof Boolean) {
                            logger.warn("⚠️ API returned success code but boolean data was in a map for: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                            return (Boolean) dataMap.get("success");
                        }
                    }
                    logger.error("❌ API请求失败 (Boolean direct): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                    throw new RuntimeException("API错误: " + apiResponse.getMessage());
                }
            } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                // If direct Boolean parsing fails, try parsing as ApiResponse<Map<String, Boolean>>
                logger.warn("⚠️ Direct boolean parsing failed for StudyRoomApi, attempting to parse as Map<String, Boolean>: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                ApiResponse<Map<String, Boolean>> apiResponseMap = objectMapper.readValue(jsonResponse,
                        new TypeReference<ApiResponse<Map<String, Boolean>>>() {});

                if (apiResponseMap.getCode() == 200) { // Assuming 200 is success
                    Map<String, Boolean> dataMap = apiResponseMap.getData();
                     if (dataMap != null) {
                        if (dataMap.containsKey("success")) return dataMap.get("success");
                        // Add other common boolean keys if necessary for this service
                        logger.warn("⚠️ Boolean response map did not contain 'success' key: {}", dataMap);
                        return !dataMap.isEmpty();
                    }
                    return false;
                } else {
                    logger.error("❌ API请求失败 (Boolean as Map): Code={}, Message={}", apiResponseMap.getCode(), apiResponseMap.getMessage());
                    throw new RuntimeException("API错误: " + apiResponseMap.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("❌ 解析布尔响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }
}