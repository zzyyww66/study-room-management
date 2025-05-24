package com.studyroom.client.controller;

import com.studyroom.client.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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
    @FXML private TableView<?> userTableView;
    @FXML private TableColumn<?, ?> idColumn;
    @FXML private TableColumn<?, ?> usernameColumn;
    @FXML private TableColumn<?, ?> realNameColumn;
    @FXML private TableColumn<?, ?> emailColumn;
    @FXML private TableColumn<?, ?> phoneColumn;
    @FXML private TableColumn<?, ?> roleColumn;
    @FXML private TableColumn<?, ?> statusColumn;
    @FXML private TableColumn<?, ?> registerTimeColumn;
    @FXML private TableColumn<?, ?> lastLoginColumn;
    @FXML private TableColumn<?, ?> actionColumn;

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

    // 分页数据
    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化用户管理界面...");
        
        try {
            // 初始化组件
            initializeComponents();
            
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
        
        statusFilterComboBox.getItems().addAll("全部状态", "活跃", "禁用", "锁定");
        statusFilterComboBox.setValue("全部状态");

        // 初始化分页大小选择器
        pageSizeComboBox.getItems().addAll("10", "20", "50", "100");
        pageSizeComboBox.setValue("20");

        // 初始化表格
        initializeTable();
        
        // 设置默认状态
        updateStatus("就绪");
        updateLastUpdate();
        updateStatistics(0, 0, 0, 0);
    }

    /**
     * 初始化表格
     */
    private void initializeTable() {
        // TODO: 配置表格列和数据绑定
        userTableView.setPlaceholder(new Label("暂无用户数据"));
    }

    /**
     * 加载用户数据
     */
    private void loadUsers() {
        updateStatus("正在加载用户数据...");
        
        // TODO: 从服务器加载用户数据
        Platform.runLater(() -> {
            try {
                // 暂时显示模拟统计数据
                updateStatistics(1250, 980, 25, 68);
                totalRecordsLabel.setText("共 0 条记录");
                updatePageInfo();
                updateStatus("数据加载完成");
                updateLastUpdate();
                
            } catch (Exception e) {
                logger.error("❌ 加载用户数据失败", e);
                updateStatus("数据加载失败: " + e.getMessage());
            }
        });
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
        // TODO: 实现过滤逻辑
        updateStatus("过滤条件已应用");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword != null && !keyword.trim().isEmpty()) {
            logger.info("🔍 搜索用户: {}", keyword);
            // TODO: 实现搜索逻辑
            updateStatus("搜索: " + keyword);
        }
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        roleFilterComboBox.setValue("全部角色");
        statusFilterComboBox.setValue("全部状态");
        dateFilterPicker.setValue(null);
        
        logger.info("🧹 清除过滤条件");
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