package com.xhs.repository;

import com.xhs.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    // 根据用户ID查找定时任务
    List<ScheduledTask> findByUserId(Long userId);

    // 根据状态查找定时任务
    List<ScheduledTask> findByStatus(String status);

    // 根据用户ID和状态查找定时任务
    List<ScheduledTask> findByUserIdAndStatus(Long userId, String status);

    // 查找指定时间范围内的定时任务
    List<ScheduledTask> findByScheduledTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 查找未执行且在指定时间之前的定时任务
    List<ScheduledTask> findByStatusAndScheduledTimeLessThanEqual(String status, LocalDateTime time);

    // 统计指定状态的任务数量
    long countByStatus(String status);
}