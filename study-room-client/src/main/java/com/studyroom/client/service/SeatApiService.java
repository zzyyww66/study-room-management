package com.studyroom.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.client.model.ApiResponse;
import com.studyroom.client.model.PageData;
import com.studyroom.client.model.Seat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 座位API服务类
 * 负责与后端座位相关API的通信
 * 
 * @author Developer
 * @version 1.0.0
 */
public class SeatApiService {

    private static final Logger logger = LoggerFactory.getLogger(SeatApiService.class);
    
    // 单例实例
    private static SeatApiService instance;
    
    // HTTP客户端服务
    private final HttpClientService httpClient;
    
    // JSON处理器
    private final ObjectMapper objectMapper;

    /**
     * 私有构造函数 - 单例模式
     */
    private SeatApiService() {
        this.httpClient = HttpClientService.getInstance();
        this.objectMapper = httpClient.getObjectMapper();
        logger.info("💺 座位API服务初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized SeatApiService getInstance() {
        if (instance == null) {
            instance = new SeatApiService();
        }
        return instance;
    }

    /**
     * 根据ID获取座位信息
     */
    public CompletableFuture<Seat> getSeatById(Long seatId) {
        logger.debug("🔍 获取座位信息: ID={}", seatId);
        
        return httpClient.get("/seats/" + seatId)
            .thenApply(this::parseSeatResponse)
            .whenComplete((seat, throwable) -> {
                if (throwable == null && seat != null) {
                    logger.debug("✅ 获取座位信息成功: {}", seat.getSeatNumber());
                } else {
                    logger.warn("❌ 获取座位信息失败: ID={}, 错误={}", seatId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 根据自习室ID获取座位列表
     */
    public CompletableFuture<List<Seat>> getSeatsByStudyRoom(Long studyRoomId) {
        logger.debug("📋 获取自习室座位列表: studyRoomId={}", studyRoomId);
        
        return httpClient.get("/seats/study-room/" + studyRoomId)
            .thenApply(this::parseSeatListResponse)
            .whenComplete((seats, throwable) -> {
                if (throwable == null && seats != null) {
                    logger.debug("✅ 获取座位列表成功: 共{}个座位", seats.size());
                } else {
                    logger.warn("❌ 获取座位列表失败: studyRoomId={}, 错误={}", studyRoomId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取可用座位列表
     */
    public CompletableFuture<List<Seat>> getAvailableSeats(Long studyRoomId) {
        logger.debug("🔍 获取可用座位: studyRoomId={}", studyRoomId);
        
        String url = studyRoomId != null ? 
            "/seats/available?studyRoomId=" + studyRoomId : 
            "/seats/available";
        
        return httpClient.get(url)
            .thenApply(this::parseSeatListResponse)
            .whenComplete((seats, throwable) -> {
                if (throwable == null && seats != null) {
                    logger.debug("✅ 获取可用座位成功: 共{}个可用", seats.size());
                } else {
                    logger.warn("❌ 获取可用座位失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 分页查询座位列表
     */
    public CompletableFuture<PageData<Seat>> getSeats(int page, int size, Long studyRoomId, 
                                                      Seat.Type type, Seat.Status status) {
        logger.debug("📋 查询座位列表: page={}, size={}, studyRoomId={}, type={}, status={}", 
            page, size, studyRoomId, type, status);
        
        StringBuilder url = new StringBuilder("/seats?page=" + page + "&size=" + size);
        if (studyRoomId != null) {
            url.append("&studyRoomId=").append(studyRoomId);
        }
        if (type != null) {
            url.append("&type=").append(type.name());
        }
        if (status != null) {
            url.append("&status=").append(status.name());
        }
        
        return httpClient.get(url.toString())
            .thenApply(this::parseSeatPageResponse)
            .whenComplete((pageData, throwable) -> {
                if (throwable == null && pageData != null) {
                    logger.debug("✅ 查询座位列表成功: 共{}条记录", pageData.getTotalElements());
                } else {
                    logger.warn("❌ 查询座位列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 搜索座位（按特征筛选）
     */
    public CompletableFuture<List<Seat>> searchSeats(Long studyRoomId, Seat.Type type, 
                                                     Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp) {
        logger.warn("⚠️ 调用了 searchSeats() 方法. This method uses a generic /seats/search endpoint " +
                    "which is not standard on the server. Server has specific search paths like /available/features, /type/{type}. " +
                    "This method needs a more detailed refactoring to align with server capabilities.");
        logger.debug("🔍 搜索座位 (legacy): studyRoomId={}, type={}, hasWindow={}, hasPowerOutlet={}, hasLamp={}",
            studyRoomId, type, hasWindow, hasPowerOutlet, hasLamp);
        
        StringBuilder url = new StringBuilder("/seats/search?");
        boolean hasParam = false;
        
        if (studyRoomId != null) {
            url.append("studyRoomId=").append(studyRoomId);
            hasParam = true;
        }
        if (type != null) {
            if (hasParam) url.append("&");
            url.append("type=").append(type.name());
            hasParam = true;
        }
        if (hasWindow != null) {
            if (hasParam) url.append("&");
            url.append("hasWindow=").append(hasWindow);
            hasParam = true;
        }
        if (hasPowerOutlet != null) {
            if (hasParam) url.append("&");
            url.append("hasPowerOutlet=").append(hasPowerOutlet);
            hasParam = true;
        }
        if (hasLamp != null) {
            if (hasParam) url.append("&");
            url.append("hasLamp=").append(hasLamp);
        }
        
        return httpClient.get(url.toString())
            .thenApply(this::parseSeatListResponse)
            .whenComplete((seats, throwable) -> {
                if (throwable == null && seats != null) {
                    logger.debug("✅ 搜索座位成功: 找到{}个匹配结果", seats.size());
                } else {
                    logger.warn("❌ 搜索座位失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 创建座位（管理员功能）
     */
    public CompletableFuture<Seat> createSeat(Seat seat) {
        logger.info("📝 创建座位: {}", seat.getSeatNumber());
        
        Map<String, Object> seatData = new HashMap<>();
        seatData.put("seatNumber", seat.getSeatNumber());
        seatData.put("type", seat.getType() != null ? seat.getType().name() : null); // Send enum name
        // hourlyRate is not a seat property on the server for creation
        seatData.put("hasWindow", seat.getHasWindow());
        seatData.put("hasPowerOutlet", seat.getHasPowerOutlet());
        seatData.put("hasLamp", seat.getHasLamp());
        seatData.put("studyRoomId", seat.getStudyRoomId());
        seatData.put("rowNum", seat.getRowNum());
        seatData.put("colNum", seat.getColNum());
        // Description and equipment are not part of the client Seat model directly,
        // but server createSeat accepts them. If they were on client model, they'd be added here.
        // seatData.put("description", seat.getDescription());
        // seatData.put("equipment", seat.getEquipment());
        
        return httpClient.post("/seats", seatData)
            .thenApply(this::parseSeatResponse)
            .whenComplete((newSeat, throwable) -> {
                if (throwable == null && newSeat != null) {
                    logger.info("✅ 座位创建成功: {}", newSeat.getSeatNumber());
                } else {
                    logger.error("❌ 座位创建失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 更新座位信息（管理员功能）
     */
    public CompletableFuture<Seat> updateSeat(Seat seat) {
        logger.info("📝 更新座位信息: {}", seat.getSeatNumber());
        
        Map<String, Object> seatData = new HashMap<>();
        seatData.put("seatNumber", seat.getSeatNumber());
        seatData.put("type", seat.getType() != null ? seat.getType().name() : null); // Send enum name
        // hourlyRate is not a seat property
        seatData.put("hasWindow", seat.getHasWindow());
        seatData.put("hasPowerOutlet", seat.getHasPowerOutlet());
        seatData.put("hasLamp", seat.getHasLamp());
        // Status is updated via a separate endpoint PUT /seats/{seatId}/status
        // Description and equipment are not part of the client Seat model directly for update here.
        // if (seat.getDescription() != null) seatData.put("description", seat.getDescription());
        // if (seat.getEquipment() != null) seatData.put("equipment", seat.getEquipment());
        if (seat.getRowNum() != null) { // Only include if set, server might not allow unsetting via null
            seatData.put("rowNum", seat.getRowNum());
        }
        if (seat.getColNum() != null) {
            seatData.put("colNum", seat.getColNum());
        }
        
        return httpClient.put("/seats/" + seat.getId(), seatData)
            .thenApply(this::parseSeatResponse)
            .whenComplete((updatedSeat, throwable) -> {
                if (throwable == null && updatedSeat != null) {
                    logger.info("✅ 座位信息更新成功: {}", updatedSeat.getSeatNumber());
                } else {
                    logger.error("❌ 座位信息更新失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 删除座位（管理员功能）
     */
    public CompletableFuture<Boolean> deleteSeat(Long seatId) {
        logger.info("🗑️ 删除座位: ID={}", seatId);
        
        return httpClient.delete("/seats/" + seatId)
            .thenApply(this::parseBooleanResponse)
            .whenComplete((success, throwable) -> {
                if (throwable == null && success) {
                    logger.info("✅ 座位删除成功: ID={}", seatId);
                } else {
                    logger.error("❌ 座位删除失败: ID={}, 错误={}", seatId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 更新座位状态
     */
    public CompletableFuture<Seat> updateSeatStatus(Long seatId, Seat.Status status) {
        logger.info("📝 更新座位状态: ID={}, status={}", seatId, status);
        
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("status", status);
        
        return httpClient.put("/seats/" + seatId + "/status", statusData)
            .thenApply(this::parseSeatResponse)
            .whenComplete((updatedSeat, throwable) -> {
                if (throwable == null && updatedSeat != null) {
                    logger.info("✅ 座位状态更新成功: {} -> {}", updatedSeat.getSeatNumber(), status.getDisplayName());
                } else {
                    logger.error("❌ 座位状态更新失败: ID={}, 错误={}", seatId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取座位统计信息
     */
    public CompletableFuture<Map<String, Object>> getSeatStatistics(Long seatId) {
        logger.debug("📊 获取座位统计信息: ID={}", seatId);
        
        return httpClient.get("/seats/" + seatId + "/statistics")
            .thenApply(this::parseStatisticsResponse)
            .whenComplete((stats, throwable) -> {
                if (throwable == null && stats != null) {
                    logger.debug("✅ 获取座位统计信息成功: ID={}", seatId);
                } else {
                    logger.warn("❌ 获取座位统计信息失败: ID={}, 错误={}", seatId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取自习室座位统计信息
     */
    public CompletableFuture<Map<String, Object>> getStudyRoomSeatStatistics(Long studyRoomId) {
        logger.debug("📊 获取自习室座位统计: studyRoomId={}", studyRoomId);
        
        return httpClient.get("/seats/study-room/" + studyRoomId + "/statistics")
            .thenApply(this::parseStatisticsResponse)
            .whenComplete((stats, throwable) -> {
                if (throwable == null && stats != null) {
                    logger.debug("✅ 获取座位统计信息成功: studyRoomId={}", studyRoomId);
                } else {
                    logger.warn("❌ 获取座位统计信息失败: studyRoomId={}, 错误={}", studyRoomId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    // 私有辅助方法

    /**
     * 解析座位响应
     */
    private Seat parseSeatResponse(String jsonResponse) {
        try {
            ApiResponse<Seat> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<Seat>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析座位响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析座位列表响应
     */
    private List<Seat> parseSeatListResponse(String jsonResponse) {
        try {
            ApiResponse<List<Seat>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<List<Seat>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析座位列表响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析座位分页响应
     */
    private PageData<Seat> parseSeatPageResponse(String jsonResponse) {
        try {
            ApiResponse<PageData<Seat>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<PageData<Seat>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                PageData<Seat> pageData = apiResponse.getData();
                if (pageData != null) {
                    // Ensure content is correctly deserialized if it's a list of maps initially
                    if (pageData.getContent() != null && !pageData.getContent().isEmpty() && !(pageData.getContent().get(0) instanceof Seat)) {
                        List<Seat> seats = pageData.getContent().stream()
                            .map(item -> objectMapper.convertValue(item, Seat.class))
                            .collect(java.util.stream.Collectors.toList());
                        pageData.setContent(seats);
                    }
                     logger.debug("✅ 座位分页数据解析成功: 当前页={}, 总页数={}, 总记录数={}, 当前页记录数={}",
                        pageData.getPageNumber(), pageData.getTotalPages(), pageData.getTotalElements(), pageData.getContent() != null ? pageData.getContent().size() : 0);
                    return pageData;
                } else {
                    logger.error("❌ 座位分页响应数据为空");
                    throw new RuntimeException("API错误: 响应数据为空");
                }
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析座位分页响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
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
                         if (dataMap.containsKey("available") && dataMap.get("available") instanceof Boolean) {
                            logger.warn("⚠️ API returned success code but boolean data was in a map for: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                            return (Boolean) dataMap.get("available");
                        }
                    }
                    logger.error("❌ API请求失败 (Boolean direct): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                    throw new RuntimeException("API错误: " + apiResponse.getMessage());
                }
            } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                // If direct Boolean parsing fails, try parsing as ApiResponse<Map<String, Boolean>>
                logger.warn("⚠️ Direct boolean parsing failed for SeatApi, attempting to parse as Map<String, Boolean>: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                ApiResponse<Map<String, Boolean>> apiResponseMap = objectMapper.readValue(jsonResponse,
                        new TypeReference<ApiResponse<Map<String, Boolean>>>() {});

                if (apiResponseMap.getCode() == 200) { // Assuming 200 is success
                    Map<String, Boolean> dataMap = apiResponseMap.getData();
                     if (dataMap != null) {
                        if (dataMap.containsKey("success")) return dataMap.get("success");
                        if (dataMap.containsKey("available")) return dataMap.get("available");
                        logger.warn("⚠️ Boolean response map did not contain 'success' or 'available' key: {}", dataMap);
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