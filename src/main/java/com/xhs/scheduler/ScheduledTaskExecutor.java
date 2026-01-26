package com.xhs.scheduler;

import com.xhs.service.impl.ScheduledTaskServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ScheduledTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskExecutor.class);

    private final ScheduledTaskServiceImpl scheduledTaskService;

    // 构造函数
    public ScheduledTaskExecutor(ScheduledTaskServiceImpl scheduledTaskService) {
        this.scheduledTaskService = Objects.requireNonNull(scheduledTaskService, "scheduledTaskService must not be null");
    }

    /**
     * 每5分钟检查一次到期的定时任务
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkDueTasks() {
        logger.info("开始检查到期的定时任务...");
        scheduledTaskService.checkAndExecuteDueTasks();
        logger.info("检查到期定时任务完成");
    }

    /**
     * 每小时执行一次清理任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupTasks() {
        logger.info("开始清理过期任务...");
        // TODO: 实现任务清理逻辑，例如删除超过一定时间的已完成任务
        logger.info("清理过期任务完成");
    }
}