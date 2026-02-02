package com.xhs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 远程工作流API配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "workflow.api")
public class WorkflowApiConfig {
    
    /**
     * API基础URL
     */
    private String baseUrl = "http://8.137.103.115:8081";
    
    /**
     * 工作流ID
     */
    private String workflowId = "7431484143153070132";
    
    /**
     * 连接超时时间(秒)
     */
    private Integer connectTimeout = 5;
    
    /**
     * 读取超时时间(秒)
     */
    private Integer readTimeout = 120;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;
    
    /**
     * 重试间隔(秒)
     */
    private Integer retryDelay = 2;
    
    /**
     * 是否启用备用生成器
     */
    private Boolean enableBackup = true;
    
    /**
     * User-Agent
     */
    private String userAgent = "XhsAiPublisher/2.0";
}