package com.xhs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    // 视频去水印
    @PostMapping("/remove-watermark")
    public ResponseEntity<Map<String, Object>> removeWatermark(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        String videoUrl = request.get("url");
        
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入视频URL");
            return ResponseEntity.badRequest().body(result);
        }
        
        try {
            // 模拟去水印处理
            // 实际项目中需要调用第三方API或使用FFmpeg处理视频
            
            // 检查URL格式
            if (!isValidVideoUrl(videoUrl)) {
                result.put("success", false);
                result.put("message", "无效的视频URL");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 模拟处理结果
            Map<String, Object> videoInfo = new HashMap<>();
            videoInfo.put("title", "示例视频标题");
            videoInfo.put("author", "示例作者");
            videoInfo.put("description", "这是一个示例视频描述");
            videoInfo.put("coverUrl", "https://via.placeholder.com/300x400");
            videoInfo.put("videoUrl", videoUrl);
            videoInfo.put("views", 1000000);
            videoInfo.put("likes", 50000);
            videoInfo.put("comments", 1000);
            
            result.put("success", true);
            result.put("message", "去水印成功");
            result.put("data", videoInfo);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    private boolean isValidVideoUrl(String url) {
        // 简单的URL验证
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}