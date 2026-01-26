package com.xhs.service;

import com.xhs.ai.AIProviderFactory;

import java.util.Map;

public interface AIService {

    /**
     * 生成内容
     * @param providerType AI提供商类型
     * @param apiKey API密钥
     * @param prompt 提示词
     * @param params 额外参数
     * @return 生成的内容
     */
    String generateContent(String providerType, String apiKey, String prompt, Map<String, Object> params) throws Exception;

    /**
     * 测试AI连接
     * @param providerType AI提供商类型
     * @param apiKey API密钥
     * @return 是否连接成功
     */
    boolean testAIConnection(String providerType, String apiKey) throws Exception;

    /**
     * 获取支持的AI提供商列表
     * @return 支持的AI提供商列表
     */
    Map<String, AIProviderFactory.ProviderInfo> getSupportedProviders();
}