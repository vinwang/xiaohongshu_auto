package com.xhs.controller;

import com.xhs.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    // 生成内容
    @PostMapping("/generate")
    public ResponseEntity<String> generateContent(
            @RequestParam String providerType,
            @RequestParam String apiKey,
            @RequestParam String prompt,
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            if (params == null) {
                params = Map.of();
            }
            String result = aiService.generateContent(providerType, apiKey, prompt, params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("AI生成内容失败: " + e.getMessage());
        }
    }

    // 测试AI连接
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testAIConnection(@RequestBody Map<String, String> request) {
        try {
            String providerType = request.get("providerType");
            String apiKey = request.get("apiKey");
            boolean isConnected = aiService.testAIConnection(providerType, apiKey);
            return ResponseEntity.ok(Map.of("success", isConnected));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // 获取支持的AI提供商列表
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getSupportedProviders() {
        try {
            var providers = aiService.getSupportedProviders();
            return ResponseEntity.ok(Map.of("providers", providers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取AI提供商列表失败: " + e.getMessage()));
        }
    }
}