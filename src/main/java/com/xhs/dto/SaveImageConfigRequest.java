package com.xhs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存生图配置请求
 */
@Data
public class SaveImageConfigRequest {

    /**
     * 生图平台
     */
    @NotBlank(message = "provider不能为空")
    @Pattern(regexp = "^(openai|volcengine|qwen)$", message = "provider必须是: openai, volcengine 或 qwen")
    private String provider;

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
    private String model;

    /**
     * 端点地址
     */
    @Pattern(regexp = "^https?://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$|^$", message = "endpoint必须是有效的URL格式")
    private String endpoint;
}