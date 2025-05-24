package com.studyroom.client.controller;

import com.studyroom.client.model.StudyRoom;
import com.studyroom.client.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 座位选择控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class SeatSelectionController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SeatSelectionController.class);

    // 顶部自习室信息
    @FXML private Label roomNameLabel;
    @FXML private Label roomLocationLabel;
    @FXML private Label roomPriceLabel;
    @FXML private Button backButton;

    // 座位布局容器
    @FXML private VBox seatGridContainer;

    // 右侧信息面板
    @FXML private Label selectedSeatLabel;
    @FXML private Label seatTypeLabel;
    @FXML private Label seatFeaturesLabel;

    // 时间选择
    @FXML private DatePicker reservationDatePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private ComboBox<String> endTimeComboBox;
    @FXML private Label durationLabel;

    // 费用计算
    @FXML private Label baseCostLabel;
    @FXML private Label seatCostLabel;
    @FXML private Label totalCostLabel;

    // 操作按钮
    @FXML private Button confirmButton;
    @FXML private Button clearButton;

    // 状态信息
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;

    // 数据
    private StudyRoom currentRoom;
    private String selectedSeatId;
    private BigDecimal hourlyRate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化座位选择界面...");
        
        try {
            // 初始化组件
            initializeComponents();
            
            logger.info("✅ 座位选择界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 座位选择界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 初始化时间选择器
        initializeTimeSelectors();
        
        // 设置默认日期为今天
        reservationDatePicker.setValue(LocalDate.now());
        
        // 初始化状态
        updateStatus("请选择座位");
        updateLastUpdate();
        
        // 清除选择状态
        clearSelection();
    }

    /**
     * 初始化时间选择器
     */
    private void initializeTimeSelectors() {
        // 添加时间选项（8:00-22:00）
        for (int hour = 8; hour <= 22; hour++) {
            String timeStr = String.format("%02d:00", hour);
            startTimeComboBox.getItems().add(timeStr);
            endTimeComboBox.getItems().add(timeStr);
        }
        
        // 设置默认值
        startTimeComboBox.setValue("09:00");
        endTimeComboBox.setValue("12:00");
    }

    /**
     * 设置当前自习室
     */
    public void setCurrentRoom(StudyRoom room) {
        this.currentRoom = room;
        if (room != null) {
            roomNameLabel.setText(room.getName());
            roomLocationLabel.setText(room.getLocation());
            roomPriceLabel.setText("¥" + room.getPricePerHour() + "/小时");
            this.hourlyRate = room.getPricePerHour();
            
            // 加载座位布局
            loadSeatLayout();
        }
    }

    /**
     * 加载座位布局
     */
    private void loadSeatLayout() {
        seatGridContainer.getChildren().clear();
        
        try {
            updateStatus("正在加载座位布局...");
            
            // 创建座位网格
            createSeatGrid();
            
            updateStatus("座位布局加载完成");
            
        } catch (Exception e) {
            logger.error("❌ 加载座位布局失败", e);
            updateStatus("座位布局加载失败");
            
            // 显示错误占位符
            Label errorLabel = new Label("座位布局加载失败: " + e.getMessage());
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff0000; -fx-padding: 50px;");
            seatGridContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 创建座位网格
     */
    private void createSeatGrid() {
        int capacity = (currentRoom != null) ? currentRoom.getCapacity() : 50;
        
        // 计算网格布局（尽量接近正方形）
        int cols = (int) Math.ceil(Math.sqrt(capacity * 1.2)); // 稍微宽一点的布局
        int rows = (int) Math.ceil((double) capacity / cols);
        
        logger.info("📊 创建座位网格: {}行 × {}列，总容量: {}", rows, cols, capacity);
        
        int seatNumber = 1;
        
        for (int row = 0; row < rows && seatNumber <= capacity; row++) {
            // 创建每一行
            javafx.scene.layout.HBox rowBox = new javafx.scene.layout.HBox(8);
            rowBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            for (int col = 0; col < cols && seatNumber <= capacity; col++) {
                // 创建座位按钮
                Button seatButton = createSeatButton(seatNumber, row, col);
                rowBox.getChildren().add(seatButton);
                seatNumber++;
            }
            
            seatGridContainer.getChildren().add(rowBox);
        }
    }
    
    /**
     * 创建座位按钮
     */
    private Button createSeatButton(int seatNumber, int row, int col) {
        Button seatButton = new Button(String.valueOf(seatNumber));
        String seatId = "S" + String.format("%03d", seatNumber);
        
        // 设置按钮基本属性
        seatButton.setPrefSize(45, 45);
        seatButton.setMinSize(45, 45);
        seatButton.setMaxSize(45, 45);
        seatButton.setUserData(seatId);
        
        // 设置座位状态（模拟数据）
        SeatStatus status = generateMockSeatStatus(seatNumber);
        applySeatStyle(seatButton, status);
        
        // 添加点击事件
        seatButton.setOnAction(e -> handleSeatSelection(seatButton, seatId, status));
        
        return seatButton;
    }
    
    /**
     * 生成模拟座位状态
     */
    private SeatStatus generateMockSeatStatus(int seatNumber) {
        // 模拟不同的座位状态
        if (seatNumber % 13 == 0) {
            return SeatStatus.MAINTENANCE; // 维护中
        } else if (seatNumber % 7 == 0) {
            return SeatStatus.OCCUPIED; // 已占用
        } else if (seatNumber % 11 == 0) {
            return SeatStatus.VIP; // VIP座位
        } else {
            return SeatStatus.AVAILABLE; // 可用
        }
    }
    
    /**
     * 应用座位样式
     */
    private void applySeatStyle(Button seatButton, SeatStatus status) {
        // 移除所有状态样式
        seatButton.getStyleClass().removeAll("seat-available", "seat-occupied", 
                                           "seat-selected", "seat-maintenance", "seat-vip");
        
        // 添加对应状态样式
        switch (status) {
            case AVAILABLE:
                seatButton.getStyleClass().add("seat-available");
                seatButton.setDisable(false);
                break;
            case OCCUPIED:
                seatButton.getStyleClass().add("seat-occupied");
                seatButton.setDisable(true);
                break;
            case MAINTENANCE:
                seatButton.getStyleClass().add("seat-maintenance");
                seatButton.setDisable(true);
                break;
            case VIP:
                seatButton.getStyleClass().add("seat-vip");
                seatButton.setDisable(false);
                break;
            case SELECTED:
                seatButton.getStyleClass().add("seat-selected");
                seatButton.setDisable(false);
                break;
        }
    }
    
    /**
     * 处理座位选择
     */
    private void handleSeatSelection(Button seatButton, String seatId, SeatStatus status) {
        if (status == SeatStatus.OCCUPIED || status == SeatStatus.MAINTENANCE) {
            return; // 不可选择的座位
        }
        
        // 清除之前的选择
        clearPreviousSelection();
        
        // 设置新选择
        selectedSeatId = seatId;
        applySeatStyle(seatButton, SeatStatus.SELECTED);
        
        // 更新右侧信息
        updateSeatInfo(seatId, status);
        
        // 启用确认按钮
        confirmButton.setDisable(false);
        
        // 重新计算费用
        calculateCost();
        
        logger.info("🪑 选择座位: {}", seatId);
        updateStatus("已选择座位 " + seatId);
    }
    
    /**
     * 清除之前的选择
     */
    private void clearPreviousSelection() {
        if (selectedSeatId != null) {
            // 找到之前选择的座位按钮并重置样式
            findSeatButton(selectedSeatId).ifPresent(button -> {
                String seatIdFromButton = (String) button.getUserData();
                int seatNumber = Integer.parseInt(seatIdFromButton.substring(1));
                SeatStatus originalStatus = generateMockSeatStatus(seatNumber);
                applySeatStyle(button, originalStatus);
            });
        }
    }
    
    /**
     * 查找座位按钮
     */
    private java.util.Optional<Button> findSeatButton(String seatId) {
        return seatGridContainer.getChildren().stream()
            .filter(node -> node instanceof javafx.scene.layout.HBox)
            .flatMap(hbox -> ((javafx.scene.layout.HBox) hbox).getChildren().stream())
            .filter(node -> node instanceof Button)
            .map(node -> (Button) node)
            .filter(button -> seatId.equals(button.getUserData()))
            .findFirst();
    }
    
    /**
     * 更新座位信息
     */
    private void updateSeatInfo(String seatId, SeatStatus status) {
        selectedSeatLabel.setText(seatId);
        
        switch (status) {
            case VIP:
                seatTypeLabel.setText("VIP座位");
                seatFeaturesLabel.setText("电源插座、台灯、靠背椅");
                break;
            default:
                seatTypeLabel.setText("普通座位");
                seatFeaturesLabel.setText("基础桌椅");
                break;
        }
    }
    
    /**
     * 座位状态枚举
     */
    private enum SeatStatus {
        AVAILABLE,  // 可用
        OCCUPIED,   // 已占用
        MAINTENANCE, // 维护中
        VIP,        // VIP座位
        SELECTED    // 已选择
    }

    // 事件处理方法
    @FXML
    private void handleBack() {
        logger.info("🔙 返回自习室列表");
        // TODO: 实现返回逻辑
        AlertUtils.showInfo("提示", "返回功能正在开发中");
    }

    @FXML
    private void handleTimeChange() {
        calculateCost();
    }

    @FXML
    private void handleConfirmReservation() {
        if (selectedSeatId == null) {
            AlertUtils.showWarning("请选择座位", "请先选择一个座位");
            return;
        }
        
        logger.info("📅 确认预订座位: {}", selectedSeatId);
        AlertUtils.showInfo("预订确认", "预订功能正在开发中");
    }

    @FXML
    private void handleClearSelection() {
        clearSelection();
        updateStatus("已清除选择");
    }

    /**
     * 清除选择
     */
    private void clearSelection() {
        // 清除之前的选择样式
        clearPreviousSelection();
        
        // 重置选择状态
        selectedSeatId = null;
        selectedSeatLabel.setText("未选择");
        seatTypeLabel.setText("普通座位");
        seatFeaturesLabel.setText("无");
        confirmButton.setDisable(true);
        calculateCost();
    }

    /**
     * 计算费用
     */
    private void calculateCost() {
        try {
            String startTime = startTimeComboBox.getValue();
            String endTime = endTimeComboBox.getValue();
            
            if (startTime == null || endTime == null) {
                resetCostLabels();
                return;
            }
            
            // 计算时长
            int startHour = Integer.parseInt(startTime.split(":")[0]);
            int endHour = Integer.parseInt(endTime.split(":")[0]);
            int duration = endHour - startHour;
            
            if (duration <= 0) {
                resetCostLabels();
                durationLabel.setText("时间选择错误");
                return;
            }
            
            durationLabel.setText(duration + "小时");
            
            // 计算费用
            if (hourlyRate != null) {
                BigDecimal baseCost = hourlyRate.multiply(new BigDecimal(duration));
                BigDecimal seatCost = BigDecimal.ZERO; // VIP座位可能有额外费用
                BigDecimal totalCost = baseCost.add(seatCost);
                
                baseCostLabel.setText("¥" + baseCost);
                seatCostLabel.setText("¥" + seatCost);
                totalCostLabel.setText("¥" + totalCost);
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ 费用计算失败", e);
            resetCostLabels();
        }
    }

    /**
     * 重置费用标签
     */
    private void resetCostLabels() {
        durationLabel.setText("0小时");
        baseCostLabel.setText("¥0");
        seatCostLabel.setText("¥0");
        totalCostLabel.setText("¥0");
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