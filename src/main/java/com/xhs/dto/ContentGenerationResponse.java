package com.xhs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容生成响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentGenerationResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 内容图片URL列表
     */
    private List<String> contentImages;
    
    /**
     * 输入文本
     */
    private String inputText;
    
    /**
     * 生成器类型(remote/backup/llm)
     */
    private String generator;
    
    /**
     * 信息说明
     */
    private String infoReason;
    
    /**
     * 内容分页
     */
    private List<ContentPage> contentPages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentPage {
        private String title;
        private String content;
        private String image;
    }
}