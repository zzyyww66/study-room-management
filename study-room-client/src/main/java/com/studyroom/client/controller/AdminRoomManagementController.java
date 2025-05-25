package com.studyroom.client.controller;

import com.studyroom.client.model.StudyRoom;
import com.studyroom.client.service.ApiServiceManager;
import com.studyroom.client.service.StudyRoomApiService;
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

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 管理员自习室管理控制器
 * 
 * @author Developer
 * @version 1.0.0
 */
public class AdminRoomManagementController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AdminRoomManagementController.class);

    // 顶部操作按钮
    @FXML private Button addRoomButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    // 统计卡片
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label maintenanceRoomsLabel;

    // 搜索和过滤
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> capacityFilterComboBox;
    @FXML private Button clearFiltersButton;

    // 自习室表格
    @FXML private TableView<StudyRoom> roomTableView;
    @FXML private TableColumn<StudyRoom, Long> idColumn;
    @FXML private TableColumn<StudyRoom, String> nameColumn;
    @FXML private TableColumn<StudyRoom, String> locationColumn;
    @FXML private TableColumn<StudyRoom, Integer> capacityColumn;
    @FXML private TableColumn<StudyRoom, BigDecimal> hourlyRateColumn;
    @FXML private TableColumn<StudyRoom, String> statusColumn;
    @FXML private TableColumn<StudyRoom, LocalTime> openTimeColumn;
    @FXML private TableColumn<StudyRoom, LocalTime> closeTimeColumn;
    @FXML private TableColumn<StudyRoom, Void> actionColumn;

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
    private final StudyRoomApiService studyRoomApiService;
    private final ObservableList<StudyRoom> roomList = FXCollections.observableArrayList();
    
    // 分页数据
    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;
    private long totalElements = 0;

    public AdminRoomManagementController() {
        this.studyRoomApiService = ApiServiceManager.getInstance().getStudyRoomApiService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化管理员自习室管理界面...");
        
        try {
            // 初始化组件
            initializeComponents();
            
            // 初始化表格
            initializeTable();
            
            // 加载数据
            loadStudyRooms();
            
            logger.info("✅ 管理员自习室管理界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 管理员自习室管理界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        // 初始化过滤器
        statusFilterComboBox.getItems().addAll("全部状态", "可用", "维护中", "已关闭");
        statusFilterComboBox.setValue("全部状态");
        
        capacityFilterComboBox.getItems().addAll("全部容量", "小型(1-20)", "中型(21-50)", "大型(50+)");
        capacityFilterComboBox.setValue("全部容量");

        // 初始化分页大小选择器
        pageSizeComboBox.getItems().addAll("10", "20", "50", "100");
        pageSizeComboBox.setValue("20");

        // 设置默认状态
        updateStatus("就绪");
        updateStatistics(0, 0, 0, 0);
    }

    private void initializeTable() {
        // 绑定数据到表格
        roomTableView.setItems(roomList);
        
        // 设置表格列的数据绑定
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        hourlyRateColumn.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        
        // 状态列 - 显示中文
        statusColumn.setCellValueFactory(cellData -> {
            StudyRoom.Status status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(
                status != null ? status.getDisplayName() : "未知"
            );
        });
        
        // 时间列 - 格式化显示
        openTimeColumn.setCellValueFactory(new PropertyValueFactory<>("openTime"));
        openTimeColumn.setCellFactory(column -> new TableCell<StudyRoom, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });
        
        closeTimeColumn.setCellValueFactory(new PropertyValueFactory<>("closeTime"));
        closeTimeColumn.setCellFactory(column -> new TableCell<StudyRoom, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        });
        
        // 操作列 - 添加编辑、删除、管理座位按钮
        actionColumn.setCellFactory(new Callback<TableColumn<StudyRoom, Void>, TableCell<StudyRoom, Void>>() {
            @Override
            public TableCell<StudyRoom, Void> call(TableColumn<StudyRoom, Void> param) {
                return new TableCell<StudyRoom, Void>() {
                    private final Button editButton = new Button("编辑");
                    private final Button deleteButton = new Button("删除");
                    private final Button seatsButton = new Button("座位管理");
                    
                    {
                        editButton.setOnAction(event -> {
                            StudyRoom room = getTableView().getItems().get(getIndex());
                            handleEditRoom(room);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            StudyRoom room = getTableView().getItems().get(getIndex());
                            handleDeleteRoom(room);
                        });
                        
                        seatsButton.setOnAction(event -> {
                            StudyRoom room = getTableView().getItems().get(getIndex());
                            handleManageSeats(room);
                        });
                        
                        editButton.getStyleClass().add("button-primary");
                        deleteButton.getStyleClass().add("button-danger");
                        seatsButton.getStyleClass().add("button-secondary");
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                            buttons.getChildren().addAll(editButton, seatsButton, deleteButton);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
        
        // 设置表格为空时的提示
        roomTableView.setPlaceholder(new Label("正在加载自习室数据..."));
    }

    private void loadStudyRooms() {
        updateStatus("正在加载自习室数据...");
        
        try {
            // 获取过滤条件
            String searchKeyword = searchField != null ? searchField.getText() : "";
            String statusFilter = statusFilterComboBox != null ? statusFilterComboBox.getValue() : "全部状态";
            
            // 转换状态过滤条件
            StudyRoom.Status status = null;
            if (statusFilter != null && !"全部状态".equals(statusFilter)) {
                switch (statusFilter) {
                    case "可用":
                        status = StudyRoom.Status.AVAILABLE;
                        break;
                    case "维护中":
                        status = StudyRoom.Status.MAINTENANCE;
                        break;
                    case "已关闭":
                        status = StudyRoom.Status.CLOSED;
                        break;
                }
            }
            
            studyRoomApiService.getStudyRooms(currentPage - 1, pageSize, searchKeyword, status)
                .thenAccept(pageData -> {
                    Platform.runLater(() -> {
                        try {
                            if (pageData != null && pageData.getContent() != null) {
                                // 更新表格数据
                                roomList.clear();
                                roomList.addAll(pageData.getContent());
                                
                                // 更新分页信息
                                totalElements = pageData.getTotalElements();
                                totalPages = pageData.getTotalPages();
                                
                                // 更新UI显示
                                updatePageInfo();
                                updateStatistics(pageData.getContent());
                                updateLastUpdate();
                                totalRecordsLabel.setText("共 " + totalElements + " 条记录");
                                
                                // 更新状态
                                updateStatus("数据加载完成，共 " + pageData.getContent().size() + " 条记录");
                                
                                logger.info("✅ 自习室数据加载成功，当前页: {}/{}, 记录数: {}", 
                                    currentPage, totalPages, pageData.getContent().size());
                            } else {
                                // 显示空数据状态
                                roomList.clear();
                                updateStatistics(0, 0, 0, 0);
                                totalRecordsLabel.setText("共 0 条记录");
                                updateStatus("暂无自习室数据");
                                logger.warn("⚠️ 获取自习室数据为空");
                            }
                            
                        } catch (Exception e) {
                            logger.error("❌ 处理自习室数据失败", e);
                            updateStatus("数据处理失败: " + e.getMessage());
                            
                            // 清空表格并显示错误状态
                            roomList.clear();
                            updateStatistics(0, 0, 0, 0);
                            totalRecordsLabel.setText("数据加载失败");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        logger.error("❌ 加载自习室数据失败", throwable);
                        updateStatus("数据加载失败: " + throwable.getMessage());
                        
                        // 清空表格并显示错误状态
                        roomList.clear();
                        updateStatistics(0, 0, 0, 0);
                        totalRecordsLabel.setText("网络连接失败");
                        
                        // 显示友好的错误提示
                        AlertUtils.showError("数据加载失败", 
                            "无法从服务器加载自习室数据。\n\n可能的原因：\n" +
                            "1. 网络连接问题\n" +
                            "2. 服务器暂时不可用\n" +
                            "3. 数据格式不兼容\n\n" +
                            "请稍后重试或联系管理员。\n\n" +
                            "错误详情: " + throwable.getMessage());
                    });
                    return null;
                });
                
        } catch (Exception e) {
            // 处理初始化阶段的异常
            logger.error("❌ 初始化加载自习室数据失败", e);
            updateStatus("初始化失败: " + e.getMessage());
            
            Platform.runLater(() -> {
                roomList.clear();
                updateStatistics(0, 0, 0, 0);
                totalRecordsLabel.setText("初始化失败");
                AlertUtils.showError("初始化失败", 
                    "自习室管理界面初始化失败：\n" + e.getMessage());
            });
        }
    }

    private void updateStatistics(List<StudyRoom> rooms) {
        if (rooms != null) {
            int total = rooms.size();
            int available = (int) rooms.stream().mapToLong(room -> 
                room.getStatus() == StudyRoom.Status.AVAILABLE ? 1 : 0).sum();
            int occupied = 0; // 需要根据实际座位使用情况计算
            int maintenance = (int) rooms.stream().mapToLong(room -> 
                room.getStatus() == StudyRoom.Status.MAINTENANCE ? 1 : 0).sum();
            
            updateStatistics(total, available, occupied, maintenance);
        }
    }

    private void updateStatistics(int total, int available, int occupied, int maintenance) {
        totalRoomsLabel.setText(String.valueOf(total));
        availableRoomsLabel.setText(String.valueOf(available));
        occupiedRoomsLabel.setText(String.valueOf(occupied));
        maintenanceRoomsLabel.setText(String.valueOf(maintenance));
    }

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
    private void handleAddRoom() {
        logger.info("➕ 添加新自习室");
        AlertUtils.showInfo("添加自习室", "添加自习室功能正在开发中");
    }

    @FXML
    private void handleExport() {
        logger.info("📤 导出自习室数据");
        AlertUtils.showInfo("导出数据", "导出功能正在开发中");
    }

    @FXML
    private void handleRefresh() {
        logger.info("🔄 刷新自习室列表");
        loadStudyRooms();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        logger.info("🔍 搜索自习室: {}", keyword);
        currentPage = 1;
        loadStudyRooms();
    }

    @FXML
    private void handleFilter() {
        logger.info("🔍 应用过滤条件");
        currentPage = 1;
        loadStudyRooms();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("全部状态");
        capacityFilterComboBox.setValue("全部容量");
        
        logger.info("🧹 清除过滤条件");
        currentPage = 1;
        loadStudyRooms();
    }

    private void handleEditRoom(StudyRoom room) {
        logger.info("✏️ 编辑自习室: {}", room.getName());
        AlertUtils.showInfo("编辑自习室", "编辑自习室功能正在开发中\n自习室: " + room.getName());
    }

    private void handleDeleteRoom(StudyRoom room) {
        logger.info("🗑️ 删除自习室: {}", room.getName());
        AlertUtils.showInfo("删除自习室", "删除自习室功能正在开发中\n自习室: " + room.getName());
    }

    private void handleManageSeats(StudyRoom room) {
        logger.info("🪑 管理座位: {}", room.getName());
        AlertUtils.showInfo("座位管理", "座位管理功能正在开发中\n自习室: " + room.getName());
    }

    // 分页事件处理
    @FXML
    private void handleFirstPage() {
        currentPage = 1;
        loadStudyRooms();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadStudyRooms();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadStudyRooms();
        }
    }

    @FXML
    private void handleLastPage() {
        currentPage = totalPages;
        loadStudyRooms();
    }

    @FXML
    private void handlePageSizeChange() {
        String newSize = pageSizeComboBox.getValue();
        if (newSize != null) {
            pageSize = Integer.parseInt(newSize);
            currentPage = 1;
            loadStudyRooms();
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    /**
     * 更新最后更新时间
     */
    private void updateLastUpdate() {
        if (lastUpdateLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            String timeText = now.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            Platform.runLater(() -> lastUpdateLabel.setText("最后更新: " + timeText));
        }
    }
} 