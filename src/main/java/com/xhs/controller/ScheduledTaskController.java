package com.xhs.controller;

import com.xhs.entity.ScheduledTask;
import com.xhs.service.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scheduled-tasks")
@RequiredArgsConstructor
public class ScheduledTaskController {

    private final ScheduledTaskService scheduledTaskService;

    // 获取所有定时任务
    @GetMapping
    public ResponseEntity<List<ScheduledTask>> getAllScheduledTasks() {
        List<ScheduledTask> tasks = scheduledTaskService.getAllScheduledTasks();
        return ResponseEntity.ok(tasks);
    }

    // 获取用户的所有定时任务
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduledTask>> getScheduledTasksByUserId(@PathVariable Long userId) {
        List<ScheduledTask> tasks = scheduledTaskService.getScheduledTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    // 获取单个定时任务
    @GetMapping("/{taskId}")
    public ResponseEntity<ScheduledTask> getScheduledTaskById(@PathVariable Long taskId) {
        return scheduledTaskService.getScheduledTaskById(taskId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 创建定时任务
    @PostMapping
    public ResponseEntity<ScheduledTask> createScheduledTask(@RequestBody ScheduledTask task) {
        ScheduledTask createdTask = scheduledTaskService.createScheduledTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    // 更新定时任务
    @PutMapping("/{taskId}")
    public ResponseEntity<ScheduledTask> updateScheduledTask(@PathVariable Long taskId, @RequestBody ScheduledTask task) {
        ScheduledTask updatedTask = scheduledTaskService.updateScheduledTask(taskId, task);
        return ResponseEntity.ok(updatedTask);
    }

    // 删除定时任务
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteScheduledTask(@PathVariable Long taskId) {
        scheduledTaskService.deleteScheduledTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // 立即执行定时任务
    @PostMapping("/{taskId}/execute")
    public ResponseEntity<Boolean> executeTaskNow(@PathVariable Long taskId) {
        boolean result = scheduledTaskService.executeTaskNow(taskId);
        return ResponseEntity.ok(result);
    }

    // 暂停定时任务
    @PostMapping("/{taskId}/pause")
    public ResponseEntity<Void> pauseTask(@PathVariable Long taskId) {
        scheduledTaskService.pauseTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // 恢复定时任务
    @PostMapping("/{taskId}/resume")
    public ResponseEntity<Void> resumeTask(@PathVariable Long taskId) {
        scheduledTaskService.resumeTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // 获取指定状态的定时任务
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ScheduledTask>> getTasksByStatus(@PathVariable String status) {
        List<ScheduledTask> tasks = scheduledTaskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    // 获取用户指定状态的定时任务
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<ScheduledTask>> getTasksByUserIdAndStatus(@PathVariable Long userId, @PathVariable String status) {
        List<ScheduledTask> tasks = scheduledTaskService.getTasksByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(tasks);
    }

    // 获取指定时间范围内的定时任务
    @GetMapping("/time-range")
    public ResponseEntity<List<ScheduledTask>> getTasksBetweenTimes(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<ScheduledTask> tasks = scheduledTaskService.getTasksBetweenTimes(startTime, endTime);
        return ResponseEntity.ok(tasks);
    }
}