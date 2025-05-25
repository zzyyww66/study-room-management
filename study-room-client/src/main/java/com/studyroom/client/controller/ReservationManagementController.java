package com.studyroom.client.controller;

import com.studyroom.client.model.Reservation;
import com.studyroom.client.model.PageData;
import com.studyroom.client.service.ApiServiceManager;
import com.studyroom.client.service.ReservationApiService;
import com.studyroom.client.util.AlertUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 预订管理控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class ReservationManagementController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ReservationManagementController.class);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // 顶部操作按钮
    @FXML private Button newReservationButton;
    @FXML private Button refreshButton;

    // 过滤组件
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private DatePicker dateFilterPicker;
    @FXML private TextField searchField;
    @FXML private Button clearFiltersButton;

    // 预订表格
    @FXML private TableView<Reservation> reservationTableView;
    @FXML private TableColumn<Reservation, Long> idColumn;
    @FXML private TableColumn<Reservation, String> userColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;
    @FXML private TableColumn<Reservation, String> seatColumn;
    @FXML private TableColumn<Reservation, String> dateColumn;
    @FXML private TableColumn<Reservation, String> timeColumn;
    @FXML private TableColumn<Reservation, BigDecimal> costColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    // 底部状态栏
    @FXML private Label totalReservationsLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;

    private ReservationApiService reservationApiService;
    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化预订管理界面...");
        this.reservationApiService = ApiServiceManager.getInstance().getReservationApiService();
        
        try {
            initializeComponents();
            initializeTable();
            loadReservations();
            logger.info("✅ 预订管理界面初始化完成");
        } catch (Exception e) {
            logger.error("❌ 预订管理界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
            AlertUtils.showError("界面加载错误", "预订管理界面初始化失败，请查看日志。");
        }
    }

    /**
     * 初始化组件
     */
    private void initializeComponents() {
        statusFilterComboBox.getItems().addAll("全部状态", "已确认/进行中", "已完成", "已取消", "已过期", "未到场");
        statusFilterComboBox.setValue("全部状态");
        updateStatus("就绪");
        updateLastUpdate();
    }

    /**
     * 初始化表格
     */
    private void initializeTable() {
        reservationTableView.setItems(reservationList);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser() != null ? cellData.getValue().getUser().getUsername() : "N/A"));
        roomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSeat() != null && cellData.getValue().getSeat().getStudyRoom() != null ? cellData.getValue().getSeat().getStudyRoom().getName() : "N/A"));
        seatColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSeat() != null ? cellData.getValue().getSeat().getSeatNumber() : "N/A"));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartTime() != null ? cellData.getValue().getStartTime().format(dateFormatter) : ""));
        timeColumn.setCellValueFactory(cellData -> {
            Reservation res = cellData.getValue();
            if (res.getStartTime() != null && res.getEndTime() != null) {
                return new SimpleStringProperty(res.getStartTime().format(timeFormatter) + " - " + res.getEndTime().format(timeFormatter));
            }
            return new SimpleStringProperty("");
        });
        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().getDisplayName() : ""));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("查看");
            private final Button cancelButton = new Button("取消");
            private final HBox pane = new HBox(viewButton, cancelButton);

            {
                pane.setSpacing(5);
                viewButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleViewReservation(reservation);
                });
                cancelButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    handleCancelReservation(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    cancelButton.setDisable(!reservation.canCancel());
                    setGraphic(pane);
                }
            }
        });
        reservationTableView.setPlaceholder(new Label("没有符合条件的预订记录"));
    }

    /**
     * 加载预订数据
     */
    private void loadReservations() {
        updateStatus("正在加载预订数据...");
        String statusFilterValue = statusFilterComboBox.getValue(); // 获取显示名
        LocalDate date = dateFilterPicker.getValue();
        String keyword = searchField.getText();

        Reservation.Status queryStatus = null;
        if (statusFilterValue != null && !"全部状态".equals(statusFilterValue)) {
            // 根据 displayName 反向查找枚举常量名
            for (Reservation.Status s : Reservation.Status.values()) {
                if (s.getDisplayName().equals(statusFilterValue)) {
                    queryStatus = s;
                    break;
                }
            }
            if (queryStatus == null) {
                logger.warn("无法将显示名 '{}' 转换为有效的预订状态枚举", statusFilterValue);
            }
        }
        
        //TODO: 添加日期和关键字过滤参数到API调用
        reservationApiService.getReservations(0, 20, null, queryStatus, null) 
            .thenAccept(pageData -> Platform.runLater(() -> {
                if (pageData != null && pageData.getContent() != null) {
                    reservationList.setAll(pageData.getContent());
                    totalReservationsLabel.setText("总计: " + pageData.getTotalElements() + " 个预订");
                    updateStatus("数据加载完成，共 " + pageData.getContent().size() + " 条");
                } else {
                    reservationList.clear();
                    totalReservationsLabel.setText("总计: 0 个预订");
                    updateStatus("未获取到预订数据");
                }
                updateLastUpdate();
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    logger.error("❌ 加载预订数据失败", throwable);
                    updateStatus("数据加载失败: " + throwable.getMessage());
                    reservationList.clear();
                    totalReservationsLabel.setText("加载失败");
                    AlertUtils.showError("加载错误", "无法从服务器加载预订数据: " + throwable.getMessage());
                });
                return null;
            });
    }

    // 事件处理方法
    @FXML
    private void handleNewReservation() {
        logger.info("➕ 创建新预订");
        AlertUtils.showInfo("新建预订", "新建预订功能正在开发中");
        // TODO: 实现打开新建预订对话框的逻辑
    }

    @FXML
    private void handleRefresh() {
        logger.info("🔄 刷新预订列表");
        loadReservations();
    }

    @FXML
    private void handleFilter() {
        logger.info("🔍 应用过滤条件");
        loadReservations(); // 重新加载数据以应用过滤器
    }

    @FXML
    private void handleSearch() {
        loadReservations(); // 简单实现，每次按键都重新加载
    }

    @FXML
    private void handleClearFilters() {
        statusFilterComboBox.setValue("全部状态");
        dateFilterPicker.setValue(null);
        searchField.clear();
        
        logger.info("🧹 清除过滤条件");
        loadReservations();
    }

    private void handleViewReservation(Reservation reservation) {
        logger.info("👁️ 查看预订: ID={}", reservation.getId());
        // TODO: 实现查看预订详情对话框
        AlertUtils.showInfo("预订详情", "预订号: " + reservation.getId() + "\n用户: " + (reservation.getUser() != null ? reservation.getUser().getUsername() : "N/A") + "\n自习室: " + (reservation.getSeat() !=null && reservation.getSeat().getStudyRoom() != null ? reservation.getSeat().getStudyRoom().getName() : "N/A"));
    }

    private void handleCancelReservation(Reservation reservation) {
        logger.info("🚫 取消预订: ID={}", reservation.getId());
        if (AlertUtils.showConfirm("确认取消", "您确定要取消这个预订吗？此操作无法撤销。")) {
            reservationApiService.cancelReservation(reservation.getId())
                .thenAccept(updatedReservation -> Platform.runLater(() -> {
                    if (updatedReservation != null && updatedReservation.getStatus() == Reservation.Status.CANCELLED) {
                        AlertUtils.showInfo("取消成功", "预订已成功取消。");
                        loadReservations(); // 重新加载列表
                    } else {
                        AlertUtils.showError("取消失败", "未能取消预订，请重试或联系管理员。");
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        logger.error("❌ 取消预订API调用失败", throwable);
                        AlertUtils.showError("取消失败", "操作失败: " + throwable.getMessage());
                    });
                    return null;
                });
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