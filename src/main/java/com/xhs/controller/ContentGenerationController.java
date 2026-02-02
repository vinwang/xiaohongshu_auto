package com.xhs.controller;

import com.xhs.dto.ContentGenerationRequest;
import com.xhs.dto.ContentGenerationResponse;
import com.xhs.dto.CoverTemplateDTO;
import com.xhs.service.ContentGenerationService;
import com.xhs.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 内容生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ContentGenerationController {
    
    private final ContentGenerationService contentGenerationService;
    private final TemplateService templateService;
    
    /**
     * 生成内容
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateContent(@Valid @RequestBody ContentGenerationRequest request) {
        log.info("收到内容生成请求: {}", request.getInputText());
        
        try {
            ContentGenerationResponse response = contentGenerationService.generateContent(request);
            
            return ResponseEntity.ok(Map.of(
                "success", response.getSuccess(),
                "message", response.getMessage(),
                "data", response
            ));
            
        } catch (Exception e) {
            log.error("内容生成失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "生成失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取所有封面模板
     */
    @GetMapping("/templates/cover")
    public ResponseEntity<Map<String, Object>> getCoverTemplates() {
        try {
            List<CoverTemplateDTO> templates = templateService.getAllCoverTemplates();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", templates
            ));
            
        } catch (Exception e) {
            log.error("获取封面模板失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "获取模板失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 根据ID获取模板
     */
    @GetMapping("/templates/cover/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable String templateId) {
        try {
            CoverTemplateDTO template = templateService.getTemplateById(templateId);
            
            if (template == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "模板不存在"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", template
            ));
            
        } catch (Exception e) {
            log.error("获取模板失败", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "获取模板失败: " + e.getMessage()
            ));
        }
    }
}