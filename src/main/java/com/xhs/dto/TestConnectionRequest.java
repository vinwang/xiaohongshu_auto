package com.xhs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试AI连接请求
 */
@Data
public class TestConnectionRequest {

    /**
     * AI提供商类型
     */
    @NotBlank(message = "providerType不能为空")
    @Pattern(regexp = "^(kimi|qwen|openai|volcengine)$", message = "providerType必须是: kimi, qwen, openai 或 volcengine")
    private String providerType;

    /**
     * API密钥
     */
    @NotBlank(message = "apiKey不能为空")
    @Size(min = 10, max = 500, message = "apiKey长度必须在10-500字符之间")
    private String apiKey;

    /**
     * 模型名称
     */
    @Pattern(regexp = "^[a-zA-Z0-9-._]+$", message = "modelName只能包含字母、数字、连字符、点和下划线")
    private String modelName;
}