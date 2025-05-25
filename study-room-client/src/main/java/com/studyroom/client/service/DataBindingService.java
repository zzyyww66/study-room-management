package com.studyroom.client.service;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.studyroom.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 数据绑定服务
 * 实现数据的观察者模式和自动更新功能
 * 
 * @author Developer
 * @version 1.0.0
 */
public class DataBindingService {

    private static final Logger logger = LoggerFactory.getLogger(DataBindingService.class);
    
    // 单例实例
    private static DataBindingService instance;
    
    // 定时刷新执行器
    private final ScheduledExecutorService scheduler;
    
    // API服务引用
    private final UserApiService userApiService;
    private final StudyRoomApiService studyRoomApiService;
    private final SeatApiService seatApiService;
    private final ReservationApiService reservationApiService;
    
    // 数据观察者集合
    private final Map<String, Set<Consumer<Object>>> dataObservers = new ConcurrentHashMap<>();
    
    // 可观察数据属性
    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    private final ObservableList<StudyRoom> studyRooms = FXCollections.observableArrayList();
    private final ObservableList<Seat> seats = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private final ObservableList<Reservation> myReservations = FXCollections.observableArrayList();
    
    // 统计数据属性
    private final IntegerProperty totalUsers = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStudyRooms = new SimpleIntegerProperty(0);
    private final IntegerProperty totalSeats = new SimpleIntegerProperty(0);
    private final IntegerProperty totalReservations = new SimpleIntegerProperty(0);
    private final IntegerProperty activeReservations = new SimpleIntegerProperty(0);
    
    // 状态属性
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final StringProperty lastUpdateTime = new SimpleStringProperty();
    
    // 自动刷新设置
    private volatile boolean autoRefreshEnabled = true;
    private volatile int refreshIntervalSeconds = 30;

    /**
     * 私有构造函数 - 单例模式
     */
    private DataBindingService() {
        this.scheduler = new ScheduledThreadPoolExecutor(2, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("DataBinding-" + thread.getId());
            return thread;
        });
        
        this.userApiService = UserApiService.getInstance();
        this.studyRoomApiService = StudyRoomApiService.getInstance();
        this.seatApiService = SeatApiService.getInstance();
        this.reservationApiService = ReservationApiService.getInstance();
        
        // 启动自动刷新
        startAutoRefresh();
        
        logger.info("🔗 数据绑定服务初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized DataBindingService getInstance() {
        if (instance == null) {
            instance = new DataBindingService();
        }
        return instance;
    }

    // ==== 数据观察者管理 ====

    /**
     * 注册数据观察者
     */
    public void addDataObserver(String dataType, Consumer<Object> observer) {
        dataObservers.computeIfAbsent(dataType, k -> new HashSet<>()).add(observer);
        logger.debug("📡 注册数据观察者: {}", dataType);
    }

    /**
     * 移除数据观察者
     */
    public void removeDataObserver(String dataType, Consumer<Object> observer) {
        Set<Consumer<Object>> observers = dataObservers.get(dataType);
        if (observers != null) {
            observers.remove(observer);
            if (observers.isEmpty()) {
                dataObservers.remove(dataType);
            }
        }
        logger.debug("📡 移除数据观察者: {}", dataType);
    }

    /**
     * 通知数据观察者
     */
    private void notifyDataObservers(String dataType, Object data) {
        Set<Consumer<Object>> observers = dataObservers.get(dataType);
        if (observers != null) {
            Platform.runLater(() -> {
                observers.forEach(observer -> {
                    try {
                        observer.accept(data);
                    } catch (Exception e) {
                        logger.error("❌ 数据观察者执行失败: {}", dataType, e);
                    }
                });
            });
        }
    }

    // ==== 用户数据管理 ====

    /**
     * 设置当前登录用户
     */
    public void setCurrentUser(User user) {
        Platform.runLater(() -> {
            currentUser.set(user);
            isLoggedIn.set(user != null);
            notifyDataObservers("currentUser", user);
            logger.info("👤 设置当前用户: {}", user != null ? user.getUsername() : "null");
        });
    }

    /**
     * 清除当前用户
     */
    public void clearCurrentUser() {
        setCurrentUser(null);
    }

    /**
     * 刷新当前用户信息
     */
    public void refreshCurrentUser() {
        if (currentUser.get() != null) {
            userApiService.getCurrentUser()
                .thenAccept(this::setCurrentUser)
                .exceptionally(throwable -> {
                    logger.error("❌ 刷新用户信息失败", throwable);
                    return null;
                });
        }
    }

