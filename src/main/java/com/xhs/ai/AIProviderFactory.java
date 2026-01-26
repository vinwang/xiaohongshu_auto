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
        private final Class<? extends AIAdapter> adapterClass;
        private final String website;

        public ProviderInfo(String name, String description, Class<? extends AIAdapter> adapterClass, String website) {
            this.name = name;
            this.description = description;
            this.adapterClass = adapterClass;
            this.website = website;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Class<? extends AIAdapter> getAdapterClass() {
            return adapterClass;
        }

        public String getWebsite() {
            return website;
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
                KimiAdapter.class,
                "https://platform.moonshot.cn/"
        ));

        // 注册通义千问
        PROVIDERS.put("qwen", new ProviderInfo(
                "通义千问",
                "阿里云通义千问大模型",
                QwenAdapter.class,
                "https://dashscope.aliyun.com/"
        ));
    }

    /**
     * 创建AI服务实例
     * @param providerType 提供商类型
     * @param apiKey API密钥
     * @return AI适配器实例
     */
    public static AIAdapter createProvider(String providerType, String apiKey) {
        if (!PROVIDERS.containsKey(providerType)) {
            throw new IllegalArgumentException("不支持的AI服务商: " + providerType);
        }

        ProviderInfo providerInfo = PROVIDERS.get(providerType);
        Class<? extends AIAdapter> adapterClass = providerInfo.getAdapterClass();

        try {
            return adapterClass.getConstructor(String.class).newInstance(apiKey);
        } catch (Exception e) {
            throw new RuntimeException("创建AI服务实例失败: " + e.getMessage(), e);
        }
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