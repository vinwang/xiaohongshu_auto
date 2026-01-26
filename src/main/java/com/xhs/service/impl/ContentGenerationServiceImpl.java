package com.xhs.service.impl;

import com.xhs.analysis.ContentAnalysis;
import com.xhs.analysis.ContentAnalyzer;
import com.xhs.service.AIService;
import com.xhs.service.ContentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentGenerationServiceImpl implements ContentGenerationService {

    private final ContentAnalyzer contentAnalyzer;
    private final AIService aiService;

    @Override
    public ContentAnalysis analyzeContent(String text, String imageType) {
        return contentAnalyzer.analyzeText(text, imageType);
    }

    @Override
    public String generateContent(String providerType, String apiKey, String prompt, Map<String, Object> params) throws Exception {
        return aiService.generateContent(providerType, apiKey, prompt, params);
    }

    @Override
    public String generateXiaohongshuContent(String providerType, String apiKey, ContentAnalysis analysis, String originalText) throws Exception {
        // 构建小红书文案生成提示词
        String prompt = String.format("""
        请根据以下内容分析结果，将原始文本改写成适合小红书平台的文案：
        
        【分析结果】
        标题：%s
        主题：%s
        关键词：%s
        情感：%s
        目标受众：%s
        配色方案：%s
        风格偏好：%s
        
        【原始文本】
        %s
        
        【要求】
        1. 语言风格符合小红书平台特性，亲切自然，有网感
        2. 保留核心信息，突出亮点
        3. 结构清晰，适合阅读
        4. 适当添加表情符号和话题标签
        5. 风格符合%s
        6. 长度适中，适合小红书笔记
        """, 
        analysis.getTitle(),
        String.join(", ", analysis.getTopics()),
        String.join(", ", analysis.getKeywords()),
        analysis.getSentiment(),
        analysis.getTargetAudience(),
        analysis.getColorScheme(),
        analysis.getStylePreference(),
        originalText,
        analysis.getStylePreference());

        // 使用AI生成小红书文案
        return aiService.generateContent(providerType, apiKey, prompt, Map.of("temperature", 0.7));
    }

    @Override
    public String generateTitle(String providerType, String apiKey, String content) throws Exception {
        // 构建标题生成提示词
        String prompt = String.format("""
        请为以下内容生成一个吸引人的小红书标题：
        
        【内容】
        %s
        
        【要求】
        1. 吸引人，有点击欲望
        2. 突出核心亮点
        3. 符合小红书平台风格
        4. 适当添加表情符号
        5. 长度适中，不超过20个字符
        6. 有网感，符合年轻人的阅读习惯
        """, content);

        // 使用AI生成标题
        return aiService.generateContent(providerType, apiKey, prompt, Map.of("temperature", 0.8));
    }

    @Override
    public Map<String, Object> generateContent(String type, String prompt, Long userId) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            // 使用默认的Kimi模型生成内容
            String content = aiService.generateContent("kimi", "", prompt, Map.of("temperature", 0.7));
            
            result.put("success", true);
            result.put("content", content);
            result.put("type", type);
            result.put("userId", userId);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}