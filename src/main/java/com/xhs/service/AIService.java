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
     * @param modelName 模型名称(可选)
     * @return 是否连接成功
     */
    boolean testAIConnection(String providerType, String apiKey, String modelName) throws Exception;

    /**
     * 获取支持的AI提供商列表
     * @return 支持的AI提供商列表
     */
    Map<String, AIProviderFactory.ProviderInfo> getSupportedProviders();

    /**
     * 测试生图连接
     * @param provider 生图平台
     * @param apiKey API密钥
     * @param modelName 模型名称
     * @return 是否连接成功
     */
    boolean testImageConnection(String provider, String apiKey, String modelName) throws Exception;

    /**
     * 生成图片
     * @param provider 生图平台
     * @param apiKey API密钥
     * @param prompt 图片提示词
     * @param params 额外参数(size, n等)
     * @return 生成的图片URL或base64数据
     */
    String generateImage(String provider, String apiKey, String prompt, Map<String, Object> params) throws Exception;
}