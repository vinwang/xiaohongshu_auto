package com.xhs.service;

import com.xhs.entity.ScheduledTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledTaskService {

    /**
     * 获取所有定时任务
     * @return 定时任务列表
     */
    List<ScheduledTask> getAllScheduledTasks();

    /**
     * 获取用户的所有定时任务
     * @param userId 用户ID
     * @return 定时任务列表
     */
    List<ScheduledTask> getScheduledTasksByUserId(Long userId);

    /**
     * 获取单个定时任务
     * @param taskId 任务ID
     * @return 定时任务
     */
    Optional<ScheduledTask> getScheduledTaskById(Long taskId);

    /**
     * 创建定时任务
     * @param task 定时任务信息
     * @return 创建的定时任务
     */
    ScheduledTask createScheduledTask(ScheduledTask task);

    /**
     * 更新定时任务
     * @param taskId 任务ID
     * @param task 定时任务信息
     * @return 更新后的定时任务
     */
    ScheduledTask updateScheduledTask(Long taskId, ScheduledTask task);

    /**
     * 删除定时任务
     * @param taskId 任务ID
     */
    void deleteScheduledTask(Long taskId);

    /**
     * 立即执行定时任务
     * @param taskId 任务ID
     * @return 执行结果
     */
    boolean executeTaskNow(Long taskId);

    /**
     * 暂停定时任务
     * @param taskId 任务ID
     */
    void pauseTask(Long taskId);

    /**
     * 恢复定时任务
     * @param taskId 任务ID
     */
    void resumeTask(Long taskId);

    /**
     * 获取指定状态的定时任务
     * @param status 任务状态
     * @return 定时任务列表
     */
    List<ScheduledTask> getTasksByStatus(String status);

    /**
     * 获取用户指定状态的定时任务
     * @param userId 用户ID
     * @param status 任务状态
     * @return 定时任务列表
     */
    List<ScheduledTask> getTasksByUserIdAndStatus(Long userId, String status);

    /**
     * 获取指定时间范围内的定时任务
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 定时任务列表
     */
    List<ScheduledTask> getTasksBetweenTimes(LocalDateTime startTime, LocalDateTime endTime);

    long count();
    long countPending();
    long countCompleted();
    void executeTask(Long taskId);
}