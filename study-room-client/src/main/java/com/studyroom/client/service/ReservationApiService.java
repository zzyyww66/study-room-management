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
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            ApiResponse<Reservation> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<Reservation>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析预订响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析预订列表响应
     */
    private List<Reservation> parseReservationListResponse(String jsonResponse) {
        try {
            ApiResponse<List<Reservation>> response = objectMapper.readValue(jsonResponse, 
                new TypeReference<ApiResponse<List<Reservation>>>() {});
            
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("API错误: " + response.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ 解析预订列表响应失败", e);
            throw new RuntimeException("数据解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析预订分页响应
     */
    private PageData<Reservation> parseReservationPageResponse(String jsonResponse) {
        try {
            logger.debug("🔄 解析预订分页响应: {}", jsonResponse.substring(0, Math.min(200, jsonResponse.length())));

            // 直接解析后端返回的实际格式 (类似StudyRoomApiService的修复)
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);

            Boolean success = (Boolean) responseMap.get("success");
            // 后端直接返回分页对象，不一定有success字段，但我们需要content列表
            // if (success == null || !success) {
            //     String message = (String) responseMap.getOrDefault("message", "未知API错误");
            //     logger.error("API响应错误: {}", message);
            //     throw new RuntimeException("API错误: " + message);
            // }

            List<Map<String, Object>> reservationsData = (List<Map<String, Object>>) responseMap.get("content"); // 后端Page对象通常用content
            if (reservationsData == null) {
                // 尝试另一种可能的key，如后端直接返回reservations数组
                reservationsData = (List<Map<String, Object>>) responseMap.get("reservations");
                if (reservationsData == null) {
                     logger.warn("分页响应中未找到 'content' 或 'reservations' 预订列表");
                     reservationsData = new java.util.ArrayList<>(); // 返回空列表避免NPE
                }
            }

            Integer totalPages = (Integer) responseMap.get("totalPages");
            Long totalElements = null;
            Object totalElementsObj = responseMap.get("totalElements");
            if (totalElementsObj instanceof Integer) {
                totalElements = ((Integer) totalElementsObj).longValue();
            } else if (totalElementsObj instanceof Long) {
                totalElements = (Long) totalElementsObj;
            }
            
            // Integer currentPage = (Integer) responseMap.get("currentPage"); // Spring Page 是0-indexed, FXML通常1-indexed
            // int number = responseMap.get("number") != null ? (Integer)responseMap.get("number") : 0;

            List<Reservation> reservations = reservationsData.stream()
                .map(data -> objectMapper.convertValue(data, Reservation.class))
                .collect(java.util.stream.Collectors.toList());

            PageData<Reservation> pageData = new PageData<>();
            pageData.setContent(reservations);
            pageData.setTotalElements(totalElements != null ? totalElements : 0L);
            pageData.setTotalPages(totalPages != null ? totalPages : 0);
            // pageData.setCurrentPage(number); // 如果需要当前页码

            logger.debug("✅ 预订分页数据解析成功: 总页数={}, 总记录数={}, 当前页记录数={}",
                totalPages, totalElements, reservations.size());

            return pageData;

        } catch (Exception e) {
            logger.error("❌ 解析预订分页响应失败: {}", e.getMessage(), e);
            logger.debug("原始预订分页响应: {}", jsonResponse);
            throw new RuntimeException("预订数据解析失败: " + e.getMessage(), e);
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