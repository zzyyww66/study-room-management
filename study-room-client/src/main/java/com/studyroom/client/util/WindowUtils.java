package com.studyroom.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

/**
 * 窗口管理工具类
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class WindowUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(WindowUtils.class);
    
    /**
     * 打开座位选择窗口
     * 
     * @param owner 父窗口
     * @param studyRoomId 自习室ID
     * @param studyRoomName 自习室名称
     * @return 打开的舞台对象
     */
    public static Stage openSeatSelectionWindow(Window owner, Long studyRoomId, String studyRoomName) {
        try {
            logger.info("🔄 正在打开座位选择窗口: {}", studyRoomName);
            
            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(WindowUtils.class.getResource("/fxml/seat-selection.fxml"));
            Parent root = loader.load();
            
            // 获取控制器并设置数据
            Object controllerObj = loader.getController();
            if (controllerObj != null) {
                try {
                    // 通过反射调用setCurrentRoom方法
                    // 这里创建一个模拟的StudyRoom对象用于测试
                    com.studyroom.client.model.StudyRoom mockRoom = createMockStudyRoom(studyRoomId, studyRoomName);
                    
                    // 调用控制器的setCurrentRoom方法
                    controllerObj.getClass()
                        .getMethod("setCurrentRoom", com.studyroom.client.model.StudyRoom.class)
                        .invoke(controllerObj, mockRoom);
                    
                    logger.info("✅ 座位选择控制器数据已设置");
                } catch (Exception e) {
                    logger.warn("⚠️ 设置控制器数据失败，但窗口仍会打开", e);
                }
            }
            
            // 创建新舞台
            Stage stage = new Stage();
            stage.setTitle("座位选择 - " + studyRoomName);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);
            
            // 设置场景
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            
            // 设置窗口属性
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setResizable(true);
            
            // 显示窗口
            stage.show();
            
            logger.info("✅ 座位选择窗口已打开");
            return stage;
            
        } catch (Exception e) {
            logger.error("❌ 打开座位选择窗口失败", e);
            AlertUtils.showError("错误", "无法打开座位选择窗口: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建模拟自习室对象用于测试
     */
    private static com.studyroom.client.model.StudyRoom createMockStudyRoom(Long id, String name) {
        com.studyroom.client.model.StudyRoom room = new com.studyroom.client.model.StudyRoom();
        room.setId(id != null ? id : 1L);
        room.setName(name != null ? name : "测试自习室");
        room.setLocation("1楼东侧");
        room.setPricePerHour(new java.math.BigDecimal("15.00"));
        room.setCapacity(50);
        room.setOpenTime(LocalTime.parse("08:00"));
        room.setCloseTime(LocalTime.parse("22:00"));
        room.setStatus(com.studyroom.client.model.StudyRoom.Status.AVAILABLE);
        return room;
    }
    
    /**
     * 打开通用对话框窗口
     * 
     * @param owner 父窗口
     * @param fxmlPath FXML文件路径
     * @param title 窗口标题
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 打开的舞台对象
     */
    public static Stage openDialog(Window owner, String fxmlPath, String title, double width, double height) {
        try {
            logger.info("🔄 正在打开对话框: {}", title);
            
            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(WindowUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // 创建新舞台
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);
            
            // 设置场景
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            // 设置窗口属性
            stage.setResizable(false);
            
            // 显示窗口
            stage.show();
            
            logger.info("✅ 对话框已打开: {}", title);
            return stage;
            
        } catch (Exception e) {
            logger.error("❌ 打开对话框失败: {}", title, e);
            AlertUtils.showError("错误", "无法打开窗口: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取当前活动窗口
     * 
     * @return 当前活动窗口
     */
    public static Window getCurrentWindow() {
        return Stage.getWindows().stream()
            .filter(Window::isShowing)
            .filter(window -> window instanceof Stage)
            .filter(window -> ((Stage) window).isFocused())
            .findFirst()
            .orElse(null);
    }
} 