    // ==== 自习室数据管理 ====

    /**
     * 刷新自习室列表
     */
    public void refreshStudyRooms() {
        setLoading(true);
        studyRoomApiService.getAllStudyRooms()
            .thenAccept(rooms -> Platform.runLater(() -> {
                studyRooms.setAll(rooms);
                totalStudyRooms.set(rooms.size());
                notifyDataObservers("studyRooms", rooms);
                updateLastUpdateTime();
                logger.debug("🏢 刷新自习室列表: {}个", rooms.size());
            }))
            .exceptionally(throwable -> {
                logger.error("❌ 刷新自习室列表失败", throwable);
                return null;
            })
            .whenComplete((result, throwable) -> setLoading(false));
    }

    /**
     * 添加自习室
     */
    public void addStudyRoom(StudyRoom room) {
        Platform.runLater(() -> {
            studyRooms.add(room);
            totalStudyRooms.set(studyRooms.size());
            notifyDataObservers("studyRoomAdded", room);
        });
    }

    /**
     * 更新自习室
     */
    public void updateStudyRoom(StudyRoom updatedRoom) {
        Platform.runLater(() -> {
            for (int i = 0; i < studyRooms.size(); i++) {
                if (studyRooms.get(i).getId().equals(updatedRoom.getId())) {
                    studyRooms.set(i, updatedRoom);
                    notifyDataObservers("studyRoomUpdated", updatedRoom);
                    break;
                }
            }
        });
    }

    /**
     * 删除自习室
     */
    public void removeStudyRoom(Long roomId) {
        Platform.runLater(() -> {
            studyRooms.removeIf(room -> room.getId().equals(roomId));
            totalStudyRooms.set(studyRooms.size());
            notifyDataObservers("studyRoomRemoved", roomId);
        });
    }

    // ==== 座位数据管理 ====

    /**
     * 刷新座位列表
     */
    public void refreshSeats(Long studyRoomId) {
        setLoading(true);
        (studyRoomId != null ? 
            seatApiService.getSeatsByStudyRoom(studyRoomId) : 
            seatApiService.getAvailableSeats(null))
            .thenAccept(seatList -> Platform.runLater(() -> {
                seats.setAll(seatList);
                totalSeats.set(seatList.size());
                notifyDataObservers("seats", seatList);
                updateLastUpdateTime();
                logger.debug("💺 刷新座位列表: {}个", seatList.size());
            }))
            .exceptionally(throwable -> {
                logger.error("❌ 刷新座位列表失败", throwable);
                return null;
            })
            .whenComplete((result, throwable) -> setLoading(false));
    }

