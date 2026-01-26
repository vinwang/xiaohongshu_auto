package com.xhs.controller;

import com.xhs.entity.PublishHistory;
import com.xhs.service.PublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/publish")
@RequiredArgsConstructor
public class PublishController {

    private final PublishService publishService;

    // 发布小红书笔记
    @PostMapping
    public ResponseEntity<PublishHistory> publishNote(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestBody Map<String, Object> requestBody) {
        try {
            // 从请求体中提取图片路径
            List<String> imagePathList = (List<String>) requestBody.getOrDefault("imagePaths", List.of());
            String[] imagePaths = imagePathList.toArray(new String[0]);
            
            // 调用发布服务
            PublishHistory publishHistory = publishService.publishNote(userId, title, content, imagePaths);
            return ResponseEntity.status(HttpStatus.CREATED).body(publishHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 获取用户的发布历史
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PublishHistory>> getPublishHistory(@PathVariable Long userId) {
        List<PublishHistory> publishHistories = publishService.getPublishHistory(userId);
        return ResponseEntity.ok(publishHistories);
    }

    // 获取发布历史详情
    @GetMapping("/{historyId}")
    public ResponseEntity<PublishHistory> getPublishHistoryById(@PathVariable Long historyId) {
        PublishHistory publishHistory = publishService.getPublishHistoryById(historyId);
        return ResponseEntity.ok(publishHistory);
    }
}