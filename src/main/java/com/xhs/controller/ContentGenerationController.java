package com.xhs.controller;

import com.xhs.analysis.ContentAnalysis;
import com.xhs.service.ContentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentGenerationController {

    private final ContentGenerationService contentGenerationService;

    // 分析内容
    @PostMapping("/analyze")
    public ResponseEntity<ContentAnalysis> analyzeContent(
            @RequestParam String text,
            @RequestParam(defaultValue = "cover") String imageType) {
        ContentAnalysis analysis = contentGenerationService.analyzeContent(text, imageType);
        return ResponseEntity.ok(analysis);
    }

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
            String result = contentGenerationService.generateContent(providerType, apiKey, prompt, params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("内容生成失败: " + e.getMessage());
        }
    }

    // 生成小红书文案
    @PostMapping("/xiaohongshu/generate")
    public ResponseEntity<String> generateXiaohongshuContent(
            @RequestParam String providerType,
            @RequestParam String apiKey,
            @RequestBody Map<String, Object> requestBody) {
        try {
            // 从请求体中提取参数
            String originalText = (String) requestBody.get("originalText");
            String imageType = (String) requestBody.getOrDefault("imageType", "cover");
            
            // 先分析内容
            ContentAnalysis analysis = contentGenerationService.analyzeContent(originalText, imageType);
            
            // 生成小红书文案
            String result = contentGenerationService.generateXiaohongshuContent(providerType, apiKey, analysis, originalText);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("小红书文案生成失败: " + e.getMessage());
        }
    }

    // 生成标题
    @PostMapping("/title/generate")
    public ResponseEntity<String> generateTitle(
            @RequestParam String providerType,
            @RequestParam String apiKey,
            @RequestParam String content) {
        try {
            String result = contentGenerationService.generateTitle(providerType, apiKey, content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("标题生成失败: " + e.getMessage());
        }
    }

    // 分析并生成小红书文案（组合接口）
    @PostMapping("/analyze-and-generate")
    public ResponseEntity<Map<String, Object>> analyzeAndGenerate(
            @RequestParam String providerType,
            @RequestParam String apiKey,
            @RequestParam String text,
            @RequestParam(defaultValue = "cover") String imageType) {
        try {
            // 分析内容
            ContentAnalysis analysis = contentGenerationService.analyzeContent(text, imageType);
            
            // 生成小红书文案
            String generatedContent = contentGenerationService.generateXiaohongshuContent(providerType, apiKey, analysis, text);
            
            // 生成标题
            String generatedTitle = contentGenerationService.generateTitle(providerType, apiKey, generatedContent);
            
            // 构建响应
            Map<String, Object> response = Map.of(
                    "analysis", analysis,
                    "generatedContent", generatedContent,
                    "generatedTitle", generatedTitle
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "分析和生成失败: " + e.getMessage()));
        }
    }
}