package com.xhs.controller;

import com.xhs.dto.SaveAIConfigRequest;
import com.xhs.dto.TestConnectionRequest;
import com.xhs.dto.TestImageConnectionRequest;
import com.xhs.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
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
            log.error("AI生成内容失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("AI生成内容失败");
        }
    }

    // 测试AI连接
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testAIConnection(
            @Valid @RequestBody TestConnectionRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", errors));
        }

        try {
            boolean isConnected = aiService.testAIConnection(
                    request.getProviderType(),
                    request.getApiKey(),
                    request.getModelName()
            );
            return ResponseEntity.ok(Map.of("success", isConnected));
        } catch (Exception e) {
            log.error("测试AI连接失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("success", false, "error", "连接测试失败"));
        }
    }

    // 获取支持的AI提供商列表
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getSupportedProviders() {
        try {
            var providers = aiService.getSupportedProviders();
            return ResponseEntity.ok(Map.of("providers", providers));
        } catch (Exception e) {
            log.error("获取AI提供商列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取AI提供商列表失败"));
        }
    }

    // 测试生图连接
    @PostMapping("/test-image-connection")
    public ResponseEntity<Map<String, Object>> testImageConnection(
            @Valid @RequestBody TestImageConnectionRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", errors));
        }

        try {
            boolean isConnected = aiService.testImageConnection(
                    request.getProvider(),
                    request.getApiKey(),
                    request.getModelName()
            );
            return ResponseEntity.ok(Map.of("success", isConnected));
        } catch (Exception e) {
            log.error("测试生图连接失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("success", false, "error", "连接测试失败"));
        }
    }
}