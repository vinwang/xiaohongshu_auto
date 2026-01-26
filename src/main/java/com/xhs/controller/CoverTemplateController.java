package com.xhs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cover-templates")
@CrossOrigin(origins = "*")
public class CoverTemplateController {

    private static final String TEMPLATE_DIR = "assets/system_templates/template_showcase/";

    // 获取所有封面模板
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCoverTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        
        File templateDir = new File(TEMPLATE_DIR);
        if (templateDir.exists() && templateDir.isDirectory()) {
            File[] files = templateDir.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
            
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> template = new java.util.HashMap<>();
                    template.put("id", file.getName());
                    template.put("name", file.getName().replace(".png", "").replace(".jpg", ""));
                    template.put("url", "/api/assets/system_templates/template_showcase/" + file.getName());
                    templates.add(template);
                }
            }
        }
        
        return ResponseEntity.ok(templates);
    }

    // 获取封面模板详情
    @GetMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> getCoverTemplate(@PathVariable String templateId) {
        File templateFile = new File(TEMPLATE_DIR + templateId);
        
        if (!templateFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> template = new java.util.HashMap<>();
        template.put("id", templateId);
        template.put("name", templateId.replace(".png", "").replace(".jpg", ""));
        template.put("url", "/api/assets/system_templates/template_showcase/" + templateId);
        
        return ResponseEntity.ok(template);
    }
}