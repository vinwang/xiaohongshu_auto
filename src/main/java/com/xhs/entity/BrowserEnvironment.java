package com.xhs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "browser_environments")
public class BrowserEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "tinyint(1) default 1")
    private Boolean isActive = true;

    @Column(columnDefinition = "tinyint(1) default 0")
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fingerprint_id")
    private BrowserFingerprint fingerprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proxy_id")
    private ProxyConfig proxyConfig;

    @Column(length = 20)
    private String geolocationLatitude;

    @Column(length = 20)
    private String geolocationLongitude;

    private LocalDateTime lastUsedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}