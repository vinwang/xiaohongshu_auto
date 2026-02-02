package com.xhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI模型配置实体
 * 存储各个AI模型的配置信息
 */
@Data
@Entity
@Table(name = "ai_model_configs", indexes = {
    @Index(name = "idx_provider", columnList = "provider"),
    @Index(name = "idx_enabled", columnList = "enabled"),
    @Index(name = "idx_model_name", columnList = "model_name")
})
public class AIModelConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模型名称: kimi, qwen, openai, volcengine
     */
    @Column(name = "model_name", nullable = false, unique = true, length = 50)
    private String modelName;
    
    /**
     * 提供商: OPENAI, CLAUDE, LOCAL, VOLCENGINE
     */
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;
    
    /**
     * API端点URL
     */
    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;
    
    /**
     * 加密的API密钥
     */
    @Column(name = "api_key_encrypted", length = 255)
    private String apiKeyEncrypted;
    
    /**
     * 模型参数配置(JSON格式)
     */
    @Column(name = "model_parameters", columnDefinition = "JSON")
    private String modelParameters;
    
    /**
     * 速率限制(每分钟请求数)
     */
    @Column(name = "rate_limit")
    private Integer rateLimit = 100;
    
    /**
     * 每1K token花费
     */
    @Column(name = "cost_per_1k_tokens", precision = 10, scale = 4)
    private java.math.BigDecimal costPer1kTokens;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}