package com.studyroom.client.controller;

import com.studyroom.client.model.StudyRoom;
import com.studyroom.client.util.AlertUtils;
import com.studyroom.client.util.WindowUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 自习室列表控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class StudyRoomListController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(StudyRoomListController.class);

    // 搜索和过滤组件
    @FXML private TextField searchField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> capacityComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button clearFiltersButton;

    // 排序组件
    @FXML private ComboBox<String> sortComboBox;
    @FXML private CheckBox descendingCheckBox;

    // 统计标签
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label maintenanceRoomsLabel;

    // 主要内容
    @FXML private VBox roomListContainer;

    // 操作按钮
    @FXML private Button refreshButton;
    @FXML private Button viewModeButton;
    @FXML private Button favoriteButton;
    @FXML private Label statusLabel;

    // 数据
    private List<StudyRoom> allStudyRooms = new ArrayList<>();
    private List<StudyRoom> filteredStudyRooms = new ArrayList<>();
    private boolean isGridView = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化自习室列表界面...");
        
        try {
            // 初始化组件
            initializeComponents();
            
            // 加载数据
            loadStudyRooms();
            
            logger.info("✅ 自习室列表界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 自习室列表界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 初始化容量下拉框
        capacityComboBox.getItems().addAll(
            "不限", "1-10人", "11-20人", "21-50人", "50人以上"
        );

        // 初始化状态下拉框
        statusComboBox.getItems().addAll(
            "全部", "可用", "占用", "维护中", "已关闭"
        );

        // 初始化排序下拉框
        sortComboBox.getItems().addAll(
            "按名称", "按价格", "按容量", "按可用座位", "按创建时间"
        );
        sortComboBox.setValue("按名称");

        // 设置默认值
        updateStatus("就绪");
    }

    /**
     * 加载自习室数据
     */
    private void loadStudyRooms() {
        updateStatus("正在加载自习室数据...");
        
        // TODO: 从服务器加载数据
        // 这里使用模拟数据
        Platform.runLater(() -> {
            try {
                allStudyRooms = createMockStudyRooms();
                filteredStudyRooms = new ArrayList<>(allStudyRooms);
                
                updateStatistics();
                displayStudyRooms();
                updateStatus("数据加载完成");
                
            } catch (Exception e) {
                logger.error("❌ 加载自习室数据失败", e);
                updateStatus("数据加载失败: " + e.getMessage());
            }
        });
    }

    /**
     * 创建模拟自习室数据
     */
    private List<StudyRoom> createMockStudyRooms() {
        List<StudyRoom> rooms = new ArrayList<>();
        
        // 创建一些模拟自习室
        for (int i = 1; i <= 12; i++) {
            StudyRoom room = new StudyRoom();
            room.setId((long) i);
            room.setName("自习室 " + i + "号");
            room.setLocation("图书馆" + ((i - 1) / 4 + 1) + "楼");
            room.setCapacity(20 + (i * 5));
            room.setPricePerHour(new BigDecimal(5 + (i % 3) * 2));
            room.setDescription("舒适安静的学习环境，适合专注学习");
            
            // 设置不同的状态
            if (i % 4 == 0) {
                room.setStatus(StudyRoom.Status.MAINTENANCE);
            } else if (i % 3 == 0) {
                room.setStatus(StudyRoom.Status.OCCUPIED);
            } else {
                room.setStatus(StudyRoom.Status.AVAILABLE);
            }
            
            // 设置开放时间
            room.setOpenTime("08:00");
            room.setCloseTime("22:00");
            
            rooms.add(room);
        }
        
        return rooms;
    }

    /**
     * 显示自习室列表
     */
    private void displayStudyRooms() {
        roomListContainer.getChildren().clear();
        
        if (filteredStudyRooms.isEmpty()) {
            Label emptyLabel = new Label("没有找到符合条件的自习室");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 50px;");
            roomListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (StudyRoom room : filteredStudyRooms) {
            Node roomCard = createStudyRoomCard(room);
            roomListContainer.getChildren().add(roomCard);
        }
    }

    /**
     * 创建自习室卡片
     */
    private Node createStudyRoomCard(StudyRoom room) {
        VBox card = new VBox(10);
        card.getStyleClass().add("room-card");
        card.setPadding(new Insets(15));

        // 头部信息
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(room.getName());
        nameLabel.getStyleClass().add("room-name");

        Label statusLabel = new Label(room.getStatus().getDisplayName());
        statusLabel.getStyleClass().addAll("status-badge", 
            "status-" + room.getStatus().name().toLowerCase());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label("¥" + room.getPricePerHour() + "/小时");
        priceLabel.getStyleClass().add("price-label");

        header.getChildren().addAll(nameLabel, statusLabel, spacer, priceLabel);

        // 详细信息
        HBox details = new HBox(20);
        details.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label locationLabel = new Label("📍 " + room.getLocation());
        Label capacityLabel = new Label("👥 容量: " + room.getCapacity() + "人");
        Label timeLabel = new Label("🕐 " + room.getOpenTime() + " - " + room.getCloseTime());

        details.getChildren().addAll(locationLabel, capacityLabel, timeLabel);

        // 描述
        Label descLabel = new Label(room.getDescription());
        descLabel.getStyleClass().add("room-description");
        descLabel.setWrapText(true);

        // 操作按钮
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button viewButton = new Button("查看详情");
        viewButton.getStyleClass().add("action-button");
        viewButton.setOnAction(e -> handleViewDetails(room));

        Button reserveButton = new Button("立即预订");
        reserveButton.getStyleClass().addAll("action-button", "primary-button");
        reserveButton.setOnAction(e -> handleReserve(room));
        reserveButton.setDisable(room.getStatus() != StudyRoom.Status.AVAILABLE);

        Button favoriteButton = new Button("♡");
        favoriteButton.getStyleClass().addAll("icon-button", "favorite-button");
        favoriteButton.setOnAction(e -> handleToggleFavorite(room));

        actions.getChildren().addAll(favoriteButton, viewButton, reserveButton);

        card.getChildren().addAll(header, details, descLabel, actions);
        return card;
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        int total = filteredStudyRooms.size();
        int available = (int) filteredStudyRooms.stream()
            .filter(r -> r.getStatus() == StudyRoom.Status.AVAILABLE).count();
        int occupied = (int) filteredStudyRooms.stream()
            .filter(r -> r.getStatus() == StudyRoom.Status.OCCUPIED).count();
        int maintenance = (int) filteredStudyRooms.stream()
            .filter(r -> r.getStatus() == StudyRoom.Status.MAINTENANCE).count();

        totalRoomsLabel.setText("共找到 " + total + " 个自习室");
        availableRoomsLabel.setText("可用: " + available);
        occupiedRoomsLabel.setText("占用: " + occupied);
        maintenanceRoomsLabel.setText("维护: " + maintenance);
    }

    // 事件处理方法
    @FXML
    private void handleRefresh() {
        logger.info("🔄 刷新自习室列表");
        loadStudyRooms();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handlePriceFilter() {
        applyFilters();
    }

    @FXML
    private void handleCapacityFilter() {
        applyFilters();
    }

    @FXML
    private void handleStatusFilter() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        capacityComboBox.setValue(null);
        statusComboBox.setValue(null);
        applyFilters();
    }

    @FXML
    private void handleSort() {
        applySorting();
        displayStudyRooms();
    }

    @FXML
    private void handleViewModeToggle() {
        isGridView = !isGridView;
        viewModeButton.setText(isGridView ? "列表视图" : "网格视图");
        displayStudyRooms();
    }

    @FXML
    private void handleShowFavorites() {
        AlertUtils.showInfo("功能提示", "收藏功能正在开发中");
    }

    /**
     * 应用过滤条件
     */
    private void applyFilters() {
        filteredStudyRooms = allStudyRooms.stream()
            .filter(this::matchesSearchCriteria)
            .collect(Collectors.toList());

        applySorting();
        updateStatistics();
        displayStudyRooms();
    }

    /**
     * 检查是否匹配搜索条件
     */
    private boolean matchesSearchCriteria(StudyRoom room) {
        // 搜索关键词
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String keyword = searchText.toLowerCase();
            if (!room.getName().toLowerCase().contains(keyword) &&
                !room.getLocation().toLowerCase().contains(keyword)) {
                return false;
            }
        }

        // 价格范围
        try {
            String minPriceText = minPriceField.getText();
            if (minPriceText != null && !minPriceText.trim().isEmpty()) {
                BigDecimal minPrice = new BigDecimal(minPriceText);
                if (room.getPricePerHour().compareTo(minPrice) < 0) {
                    return false;
                }
            }

            String maxPriceText = maxPriceField.getText();
            if (maxPriceText != null && !maxPriceText.trim().isEmpty()) {
                BigDecimal maxPrice = new BigDecimal(maxPriceText);
                if (room.getPricePerHour().compareTo(maxPrice) > 0) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            // 忽略价格格式错误
        }

        // 容量过滤
        String capacity = capacityComboBox.getValue();
        if (capacity != null && !capacity.equals("不限")) {
            if (!matchesCapacityRange(room.getCapacity(), capacity)) {
                return false;
            }
        }

        // 状态过滤
        String status = statusComboBox.getValue();
        if (status != null && !status.equals("全部")) {
            if (!matchesStatus(room.getStatus(), status)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 应用排序
     */
    private void applySorting() {
        String sortBy = sortComboBox.getValue();
        boolean descending = descendingCheckBox.isSelected();

        Comparator<StudyRoom> comparator = null;

        switch (sortBy) {
            case "按名称":
                comparator = Comparator.comparing(StudyRoom::getName);
                break;
            case "按价格":
                comparator = Comparator.comparing(StudyRoom::getPricePerHour);
                break;
            case "按容量":
                comparator = Comparator.comparing(StudyRoom::getCapacity);
                break;
            case "按可用座位":
                comparator = Comparator.comparing(r -> r.getCapacity()); // TODO: 改为可用座位数
                break;
            case "按创建时间":
                comparator = Comparator.comparing(StudyRoom::getId); // 使用ID作为创建时间排序
                break;
        }

        if (comparator != null) {
            if (descending) {
                comparator = comparator.reversed();
            }
            filteredStudyRooms.sort(comparator);
        }
    }

    // 辅助方法
    private boolean matchesCapacityRange(int capacity, String range) {
        switch (range) {
            case "1-10人": return capacity >= 1 && capacity <= 10;
            case "11-20人": return capacity >= 11 && capacity <= 20;
            case "21-50人": return capacity >= 21 && capacity <= 50;
            case "50人以上": return capacity > 50;
            default: return true;
        }
    }

    private boolean matchesStatus(StudyRoom.Status status, String statusText) {
        switch (statusText) {
            case "可用": return status == StudyRoom.Status.AVAILABLE;
            case "占用": return status == StudyRoom.Status.OCCUPIED;
            case "维护中": return status == StudyRoom.Status.MAINTENANCE;
            case "已关闭": return status == StudyRoom.Status.CLOSED;
            default: return true;
        }
    }

    private void handleViewDetails(StudyRoom room) {
        logger.info("📋 查看自习室详情: {}", room.getName());
        AlertUtils.showInfo("自习室详情", 
            "名称: " + room.getName() + "\n" +
            "位置: " + room.getLocation() + "\n" +
            "容量: " + room.getCapacity() + "人\n" +
            "价格: ¥" + room.getPricePerHour() + "/小时\n" +
            "状态: " + room.getStatus().getDisplayName() + "\n" +
            "开放时间: " + room.getOpenTime() + " - " + room.getCloseTime());
    }

    private void handleReserve(StudyRoom room) {
        logger.info("📅 预订自习室: {}", room.getName());
        
        try {
            // 获取当前窗口
            Window currentWindow = null;
            for (Window window : Window.getWindows()) {
                if (window.isShowing() && window.isFocused()) {
                    currentWindow = window;
                    break;
                }
            }
            
            // 打开座位选择窗口
            WindowUtils.openSeatSelectionWindow(currentWindow, room.getId(), room.getName());
            
        } catch (Exception e) {
            logger.error("❌ 打开座位选择窗口失败", e);
            AlertUtils.showError("错误", "无法打开座位选择窗口: " + e.getMessage());
        }
    }

    private void handleToggleFavorite(StudyRoom room) {
        logger.info("❤️ 切换收藏状态: {}", room.getName());
        AlertUtils.showInfo("收藏", "收藏功能正在开发中");
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }
} 