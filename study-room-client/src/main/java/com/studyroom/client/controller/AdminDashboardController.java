package com.studyroom.client.controller;

import com.studyroom.client.model.User;
import com.studyroom.client.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 管理员仪表板控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class AdminDashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    // 顶部组件
    @FXML private Label systemStatusLabel;
    @FXML private Label adminNameLabel;
    @FXML private Label adminRoleLabel;
    @FXML private Button systemSettingsButton;
    @FXML private Button logoutButton;

    // 主要内容区域
    @FXML private TabPane adminTabPane;
    
    // 系统概览组件
    @FXML private Label systemOverviewLabel;
    @FXML private Label systemStatusSubtitle;
    @FXML private Button userManagementButton;
    @FXML private Button roomManagementButton;
    
    // 系统统计标签
    @FXML private Label onlineUsersLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeReservationsLabel;
    @FXML private Label utilizationRateLabel;
    
    // 最近活动表格
    @FXML private TableView<?> recentActivitiesTable;
    @FXML private TableColumn<?, ?> activityTimeColumn;
    @FXML private TableColumn<?, ?> activityUserColumn;
    @FXML private TableColumn<?, ?> activityTypeColumn;
    @FXML private TableColumn<?, ?> activityDescColumn;
    @FXML private TableColumn<?, ?> activityStatusColumn;
    
    // 管理功能标签页内容
    @FXML private StackPane userManagementPane;
    @FXML private StackPane roomManagementPane;
    @FXML private StackPane reservationManagementPane;
    @FXML private StackPane statisticsPane;
    @FXML private StackPane systemSettingsPane;

    // 底部状态栏
    @FXML private Label statusLabel;
    @FXML private Label systemLoadLabel;
    @FXML private Label serverLabel;
    @FXML private Label timeLabel;

    // 当前管理员用户
    private User currentAdmin;
    
    // 时间更新定时器
    private Timer timeUpdateTimer;
    
    // 系统监控定时器
    private Timer systemMonitorTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化管理员仪表板界面...");
        
        try {
            // 初始化界面组件
            initializeComponents();
            
            // 启动时间更新
            startTimeUpdate();
            
            // 启动系统监控
            startSystemMonitoring();
            
            logger.info("✅ 管理员仪表板界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 管理员仪表板界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化界面组件
     */
    private void initializeComponents() {
        // 设置默认状态
        updateStatus("系统就绪");
        updateSystemStatus(true);
        
        // 初始化活动表格
        initializeActivitiesTable();
        
        // 设置默认统计数据
        updateSystemStatistics(0, 0, 0, 0.0);
        
        // 加载标签页内容
        loadTabContents();
    }

    /**
     * 加载标签页内容
     */
    private void loadTabContents() {
        // 单独加载每个模块，避免一个失败导致全部失败
        try {
            loadUserManagement();
        } catch (Exception e) {
            logger.error("❌❌❌ 加载用户管理模块严重失败", e);
            addPlaceholderLabel(userManagementPane, "用户管理模块加载失败，请查看日志");
        }
        
        try {
            loadRoomManagement();
        } catch (Exception e) {
            logger.error("❌❌❌ 加载自习室管理模块严重失败", e);
            addPlaceholderLabel(roomManagementPane, "自习室管理模块加载失败，请查看日志");
        }
        
        try {
            loadReservationManagement();
        } catch (Exception e) {
            logger.error("❌❌❌ 加载预约管理模块严重失败", e);
            addPlaceholderLabel(reservationManagementPane, "预约管理模块加载失败，请查看日志");
        }
        
        try {
            loadStatistics();
        } catch (Exception e) {
            logger.error("❌❌❌ 加载统计报表模块严重失败", e);
            addPlaceholderLabel(statisticsPane, "统计报表模块加载失败，请查看日志");
        }
        
        try {
            loadSystemSettings();
        } catch (Exception e) {
            logger.error("❌❌❌ 加载系统设置模块严重失败", e);
            addPlaceholderLabel(systemSettingsPane, "系统设置模块加载失败，请查看日志");
        }
    }

    /**
     * 加载用户管理页面
     */
    private void loadUserManagement() throws IOException {
        logger.info("🔄 开始加载用户管理页面...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-management.fxml"));
        userManagementPane.getChildren().clear();
        userManagementPane.getChildren().add(loader.load());
        logger.info("✅ 用户管理页面加载成功");
    }

    /**
     * 加载自习室管理页面
     */
    private void loadRoomManagement() throws IOException {
        logger.info("🔄 开始加载自习室管理页面...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-room-management.fxml"));
        roomManagementPane.getChildren().clear();
        roomManagementPane.getChildren().add(loader.load());
        logger.info("✅ 自习室管理页面加载成功");
    }

    /**
     * 加载预约管理页面
     */
    private void loadReservationManagement() throws IOException {
        logger.info("🔄 开始加载预约管理页面...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservation-management.fxml"));
        reservationManagementPane.getChildren().clear();
        reservationManagementPane.getChildren().add(loader.load());
        logger.info("✅ 预约管理页面加载成功");
    }

    /**
     * 加载统计报表页面
     */
    private void loadStatistics() throws IOException {
        logger.info("🔄 开始加载统计报表页面...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/system-statistics.fxml"));
        statisticsPane.getChildren().clear();
        statisticsPane.getChildren().add(loader.load());
        logger.info("✅ 统计报表页面加载成功");
    }

    /**
     * 加载系统设置页面
     */
    private void loadSystemSettings() {
        // 暂时显示占位符，后续可以创建系统设置FXML
        addPlaceholderLabel(systemSettingsPane, "系统设置页面正在开发中");
    }

    /**
     * 添加占位符标签
     */
    private void addPlaceholderLabel(StackPane container, String message) {
        container.getChildren().clear();
        Label placeholder = new Label(message);
        placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 50px;");
        container.getChildren().add(placeholder);
    }

    /**
     * 初始化活动表格
     */
    private void initializeActivitiesTable() {
        // TODO: 配置表格列和数据绑定
        // 这里暂时设置为空表格
        recentActivitiesTable.setPlaceholder(new Label("暂无系统活动记录"));
    }

    /**
     * 启动时间更新定时器
     */
    private void startTimeUpdate() {
        timeUpdateTimer = new Timer(true);
        timeUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateCurrentTime());
            }
        }, 0, 1000); // 每秒更新一次
    }

    /**
     * 启动系统监控
     */
    private void startSystemMonitoring() {
        systemMonitorTimer = new Timer(true);
        systemMonitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateSystemMonitoring());
            }
        }, 0, 30000); // 每30秒更新一次
    }

    /**
     * 更新当前时间显示
     */
    private void updateCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        String timeText = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        timeLabel.setText(timeText);
    }

    /**
     * 更新系统监控信息
     */
    private void updateSystemMonitoring() {
        try {
            // TODO: 从服务器获取实时系统状态
            // 这里暂时使用模拟数据
            
            // 模拟系统负载
            double load = Math.random() * 100;
            String loadStatus;
            if (load < 30) {
                loadStatus = "负载: 轻度";
                systemLoadLabel.setStyle("-fx-text-fill: #4CAF50;");
            } else if (load < 70) {
                loadStatus = "负载: 中等";
                systemLoadLabel.setStyle("-fx-text-fill: #FF9800;");
            } else {
                loadStatus = "负载: 重度";
                systemLoadLabel.setStyle("-fx-text-fill: #F44336;");
            }
            systemLoadLabel.setText(loadStatus);
            
            // 更新统计数据
            int onlineUsers = (int) (Math.random() * 50) + 10;
            int totalUsers = 1250 + (int) (Math.random() * 100);
            int activeReservations = (int) (Math.random() * 200) + 50;
            double utilizationRate = Math.random() * 100;
            
            updateSystemStatistics(onlineUsers, totalUsers, activeReservations, utilizationRate);
            
        } catch (Exception e) {
            logger.warn("⚠️ 系统监控更新失败", e);
        }
    }

    /**
     * 设置当前管理员用户
     */
    public void setCurrentUser(User admin) {
        this.currentAdmin = admin;
        updateAdminInfo();
        loadSystemData();
    }

    /**
     * 更新管理员信息显示
     */
    private void updateAdminInfo() {
        if (currentAdmin != null) {
            adminNameLabel.setText(currentAdmin.getDisplayName());
            adminRoleLabel.setText(currentAdmin.getRole().getDisplayName());
            
            // 根据时间设置系统状态副标题
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            String statusMessage;
            if (hour >= 0 && hour < 6) {
                statusMessage = "深夜时段，系统运行稳定";
            } else if (hour >= 6 && hour < 12) {
                statusMessage = "上午时段，用户活跃度较高";
            } else if (hour >= 12 && hour < 18) {
                statusMessage = "下午时段，系统负载正常";
            } else {
                statusMessage = "晚间时段，预约高峰期";
            }
            systemStatusSubtitle.setText(statusMessage);
        }
    }

    /**
     * 加载系统数据
     */
    private void loadSystemData() {
        if (currentAdmin == null) {
            return;
        }
        
        try {
            // TODO: 从服务器加载系统统计数据
            // 这里暂时使用模拟数据
            updateSystemStatistics(25, 1250, 150, 75.5);
            
            updateStatus("系统数据加载完成");
            
        } catch (Exception e) {
            logger.error("❌ 加载系统数据失败", e);
            updateStatus("数据加载失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统统计数据
     */
    private void updateSystemStatistics(int onlineUsers, int totalUsers, 
                                       int activeReservations, double utilizationRate) {
        onlineUsersLabel.setText(String.valueOf(onlineUsers));
        totalUsersLabel.setText(String.valueOf(totalUsers));
        activeReservationsLabel.setText(String.valueOf(activeReservations));
        utilizationRateLabel.setText(String.format("%.1f%%", utilizationRate));
    }

    /**
     * 更新系统状态
     */
    private void updateSystemStatus(boolean normal) {
        if (normal) {
            systemStatusLabel.setText("● 系统正常");
            systemStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            systemStatusLabel.setText("● 系统异常");
            systemStatusLabel.setStyle("-fx-text-fill: #F44336;");
        }
    }

    /**
     * 更新状态信息
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * 处理用户管理
     */
    @FXML
    private void handleUserManagement() {
        logger.info("🔄 管理员点击用户管理");
        
        // 切换到用户管理标签页
        adminTabPane.getSelectionModel().select(1);
        
        // TODO: 加载用户管理界面
        updateStatus("正在加载用户管理界面...");
    }

    /**
     * 处理自习室管理
     */
    @FXML
    private void handleRoomManagement() {
        logger.info("🔄 管理员点击自习室管理");
        
        // 切换到自习室管理标签页
        adminTabPane.getSelectionModel().select(2);
        
        // TODO: 加载自习室管理界面
        updateStatus("正在加载自习室管理界面...");
    }

    /**
     * 处理系统设置
     */
    @FXML
    private void handleSystemSettings() {
        logger.info("🔄 管理员点击系统设置");
        
        // 切换到系统设置标签页
        adminTabPane.getSelectionModel().select(5);
        
        // TODO: 加载系统设置界面
        updateStatus("正在加载系统设置界面...");
    }

    /**
     * 处理注销
     */
    @FXML
    private void handleLogout() {
        logger.info("🔄 管理员请求注销");
        
        boolean confirmed = AlertUtils.showConfirm(
            "确认注销", 
            "您确定要注销管理员账户吗？\n\n注销后将返回登录界面。"
        );
        
        if (confirmed) {
            try {
                // 停止定时器
                if (timeUpdateTimer != null) {
                    timeUpdateTimer.cancel();
                }
                if (systemMonitorTimer != null) {
                    systemMonitorTimer.cancel();
                }
                
                // TODO: 清理管理员会话
                // TODO: 返回登录界面
                
                logger.info("✅ 管理员注销成功");
                
            } catch (Exception e) {
                logger.error("❌ 管理员注销失败", e);
                AlertUtils.showError("注销失败", "注销过程中发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (timeUpdateTimer != null) {
            timeUpdateTimer.cancel();
            timeUpdateTimer = null;
        }
        if (systemMonitorTimer != null) {
            systemMonitorTimer.cancel();
            systemMonitorTimer = null;
        }
        logger.info("🧹 管理员仪表板资源清理完成");
    }
} 