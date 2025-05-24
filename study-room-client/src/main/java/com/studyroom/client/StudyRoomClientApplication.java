package com.studyroom.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 共享自习室管理系统 - JavaFX 客户端主应用程序
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class StudyRoomClientApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(StudyRoomClientApplication.class);
    
    // 应用配置常量
    private static final String APP_TITLE = "共享自习室管理系统";
    private static final String APP_VERSION = "v1.0.0";
    private static final int LOGIN_WIDTH = 600;
    private static final int LOGIN_HEIGHT = 500;
    private static final int MIN_WIDTH = 1000;
    private static final int MIN_HEIGHT = 700;
    
    // 主舞台引用
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        logger.info("🚀 启动共享自习室管理系统客户端...");
        
        try {
            // 显示登录界面
            showLoginWindow();
            
            logger.info("✅ 应用程序启动成功！");
            
        } catch (Exception e) {
            logger.error("❌ 应用程序启动失败", e);
            throw e;
        }
    }

    /**
     * 显示登录窗口
     */
    private void showLoginWindow() throws IOException {
        // 加载登录界面FXML
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/fxml/login.fxml")
        );
        
        Scene scene = new Scene(fxmlLoader.load(), LOGIN_WIDTH, LOGIN_HEIGHT);
        
        // 加载CSS样式
        scene.getStylesheets().add(
            getClass().getResource("/css/login.css").toExternalForm()
        );
        
        // 设置舞台属性
        setupLoginStage(primaryStage, scene);
        
        // 设置登录控制器的舞台引用
        Object controller = fxmlLoader.getController();
        if (controller instanceof com.studyroom.client.controller.LoginController) {
            ((com.studyroom.client.controller.LoginController) controller).setLoginStage(primaryStage);
        }
        
        // 显示窗口
        primaryStage.show();
    }

    /**
     * 配置登录舞台属性
     */
    private void setupLoginStage(Stage stage, Scene scene) {
        // 基本属性
        stage.setTitle(APP_TITLE + " " + APP_VERSION + " - 用户登录");
        stage.setScene(scene);
        
        // 窗口大小设置
        stage.setWidth(LOGIN_WIDTH);
        stage.setHeight(LOGIN_HEIGHT);
        stage.setResizable(false);
        
        // 设置应用图标
        try {
            stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/images/app-icon.png")
            ));
        } catch (Exception e) {
            logger.warn("⚠️ 无法加载应用图标: {}", e.getMessage());
        }
        
        // 居中显示
        stage.centerOnScreen();
        
        // 关闭事件处理
        stage.setOnCloseRequest(event -> {
            logger.info("📝 用户请求关闭应用程序");
            handleApplicationExit();
        });
    }

    /**
     * 显示主界面（登录成功后调用）
     */
    public static void showMainWindow() {
        try {
            // 加载主界面FXML
            FXMLLoader fxmlLoader = new FXMLLoader(
                StudyRoomClientApplication.class.getResource("/fxml/main.fxml")
            );
            
            Scene scene = new Scene(fxmlLoader.load(), MIN_WIDTH, MIN_HEIGHT);
            
            // 加载CSS样式
            scene.getStylesheets().add(
                StudyRoomClientApplication.class.getResource("/css/main.css").toExternalForm()
            );
            
            // 设置舞台属性
            setupMainStage(primaryStage, scene);
            
            // 显示窗口
            primaryStage.show();
            
        } catch (IOException e) {
            logger.error("❌ 无法加载主界面", e);
        }
    }

    /**
     * 配置主舞台属性
     */
    private static void setupMainStage(Stage stage, Scene scene) {
        // 基本属性
        stage.setTitle(APP_TITLE + " " + APP_VERSION);
        stage.setScene(scene);
        
        // 窗口大小限制
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setResizable(true);
        
        // 居中显示
        stage.centerOnScreen();
    }

    /**
     * 处理应用程序退出
     */
    private void handleApplicationExit() {
        try {
            logger.info("🔄 正在清理资源...");
            
            // TODO: 添加清理逻辑
            // - 保存用户配置
            // - 清理缓存数据
            // - 断开网络连接
            
            logger.info("✅ 应用程序正常退出");
            
        } catch (Exception e) {
            logger.error("❌ 退出时发生错误", e);
        }
    }

    /**
     * 获取主舞台引用
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * 应用程序入口点
     */
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("🏫 " + APP_TITLE + " " + APP_VERSION);
        System.out.println("📱 JavaFX 客户端启动中...");
        System.out.println("=================================");
        
        launch(args);
    }
} 