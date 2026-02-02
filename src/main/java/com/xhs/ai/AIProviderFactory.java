package com.xhs.ai;

import java.util.HashMap;
import java.util.Map;

public class AIProviderFactory {

    /**
     * AI服务提供商信息
     */
    public static class ProviderInfo {
        private final String name;
        private final String description;
        private final String defaultBaseUrl;
        private final String defaultModel;

        public ProviderInfo(String name, String description, String defaultBaseUrl, String defaultModel) {
            this.name = name;
            this.description = description;
            this.defaultBaseUrl = defaultBaseUrl;
            this.defaultModel = defaultModel;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getDefaultBaseUrl() {
            return defaultBaseUrl;
        }

        public String getDefaultModel() {
            return defaultModel;
        }
    }

    /**
     * 支持的AI服务提供商列表
     */
    private static final Map<String, ProviderInfo> PROVIDERS = new HashMap<>();

    static {
        // 注册Kimi AI
        PROVIDERS.put("kimi", new ProviderInfo(
                "Kimi AI",
                "月之暗面Kimi大模型",
                "https://api.moonshot.cn/v1",
                "moonshot-v1-8k"
        ));

        // 注册通义千问
        PROVIDERS.put("qwen", new ProviderInfo(
                "通义千问",
                "阿里云通义千问大模型",
                "https://dashscope.aliyuncs.com/compatible-mode/v1",
                "qwen-turbo"
        ));

        // 注册OpenAI
        PROVIDERS.put("openai", new ProviderInfo(
                "OpenAI",
                "OpenAI GPT系列模型",
                "https://api.openai.com/v1",
                "gpt-3.5-turbo"
        ));

        // 注册火山引擎
        PROVIDERS.put("volcengine", new ProviderInfo(
                "火山引擎",
                "字节跳动火山引擎豆包模型",
                "https://ark.cn-beijing.volces.com/api/v3",
                "doubao-pro-4k"
        ));
    }

    /**
     * 创建AI服务实例(使用默认配置)
     * @param providerType 提供商类型
     * @param apiKey API密钥
     * @return AI适配器实例
     */
    public static AIAdapter createProvider(String providerType, String apiKey) {
        if (!PROVIDERS.containsKey(providerType)) {
            throw new IllegalArgumentException("不支持的AI服务商: " + providerType);
        }

        ProviderInfo providerInfo = PROVIDERS.get(providerType);
        return new OpenAICompatibleAdapter(apiKey, providerInfo.getDefaultBaseUrl(), providerInfo.getDefaultModel());
    }

    /**
     * 创建AI服务实例(自定义配置)
     * @param providerType 提供商类型
     * @param apiKey API密钥
     * @param baseUrl 自定义baseUrl
     * @param model 自定义模型名称
     * @return AI适配器实例
     */
    public static AIAdapter createProvider(String providerType, String apiKey, String baseUrl, String model) {
        if (!PROVIDERS.containsKey(providerType)) {
            throw new IllegalArgumentException("不支持的AI服务商: " + providerType);
        }

        ProviderInfo providerInfo = PROVIDERS.get(providerType);
        String finalBaseUrl = baseUrl != null && !baseUrl.isEmpty() ? baseUrl : providerInfo.getDefaultBaseUrl();
        String finalModel = model != null && !model.isEmpty() ? model : providerInfo.getDefaultModel();

        return new OpenAICompatibleAdapter(apiKey, finalBaseUrl, finalModel);
    }

    /**
     * 获取服务商信息
     * @param providerType 提供商类型
     * @return 提供商信息
     */
    public static ProviderInfo getProviderInfo(String providerType) {
        return PROVIDERS.get(providerType);
    }

    /**
     * 列出所有支持的服务商
     * @return 所有支持的服务商
     */
    public static Map<String, ProviderInfo> listProviders() {
        return new HashMap<>(PROVIDERS);
    }

    /**
     * 检查是否支持指定服务商
     * @param providerType 提供商类型
     * @return 是否支持
     */
    public static boolean isProviderSupported(String providerType) {
        return PROVIDERS.containsKey(providerType);
    }
}