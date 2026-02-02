package com.xhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 内容生成记录实体
 * 记录用户使用AI生成内容的详细信息
 */
@Data
@Entity
@Table(name = "content_generation_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_model", columnList = "model_name"),
    @Index(name = "idx_status", columnList = "status")
})
public class ContentGenerationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 输入提示词
     */
    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;
    
    /**
     * 生成的内容(JSON格式)
     */
    @Column(name = "generated_content", columnDefinition = "JSON")
    private String generatedContent;
    
    /**
     * 使用的模型名称
     */
    @Column(name = "model_name", length = 50)
    private String modelName;
    
    /**
     * 使用的token数量
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    /**
     * 花费金额
     */
    @Column(name = "cost", precision = 10, scale = 4)
    private java.math.BigDecimal cost;
    
    /**
     * 生成耗时(毫秒)
     */
    @Column(name = "generation_time")
    private Integer generationTime;
    
    /**
     * 状态: SUCCESS, FAILED
     */
    @Column(name = "status", length = 20)
    private String status = "SUCCESS";
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
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