    /**
     * 更新座位状态
     */
    public void updateSeatStatus(Long seatId, Seat.Status newStatus) {
        Platform.runLater(() -> {
            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);
                if (seat.getId().equals(seatId)) {
                    seat.setStatus(newStatus);
                    seats.set(i, seat); // 触发列表更新
                    notifyDataObservers("seatStatusUpdated", seat);
                    break;
                }
            }
        });
    }

    // ==== 预订数据管理 ====

    /**
     * 刷新我的预订列表
     */
    public void refreshMyReservations() {
        if (!isLoggedIn.get()) {
            return;
        }
        
        setLoading(true);
        reservationApiService.getMyReservations()
            .thenAccept(reservationList -> Platform.runLater(() -> {
                myReservations.setAll(reservationList);
                
                // 统计活跃预订
                long activeCount = reservationList.stream()
                    .filter(r -> r.isActive() || r.isConfirmed())
                    .count();
                activeReservations.set((int) activeCount);
                
                notifyDataObservers("myReservations", reservationList);
                updateLastUpdateTime();
                logger.debug("📅 刷新我的预订列表: {}个 ({}个活跃)", reservationList.size(), activeCount);
            }))
            .exceptionally(throwable -> {
                logger.error("❌ 刷新我的预订列表失败", throwable);
                return null;
            })
            .whenComplete((result, throwable) -> setLoading(false));
    }

    /**
     * 刷新所有预订列表（管理员功能）
     */
    public void refreshAllReservations() {
        setLoading(true);
        reservationApiService.getReservations(0, 1000, null, null, null)
            .thenAccept(pageData -> Platform.runLater(() -> {
                if (pageData != null && pageData.hasContent()) {
                    reservations.setAll(pageData.getContent());
                    totalReservations.set((int) pageData.getTotalElements());
                    notifyDataObservers("allReservations", pageData.getContent());
                    updateLastUpdateTime();
                    logger.debug("📅 刷新所有预订列表: {}个", pageData.getTotalElements());
                }
            }))
            .exceptionally(throwable -> {
                logger.error("❌ 刷新所有预订列表失败", throwable);
                return null;
            })
            .whenComplete((result, throwable) -> setLoading(false));
    }

    /**
     * 添加预订
     */
    public void addReservation(Reservation reservation) {
        Platform.runLater(() -> {
            myReservations.add(0, reservation); // 添加到列表开头
            if (reservation.isActive() || reservation.isConfirmed()) {
                activeReservations.set(activeReservations.get() + 1);
            }
            notifyDataObservers("reservationAdded", reservation);
        });
    }

    /**
     * 更新预订
     */
    public void updateReservation(Reservation updatedReservation) {
        Platform.runLater(() -> {
            // 更新我的预订列表
            for (int i = 0; i < myReservations.size(); i++) {
                if (myReservations.get(i).getId().equals(updatedReservation.getId())) {
                    myReservations.set(i, updatedReservation);
                    break;
                }
            }
            
            // 更新所有预订列表
            for (int i = 0; i < reservations.size(); i++) {
                if (reservations.get(i).getId().equals(updatedReservation.getId())) {
                    reservations.set(i, updatedReservation);
                    break;
                }
            }
            
            // 重新计算活跃预订数量
            long activeCount = myReservations.stream()
                .filter(r -> r.isActive() || r.isConfirmed())
                .count();
            activeReservations.set((int) activeCount);
            
            notifyDataObservers("reservationUpdated", updatedReservation);
        });
    }

    // ==== 自动刷新管理 ====

    /**
     * 启动自动刷新
     */
    private void startAutoRefresh() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (autoRefreshEnabled && isLoggedIn.get()) {
                try {
                    logger.debug("🔄 执行自动刷新");
                    refreshMyReservations();
                    // 可以根据需要添加其他数据的自动刷新
                } catch (Exception e) {
                    logger.error("❌ 自动刷新失败", e);
                }
            }
        }, refreshIntervalSeconds, refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * 设置自动刷新间隔
     */
    public void setRefreshInterval(int seconds) {
        this.refreshIntervalSeconds = Math.max(10, seconds); // 最小10秒
        logger.info("🔄 设置自动刷新间隔: {}秒", this.refreshIntervalSeconds);
    }

    /**
     * 启用/禁用自动刷新
     */
    public void setAutoRefreshEnabled(boolean enabled) {
        this.autoRefreshEnabled = enabled;
        logger.info("🔄 自动刷新状态: {}", enabled ? "启用" : "禁用");
    }

    // ==== 工具方法 ====

    /**
     * 设置加载状态
     */
    private void setLoading(boolean loading) {
        Platform.runLater(() -> isLoading.set(loading));
    }

    /**
     * 更新最后更新时间
     */
    private void updateLastUpdateTime() {
        Platform.runLater(() -> {
            lastUpdateTime.set(LocalDateTime.now().toString());
        });
    }

    /**
     * 刷新所有数据
     */
    public void refreshAllData() {
        logger.info("🔄 刷新所有数据");
        refreshStudyRooms();
        refreshSeats(null);
        if (isLoggedIn.get()) {
            refreshCurrentUser();
            refreshMyReservations();
        }
    }

    // ==== 属性访问器 ====

    public ObjectProperty<User> currentUserProperty() { return currentUser; }
    public User getCurrentUser() { return currentUser.get(); }

    public ObservableList<StudyRoom> getStudyRooms() { return studyRooms; }
    public ObservableList<Seat> getSeats() { return seats; }
    public ObservableList<Reservation> getReservations() { return reservations; }
    public ObservableList<Reservation> getMyReservations() { return myReservations; }

    public IntegerProperty totalUsersProperty() { return totalUsers; }
    public IntegerProperty totalStudyRoomsProperty() { return totalStudyRooms; }
    public IntegerProperty totalSeatsProperty() { return totalSeats; }
    public IntegerProperty totalReservationsProperty() { return totalReservations; }
    public IntegerProperty activeReservationsProperty() { return activeReservations; }

    public BooleanProperty isLoggedInProperty() { return isLoggedIn; }
    public BooleanProperty isLoadingProperty() { return isLoading; }
    public StringProperty lastUpdateTimeProperty() { return lastUpdateTime; }

    /**
     * 关闭服务
     */
    public void shutdown() {
        logger.info("🔒 关闭数据绑定服务");
        autoRefreshEnabled = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        dataObservers.clear();
    }
} 