package com.studyroom.client.controller;

import com.studyroom.client.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 系统统计控制器
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class SystemStatisticsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SystemStatisticsController.class);

    // 顶部控制
    @FXML private ComboBox<String> timePeriodComboBox;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    // 关键指标标签
    @FXML private Label totalReservationsLabel;
    @FXML private Label reservationsTrendLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label revenueTrendLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label usersTrendLabel;
    @FXML private Label utilizationRateLabel;
    @FXML private Label utilizationTrendLabel;
    @FXML private Label avgDurationLabel;
    @FXML private Label durationTrendLabel;
    @FXML private Label satisfactionLabel;
    @FXML private Label satisfactionTrendLabel;
    @FXML private Label cancellationRateLabel;
    @FXML private Label cancellationTrendLabel;
    @FXML private Label newUsersLabel;
    @FXML private Label newUsersTrendLabel;

    // 图表
    @FXML private LineChart<String, Number> reservationTrendChart;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart usageChart;

    // 图表类型选择器
    @FXML private ComboBox<String> reservationChartTypeComboBox;
    @FXML private ComboBox<String> revenueChartTypeComboBox;
    @FXML private ComboBox<String> usageChartTypeComboBox;

    // 底部状态栏
    @FXML private Label dataRangeLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🔄 初始化系统统计界面...");
        
        try {
            // 初始化组件
            initializeComponents();
            
            // 加载数据
            loadStatisticsData();
            
            logger.info("✅ 系统统计界面初始化完成");
            
        } catch (Exception e) {
            logger.error("❌ 系统统计界面初始化失败", e);
            updateStatus("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 初始化时间周期选择器
        timePeriodComboBox.getItems().addAll(
            "今日", "本周", "本月", "本季度", "本年", "自定义"
        );
        timePeriodComboBox.setValue("本月");

        // 初始化图表类型选择器
        reservationChartTypeComboBox.getItems().addAll("趋势图", "柱状图", "面积图");
        reservationChartTypeComboBox.setValue("趋势图");
        
        revenueChartTypeComboBox.getItems().addAll("柱状图", "趋势图", "堆叠图");
        revenueChartTypeComboBox.setValue("柱状图");
        
        usageChartTypeComboBox.getItems().addAll("饼图", "环形图", "柱状图");
        usageChartTypeComboBox.setValue("饼图");

        // 设置默认状态
        updateStatus("就绪");
        updateLastUpdate();
        updateDataRange("本月");
    }

    /**
     * 加载统计数据
     */
    private void loadStatisticsData() {
        updateStatus("正在加载统计数据...");
        
        // TODO: 从服务器加载统计数据
        Platform.runLater(() -> {
            try {
                // 使用模拟数据更新指标
                updateMetrics();
                
                // 加载图表数据
                loadChartData();
                
                updateStatus("数据加载完成");
                updateLastUpdate();
                
            } catch (Exception e) {
                logger.error("❌ 加载统计数据失败", e);
                updateStatus("数据加载失败: " + e.getMessage());
            }
        });
    }

    /**
     * 更新关键指标
     */
    private void updateMetrics() {
        // 模拟数据
        totalReservationsLabel.setText("2,847");
        reservationsTrendLabel.setText("↗ +12.5%");
        reservationsTrendLabel.setStyle("-fx-text-fill: #4CAF50;");

        totalRevenueLabel.setText("¥45,680");
        revenueTrendLabel.setText("↗ +8.3%");
        revenueTrendLabel.setStyle("-fx-text-fill: #4CAF50;");

        activeUsersLabel.setText("1,234");
        usersTrendLabel.setText("↗ +15.2%");
        usersTrendLabel.setStyle("-fx-text-fill: #4CAF50;");

        utilizationRateLabel.setText("78.5%");
        utilizationTrendLabel.setText("↗ +5.8%");
        utilizationTrendLabel.setStyle("-fx-text-fill: #4CAF50;");

        avgDurationLabel.setText("3.2h");
        durationTrendLabel.setText("→ +0.1%");
        durationTrendLabel.setStyle("-fx-text-fill: #FF9800;");

        satisfactionLabel.setText("4.6");
        satisfactionTrendLabel.setText("↗ +0.2");
        satisfactionTrendLabel.setStyle("-fx-text-fill: #4CAF50;");

        cancellationRateLabel.setText("5.2%");
        cancellationTrendLabel.setText("↘ -1.3%");
        cancellationTrendLabel.setStyle("-fx-text-fill: #4CAF50;");

        newUsersLabel.setText("156");
        newUsersTrendLabel.setText("↗ +22.1%");
        newUsersTrendLabel.setStyle("-fx-text-fill: #4CAF50;");
    }

    /**
     * 加载图表数据
     */
    private void loadChartData() {
        loadReservationTrendChart();
        loadRevenueChart();
        loadUsageChart();
    }

    /**
     * 加载预订趋势图
     */
    private void loadReservationTrendChart() {
        reservationTrendChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("预订数量");
        
        // 模拟数据
        series.getData().add(new XYChart.Data<>("第1周", 180));
        series.getData().add(new XYChart.Data<>("第2周", 220));
        series.getData().add(new XYChart.Data<>("第3周", 280));
        series.getData().add(new XYChart.Data<>("第4周", 320));
        
        reservationTrendChart.getData().add(series);
    }

    /**
     * 加载收入图表
     */
    private void loadRevenueChart() {
        revenueChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("收入");
        
        // 模拟数据
        series.getData().add(new XYChart.Data<>("第1周", 8500));
        series.getData().add(new XYChart.Data<>("第2周", 12300));
        series.getData().add(new XYChart.Data<>("第3周", 15600));
        series.getData().add(new XYChart.Data<>("第4周", 18200));
        
        revenueChart.getData().add(series);
    }

    /**
     * 加载使用统计图
     */
    private void loadUsageChart() {
        usageChart.getData().clear();
        
        // 模拟数据
        usageChart.getData().addAll(
            new PieChart.Data("已使用", 78.5),
            new PieChart.Data("空闲", 18.2),
            new PieChart.Data("维护", 3.3)
        );
    }

    // 事件处理方法
    @FXML
    private void handleTimePeriodChange() {
        String period = timePeriodComboBox.getValue();
        logger.info("📊 切换时间周期: {}", period);
        updateDataRange(period);
        loadStatisticsData();
    }

    @FXML
    private void handleExport() {
        logger.info("📤 导出统计报告");
        AlertUtils.showInfo("导出报告", "报告导出功能正在开发中");
    }

    @FXML
    private void handleRefresh() {
        logger.info("🔄 刷新统计数据");
        loadStatisticsData();
    }

    /**
     * 更新数据范围显示
     */
    private void updateDataRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        String range;
        
        switch (period) {
            case "今日":
                range = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                break;
            case "本周":
                range = "本周 (" + now.format(DateTimeFormatter.ofPattern("MM-dd")) + ")";
                break;
            case "本月":
                range = now.format(DateTimeFormatter.ofPattern("yyyy年MM月"));
                break;
            case "本季度":
                int quarter = (now.getMonthValue() - 1) / 3 + 1;
                range = now.getYear() + "年第" + quarter + "季度";
                break;
            case "本年":
                range = now.format(DateTimeFormatter.ofPattern("yyyy年"));
                break;
            default:
                range = "自定义范围";
                break;
        }
        
        dataRangeLabel.setText("数据范围: " + range);
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