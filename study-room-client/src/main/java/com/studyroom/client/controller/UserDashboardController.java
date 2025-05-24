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
 * 用户仪表板控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class UserDashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserDashboardController.class);

    // 顶部组件
    @FXML private Label connectionStatusLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;

    // 主要内容区域
    @FXML private TabPane mainTabPane;
    
    // 首页组件
    @FXML private Label welcomeLabel;
    @FXML private Label welcomeSubtitle;
    @FXML private Button quickReserveButton;
    @FXML private Button myReservationsButton;
    
    // 统计标签
    @FXML private Label todayReservationsLabel;
    @FXML private Label monthReservationsLabel;
    @FXML private Label totalHoursLabel;
    @FXML private Label pointsLabel;
    
    // 最近预约表格
    @FXML private TableView<?> recentReservationsTable;
    @FXML private TableColumn<?, ?> roomColumn;
    @FXML private TableColumn<?, ?> seatColumn;
    @FXML private TableColumn<?, ?> dateColumn;
    @FXML private TableColumn<?, ?> timeColumn;
    @FXML private TableColumn<?, ?> statusColumn;
    @FXML private TableColumn<?, ?> actionColumn;
    
    // 其他标签页内容
    @FXML private StackPane reservationPane;
    @FXML private StackPane studyRoomPane;
    @FXML private StackPane profilePane;

    // 底部状态栏
    @FXML private Label statusLabel;
    @FXML private Label serverLabel;
    @FXML private Label timeLabel;

    // 当前用户
    private User currentUser;
    
    // 时间更新定时器
    private Timer timeUpdateTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化用户仪表板界面...");
        
        try {
            // 初始化界面组件
            initializeComponents();
            
            // 启动时间更新
            startTimeUpdate();
            
            logger.info("✅ 用户仪表板界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 用户仪表板界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化界面组件
     */
    private void initializeComponents() {
        // 设置默认状态
        updateStatus("就绪");
        updateConnectionStatus(true);
        
        // 初始化表格
        initializeReservationsTable();
        
        // 设置默认统计数据
        updateStatistics(0, 0, 0, 0);
        
        // 加载标签页内容
        loadTabContents();
    }

    /**
     * 初始化预约表格
     */
    private void initializeReservationsTable() {
        // TODO: 配置表格列和数据绑定
        // 这里暂时设置为空表格
        recentReservationsTable.setPlaceholder(new Label("暂无预约记录"));
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
     * 更新当前时间显示
     */
    private void updateCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        String timeText = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        timeLabel.setText(timeText);
    }

    /**
     * 设置当前用户
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInfo();
        loadUserData();
    }

    /**
     * 更新用户信息显示
     */
    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getDisplayName());
            userRoleLabel.setText(currentUser.getRole().getDisplayName());
            
            // 更新欢迎信息
            welcomeLabel.setText("欢迎回来，" + currentUser.getDisplayName() + "！");
            
            // 根据时间设置欢迎副标题
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            String greeting;
            if (hour < 6) {
                greeting = "夜深了，注意休息哦";
            } else if (hour < 12) {
                greeting = "早上好，新的一天开始了";
            } else if (hour < 18) {
                greeting = "下午好，继续加油学习";
            } else {
                greeting = "晚上好，今天学习得怎么样";
            }
            welcomeSubtitle.setText(greeting);
        }
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        if (currentUser == null) {
            return;
        }
        
        try {
            // TODO: 从服务器加载用户的预约数据和统计信息
            // 这里暂时使用模拟数据
            updateStatistics(2, 15, 120, 850);
            
            updateStatus("数据加载完成");
            
        } catch (Exception e) {
            logger.error("❌ 加载用户数据失败", e);
            updateStatus("数据加载失败: " + e.getMessage());
        }
    }

    /**
     * 更新统计数据
     */
    private void updateStatistics(int todayReservations, int monthReservations, 
                                 int totalHours, int points) {
        todayReservationsLabel.setText(String.valueOf(todayReservations));
        monthReservationsLabel.setText(String.valueOf(monthReservations));
        totalHoursLabel.setText(String.valueOf(totalHours));
        pointsLabel.setText(String.valueOf(points));
    }

    /**
     * 更新连接状态
     */
    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("● 已连接");
            connectionStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            connectionStatusLabel.setText("● 未连接");
            connectionStatusLabel.setStyle("-fx-text-fill: #F44336;");
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
     * 处理快速预约
     */
    @FXML
    private void handleQuickReserve() {
        logger.info("🔄 用户点击快速预约");
        
        // 切换到自习室浏览标签页
        mainTabPane.getSelectionModel().select(2);
        
        // TODO: 实现快速预约逻辑
        updateStatus("正在加载自习室信息...");
    }

    /**
     * 处理我的预约
     */
    @FXML
    private void handleMyReservations() {
        logger.info("🔄 用户点击我的预约");
        
        // 切换到预约管理标签页
        mainTabPane.getSelectionModel().select(1);
        
        // TODO: 加载用户预约数据
        updateStatus("正在加载预约信息...");
    }

    /**
     * 处理设置
     */
    @FXML
    private void handleSettings() {
        logger.info("🔄 用户点击设置");
        
        // TODO: 打开设置对话框
        AlertUtils.showInfo("功能提示", "设置功能正在开发中");
    }

    /**
     * 处理注销
     */
    @FXML
    private void handleLogout() {
        logger.info("🔄 用户请求注销");
        
        boolean confirmed = AlertUtils.showConfirm(
            "确认注销", 
            "您确定要注销当前账户吗？"
        );
        
        if (confirmed) {
            try {
                // 停止定时器
                if (timeUpdateTimer != null) {
                    timeUpdateTimer.cancel();
                }
                
                // TODO: 清理用户会话
                // TODO: 返回登录界面
                
                logger.info("✅ 用户注销成功");
                
            } catch (Exception e) {
                logger.error("❌ 用户注销失败", e);
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
        logger.info("🧹 用户仪表板资源清理完成");
    }

    /**
     * 加载标签页内容
     */
    private void loadTabContents() {
        try {
            // 加载预约管理页面
            loadReservationManagement();
            
            // 恢复其他页面加载
            loadStudyRoomBrowsing();
            loadProfileCenter();
            
        } catch (Exception e) {
            logger.error("❌ 加载标签页内容失败", e);
            updateStatus("部分页面加载失败");
        }
    }

    /**
     * 加载预约管理页面
     */
    private void loadReservationManagement() {
        try {
            logger.info("🔄 开始加载预约管理页面...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservation-management.fxml"));
            logger.info("📄 FXML资源路径: {}", getClass().getResource("/fxml/reservation-management.fxml"));
            
            reservationPane.getChildren().clear();
            Object loadedContent = loader.load();
            logger.info("✅ FXML内容加载成功，类型: {}", loadedContent.getClass().getName());
            
            reservationPane.getChildren().add((javafx.scene.Node) loadedContent);
            logger.info("✅ 预约管理页面加载成功");
        } catch (IOException e) {
            logger.error("❌ 加载预约管理页面失败 - IOException", e);
            addPlaceholderLabel(reservationPane, "预约管理页面加载失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ 加载预约管理页面失败 - 其他异常", e);
            addPlaceholderLabel(reservationPane, "预约管理页面加载失败: " + e.getMessage());
        }
    }

    /**
     * 加载自习室浏览页面
     */
    private void loadStudyRoomBrowsing() {
        try {
            logger.info("🔄 开始加载自习室浏览页面...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/study-room-list.fxml"));
            logger.info("📄 FXML资源路径: {}", getClass().getResource("/fxml/study-room-list.fxml"));
            
            studyRoomPane.getChildren().clear();
            Object loadedContent = loader.load();
            logger.info("✅ FXML内容加载成功，类型: {}", loadedContent.getClass().getName());
            
            studyRoomPane.getChildren().add((javafx.scene.Node) loadedContent);
            logger.info("✅ 自习室浏览页面加载成功");
        } catch (IOException e) {
            logger.error("❌ 加载自习室浏览页面失败 - IOException", e);
            addPlaceholderLabel(studyRoomPane, "自习室浏览页面加载失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ 加载自习室浏览页面失败 - 其他异常", e);
            addPlaceholderLabel(studyRoomPane, "自习室浏览页面加载失败: " + e.getMessage());
        }
    }

    /**
     * 加载个人中心页面
     */
    private void loadProfileCenter() {
        // 暂时显示占位符，后续可以创建个人中心FXML
        addPlaceholderLabel(profilePane, "个人中心页面正在开发中");
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
} 