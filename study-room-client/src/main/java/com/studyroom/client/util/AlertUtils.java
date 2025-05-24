package com.studyroom.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 弹窗工具类
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class AlertUtils {

    private static final Logger logger = LoggerFactory.getLogger(AlertUtils.class);

    /**
     * 显示信息提示
     */
    public static void showInfo(String title, String message) {
        logger.debug("🔔 显示信息提示: {}", title);
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * 显示警告提示
     */
    public static void showWarning(String title, String message) {
        logger.debug("⚠️ 显示警告提示: {}", title);
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * 显示错误提示
     */
    public static void showError(String title, String message) {
        logger.debug("❌ 显示错误提示: {}", title);
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * 显示确认对话框
     */
    public static boolean showConfirm(String title, String message) {
        logger.debug("❓ 显示确认对话框: {}", title);
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, title, message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * 显示确认对话框（自定义按钮）
     */
    public static Optional<ButtonType> showConfirm(String title, String message, ButtonType... buttonTypes) {
        logger.debug("❓ 显示确认对话框: {}", title);
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, title, message);
        alert.getButtonTypes().setAll(buttonTypes);
        
        return alert.showAndWait();
    }

    /**
     * 显示文本输入对话框
     */
    public static Optional<String> showTextInput(String title, String message) {
        return showTextInput(title, message, "");
    }

    /**
     * 显示文本输入对话框（带默认值）
     */
    public static Optional<String> showTextInput(String title, String message, String defaultValue) {
        logger.debug("📝 显示文本输入对话框: {}", title);
        
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        
        // 设置对话框属性
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        
        return dialog.showAndWait();
    }

    /**
     * 显示删除确认对话框
     */
    public static boolean showDeleteConfirm(String itemName) {
        return showConfirm(
            "确认删除", 
            "您确定要删除 \"" + itemName + "\" 吗？\n\n此操作无法撤销。"
        );
    }

    /**
     * 显示保存确认对话框
     */
    public static Optional<ButtonType> showSaveConfirm() {
        ButtonType saveButton = new ButtonType("保存");
        ButtonType dontSaveButton = new ButtonType("不保存");
        ButtonType cancelButton = new ButtonType("取消");
        
        return showConfirm(
            "保存更改",
            "您有未保存的更改，是否要保存？",
            saveButton, dontSaveButton, cancelButton
        );
    }

    /**
     * 显示退出确认对话框
     */
    public static boolean showExitConfirm() {
        return showConfirm(
            "退出应用程序",
            "您确定要退出应用程序吗？"
        );
    }

    /**
     * 显示网络错误提示
     */
    public static void showNetworkError(String operation, Throwable exception) {
        String message = String.format(
            "执行 \"%s\" 时发生网络错误：\n\n%s\n\n请检查网络连接和服务器状态。",
            operation,
            exception.getMessage()
        );
        showError("网络错误", message);
    }

    /**
     * 显示加载错误提示
     */
    public static void showLoadError(String resource, Throwable exception) {
        String message = String.format(
            "加载 \"%s\" 失败：\n\n%s",
            resource,
            exception.getMessage()
        );
        showError("加载错误", message);
    }

    /**
     * 显示验证错误提示
     */
    public static void showValidationError(String field, String error) {
        String message = String.format("\"%s\" 验证失败：\n\n%s", field, error);
        showWarning("输入验证", message);
    }

    /**
     * 显示操作成功提示
     */
    public static void showSuccess(String operation) {
        showInfo("操作成功", operation + " 成功完成！");
    }

    /**
     * 显示操作失败提示
     */
    public static void showOperationError(String operation, String error) {
        String message = String.format("%s 失败：\n\n%s", operation, error);
        showError("操作失败", message);
    }

    /**
     * 通用弹窗显示方法
     */
    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        setupAlert(alert, title, message);
        alert.showAndWait();
    }

    /**
     * 设置弹窗属性
     */
    private static void setupAlert(Alert alert, String title, String message) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 设置对话框属性
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setResizable(false);
        
        // 设置图标（如果有主窗口的话）
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            // TODO: 设置应用图标
            // stage.getIcons().add(new Image(...));
        } catch (Exception e) {
            // 忽略图标设置错误
        }
    }

    /**
     * 显示详细错误信息对话框
     */
    public static void showDetailedError(String title, String message, Throwable exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText("详细错误信息：\n" + exception.getMessage());
        
        // 创建可扩展的异常详情
        String exceptionText = getStackTrace(exception);
        
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.initModality(Modality.APPLICATION_MODAL);
        
        alert.showAndWait();
    }

    /**
     * 获取异常堆栈跟踪字符串
     */
    private static String getStackTrace(Throwable exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
} 