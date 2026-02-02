package com.xhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热点话题实体
 * 存储各大平台的热点话题数据
 */
@Data
@Entity
@Table(name = "trending_topics", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_hot_score", columnList = "hot_score"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_category_created", columnList = "category, created_at")
})
public class TrendingTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分类: weibo, baidu, toutiao, bilibili
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    /**
     * 话题标题
     */
    @Column(name = "topic", nullable = false, length = 200)
    private String topic;
    
    /**
     * 热度值
     */
    @Column(name = "hot_score")
    private Integer hotScore;
    
    /**
     * 数据来源
     */
    @Column(name = "source", nullable = false, length = 50)
    private String source;
    
    /**
     * 排名
     */
    @Column(name = "trend_rank")
    private Integer trendRank;
    
    /**
     * 元数据(JSON格式)
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
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