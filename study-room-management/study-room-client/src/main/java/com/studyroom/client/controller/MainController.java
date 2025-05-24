package com.studyroom.client.controller;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 主窗口控制器
 * 
 * @author Developer
 * @version 1.0.0
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    // FXML注入的UI组件
    @FXML private MenuBar menuBar;
    @FXML private Menu adminMenu;
    @FXML private StackPane contentPane;
    
    // 状态栏组件
    @FXML private Label connectionStatusLabel;
    @FXML private Label currentUserLabel;
    @FXML private Label statusLabel;
    @FXML private Label serverLabel;
    @FXML private Label timeLabel;
    
    // 欢迎界面组件
    @FXML private Button loginButton;
    @FXML private Button testConnectionButton;
    
    // 时间更新定时器
    private Timeline timelineTimer;
    
    // 连接状态
    private boolean isConnected = false;
    private boolean isLoggedIn = false;
    private String currentUser = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🎨 初始化主窗口控制器...");
        
        // 初始化UI状态
        initializeUIState();
        
        // 启动时间更新器
        startTimeUpdater();
        
        // 测试服务器连接
        testServerConnection();
        
        logger.info("✅ 主窗口控制器初始化完成");
    }

    /**
     * 初始化UI状态
     */
    private void initializeUIState() {
        // 隐藏管理员菜单（未登录状态）
        adminMenu.setVisible(false);
        
        // 设置初始状态
        updateConnectionStatus(false);
        updateUserStatus(false, null);
        updateStatusMessage("就绪");
    }

    /**
     * 启动时间更新器
     */
    private void startTimeUpdater() {
        timelineTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        timelineTimer.setCycleCount(Timeline.INDEFINITE);
        timelineTimer.play();
    }

    /**
     * 更新当前时间显示
     */
    private void updateTime() {
        Platform.runLater(() -> {
            String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            timeLabel.setText(currentTime);
        });
    }

    /**
     * 测试服务器连接
     */
    private void testServerConnection() {
        Platform.runLater(() -> {
            updateStatusMessage("正在测试服务器连接...");
        });
        
        // TODO: 实现实际的服务器连接测试
        // 这里先模拟连接测试
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 模拟网络延迟
                
                // 模拟连接成功
                Platform.runLater(() -> {
                    updateConnectionStatus(true);
                    updateStatusMessage("服务器连接正常");
                });
                
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    updateConnectionStatus(false);
                    updateStatusMessage("服务器连接失败");
                });
            }
        }).start();
    }

    /**
     * 更新连接状态
     */
    private void updateConnectionStatus(boolean connected) {
        this.isConnected = connected;
        Platform.runLater(() -> {
            if (connected) {
                connectionStatusLabel.setText("● 已连接");
                connectionStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
            } else {
                connectionStatusLabel.setText("● 未连接");
                connectionStatusLabel.setStyle("-fx-text-fill: #F44336;");
            }
        });
    }

    /**
     * 更新用户状态
     */
    private void updateUserStatus(boolean loggedIn, String username) {
        this.isLoggedIn = loggedIn;
        this.currentUser = username;
        
        Platform.runLater(() -> {
            if (loggedIn && username != null) {
                currentUserLabel.setText("用户: " + username);
                adminMenu.setVisible(true); // 根据用户权限决定是否显示
            } else {
                currentUserLabel.setText("未登录");
                adminMenu.setVisible(false);
            }
        });
    }

    /**
     * 更新状态消息
     */
    private void updateStatusMessage(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
    }

    // ===== 菜单事件处理方法 =====

    /**
     * 处理登录事件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        logger.info("🔐 用户请求登录");
        updateStatusMessage("正在打开登录窗口...");
        
        // TODO: 实现登录窗口
        showInfoAlert("功能开发中", "登录功能正在开发中，敬请期待！");
    }

    /**
     * 处理注销事件
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        logger.info("🚪 用户请求注销");
        updateUserStatus(false, null);
        updateStatusMessage("已注销登录");
        
        showInfoAlert("注销成功", "您已成功注销登录！");
    }

    /**
     * 处理设置事件
     */
    @FXML
    private void handleSettings(ActionEvent event) {
        logger.info("⚙️ 打开设置界面");
        showInfoAlert("功能开发中", "设置功能正在开发中，敬请期待！");
    }

    /**
     * 处理退出事件
     */
    @FXML
    private void handleExit(ActionEvent event) {
        logger.info("🔚 用户请求退出应用程序");
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText("您确定要退出应用程序吗？");
        alert.setContentText("退出后需要重新启动才能使用系统。");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (timelineTimer != null) {
                    timelineTimer.stop();
                }
                Platform.exit();
            }
        });
    }

    /**
     * 处理新建预约事件
     */
    @FXML
    private void handleNewReservation(ActionEvent event) {
        logger.info("📅 打开新建预约界面");
        showInfoAlert("功能开发中", "新建预约功能正在开发中，敬请期待！");
    }

    /**
     * 处理我的预约事件
     */
    @FXML
    private void handleMyReservations(ActionEvent event) {
        logger.info("📋 打开我的预约界面");
        showInfoAlert("功能开发中", "我的预约功能正在开发中，敬请期待！");
    }

    /**
     * 处理预约历史事件
     */
    @FXML
    private void handleReservationHistory(ActionEvent event) {
        logger.info("📜 打开预约历史界面");
        showInfoAlert("功能开发中", "预约历史功能正在开发中，敬请期待！");
    }

    /**
     * 处理用户管理事件
     */
    @FXML
    private void handleUserManagement(ActionEvent event) {
        logger.info("👥 打开用户管理界面");
        showInfoAlert("功能开发中", "用户管理功能正在开发中，敬请期待！");
    }

    /**
     * 处理自习室管理事件
     */
    @FXML
    private void handleRoomManagement(ActionEvent event) {
        logger.info("🏢 打开自习室管理界面");
        showInfoAlert("功能开发中", "自习室管理功能正在开发中，敬请期待！");
    }

    /**
     * 处理预约管理事件
     */
    @FXML
    private void handleReservationManagement(ActionEvent event) {
        logger.info("📊 打开预约管理界面");
        showInfoAlert("功能开发中", "预约管理功能正在开发中，敬请期待！");
    }

    /**
     * 处理统计报表事件
     */
    @FXML
    private void handleStatistics(ActionEvent event) {
        logger.info("📈 打开统计报表界面");
        showInfoAlert("功能开发中", "统计报表功能正在开发中，敬请期待！");
    }

    /**
     * 处理帮助事件
     */
    @FXML
    private void handleHelp(ActionEvent event) {
        logger.info("❓ 打开帮助界面");
        showInfoAlert("功能开发中", "帮助功能正在开发中，敬请期待！");
    }

    /**
     * 处理关于事件
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        logger.info("ℹ️ 显示关于信息");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("共享自习室管理系统 v1.0.0");
        alert.setContentText(
            "基于JavaFX + Spring Boot + SQLite的自习室管理系统\n\n" +
            "开发者: Developer\n" +
            "技术栈: JavaFX, Spring Boot, SQLite, Maven\n" +
            "版本: 1.0.0\n\n" +
            "© 2024 版权所有"
        );
        alert.showAndWait();
    }

    /**
     * 处理测试连接事件
     */
    @FXML
    private void handleTestConnection(ActionEvent event) {
        logger.info("🔌 测试服务器连接");
        updateStatusMessage("正在测试连接...");
        testServerConnection();
    }

    /**
     * 显示信息提示对话框
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 