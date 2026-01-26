package com.xhs.service;

import com.xhs.analysis.ContentAnalysis;

import java.util.Map;

public interface ContentGenerationService {

    /**
     * 分析文本内容
     * @param text 待分析的文本
     * @param imageType 图片类型：cover 或 content
     * @return 内容分析结果
     */
    ContentAnalysis analyzeContent(String text, String imageType);

    /**
     * 使用AI生成内容
     * @param providerType AI提供商类型
     * @param apiKey API密钥
     * @param prompt 提示词
     * @param params 额外参数
     * @return 生成的内容
     */
    String generateContent(String providerType, String apiKey, String prompt, Map<String, Object> params) throws Exception;

    /**
     * 根据分析结果生成小红书文案
     * @param providerType AI提供商类型
     * @param apiKey API密钥
     * @param analysis 内容分析结果
     * @param originalText 原始文本
     * @return 生成的小红书文案
     */
    String generateXiaohongshuContent(String providerType, String apiKey, ContentAnalysis analysis, String originalText) throws Exception;

    /**
     * 生成小红书标题
     * @param providerType AI提供商类型
     * @param apiKey API密钥
     * @param content 内容
     * @return 生成的标题
     */
    String generateTitle(String providerType, String apiKey, String content) throws Exception;

    /**
     * 前端页面生成内容
     * @param type 类型
     * @param prompt 提示词
     * @param userId 用户ID
     * @return 生成结果
     */
    Map<String, Object> generateContent(String type, String prompt, Long userId);
}