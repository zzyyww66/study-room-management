package com.studyroom.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.client.model.ApiResponse;
import com.studyroom.client.model.PageData;
import com.studyroom.client.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 预订API服务类
 * 负责与后端预订相关API的通信
 * 
 * @author Developer
 * @version 1.0.0
 */
public class ReservationApiService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationApiService.class);
    
    // 单例实例
    private static ReservationApiService instance;
    
    // HTTP客户端服务
    private final HttpClientService httpClient;
    
    // JSON处理器
    private final ObjectMapper objectMapper;
    
    // 日期时间格式器
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 私有构造函数 - 单例模式
     */
    private ReservationApiService() {
        this.httpClient = HttpClientService.getInstance();
        this.objectMapper = httpClient.getObjectMapper();
        logger.info("📅 预订API服务初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized ReservationApiService getInstance() {
        if (instance == null) {
            instance = new ReservationApiService();
        }
        return instance;
    }

    /**
     * 创建预订
     */
    public CompletableFuture<Reservation> createReservation(Long seatId, LocalDateTime startTime, 
                                                           LocalDateTime endTime, String note) {
        logger.info("📝 创建预订: seatId={}, 时间={} ~ {}", seatId, 
            startTime.format(dateTimeFormatter), endTime.format(dateTimeFormatter));
        
        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("seatId", seatId);
        reservationData.put("startTime", startTime.format(dateTimeFormatter));
        reservationData.put("endTime", endTime.format(dateTimeFormatter));
        if (note != null && !note.trim().isEmpty()) {
            reservationData.put("note", note.trim());
        }
        
        return httpClient.post("/reservations", reservationData)
            .thenApply(this::parseReservationResponse)
            .whenComplete((reservation, throwable) -> {
                if (throwable == null && reservation != null) {
                    logger.info("✅ 预订创建成功: ID={}", reservation.getId());
                } else {
                    logger.error("❌ 预订创建失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 根据ID获取预订信息
     */
    public CompletableFuture<Reservation> getReservationById(Long reservationId) {
        logger.debug("🔍 获取预订信息: ID={}", reservationId);
        
        return httpClient.get("/reservations/" + reservationId)
            .thenApply(this::parseReservationResponse)
            .whenComplete((reservation, throwable) -> {
                if (throwable == null && reservation != null) {
                    logger.debug("✅ 获取预订信息成功: ID={}", reservation.getId());
                } else {
                    logger.warn("❌ 获取预订信息失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取当前用户的预订列表
     */
    public CompletableFuture<List<Reservation>> getMyReservations() {
        logger.debug("📋 获取我的预订列表");
        
        return httpClient.get("/reservations/my")
            .thenApply(this::parseReservationListResponse)
            .whenComplete((reservations, throwable) -> {
                if (throwable == null && reservations != null) {
                    logger.debug("✅ 获取预订列表成功: 共{}条记录", reservations.size());
                } else {
                    logger.warn("❌ 获取预订列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取当前用户的有效预订
     */
    public CompletableFuture<List<Reservation>> getMyActiveReservations() {
        logger.debug("📋 获取我的有效预订");
        
        return httpClient.get("/reservations/my/active")
            .thenApply(this::parseReservationListResponse)
            .whenComplete((reservations, throwable) -> {
                if (throwable == null && reservations != null) {
                    logger.debug("✅ 获取有效预订成功: 共{}条记录", reservations.size());
                } else {
                    logger.warn("❌ 获取有效预订失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 分页查询预订列表
     */
    public CompletableFuture<PageData<Reservation>> getReservations(int page, int size, Long userId, 
                                                                   Reservation.Status status, Reservation.PaymentStatus paymentStatus) {
        logger.debug("📋 查询预订列表: page={}, size={}, userId={}, status={}, paymentStatus={}", 
            page, size, userId, status, paymentStatus);
        
        StringBuilder url = new StringBuilder("/reservations?page=" + page + "&size=" + size);
        if (userId != null) {
            url.append("&userId=").append(userId);
        }
        if (status != null) {
            url.append("&status=").append(status.name());
        }
        if (paymentStatus != null) {
            url.append("&paymentStatus=").append(paymentStatus.name());
        }
        
        return httpClient.get(url.toString())
            .thenApply(this::parseReservationPageResponse)
            .whenComplete((pageData, throwable) -> {
                if (throwable == null && pageData != null) {
                    logger.debug("✅ 查询预订列表成功: 共{}条记录", pageData.getTotalElements());
                } else {
                    logger.warn("❌ 查询预订列表失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 检查座位时间冲突
     */
    public CompletableFuture<Boolean> checkTimeConflict(Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("🔍 检查时间冲突: seatId={}, 时间={} ~ {}", seatId, 
            startTime.format(dateTimeFormatter), endTime.format(dateTimeFormatter));
        
        StringBuilder url = new StringBuilder("/reservations/check-conflict?");
        url.append("seatId=").append(seatId);
        url.append("&startTime=").append(startTime.format(dateTimeFormatter));
        url.append("&endTime=").append(endTime.format(dateTimeFormatter));
        
        return httpClient.get(url.toString())
            .thenApply(this::parseBooleanResponse)
            .whenComplete((hasConflict, throwable) -> {
                if (throwable == null) {
                    logger.debug("✅ 时间冲突检查完成: {}", hasConflict ? "有冲突" : "无冲突");
                } else {
                    logger.warn("❌ 时间冲突检查失败: {}", throwable.getMessage());
                }
            });
    }

    /**
     * 更新预订信息
     */
    public CompletableFuture<Reservation> updateReservation(Reservation reservation) {
        logger.info("📝 更新预订信息: ID={}", reservation.getId());
        
        Map<String, Object> updateData = new HashMap<>();
        if (reservation.getStartTime() != null) {
            updateData.put("startTime", reservation.getStartTime().format(dateTimeFormatter));
        }
        if (reservation.getEndTime() != null) {
            updateData.put("endTime", reservation.getEndTime().format(dateTimeFormatter));
        }
        if (reservation.getNote() != null) {
            updateData.put("note", reservation.getNote());
        }
        
        return httpClient.put("/reservations/" + reservation.getId(), updateData)
            .thenApply(this::parseReservationResponse)
            .whenComplete((updatedReservation, throwable) -> {
                if (throwable == null && updatedReservation != null) {
                    logger.info("✅ 预订信息更新成功: ID={}", updatedReservation.getId());
                } else {
                    logger.error("❌ 预订信息更新失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 取消预订
     */
    public CompletableFuture<Reservation> cancelReservation(Long reservationId) {
        logger.info("❌ 取消预订: ID={}", reservationId);
        
        return httpClient.put("/reservations/" + reservationId + "/cancel", null)
            .thenApply(this::parseReservationResponse)
            .whenComplete((cancelledReservation, throwable) -> {
                if (throwable == null && cancelledReservation != null) {
                    logger.info("✅ 预订取消成功: ID={}", cancelledReservation.getId());
                } else {
                    logger.error("❌ 预订取消失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 确认预订
     */
    public CompletableFuture<Reservation> confirmReservation(Long reservationId) {
        logger.info("✅ 确认预订: ID={}", reservationId);
        
        return httpClient.put("/reservations/" + reservationId + "/confirm", null)
            .thenApply(this::parseReservationResponse)
            .whenComplete((confirmedReservation, throwable) -> {
                if (throwable == null && confirmedReservation != null) {
                    logger.info("✅ 预订确认成功: ID={}", confirmedReservation.getId());
                } else {
                    logger.error("❌ 预订确认失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 支付预订
     */
    public CompletableFuture<Reservation> payReservation(Long reservationId) {
        logger.info("💰 支付预订: ID={}", reservationId);
        
        return httpClient.put("/reservations/" + reservationId + "/pay", null)
            .thenApply(this::parseReservationResponse)
            .whenComplete((paidReservation, throwable) -> {
                if (throwable == null && paidReservation != null) {
                    logger.info("✅ 预订支付成功: ID={}", paidReservation.getId());
                } else {
                    logger.error("❌ 预订支付失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 签到（开始使用）
     */
    public CompletableFuture<Reservation> checkIn(Long reservationId) {
        logger.info("🚪 签到入住: ID={}", reservationId);
        
        return httpClient.put("/reservations/" + reservationId + "/check-in", null)
            .thenApply(this::parseReservationResponse)
            .whenComplete((checkedInReservation, throwable) -> {
                if (throwable == null && checkedInReservation != null) {
                    logger.info("✅ 签到成功: ID={}", checkedInReservation.getId());
                } else {
                    logger.error("❌ 签到失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 签退（结束使用）
     */
    public CompletableFuture<Reservation> checkOut(Long reservationId) {
        logger.info("🚶 签退离开: ID={}", reservationId);
        
        return httpClient.put("/reservations/" + reservationId + "/check-out", null)
            .thenApply(this::parseReservationResponse)
            .whenComplete((checkedOutReservation, throwable) -> {
                if (throwable == null && checkedOutReservation != null) {
                    logger.info("✅ 签退成功: ID={}", checkedOutReservation.getId());
                } else {
                    logger.error("❌ 签退失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 延长预订时间
     */
    public CompletableFuture<Reservation> extendReservation(Long reservationId, LocalDateTime newEndTime) {
        logger.info("⏰ 延长预订: ID={}, 新结束时间={}", reservationId, newEndTime.format(dateTimeFormatter));
        
        Map<String, Object> extendData = new HashMap<>();
        extendData.put("newEndTime", newEndTime.format(dateTimeFormatter));
        
        return httpClient.put("/reservations/" + reservationId + "/extend", extendData)
            .thenApply(this::parseReservationResponse)
            .whenComplete((extendedReservation, throwable) -> {
                if (throwable == null && extendedReservation != null) {
                    logger.info("✅ 预订延长成功: ID={}", extendedReservation.getId());
                } else {
                    logger.error("❌ 预订延长失败: ID={}, 错误={}", reservationId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取预订统计信息
     */
    public CompletableFuture<Map<String, Object>> getReservationStatistics(Long userId) {
        logger.debug("📊 获取预订统计信息: userId={}", userId);
        
        String url = userId != null ? 
            "/reservations/statistics?userId=" + userId : 
            "/reservations/statistics";
        
        return httpClient.get(url)
            .thenApply(this::parseStatisticsResponse)
            .whenComplete((stats, throwable) -> {
                if (throwable == null && stats != null) {
                    logger.debug("✅ 获取预订统计信息成功: userId={}", userId);
                } else {
                    logger.warn("❌ 获取预订统计信息失败: userId={}, 错误={}", userId, 
                        throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取今日预订列表
     */
    public CompletableFuture<List<Reservation>> getTodayReservations() {
        logger.debug("📋 获取今日预订列表");
        
        return httpClient.get("/reservations/today")
            .thenApply(this::parseReservationListResponse)
            .whenComplete((reservations, throwable) -> {
                if (throwable == null && reservations != null) {
                    logger.debug("✅ 获取今日预订成功: 共{}条记录", reservations.size());
                } else {
                    logger.warn("❌ 获取今日预订失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    /**
     * 获取即将到期的预订列表
     */
    public CompletableFuture<List<Reservation>> getExpiringReservations() {
        logger.debug("📋 获取即将到期的预订");
        
        return httpClient.get("/reservations/expiring")
            .thenApply(this::parseReservationListResponse)
            .whenComplete((reservations, throwable) -> {
                if (throwable == null && reservations != null) {
                    logger.debug("✅ 获取即将到期预订成功: 共{}条记录", reservations.size());
                } else {
                    logger.warn("❌ 获取即将到期预订失败: {}", throwable != null ? throwable.getMessage() : "未知错误");
                }
            });
    }

    // 私有辅助方法

    /**
     * 解析预订响应
     */
    private Reservation parseReservationResponse(String jsonResponse) {
        try {
            ApiResponse<Reservation> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<Reservation>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析预订响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析预订列表响应
     */
    private List<Reservation> parseReservationListResponse(String jsonResponse) {
        try {
            ApiResponse<List<Reservation>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<List<Reservation>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                return apiResponse.getData();
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析预订列表响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析预订分页响应
     */
    private PageData<Reservation> parseReservationPageResponse(String jsonResponse) {
        try {
            ApiResponse<PageData<Reservation>> apiResponse = objectMapper.readValue(jsonResponse,
                new TypeReference<ApiResponse<PageData<Reservation>>>() {});

            if (apiResponse.getCode() == 200) { // Assuming 200 is success
                PageData<Reservation> pageData = apiResponse.getData();
                if (pageData != null) {
                    // Ensure content is correctly deserialized if it's a list of maps initially
                    if (pageData.getContent() != null && !pageData.getContent().isEmpty() && !(pageData.getContent().get(0) instanceof Reservation)) {
                        List<Reservation> reservations = pageData.getContent().stream()
                            .map(item -> objectMapper.convertValue(item, Reservation.class))
                            .collect(java.util.stream.Collectors.toList());
                        pageData.setContent(reservations);
                    }
                    logger.debug("✅ 预订分页数据解析成功: 当前页={}, 总页数={}, 总记录数={}, 当前页记录数={}",
                        pageData.getPageNumber(), pageData.getTotalPages(), pageData.getTotalElements(), pageData.getContent() != null ? pageData.getContent().size() : 0);
                    return pageData;
                } else {
                    logger.error("❌ 预订分页响应数据为空");
                    throw new RuntimeException("API错误: 响应数据为空");
                }
            } else {
                logger.error("❌ API请求失败: Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                throw new RuntimeException("API错误: " + apiResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析预订分页响应失败: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)), e);
            throw new RuntimeException("预订数据解析失败: " + e.getMessage(), e);
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
                    // Check if data itself is the boolean or if it's a map like {"hasConflict": false}
                     Object data = apiResponse.getData();
                    if (data instanceof Boolean) {
                        return (Boolean) data;
                    } else if (data instanceof Map) {
                        Map<?,?> dataMap = (Map<?,?>) data;
                        if (dataMap.containsKey("hasConflict") && dataMap.get("hasConflict") instanceof Boolean) {
                             logger.warn("⚠️ Boolean data for 'hasConflict' was in a map for: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                            return (Boolean) dataMap.get("hasConflict");
                        }
                        if (dataMap.containsKey("success") && dataMap.get("success") instanceof Boolean) {
                             logger.warn("⚠️ Boolean data for 'success' was in a map for: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                            return (Boolean) dataMap.get("success");
                        }
                         logger.warn("⚠️ Boolean response map did not contain expected boolean key: {}", dataMap);
                         return !dataMap.isEmpty(); // Default if map is not empty
                    }
                    return false; // Default if data is null or not a recognized structure
                } else {
                    logger.error("❌ API请求失败 (Boolean direct): Code={}, Message={}", apiResponse.getCode(), apiResponse.getMessage());
                    throw new RuntimeException("API错误: " + apiResponse.getMessage());
                }
            } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                // If direct Boolean parsing fails, try parsing as ApiResponse<Map<String, Boolean>>
                logger.warn("⚠️ Direct boolean parsing failed for ReservationApi, attempting to parse as Map<String, Boolean>: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                ApiResponse<Map<String, Boolean>> apiResponseMap = objectMapper.readValue(jsonResponse,
                        new TypeReference<ApiResponse<Map<String, Boolean>>>() {});

                if (apiResponseMap.getCode() == 200) { // Assuming 200 is success
                    Map<String, Boolean> dataMap = apiResponseMap.getData();
                     if (dataMap != null) {
                        if (dataMap.containsKey("hasConflict")) return dataMap.get("hasConflict");
                        if (dataMap.containsKey("success")) return dataMap.get("success");
                        logger.warn("⚠️ Boolean response map did not contain 'hasConflict' or 'success' key: {}", dataMap);
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