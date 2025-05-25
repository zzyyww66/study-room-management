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
        logger.debug("🔍 搜索座位: studyRoomId={}, type={}, hasWindow={}, hasPowerOutlet={}, hasLamp={}", 
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
        seatData.put("type", seat.getType());
        seatData.put("hourlyRate", seat.getHourlyRate());
        seatData.put("hasWindow", seat.getHasWindow());
        seatData.put("hasPowerOutlet", seat.getHasPowerOutlet());
        seatData.put("hasLamp", seat.getHasLamp());
        seatData.put("studyRoomId", seat.getStudyRoomId());
        
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
        seatData.put("type", seat.getType());
        seatData.put("hourlyRate", seat.getHourlyRate());
        seatData.put("hasWindow", seat.getHasWindow());
        seatData.put("hasPowerOutlet", seat.getHasPowerOutlet());
        seatData.put("hasLamp", seat.getHasLamp());
        seatData.put("status", seat.getStatus());
        
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
            ApiResponse<Seat> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<Seat>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析座位响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析座位列表响应
     */
    private List<Seat> parseSeatListResponse(String jsonResponse) {
        try {
            ApiResponse<List<Seat>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<List<Seat>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析座位列表响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析座位分页响应
     */
    private PageData<Seat> parseSeatPageResponse(String jsonResponse) {
        try {
            ApiResponse<PageData<Seat>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<PageData<Seat>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析座位分页响应失败", e);
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