package com.xhs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封面模板DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverTemplateDTO {
    
    /**
     * 模板ID
     */
    private String id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板类型
     */
    private String type;
    
    /**
     * 预览图片URL
     */
    private String previewImage;
    
    /**
     * 描述
     */
    private String description;
}