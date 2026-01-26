package com.xhs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "browser_fingerprints")
public class BrowserFingerprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String userAgent;

    @Column(columnDefinition = "int default 1920")
    private Integer viewportWidth = 1920;

    @Column(columnDefinition = "int default 1080")
    private Integer viewportHeight = 1080;

    @Column(columnDefinition = "int default 1920")
    private Integer screenWidth = 1920;

    @Column(columnDefinition = "int default 1080")
    private Integer screenHeight = 1080;

    @Column(length = 50)
    private String platform;

    @Column(length = 50, columnDefinition = "varchar(50) default 'Asia/Shanghai'")
    private String timezone = "Asia/Shanghai";

    @Column(length = 20, columnDefinition = "varchar(20) default 'zh-CN'")
    private String locale = "zh-CN";

    @Column(length = 100)
    private String webglVendor;

    @Column(length = 200)
    private String webglRenderer;

    @Column(length = 100)
    private String canvasFingerprint;

    @Column(length = 50)
    private String webrtcPublicIp;

    @Column(length = 50)
    private String webrtcLocalIp;

    @Column(columnDefinition = "text")
    private String fonts;

    @Column(columnDefinition = "text")
    private String plugins;

    @Column(columnDefinition = "tinyint(1) default 1")
    private Boolean isActive = true;

    @Column(columnDefinition = "tinyint(1) default 0")
    private Boolean isDefault = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}