package com.xhs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI模型配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.model")
public class AIModelConfig {
    
    /**
     * 默认模型类型
     */
    private String defaultType = "kimi";
    
    /**
     * 生图模型配置
     */
    private ImageConfig image = new ImageConfig();
    
    /**
     * Kimi配置
     */
    private KimiConfig kimi = new KimiConfig();
    
    /**
     * Qwen配置
     */
    private QwenConfig qwen = new QwenConfig();
    
    /**
     * OpenAI配置
     */
    private OpenAIConfig openai = new OpenAIConfig();
    
    /**
     * 火山引擎配置
     */
    private VolcengineConfig volcengine = new VolcengineConfig();
    
    @Data
    public static class ImageConfig {
        private String apiKey;
        private String endpoint = "";
        private String model = "dall-e-3";
        private Integer timeout = 30;
    }
    
    @Data
    public static class KimiConfig {
        private String apiKey;
        private String baseUrl = "https://api.moonshot.cn/v1";
        private String endpoint = "";
        private String model = "moonshot-v1-8k";
        private Integer maxTokens = 2000;
        private Double temperature = 0.7;
        private Integer timeout = 30;
    }
    
    @Data
    public static class QwenConfig {
        private String apiKey;
        private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";
        private String endpoint = "";
        private String model = "qwen-turbo";
        private Integer maxTokens = 2000;
        private Double temperature = 0.7;
        private Integer timeout = 30;
    }
    
    @Data
    public static class OpenAIConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String endpoint = "";
        private String model = "gpt-3.5-turbo";
        private Integer maxTokens = 2000;
        private Double temperature = 0.7;
        private Integer timeout = 30;
    }
    
    @Data
    public static class VolcengineConfig {
        private String apiKey;
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
        private String endpoint = "";
        private String model = "doubao-pro-4k";
        private Integer maxTokens = 2000;
        private Double temperature = 0.7;
        private Integer timeout = 30;
    }
}