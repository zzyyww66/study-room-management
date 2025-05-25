package com.studyroom.client.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 异步数据加载管理器
 * 负责管理异步数据加载、进度提示和错误处理
 * 
 * @author Developer
 * @version 1.0.0
 */
public class AsyncDataManager {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDataManager.class);
    
    // 单例实例
    private static AsyncDataManager instance;
    
    // 线程池
    private final ExecutorService executorService;
    
    // 最大并发任务数
    private static final int MAX_THREADS = 10;

    /**
     * 私有构造函数 - 单例模式
     */
    private AsyncDataManager() {
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("AsyncData-" + thread.getId());
            return thread;
        });
        logger.info("⚡ 异步数据管理器初始化完成");
    }

    /**
     * 获取单例实例
     */
    public static synchronized AsyncDataManager getInstance() {
        if (instance == null) {
            instance = new AsyncDataManager();
        }
        return instance;
    }

    /**
     * 数据加载回调接口
     */
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Throwable throwable);
        default void onStart() {}
        default void onComplete() {}
    }

    /**
     * 异步加载数据（无进度提示）
     */
    public <T> void loadData(String taskName, CompletableFuture<T> dataFuture, DataCallback<T> callback) {
        loadData(taskName, dataFuture, callback, false, null);
    }

    /**
     * 异步加载数据（有进度提示）
     */
    public <T> void loadDataWithProgress(String taskName, CompletableFuture<T> dataFuture, 
                                       DataCallback<T> callback, Stage ownerStage) {
        loadData(taskName, dataFuture, callback, true, ownerStage);
    }

    /**
     * 异步加载数据（核心方法）
     */
    private <T> void loadData(String taskName, CompletableFuture<T> dataFuture, 
                            DataCallback<T> callback, boolean showProgress, Stage ownerStage) {
        logger.debug("🚀 开始异步加载: {}", taskName);
        
        // 创建JavaFX任务
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                try {
                    // 在UI线程中调用开始回调
                    Platform.runLater(callback::onStart);
                    
                    // 等待异步任务完成
                    return dataFuture.get();
                } catch (Exception e) {
                    logger.error("❌ 数据加载失败: {}", taskName, e);
                    throw e;
                }
            }
        };

        // 设置成功回调
        task.setOnSucceeded(event -> {
            try {
                T result = task.getValue();
                logger.debug("✅ 数据加载成功: {}", taskName);
                callback.onSuccess(result);
            } catch (Exception e) {
                logger.error("❌ 成功回调执行失败: {}", taskName, e);
                callback.onError(e);
            } finally {
                callback.onComplete();
            }
        });

        // 设置失败回调
        task.setOnFailed(event -> {
            try {
                Throwable exception = task.getException();
                logger.error("❌ 任务执行失败: {}", taskName, exception);
                callback.onError(exception);
            } catch (Exception e) {
                logger.error("❌ 失败回调执行失败: {}", taskName, e);
            } finally {
                callback.onComplete();
            }
        });

        // 显示进度提示
        if (showProgress && ownerStage != null) {
            showProgressDialog(taskName, task, ownerStage);
        }

        // 提交任务到线程池
        executorService.submit(task);
    }

    /**
     * 批量异步加载数据
     */
    public <T> void loadMultipleData(String taskName, CompletableFuture<T>[] dataFutures, 
                                   Consumer<T[]> onSuccess, Consumer<Throwable> onError) {
        logger.debug("🚀 开始批量异步加载: {} ({}个任务)", taskName, dataFutures.length);
        
        CompletableFuture.allOf(dataFutures)
            .thenApply(v -> {
                @SuppressWarnings("unchecked")
                T[] results = (T[]) new Object[dataFutures.length];
                for (int i = 0; i < dataFutures.length; i++) {
                    try {
                        results[i] = dataFutures[i].get();
                    } catch (Exception e) {
                        throw new RuntimeException("任务 " + i + " 执行失败", e);
                    }
                }
                return results;
            })
            .whenCompleteAsync((results, throwable) -> {
                Platform.runLater(() -> {
                    if (throwable == null) {
                        logger.debug("✅ 批量数据加载成功: {}", taskName);
                        onSuccess.accept(results);
                    } else {
                        logger.error("❌ 批量数据加载失败: {}", taskName, throwable);
                        onError.accept(throwable);
                    }
                });
            }, executorService);
    }

    /**
     * 链式异步加载（一个任务依赖另一个任务的结果）
     */
    public <T, U> void loadChainedData(String taskName, CompletableFuture<T> firstTask, 
                                     Function<T, CompletableFuture<U>> secondTaskProvider,
                                     DataCallback<U> callback) {
        logger.debug("🔗 开始链式异步加载: {}", taskName);
        
        firstTask
            .thenCompose(firstResult -> {
                logger.debug("🔄 第一阶段完成，开始第二阶段: {}", taskName);
                return secondTaskProvider.apply(firstResult);
            })
            .whenCompleteAsync((result, throwable) -> {
                Platform.runLater(() -> {
                    callback.onStart();
                    try {
                        if (throwable == null) {
                            logger.debug("✅ 链式数据加载成功: {}", taskName);
                            callback.onSuccess(result);
                        } else {
                            logger.error("❌ 链式数据加载失败: {}", taskName, throwable);
                            callback.onError(throwable);
                        }
                    } finally {
                        callback.onComplete();
                    }
                });
            }, executorService);
    }

    /**
     * 重试机制的异步加载
     */
    public <T> void loadDataWithRetry(String taskName, CompletableFuture<T> dataFuture, 
                                    DataCallback<T> callback, int maxRetries) {
        loadDataWithRetry(taskName, dataFuture, callback, maxRetries, 0);
    }

    /**
     * 重试机制的异步加载（内部实现）
     */
    private <T> void loadDataWithRetry(String taskName, CompletableFuture<T> dataFuture, 
                                     DataCallback<T> callback, int maxRetries, int currentAttempt) {
        logger.debug("🔄 尝试加载数据: {} (第{}次/共{}次)", taskName, currentAttempt + 1, maxRetries + 1);
        
        dataFuture.whenCompleteAsync((result, throwable) -> {
            Platform.runLater(() -> {
                if (throwable == null) {
                    logger.debug("✅ 数据加载成功: {} (第{}次尝试)", taskName, currentAttempt + 1);
                    callback.onSuccess(result);
                } else if (currentAttempt < maxRetries) {
                    logger.warn("⚠️ 数据加载失败，准备重试: {} (第{}次/共{}次)", taskName, currentAttempt + 1, maxRetries + 1, throwable);
                    // 延迟后重试
                    CompletableFuture.delayedExecutor(1000 + currentAttempt * 1000, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .execute(() -> {
                            // 这里需要重新创建CompletableFuture，因为原来的已经完成了
                            // 具体实现取决于数据源
                            logger.debug("🔄 开始重试: {}", taskName);
                        });
                } else {
                    logger.error("❌ 数据加载失败，已达最大重试次数: {} ({}次)", taskName, maxRetries + 1, throwable);
                    callback.onError(throwable);
                }
            });
        }, executorService);
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(String taskName, Task<?> task, Stage ownerStage) {
        Platform.runLater(() -> {
            try {
                Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                progressAlert.initOwner(ownerStage);
                progressAlert.setTitle("加载中");
                progressAlert.setHeaderText("正在" + taskName);
                progressAlert.setContentText("请稍候...");
                
                // 添加进度指示器
                ProgressIndicator progressIndicator = new ProgressIndicator();
                progressIndicator.progressProperty().bind(task.progressProperty());
                progressAlert.setGraphic(progressIndicator);
                
                // 任务完成时关闭对话框
                task.setOnSucceeded(e -> progressAlert.close());
                task.setOnFailed(e -> progressAlert.close());
                task.setOnCancelled(e -> progressAlert.close());
                
                progressAlert.show();
            } catch (Exception e) {
                logger.error("❌ 显示进度对话框失败", e);
            }
        });
    }

    /**
     * 显示错误对话框
     */
    public void showErrorDialog(String title, String message, Throwable throwable, Stage ownerStage) {
        Platform.runLater(() -> {
            try {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.initOwner(ownerStage);
                errorAlert.setTitle(title);
                errorAlert.setHeaderText(message);
                errorAlert.setContentText(throwable != null ? throwable.getMessage() : "未知错误");
                errorAlert.showAndWait();
            } catch (Exception e) {
                logger.error("❌ 显示错误对话框失败", e);
            }
        });
    }

    /**
     * 显示成功对话框
     */
    public void showSuccessDialog(String title, String message, Stage ownerStage) {
        Platform.runLater(() -> {
            try {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.initOwner(ownerStage);
                successAlert.setTitle(title);
                successAlert.setHeaderText(message);
                successAlert.showAndWait();
            } catch (Exception e) {
                logger.error("❌ 显示成功对话框失败", e);
            }
        });
    }

    /**
     * 取消所有正在执行的任务
     */
    public void cancelAllTasks() {
        logger.info("🛑 取消所有异步任务");
        executorService.shutdownNow();
    }

    /**
     * 优雅关闭
     */
    public void shutdown() {
        logger.info("🔒 关闭异步数据管理器");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取当前活跃任务数
     */
    public int getActiveTaskCount() {
        if (executorService instanceof java.util.concurrent.ThreadPoolExecutor) {
            return ((java.util.concurrent.ThreadPoolExecutor) executorService).getActiveCount();
        }
        return 0;
    }

    /**
     * 检查是否有任务正在执行
     */
    public boolean hasActiveTasks() {
        return getActiveTaskCount() > 0;
    }
} 