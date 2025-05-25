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
        logger.debug("📋 获取所有自习室列表");
        
        return httpClient.get("/study-rooms/all")
            .thenApply(this::parseStudyRoomListResponse)
            .whenComplete((rooms, throwable) -> {
                if (throwable == null && rooms != null) {
                    logger.debug("✅ 获取自习室列表成功: 共{}个自习室", rooms.size());
                } else {
                    logger.warn("❌ 获取自习室列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 分页查询自习室列表
     */
    public CompletableFuture<PageData<StudyRoom>> getStudyRooms(int page, int size, String keyword, StudyRoom.Status status) {
        logger.debug("📋 查询自习室列表: page={}, size={}, keyword={}, status={}", 
            page, size, keyword, status);
        
        StringBuilder url = new StringBuilder("/study-rooms?page=" + page + "&size=" + size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            url.append("&keyword=").append(keyword.trim());
        }
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
        logger.debug("🔍 搜索自习室: keyword={}, capacity={}-{}, price={}-{}, location={}", 
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
        roomData.put("name", room.getName());
        roomData.put("description", room.getDescription());
        roomData.put("location", room.getLocation());
        roomData.put("capacity", room.getCapacity());
        roomData.put("pricePerHour", room.getPricePerHour());
        roomData.put("openTime", room.getOpenTime());
        roomData.put("closeTime", room.getCloseTime());
        roomData.put("status", room.getStatus());
        
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
        roomData.put("name", room.getName());
        roomData.put("description", room.getDescription());
        roomData.put("location", room.getLocation());
        roomData.put("capacity", room.getCapacity());
        roomData.put("pricePerHour", room.getPricePerHour());
        roomData.put("openTime", room.getOpenTime());
        roomData.put("closeTime", room.getCloseTime());
        roomData.put("status", room.getStatus());
        
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
        logger.debug("📊 获取自习室整体统计信息");
        
        return httpClient.get("/study-rooms/statistics")
            .thenApply(this::parseStatisticsResponse)
            .whenComplete((stats, throwable) -> {
                if (throwable == null && stats != null) {
                    logger.debug("✅ 获取整体统计信息成功");
                } else {
                    logger.warn("❌ 获取整体统计信息失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    // 私有辅助方法

    /**
     * 解析自习室响应
     */
    private StudyRoom parseStudyRoomResponse(String jsonResponse) {
        try {
            ApiResponse<StudyRoom> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<StudyRoom>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析自习室响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析自习室列表响应
     */
    private List<StudyRoom> parseStudyRoomListResponse(String jsonResponse) {
        try {
            ApiResponse<List<StudyRoom>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<List<StudyRoom>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析自习室列表响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析自习室分页响应
     */
    private PageData<StudyRoom> parseStudyRoomPageResponse(String jsonResponse) {
        try {
            logger.debug("🔄 解析自习室分页响应: {}", jsonResponse.substring(0, Math.min(200, jsonResponse.length())));
            
            // 直接解析后端返回的实际格式
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            
            // 检查是否成功
            Boolean success = (Boolean) responseMap.get("success");
            if (success == null || !success) {
                String message = (String) responseMap.getOrDefault("message", "未知错误");
                throw new RuntimeException("API错误: " + message);
            }
            
            // 提取分页数据
            List<Map<String, Object>> studyRoomsData = (List<Map<String, Object>>) responseMap.get("studyRooms");
            Integer totalPages = (Integer) responseMap.get("totalPages");
            Integer totalElements = (Integer) responseMap.get("totalElements");
            Integer currentPage = (Integer) responseMap.get("currentPage");
            
            // 转换StudyRoom对象
            List<StudyRoom> studyRooms = studyRoomsData.stream()
                .map(data -> objectMapper.convertValue(data, StudyRoom.class))
                .collect(java.util.stream.Collectors.toList());
            
            // 创建PageData对象
            PageData<StudyRoom> pageData = new PageData<>();
            pageData.setContent(studyRooms);
            pageData.setTotalElements(totalElements != null ? totalElements.longValue() : 0L);
            pageData.setTotalPages(totalPages != null ? totalPages : 0);
            
            logger.debug("✅ 分页数据解析成功: 当前页={}, 总页数={}, 总记录数={}, 当前页记录数={}", 
                currentPage, totalPages, totalElements, studyRooms.size());
            
            return pageData;
            
        } catch (Exception e) {
            logger.error("❌ 解析自习室分页响应失败: {}", e.getMessage(), e);
            logger.debug("原始响应: {}", jsonResponse);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析统计信息响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseStatisticsResponse(String jsonResponse) {
        try {
            ApiResponse<Map<String, Object>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<Map<String, Object>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析统计信息响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析布尔响应
     */
    private Boolean parseBooleanResponse(String jsonResponse) {
        try {
            ApiResponse<Boolean> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<Boolean>>() {});
            
            if (response.isSuccess()) {
                return response.getData() != null ? response.getData() : false;
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析布尔响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }
} 