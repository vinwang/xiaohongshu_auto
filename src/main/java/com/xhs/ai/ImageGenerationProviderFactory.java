package com.xhs.ai;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片生成Provider工厂
 * 用于创建不同平台的图片生成Adapter(基于OpenAI兼容格式)
 */
@Slf4j
public class ImageGenerationProviderFactory {

    /**
     * 图片生服务提供商信息
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
     * 支持的图片生成服务提供商列表
     */
    private static final java.util.Map<String, ProviderInfo> PROVIDERS = new java.util.HashMap<>();

    static {
        // 注册OpenAI DALL-E
        PROVIDERS.put("openai", new ProviderInfo(
                "OpenAI",
                "OpenAI DALL-E图片生成",
                "https://api.openai.com/v1",
                "dall-e-3"
        ));

        // 注册火山引擎豆包绘图
        PROVIDERS.put("volcengine", new ProviderInfo(
                "火山引擎",
                "字节跳动火山引擎豆包绘图",
                "https://ark.cn-beijing.volces.com/api/v3",
                "doubao-image-pro"
        ));

        // 注册通义万相
        PROVIDERS.put("qwen", new ProviderInfo(
                "通义千问",
                "阿里云通义万相图片生成",
                "https://dashscope.aliyuncs.com/compatible-mode/v1",
                "wanx-v1"
        ));
    }

    /**
     * 创建图片生成Adapter(使用默认配置)
     * @param provider 平台名称 (openai, volcengine, qwen)
     * @param apiKey API密钥
     * @return 图片生成Adapter实例
     */
    public static ImageGenerationAdapter createProvider(String provider, String apiKey) {
        if (!PROVIDERS.containsKey(provider)) {
            throw new IllegalArgumentException("不支持的图片生成平台: " + provider);
        }

        ProviderInfo providerInfo = PROVIDERS.get(provider);
        return new OpenAICompatibleImageAdapter(apiKey, providerInfo.getDefaultBaseUrl(), providerInfo.getDefaultModel());
    }

    /**
     * 创建图片生成Adapter(自定义配置)
     * @param provider 平台名称
     * @param apiKey API密钥
     * @param baseUrl 自定义baseUrl
     * @param model 自定义模型名称
     * @return 图片生成Adapter实例
     */
    public static ImageGenerationAdapter createProvider(String provider, String apiKey, String baseUrl, String model) {
        if (!PROVIDERS.containsKey(provider)) {
            throw new IllegalArgumentException("不支持的图片生成平台: " + provider);
        }

        ProviderInfo providerInfo = PROVIDERS.get(provider);
        String finalBaseUrl = baseUrl != null && !baseUrl.isEmpty() ? baseUrl : providerInfo.getDefaultBaseUrl();
        String finalModel = model != null && !model.isEmpty() ? model : providerInfo.getDefaultModel();

        return new OpenAICompatibleImageAdapter(apiKey, finalBaseUrl, finalModel);
    }

    /**
     * 获取服务商信息
     * @param provider 平台名称
     * @return 提供商信息
     */
    public static ProviderInfo getProviderInfo(String provider) {
        return PROVIDERS.get(provider);
    }

    /**
     * 列出所有支持的图片生成服务商
     * @return 所有支持的服务商
     */
    public static java.util.Map<String, ProviderInfo> listProviders() {
        return new java.util.HashMap<>(PROVIDERS);
    }

    /**
     * 验证provider是否支持
     * @param provider 平台名称
     * @return 是否支持
     */
    public static boolean isProviderSupported(String provider) {
        return PROVIDERS.containsKey(provider);
    }
}