package com.studyroom.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * API服务管理器
 * 统一管理所有API服务，提供便捷的访问接口
 * 
 * @author Developer
 * @version 1.0.0
 */
public class ApiServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ApiServiceManager.class);
    
    // 单例实例
    private static ApiServiceManager instance;
    
    // 所有API服务
    private final HttpClientService httpClientService;
    private final UserApiService userApiService;
    private final StudyRoomApiService studyRoomApiService;
    private final SeatApiService seatApiService;
    private final ReservationApiService reservationApiService;
    
    // 其他服务
    private final AsyncDataManager asyncDataManager;
    private final DataBindingService dataBindingService;
    
    // 服务状态
    private boolean initialized = false;
    private String serverUrl = "http://localhost:8080";

    /**
     * 私有构造函数 - 单例模式
     */
    private ApiServiceManager() {
        // 初始化基础服务
        this.httpClientService = HttpClientService.getInstance();
        this.asyncDataManager = AsyncDataManager.getInstance();
        this.dataBindingService = DataBindingService.getInstance();
        
        // 初始化API服务
        this.userApiService = UserApiService.getInstance();
        this.studyRoomApiService = StudyRoomApiService.getInstance();
        this.seatApiService = SeatApiService.getInstance();
        this.reservationApiService = ReservationApiService.getInstance();
        
        logger.info("🌐 API服务管理器初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized ApiServiceManager getInstance() {
        if (instance == null) {
            instance = new ApiServiceManager();
        }
        return instance;
    }

    /**
     * 初始化服务
     */
    public CompletableFuture<Boolean> initialize(String serverUrl) {
        this.serverUrl = serverUrl;
        logger.info("🚀 初始化API服务管理器，服务器地址: {}", serverUrl);
        
        // 设置服务器地址
        httpClientService.setBaseUrl(serverUrl);
        
        // 测试服务器连接
        return httpClientService.testConnection()
            .thenApply(connected -> {
                if (connected) {
                    initialized = true;
                    logger.info("✅ API服务管理器初始化成功");
                    return true;
                } else {
                    logger.error("❌ 无法连接到服务器: {}", serverUrl);
                    return false;
                }
            })
            .exceptionally(throwable -> {
                logger.error("❌ API服务管理器初始化失败", throwable);
                return false;
            });
    }

    /**
     * 设置认证令牌
     */
    public void setAuthToken(String token) {
        httpClientService.setAuthToken(token);
        logger.info("🔑 设置认证令牌");
    }

    /**
     * 清除认证令牌
     */
    public void clearAuthToken() {
        httpClientService.clearAuthToken();
        dataBindingService.clearCurrentUser();
        logger.info("🗑️ 清除认证令牌");
    }

    /**
     * 检查服务是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 检查服务器连接状态
     */
    public CompletableFuture<Boolean> checkServerConnection() {
        return httpClientService.testConnection();
    }

    /**
     * 获取服务器地址
     */
    public String getServerUrl() {
        return serverUrl;
    }

    // ==== API服务访问器 ====

    /**
     * 获取HTTP客户端服务
     */
    public HttpClientService getHttpClientService() {
        return httpClientService;
    }

    /**
     * 获取用户API服务
     */
    public UserApiService getUserApiService() {
        return userApiService;
    }

    /**
     * 获取自习室API服务
     */
    public StudyRoomApiService getStudyRoomApiService() {
        return studyRoomApiService;
    }

    /**
     * 获取座位API服务
     */
    public SeatApiService getSeatApiService() {
        return seatApiService;
    }

    /**
     * 获取预订API服务
     */
    public ReservationApiService getReservationApiService() {
        return reservationApiService;
    }

    /**
     * 获取异步数据管理器
     */
    public AsyncDataManager getAsyncDataManager() {
        return asyncDataManager;
    }

    /**
     * 获取数据绑定服务
     */
    public DataBindingService getDataBindingService() {
        return dataBindingService;
    }

    // ==== 便捷方法 ====

    /**
     * 执行用户登录
     */
    public CompletableFuture<Boolean> login(String username, String password) {
        if (!initialized) {
            return CompletableFuture.completedFuture(false);
        }
        
        return userApiService.login(username, password)
            .thenApply(user -> {
                if (user != null) {
                    // 设置当前用户
                    dataBindingService.setCurrentUser(user);
                    // 刷新相关数据
                    dataBindingService.refreshAllData();
                    logger.info("✅ 用户登录成功: {}", user.getUsername());
                    return true;
                } else {
                    logger.warn("❌ 用户登录失败：用户信息为空");
                    return false;
                }
            })
            .exceptionally(throwable -> {
                logger.error("❌ 用户登录失败", throwable);
                return false;
            });
    }

    /**
     * 执行用户登出
     */
    public void logout() {
        clearAuthToken();
        logger.info("👋 用户已登出");
    }

    /**
     * 获取系统统计信息
     */
    public CompletableFuture<SystemStatistics> getSystemStatistics() {
        if (!initialized) {
            return CompletableFuture.completedFuture(new SystemStatistics());
        }
        
        // 并行获取各项统计信息
        CompletableFuture<Map<String, Object>> studyRoomStats = studyRoomApiService.getOverallStatistics();
        CompletableFuture<Map<String, Object>> reservationStats = reservationApiService.getReservationStatistics(null);
        
        return CompletableFuture.allOf(studyRoomStats, reservationStats)
            .thenApply(v -> {
                try {
                    SystemStatistics stats = new SystemStatistics();
                    
                    // 处理自习室统计
                    Map<String, Object> roomStatsData = studyRoomStats.get();
                    if (roomStatsData != null) {
                        stats.setTotalStudyRooms(getIntValue(roomStatsData, "totalRooms", 0));
                        stats.setTotalSeats(getIntValue(roomStatsData, "totalSeats", 0));
                        stats.setAvailableSeats(getIntValue(roomStatsData, "availableSeats", 0));
                    }
                    
                    // 处理预订统计
                    Map<String, Object> reservationStatsData = reservationStats.get();
                    if (reservationStatsData != null) {
                        stats.setTotalReservations(getIntValue(reservationStatsData, "totalReservations", 0));
                        stats.setActiveReservations(getIntValue(reservationStatsData, "activeReservations", 0));
                        stats.setTodayReservations(getIntValue(reservationStatsData, "todayReservations", 0));
                    }
                    
                    return stats;
                } catch (Exception e) {
                    logger.error("❌ 处理系统统计信息失败", e);
                    return new SystemStatistics();
                }
            })
            .exceptionally(throwable -> {
                logger.error("❌ 获取系统统计信息失败", throwable);
                return new SystemStatistics();
            });
    }

    /**
     * 辅助方法：安全获取整数值
     */
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * 刷新所有缓存数据
     */
    public void refreshAllData() {
        if (initialized && dataBindingService.isLoggedInProperty().get()) {
            dataBindingService.refreshAllData();
            logger.info("🔄 刷新所有缓存数据");
        }
    }

    /**
     * 设置自动刷新配置
     */
    public void configureAutoRefresh(boolean enabled, int intervalSeconds) {
        dataBindingService.setAutoRefreshEnabled(enabled);
        dataBindingService.setRefreshInterval(intervalSeconds);
        logger.info("🔄 配置自动刷新: {} ({}秒)", enabled ? "启用" : "禁用", intervalSeconds);
    }

    /**
     * 获取活跃任务数
     */
    public int getActiveTaskCount() {
        return asyncDataManager.getActiveTaskCount();
    }

    /**
     * 检查是否有任务正在执行
     */
    public boolean hasActiveTasks() {
        return asyncDataManager.hasActiveTasks();
    }

    /**
     * 关闭所有服务
     */
    public void shutdown() {
        logger.info("🔒 关闭API服务管理器");
        
        try {
            // 关闭数据绑定服务
            dataBindingService.shutdown();
            
            // 关闭异步数据管理器
            asyncDataManager.shutdown();
            
            // 关闭HTTP客户端
            httpClientService.close();
            
            initialized = false;
            logger.info("✅ API服务管理器已安全关闭");
        } catch (Exception e) {
            logger.error("❌ 关闭API服务管理器时发生错误", e);
        }
    }

    /**
     * 系统统计信息类
     */
    public static class SystemStatistics {
        private int totalStudyRooms = 0;
        private int totalSeats = 0;
        private int availableSeats = 0;
        private int totalReservations = 0;
        private int activeReservations = 0;
        private int todayReservations = 0;

        // Getter 和 Setter 方法
        public int getTotalStudyRooms() { return totalStudyRooms; }
        public void setTotalStudyRooms(int totalStudyRooms) { this.totalStudyRooms = totalStudyRooms; }

        public int getTotalSeats() { return totalSeats; }
        public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

        public int getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

        public int getTotalReservations() { return totalReservations; }
        public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }

        public int getActiveReservations() { return activeReservations; }
        public void setActiveReservations(int activeReservations) { this.activeReservations = activeReservations; }

        public int getTodayReservations() { return todayReservations; }
        public void setTodayReservations(int todayReservations) { this.todayReservations = todayReservations; }

        /**
         * 计算座位利用率（百分比）
         */
        public double getSeatUtilizationRate() {
            if (totalSeats == 0) return 0.0;
            return ((totalSeats - availableSeats) * 100.0) / totalSeats;
        }

        @Override
        public String toString() {
            return "SystemStatistics{" +
                    "totalStudyRooms=" + totalStudyRooms +
                    ", totalSeats=" + totalSeats +
                    ", availableSeats=" + availableSeats +
                    ", totalReservations=" + totalReservations +
                    ", activeReservations=" + activeReservations +
                    ", todayReservations=" + todayReservations +
                    ", utilizationRate=" + String.format("%.1f%%", getSeatUtilizationRate()) +
                    '}';
        }
    }
} 