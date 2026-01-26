package com.xhs.service.impl;

import com.xhs.entity.ScheduledTask;
import com.xhs.repository.ScheduledTaskRepository;
import com.xhs.service.PublishService;
import com.xhs.service.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScheduledTaskServiceImpl implements ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskServiceImpl.class);
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final PublishService publishService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 构造函数
    public ScheduledTaskServiceImpl(ScheduledTaskRepository scheduledTaskRepository, PublishService publishService) {
        this.scheduledTaskRepository = Objects.requireNonNull(scheduledTaskRepository, "scheduledTaskRepository must not be null");
        this.publishService = Objects.requireNonNull(publishService, "publishService must not be null");
    }

    // 任务状态常量
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_EXECUTING = "EXECUTING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PAUSED = "PAUSED";

    @Override
    public List<ScheduledTask> getAllScheduledTasks() {
        return scheduledTaskRepository.findAll();
    }

    @Override
    public List<ScheduledTask> getScheduledTasksByUserId(Long userId) {
        return scheduledTaskRepository.findByUserId(userId);
    }

    @Override
    public Optional<ScheduledTask> getScheduledTaskById(Long taskId) {
        return scheduledTaskRepository.findById(taskId);
    }

    @Override
    @Transactional
    public ScheduledTask createScheduledTask(ScheduledTask task) {
        // 初始化任务状态
        if (task.getStatus() == null) {
            task.setStatus(STATUS_PENDING);
        }
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return scheduledTaskRepository.save(task);
    }

    @Override
    @Transactional
    public ScheduledTask updateScheduledTask(Long taskId, ScheduledTask task) {
        ScheduledTask existingTask = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("定时任务不存在"));

        // 更新任务信息
        if (task.getTitle() != null) {
            existingTask.setTitle(task.getTitle());
        }
        if (task.getContent() != null) {
            existingTask.setContent(task.getContent());
        }
        if (task.getScheduledTime() != null) {
            existingTask.setScheduledTime(task.getScheduledTime());
        }
        if (task.getStatus() != null) {
            existingTask.setStatus(task.getStatus());
        }
        if (task.getImagePaths() != null) {
            existingTask.setImagePaths(task.getImagePaths());
        }
        existingTask.setUpdatedAt(LocalDateTime.now());
        return scheduledTaskRepository.save(existingTask);
    }

    @Override
    @Transactional
    public void deleteScheduledTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("定时任务不存在"));
        scheduledTaskRepository.delete(task);
    }

    @Override
    @Transactional
    public boolean executeTaskNow(Long taskId) {
        Optional<ScheduledTask> optionalTask = scheduledTaskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            logger.error("定时任务不存在, 任务ID: {}", taskId);
            return false;
        }

        ScheduledTask task = optionalTask.get();
        try {
            // 更新任务状态为执行中
            task.setStatus(STATUS_EXECUTING);
            task.setUpdatedAt(LocalDateTime.now());
            scheduledTaskRepository.save(task);

            // 异步执行任务
            CompletableFuture.runAsync(() -> {
                try {
                    // 提取图片路径
                    String[] imagePaths = extractImagePaths(task.getImagePaths());
                    
                    // 调用发布服务
                    publishService.publishNote(task.getUser().getId(), task.getTitle(), task.getContent(), imagePaths);
                    
                    // 更新任务状态为完成
                    updateTaskStatus(taskId, STATUS_COMPLETED);
                    logger.info("定时任务执行成功, 任务ID: {}", taskId);
                } catch (Exception e) {
                    // 更新任务状态为失败
                    updateTaskStatus(taskId, STATUS_FAILED);
                    logger.error("定时任务执行失败, 任务ID: {}: {}", taskId, e.getMessage(), e);
                }
            }, executorService);

            return true;
        } catch (Exception e) {
            logger.error("执行定时任务失败, 任务ID: {}: {}", taskId, e.getMessage(), e);
            // 更新任务状态为失败
            updateTaskStatus(taskId, STATUS_FAILED);
            return false;
        }
    }

    @Override
    @Transactional
    public void pauseTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("定时任务不存在"));
        task.setStatus(STATUS_PAUSED);
        task.setUpdatedAt(LocalDateTime.now());
        scheduledTaskRepository.save(task);
    }

    @Override
    @Transactional
    public void resumeTask(Long taskId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("定时任务不存在"));
        task.setStatus(STATUS_PENDING);
        task.setUpdatedAt(LocalDateTime.now());
        scheduledTaskRepository.save(task);
    }

    @Override
    public List<ScheduledTask> getTasksByStatus(String status) {
        return scheduledTaskRepository.findByStatus(status);
    }

    @Override
    public List<ScheduledTask> getTasksByUserIdAndStatus(Long userId, String status) {
        return scheduledTaskRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<ScheduledTask> getTasksBetweenTimes(LocalDateTime startTime, LocalDateTime endTime) {
        return scheduledTaskRepository.findByScheduledTimeBetween(startTime, endTime);
    }

    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 新状态
     */
    @Transactional
    private void updateTaskStatus(Long taskId, String status) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("定时任务不存在"));
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        if (STATUS_COMPLETED.equals(status)) {
            task.setExecutedTime(LocalDateTime.now());
        }
        scheduledTaskRepository.save(task);
    }

    /**
     * 从字符串中提取图片路径数组
     * @param imagePathsStr 图片路径字符串
     * @return 图片路径数组
     */
    private String[] extractImagePaths(String imagePathsStr) {
        if (imagePathsStr == null || imagePathsStr.isEmpty()) {
            return new String[0];
        }
        // 假设图片路径是JSON格式存储的数组
        // 这里需要根据实际存储格式进行调整
        try {
            // 简单处理，假设格式为["path1", "path2"]
            imagePathsStr = imagePathsStr.replace("[", "").replace("]", "").replace("\"", "");
            return imagePathsStr.split(",");
        } catch (Exception e) {
            logger.error("解析图片路径失败: {}", e.getMessage());
            return new String[0];
        }
    }

    /**
     * 检查并执行到期的定时任务
     * 由定时调度器调用
     */
    @Transactional
    public void checkAndExecuteDueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTask> dueTasks = scheduledTaskRepository.findByStatusAndScheduledTimeLessThanEqual(STATUS_PENDING, now);
        
        for (ScheduledTask task : dueTasks) {
            logger.info("执行到期定时任务, 任务ID: {}, 标题: {}", task.getId(), task.getTitle());
            executeTaskNow(task.getId());
        }
    }

    @Override
    public long count() {
        return scheduledTaskRepository.count();
    }

    @Override
    public long countPending() {
        return scheduledTaskRepository.countByStatus(STATUS_PENDING);
    }

    @Override
    public long countCompleted() {
        return scheduledTaskRepository.countByStatus(STATUS_COMPLETED);
    }

    @Override
    public void executeTask(Long taskId) {
        executeTaskNow(taskId);
    }
}