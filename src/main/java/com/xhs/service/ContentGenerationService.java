package com.xhs.service;

import com.xhs.dto.ContentGenerationRequest;
import com.xhs.dto.ContentGenerationResponse;

/**
 * 内容生成服务接口
 */
public interface ContentGenerationService {
    
    /**
     * 生成内容
     */
    ContentGenerationResponse generateContent(ContentGenerationRequest request);
}