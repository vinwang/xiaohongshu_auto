package com.xhs.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 内容生成请求
 */
@Data
public class ContentGenerationRequest {
    
    /**
     * 输入内容/主题
     */
    @NotBlank(message = "输入内容不能为空")
    private String inputText;
    
    /**
     * 眉头标题
     */
    private String headerTitle;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * AI模型类型(kimi/qwen/openai)
     */
    private String aiModelType;
    
    /**
     * 封面模板ID
     */
    private String coverTemplateId;
    
    /**
     * 是否仅生成文案(不生成图片)
     */
    private Boolean textOnly = false;
}