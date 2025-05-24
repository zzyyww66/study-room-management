package com.studyroom.client.controller;

import com.studyroom.client.StudyRoomClientApplication;
import com.studyroom.client.model.User;
import com.studyroom.client.service.AuthService;
import com.studyroom.client.service.ConfigService;
import com.studyroom.client.util.AlertUtils;
import com.studyroom.client.util.LoadingUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 登录界面控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class LoginController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    // FXML 组件
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberPasswordBox;
    @FXML private CheckBox autoLoginBox;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private TextField serverField;
    @FXML private Button testConnectionButton;
    @FXML private Label statusLabel;

    // 服务类
    private AuthService authService;
    private ConfigService configService;

    // 当前登录窗口
    private Stage loginStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化登录界面...");
        
        try {
            // 初始化服务
            initializeServices();
            
            // 设置组件状态
            setupComponents();
            
            // 加载保存的配置
            loadSavedConfig();
            
            // 绑定事件监听器
            bindEventListeners();
            
            logger.info("✅ 登录界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 登录界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage(), true);
        }
    }

    /**
     * 初始化服务类
     */
    private void initializeServices() {
        // TODO: 从依赖注入容器获取服务实例
        // authService = ApplicationContext.getBean(AuthService.class);
        // configService = ApplicationContext.getBean(ConfigService.class);
        
        // 临时使用模拟服务
        authService = new AuthService();
        configService = new ConfigService();
    }

    /**
     * 设置组件初始状态
     */
    private void setupComponents() {
        // 设置输入框焦点顺序
        usernameField.setFocusTraversable(true);
        
        // 设置默认按钮
        loginButton.setDefaultButton(true);
        
        // 设置提示文本
        updateStatus("请输入用户名和密码");
    }

    /**
     * 加载保存的配置
     */
    private void loadSavedConfig() {
        try {
            // 加载服务器地址
            String savedServer = configService.getServerUrl();
            if (savedServer != null && !savedServer.isEmpty()) {
                serverField.setText(savedServer);
            }
            
            // 加载记住的用户名
            String savedUsername = configService.getSavedUsername();
            if (savedUsername != null && !savedUsername.isEmpty()) {
                usernameField.setText(savedUsername);
                rememberPasswordBox.setSelected(true);
                
                // 如果记住了密码，也加载密码
                String savedPassword = configService.getSavedPassword();
                if (savedPassword != null && !savedPassword.isEmpty()) {
                    passwordField.setText(savedPassword);
                }
            }
            
            // 检查自动登录设置
            boolean autoLogin = configService.isAutoLoginEnabled();
            autoLoginBox.setSelected(autoLogin);
            
            // 如果启用了自动登录且有保存的凭据，执行自动登录
            if (autoLogin && !usernameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
                Platform.runLater(() -> handleLogin());
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ 加载保存的配置失败", e);
        }
    }

    /**
     * 绑定事件监听器
     */
    private void bindEventListeners() {
        // 用户名输入框回车事件
        usernameField.setOnAction(event -> passwordField.requestFocus());
        
        // 密码输入框回车事件
        passwordField.setOnAction(event -> handleLogin());
        
        // 服务器地址变更事件
        serverField.textProperty().addListener((observable, oldValue, newValue) -> {
            configService.setServerUrl(newValue);
        });
    }

    /**
     * 处理登录事件
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String serverUrl = serverField.getText().trim();

        // 验证输入
        if (!validateLoginInput(username, password, serverUrl)) {
            return;
        }

        // 创建登录任务
        Task<User> loginTask = createLoginTask(username, password, serverUrl);
        
        // 显示加载状态
        LoadingUtils.showLoadingIndicator(loginButton, "登录中...");
        updateStatus("正在验证用户信息...");
        
        // 执行异步登录
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    /**
     * 验证登录输入
     */
    private boolean validateLoginInput(String username, String password, String serverUrl) {
        if (username.isEmpty()) {
            AlertUtils.showWarning("输入验证", "请输入用户名");
            usernameField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            AlertUtils.showWarning("输入验证", "请输入密码");
            passwordField.requestFocus();
            return false;
        }
        
        if (serverUrl.isEmpty()) {
            AlertUtils.showWarning("输入验证", "请输入服务器地址");
            serverField.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * 创建登录任务
     */
    private Task<User> createLoginTask(String username, String password, String serverUrl) {
        return new Task<User>() {
            @Override
            protected User call() throws Exception {
                // 设置服务器地址
                authService.setServerUrl(serverUrl);
                
                // 执行登录验证
                return authService.login(username, password);
            }

            @Override
            protected void succeeded() {
                User user = getValue();
                Platform.runLater(() -> onLoginSuccess(user));
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                Platform.runLater(() -> onLoginFailure(exception));
            }
        };
    }

    /**
     * 登录成功处理
     */
    private void onLoginSuccess(User user) {
        try {
            logger.info("✅ 用户登录成功: {}", user.getUsername());
            
            // 保存登录配置
            saveLoginConfig();
            
            // 隐藏加载状态
            LoadingUtils.hideLoadingIndicator(loginButton, "登录");
            updateStatus("登录成功！正在跳转...");
            
            // 根据用户角色跳转到相应界面
            navigateToMainInterface(user);
            
        } catch (Exception e) {
            logger.error("❌ 登录成功后处理失败", e);
            onLoginFailure(e);
        }
    }

    /**
     * 登录失败处理
     */
    private void onLoginFailure(Throwable exception) {
        logger.warn("⚠️ 用户登录失败: {}", exception.getMessage());
        
        // 隐藏加载状态
        LoadingUtils.hideLoadingIndicator(loginButton, "登录");
        updateStatus("登录失败: " + exception.getMessage(), true);
        
        // 显示错误提示
        AlertUtils.showError("登录失败", exception.getMessage());
        
        // 清空密码框
        passwordField.clear();
        passwordField.requestFocus();
    }

    /**
     * 保存登录配置
     */
    private void saveLoginConfig() {
        if (rememberPasswordBox.isSelected()) {
            configService.saveCredentials(usernameField.getText(), passwordField.getText());
        } else {
            configService.clearSavedCredentials();
        }
        
        configService.setAutoLoginEnabled(autoLoginBox.isSelected());
        configService.setServerUrl(serverField.getText());
    }

    /**
     * 跳转到主界面
     */
    private void navigateToMainInterface(User user) {
        try {
            String fxmlPath;
            String windowTitle;
            
            // 根据用户角色确定界面
            if (user.getRole() == User.Role.ADMIN) {
                fxmlPath = "/fxml/admin-dashboard.fxml";
                windowTitle = "管理员仪表板";
            } else {
                fxmlPath = "/fxml/user-dashboard.fxml";
                windowTitle = "用户仪表板";
            }
            
            // 加载新界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            
            // 加载对应的CSS样式
            scene.getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm()
            );
            
            // 设置用户信息到控制器
            Object controller = loader.getController();
            if (controller instanceof UserDashboardController) {
                ((UserDashboardController) controller).setCurrentUser(user);
            } else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setCurrentUser(user);
            }
            
            // 获取主舞台并切换场景
            Stage primaryStage = StudyRoomClientApplication.getPrimaryStage();
            primaryStage.setScene(scene);
            primaryStage.setTitle(windowTitle + " - 共享自习室管理系统");
            
            // 调整窗口大小以适应仪表板
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
        } catch (IOException e) {
            logger.error("❌ 界面跳转失败", e);
            AlertUtils.showError("界面错误", "无法加载主界面: " + e.getMessage());
        }
    }

    /**
     * 处理注册事件
     */
    @FXML
    private void handleRegister() {
        try {
            // 加载注册界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load());
            
            // 创建新窗口
            Stage registerStage = new Stage();
            registerStage.setTitle("用户注册 - 共享自习室管理系统");
            registerStage.setScene(scene);
            registerStage.initModality(Modality.APPLICATION_MODAL);
            registerStage.setResizable(false);
            registerStage.centerOnScreen();
            
            // 显示注册窗口
            registerStage.showAndWait();
            
        } catch (IOException e) {
            logger.error("❌ 无法加载注册界面", e);
            AlertUtils.showError("界面错误", "无法打开注册页面");
        }
    }

    /**
     * 处理忘记密码事件
     */
    @FXML
    private void handleForgotPassword() {
        // TODO: 实现密码找回功能
        AlertUtils.showInfo("功能提示", "密码找回功能正在开发中，请联系管理员重置密码。");
    }

    /**
     * 测试服务器连接
     */
    @FXML
    private void handleTestConnection() {
        String serverUrl = serverField.getText().trim();
        
        if (serverUrl.isEmpty()) {
            AlertUtils.showWarning("输入验证", "请输入服务器地址");
            return;
        }

        // 创建连接测试任务
        Task<Boolean> testTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return authService.testConnection(serverUrl);
            }

            @Override
            protected void succeeded() {
                boolean connected = getValue();
                Platform.runLater(() -> {
                    if (connected) {
                        updateStatus("服务器连接正常");
                        AlertUtils.showInfo("连接测试", "服务器连接成功！");
                    } else {
                        updateStatus("服务器连接失败", true);
                        AlertUtils.showWarning("连接测试", "无法连接到服务器");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    updateStatus("连接测试失败", true);
                    AlertUtils.showError("连接测试", "连接测试失败: " + getException().getMessage());
                });
            }
        };

        // 显示测试状态
        LoadingUtils.showLoadingIndicator(testConnectionButton, "测试中...");
        updateStatus("正在测试服务器连接...");

        // 执行测试
        Thread testThread = new Thread(testTask);
        testThread.setDaemon(true);
        testThread.start();
    }

    /**
     * 更新状态标签
     */
    private void updateStatus(String message) {
        updateStatus(message, false);
    }

    /**
     * 更新状态标签
     */
    private void updateStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            
            // 设置样式类
            statusLabel.getStyleClass().removeAll("status-error", "status-normal");
            statusLabel.getStyleClass().add(isError ? "status-error" : "status-normal");
        }
    }

    /**
     * 设置登录窗口引用
     */
    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }
} 