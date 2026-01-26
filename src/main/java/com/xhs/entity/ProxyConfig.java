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
@Table(name = "proxy_configs")
public class ProxyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String proxyType;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(length = 100)
    private String username;

    @Column(length = 100)
    private String password;

    @Column(columnDefinition = "tinyint(1) default 1")
    private Boolean isActive = true;

    @Column(columnDefinition = "tinyint(1) default 0")
    private Boolean isDefault = false;

    @Column(length = 500, columnDefinition = "varchar(500) default 'https://httpbin.org/ip'")
    private String testUrl = "https://httpbin.org/ip";

    private Float testLatency;

    private Boolean testSuccess;

    private LocalDateTime lastTestAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}