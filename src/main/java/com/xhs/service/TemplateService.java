package com.xhs.service;

import com.xhs.dto.CoverTemplateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板服务
 * 管理封面模板和海报模板
 */
@Slf4j
@Service
public class TemplateService {
    
    /**
     * 获取所有封面模板
     */
    public List<CoverTemplateDTO> getAllCoverTemplates() {
        List<CoverTemplateDTO> templates = new ArrayList<>();
        
        // 产品展示模板
        templates.add(CoverTemplateDTO.builder()
            .id("showcase_product")
            .name("产品展示")
            .type("product")
            .previewImage("/api/assets/system_templates/template_showcase/showcase_mkt_product_showcase_professional.png")
            .build());
        
        // 教程卡片模板
        templates.add(CoverTemplateDTO.builder()
            .id("showcase_tutorial")
            .name("教程卡片")
            .type("tutorial")
            .previewImage("/api/assets/system_templates/template_showcase/showcase_edu_course_intro_elegant.png")
            .build());
        
        // 测评风格模板
        templates.add(CoverTemplateDTO.builder()
            .id("showcase_review")
            .name("测评风格")
            .type("review")
            .previewImage("/api/assets/system_templates/template_showcase/showcase_edu_grade_card_professional.png")
            .build());
        
        // 营销海报模板
        templates.add(CoverTemplateDTO.builder()
            .id("showcase_marketing_poster")
            .name("营销海报")
            .type("marketing")
            .previewImage("/api/assets/system_templates/template_showcase/showcase_marketing_poster.png")
            .build());
        
        // 技术对比模板
        templates.add(CoverTemplateDTO.builder()
            .id("showcase_tech_comparison")
            .name("技术对比")
            .type("comparison")
            .previewImage("/api/assets/system_templates/template_showcase/showcase_tech_comparison_cool.png")
            .build());
        
        // 时间线模板
        templates.add(CoverTemplateDTO.builder()
            .id("showcase_timeline")
            .name("时间线")
            .type("timeline")
            .previewImage("/api/assets/system_templates/template_showcase/showcase_timeline_vertical_elegant.png")
            .build());
        
        return templates;
    }
    
    /**
     * 根据ID获取模板
     */
    public CoverTemplateDTO getTemplateById(String templateId) {
        return getAllCoverTemplates().stream()
            .filter(t -> t.getId().equals(templateId))
            .findFirst()
            .orElse(null);
    }
}