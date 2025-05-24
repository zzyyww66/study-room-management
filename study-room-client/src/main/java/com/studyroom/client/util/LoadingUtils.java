package com.studyroom.client.util;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 加载指示器工具类
 * 
 * @author Developer
 * @version 1.0.0
 * @since 2024
 */
public class LoadingUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoadingUtils.class);
    
    // 存储原始按钮文本的映射
    private static final Map<Button, String> originalTexts = new HashMap<>();
    
    // 存储原始按钮禁用状态的映射
    private static final Map<Button, Boolean> originalDisabled = new HashMap<>();

    /**
     * 在按钮上显示加载指示器
     */
    public static void showLoadingIndicator(Button button, String loadingText) {
        if (button == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                // 保存原始状态
                originalTexts.put(button, button.getText());
                originalDisabled.put(button, button.isDisabled());
                
                // 设置加载状态
                button.setText(loadingText);
                button.setDisable(true);
                
                logger.debug("🔄 显示加载指示器: {}", loadingText);
                
            } catch (Exception e) {
                logger.warn("⚠️ 显示加载指示器失败", e);
            }
        });
    }

    /**
     * 隐藏按钮上的加载指示器
     */
    public static void hideLoadingIndicator(Button button, String originalText) {
        if (button == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                // 恢复原始状态
                String savedText = originalTexts.remove(button);
                Boolean savedDisabled = originalDisabled.remove(button);
                
                if (savedText != null) {
                    button.setText(savedText);
                } else if (originalText != null) {
                    button.setText(originalText);
                }
                
                if (savedDisabled != null) {
                    button.setDisable(savedDisabled);
                } else {
                    button.setDisable(false);
                }
                
                logger.debug("✅ 隐藏加载指示器");
                
            } catch (Exception e) {
                logger.warn("⚠️ 隐藏加载指示器失败", e);
            }
        });
    }

    /**
     * 创建进度指示器
     */
    public static ProgressIndicator createProgressIndicator() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setMaxSize(30, 30);
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        return indicator;
    }

    /**
     * 创建带文本的加载容器
     */
    public static StackPane createLoadingPane(String message) {
        StackPane loadingPane = new StackPane();
        
        // 创建进度指示器
        ProgressIndicator indicator = createProgressIndicator();
        
        // 创建文本标签
        javafx.scene.control.Label label = new javafx.scene.control.Label(message);
        label.getStyleClass().add("loading-text");
        
        // 创建垂直布局
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.getChildren().addAll(indicator, label);
        
        loadingPane.getChildren().add(vbox);
        loadingPane.getStyleClass().add("loading-pane");
        
        return loadingPane;
    }

    /**
     * 在指定容器中显示加载指示器
     */
    public static void showLoadingInContainer(javafx.scene.layout.Pane container, String message) {
        if (container == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                // 清空容器
                container.getChildren().clear();
                
                // 添加加载指示器
                StackPane loadingPane = createLoadingPane(message);
                container.getChildren().add(loadingPane);
                
                logger.debug("🔄 在容器中显示加载指示器: {}", message);
                
            } catch (Exception e) {
                logger.warn("⚠️ 在容器中显示加载指示器失败", e);
            }
        });
    }

    /**
     * 隐藏容器中的加载指示器
     */
    public static void hideLoadingInContainer(javafx.scene.layout.Pane container) {
        if (container == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                // 清空容器
                container.getChildren().clear();
                
                logger.debug("✅ 隐藏容器中的加载指示器");
                
            } catch (Exception e) {
                logger.warn("⚠️ 隐藏容器中的加载指示器失败", e);
            }
        });
    }

    /**
     * 创建全屏加载遮罩
     */
    public static StackPane createFullScreenLoadingMask(String message) {
        StackPane mask = new StackPane();
        mask.getStyleClass().add("loading-mask");
        
        // 设置背景
        mask.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        // 创建加载内容
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.getStyleClass().add("loading-content");
        content.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10;");
        
        // 添加进度指示器
        ProgressIndicator indicator = createProgressIndicator();
        indicator.setMaxSize(50, 50);
        
        // 添加文本标签
        javafx.scene.control.Label label = new javafx.scene.control.Label(message);
        label.getStyleClass().add("loading-text");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        content.getChildren().addAll(indicator, label);
        mask.getChildren().add(content);
        
        return mask;
    }

    /**
     * 显示全屏加载遮罩
     */
    public static void showFullScreenLoading(javafx.scene.layout.Pane rootPane, String message) {
        if (rootPane == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                // 创建加载遮罩
                StackPane mask = createFullScreenLoadingMask(message);
                mask.setId("fullscreen-loading-mask");
                
                // 添加到根容器
                rootPane.getChildren().add(mask);
                
                logger.debug("🔄 显示全屏加载遮罩: {}", message);
                
            } catch (Exception e) {
                logger.warn("⚠️ 显示全屏加载遮罩失败", e);
            }
        });
    }

    /**
     * 隐藏全屏加载遮罩
     */
    public static void hideFullScreenLoading(javafx.scene.layout.Pane rootPane) {
        if (rootPane == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                // 查找并移除加载遮罩
                rootPane.getChildren().removeIf(node -> 
                    "fullscreen-loading-mask".equals(node.getId()));
                
                logger.debug("✅ 隐藏全屏加载遮罩");
                
            } catch (Exception e) {
                logger.warn("⚠️ 隐藏全屏加载遮罩失败", e);
            }
        });
    }

    /**
     * 为按钮设置加载状态（带图标）
     */
    public static void setButtonLoading(Button button, boolean loading) {
        if (button == null) {
            return;
        }
        
        Platform.runLater(() -> {
            try {
                if (loading) {
                    // 保存原始状态
                    originalTexts.put(button, button.getText());
                    originalDisabled.put(button, button.isDisabled());
                    
                    // 创建小的进度指示器
                    ProgressIndicator indicator = new ProgressIndicator();
                    indicator.setMaxSize(16, 16);
                    indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                    
                    // 设置按钮图标和状态
                    button.setGraphic(indicator);
                    button.setDisable(true);
                    
                } else {
                    // 恢复原始状态
                    String savedText = originalTexts.remove(button);
                    Boolean savedDisabled = originalDisabled.remove(button);
                    
                    button.setGraphic(null);
                    
                    if (savedText != null) {
                        button.setText(savedText);
                    }
                    
                    if (savedDisabled != null) {
                        button.setDisable(savedDisabled);
                    } else {
                        button.setDisable(false);
                    }
                }
                
            } catch (Exception e) {
                logger.warn("⚠️ 设置按钮加载状态失败", e);
            }
        });
    }

    /**
     * 清理所有缓存的状态
     */
    public static void clearAllStates() {
        originalTexts.clear();
        originalDisabled.clear();
        logger.debug("🧹 清理所有加载状态缓存");
    }
} 