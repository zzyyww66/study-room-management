package com.studyroom.client.controller;

import com.studyroom.client.model.User;
import com.studyroom.client.model.PageData;
import com.studyroom.client.service.ApiServiceManager;
import com.studyroom.client.service.UserApiService;
import com.studyroom.client.util.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * 用户管理控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class UserManagementController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    // 顶部操作按钮
    @FXML private Button addUserButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    // 统计卡片
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label adminUsersLabel;
    @FXML private Label newUsersLabel;

    // 过滤和搜索
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private DatePicker dateFilterPicker;
    @FXML private Button clearFiltersButton;

    // 用户表格
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> realNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, LocalDateTime> registerTimeColumn;
    @FXML private TableColumn<User, LocalDateTime> lastLoginColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    // 分页控制
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<String> pageSizeComboBox;

    // 底部状态栏
    @FXML private Label totalRecordsLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;

    // 服务和数据
    private final UserApiService userApiService;
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    
    // 分页数据
    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;
    private long totalElements = 0;

    // 构造函数
    public UserManagementController() {
        this.userApiService = ApiServiceManager.getInstance().getUserApiService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化用户管理界面...");
        
        try {
            // 初始化组件
            initializeComponents();
            
            // 初始化表格
            initializeTable();
            
            // 加载数据
            loadUsers();
            
            logger.info("✅ 用户管理界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 用户管理界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 初始化过滤器
        roleFilterComboBox.getItems().addAll("全部角色", "管理员", "普通用户");
        roleFilterComboBox.setValue("全部角色");
        
        statusFilterComboBox.getItems().addAll("全部状态", "正常", "停用", "封禁");
        statusFilterComboBox.setValue("全部状态");

        // 初始化分页大小选择器
        pageSizeComboBox.getItems().addAll("10", "20", "50", "100");
        pageSizeComboBox.setValue("20");

        // 设置默认状态
        updateStatus("就绪");
        updateLastUpdate();
        updateStatistics(0, 0, 0, 0);
    }

    /**
     * 初始化表格
     */
    private void initializeTable() {
        // 绑定数据到表格
        userTableView.setItems(userList);
        
        // 设置表格列的数据绑定
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        realNameColumn.setCellValueFactory(new PropertyValueFactory<>("realName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // 角色列 - 显示中文
        roleColumn.setCellValueFactory(cellData -> {
            User.Role role = cellData.getValue().getRole();
            return new javafx.beans.property.SimpleStringProperty(
                role != null ? role.getDisplayName() : "未知"
            );
        });
        
        // 状态列 - 显示中文
        statusColumn.setCellValueFactory(cellData -> {
            User.Status status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(
                status != null ? status.getDisplayName() : "未知"
            );
        });
        
        // 注册时间列 - 格式化显示
        registerTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        registerTimeColumn.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        
        // 最后登录时间列 - 格式化显示
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLoginAt"));
        lastLoginColumn.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("从未登录");
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        
        // 操作列 - 添加编辑、删除按钮
        actionColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button editButton = new Button("编辑");
                    private final Button deleteButton = new Button("删除");
                    
                    {
                        editButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleEditUser(user);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDeleteUser(user);
                        });
                        
                        editButton.getStyleClass().add("button-primary");
                        deleteButton.getStyleClass().add("button-danger");
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                            buttons.getChildren().addAll(editButton, deleteButton);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
        
        // 设置表格为空时的提示
        userTableView.setPlaceholder(new Label("正在加载用户数据..."));
    }

    /**
     * 加载用户数据
     */
    private void loadUsers() {
        updateStatus("正在加载用户数据...");
        
        // 获取过滤条件
        String searchKeyword = searchField.getText();
        String roleFilter = roleFilterComboBox.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        
        // 转换过滤条件
        User.Role role = convertRoleFilter(roleFilter);
        User.Status status = convertStatusFilter(statusFilter);
        
        // 调用API获取分页数据
        CompletableFuture<PageData<User>> future = userApiService.getUsers(
            currentPage - 1, // API从0开始计数
            pageSize,
            searchKeyword,
            role,
            status
        );
        
        future.thenAccept(pageData -> {
            Platform.runLater(() -> {
                try {
                    if (pageData != null) {
                        // 更新表格数据
                        userList.clear();
                        userList.addAll(pageData.getContent());
                        
                        // 更新分页信息
                        totalElements = pageData.getTotalElements();
                        totalPages = pageData.getTotalPages();
                        
                        // 更新UI显示
                        updatePageInfo();
                        updateStatistics(pageData);
                        totalRecordsLabel.setText("共 " + totalElements + " 条记录");
                        
                        // 更新状态
                        updateStatus("数据加载完成，共 " + pageData.getContent().size() + " 条记录");
                        updateLastUpdate();
                        
                        logger.info("✅ 用户数据加载成功，当前页: {}/{}, 记录数: {}", 
                            currentPage, totalPages, pageData.getContent().size());
                    } else {
                        updateStatus("未获取到数据");
                        logger.warn("⚠️ 获取用户数据为空");
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ 处理用户数据失败", e);
                    updateStatus("数据处理失败: " + e.getMessage());
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                logger.error("❌ 加载用户数据失败", throwable);
                updateStatus("数据加载失败: " + throwable.getMessage());
                
                // 显示错误提示
                AlertUtils.showError("数据加载失败", 
                    "无法连接到服务器或数据加载出错：\n" + throwable.getMessage());
            });
            return null;
        });
    }

    /**
     * 转换角色过滤条件
     */
    private User.Role convertRoleFilter(String roleFilter) {
        if (roleFilter == null || "全部角色".equals(roleFilter)) {
            return null;
        }
        switch (roleFilter) {
            case "管理员":
                return User.Role.ADMIN;
            case "普通用户":
                return User.Role.USER;
            default:
                return null;
        }
    }

    /**
     * 转换状态过滤条件
     */
    private User.Status convertStatusFilter(String statusFilter) {
        if (statusFilter == null || "全部状态".equals(statusFilter)) {
            return null;
        }
        switch (statusFilter) {
            case "正常":
                return User.Status.ACTIVE;
            case "停用":
                return User.Status.INACTIVE;
            case "封禁":
                return User.Status.BANNED;
            default:
                return null;
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(PageData<User> pageData) {
        if (pageData != null) {
            // 计算统计数据
            long totalUsers = pageData.getTotalElements();
            long activeUsers = pageData.getContent().stream()
                .mapToLong(user -> user.getStatus() == User.Status.ACTIVE ? 1 : 0)
                .sum();
            long adminUsers = pageData.getContent().stream()
                .mapToLong(user -> user.getRole() == User.Role.ADMIN ? 1 : 0)
                .sum();
            long newUsers = pageData.getContent().stream()
                .mapToLong(user -> {
                    if (user.getCreatedAt() != null) {
                        return user.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)) ? 1 : 0;
                    }
                    return 0;
                })
                .sum();
            
            updateStatistics((int)totalUsers, (int)activeUsers, (int)adminUsers, (int)newUsers);
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(int total, int active, int admin, int newUsers) {
        totalUsersLabel.setText(String.valueOf(total));
        activeUsersLabel.setText(String.valueOf(active));
        adminUsersLabel.setText(String.valueOf(admin));
        newUsersLabel.setText(String.valueOf(newUsers));
    }

    /**
     * 更新分页信息
     */
    private void updatePageInfo() {
        pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页");
        
        // 更新分页按钮状态
        firstPageButton.setDisable(currentPage <= 1);
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
        lastPageButton.setDisable(currentPage >= totalPages);
    }

    /**
     * 处理编辑用户
     */
    private void handleEditUser(User user) {
        logger.info("✏️ 编辑用户: {}", user.getUsername());
        AlertUtils.showInfo("编辑用户", "编辑用户功能正在开发中\n用户: " + user.getUsername());
    }

    /**
     * 处理删除用户
     */
    private void handleDeleteUser(User user) {
        logger.info("🗑️ 删除用户: {}", user.getUsername());
        
        // 确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText("删除用户");
        confirmDialog.setContentText("确定要删除用户 " + user.getUsername() + " 吗？");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 调用删除API
                userApiService.deleteUser(user.getId())
                    .thenAccept(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                logger.info("✅ 用户删除成功: {}", user.getUsername());
                                AlertUtils.showInfo("删除成功", "用户已成功删除");
                                loadUsers(); // 重新加载数据
                            } else {
                                logger.error("❌ 用户删除失败: {}", user.getUsername());
                                AlertUtils.showError("删除失败", "删除用户失败，请稍后重试");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            logger.error("❌ 删除用户API调用失败", throwable);
                            AlertUtils.showError("删除失败", "删除用户时发生错误：\n" + throwable.getMessage());
                        });
                        return null;
                    });
            }
        });
    }

    // 事件处理方法
    @FXML
    private void handleAddUser() {
        logger.info("➕ 添加新用户");
        AlertUtils.showInfo("添加用户", "添加用户功能正在开发中");
    }

    @FXML
    private void handleExport() {
        logger.info("📤 导出用户数据");
        AlertUtils.showInfo("导出数据", "导出功能正在开发中");
    }

    @FXML
    private void handleRefresh() {
        logger.info("🔄 刷新用户列表");
        loadUsers();
    }

    @FXML
    private void handleFilter() {
        logger.info("🔍 应用过滤条件");
        currentPage = 1; // 重置到第一页
        loadUsers();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        logger.info("🔍 搜索用户: {}", keyword);
        currentPage = 1; // 重置到第一页
        loadUsers();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        roleFilterComboBox.setValue("全部角色");
        statusFilterComboBox.setValue("全部状态");
        dateFilterPicker.setValue(null);
        
        logger.info("🧹 清除过滤条件");
        currentPage = 1; // 重置到第一页
        loadUsers();
    }

    // 分页事件处理
    @FXML
    private void handleFirstPage() {
        currentPage = 1;
        loadUsers();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadUsers();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadUsers();
        }
    }

    @FXML
    private void handleLastPage() {
        currentPage = totalPages;
        loadUsers();
    }

    @FXML
    private void handlePageSizeChange() {
        String newSize = pageSizeComboBox.getValue();
        if (newSize != null) {
            pageSize = Integer.parseInt(newSize);
            currentPage = 1; // 重置到第一页
            loadUsers();
        }
    }

    /**
     * 更新状态
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    /**
     * 更新最后更新时间
     */
    private void updateLastUpdate() {
        LocalDateTime now = LocalDateTime.now();
        String timeText = now.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        Platform.runLater(() -> lastUpdateLabel.setText("最后更新: " + timeText));
    }
} 