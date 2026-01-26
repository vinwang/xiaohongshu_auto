package com.xhs.controller;

import com.xhs.entity.ProxyConfig;
import com.xhs.service.ProxyConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proxy-configs")
@RequiredArgsConstructor
public class ProxyConfigController {

    private final ProxyConfigService proxyConfigService;

    // 获取用户的所有代理配置
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProxyConfig>> getAllProxyConfigs(@PathVariable Long userId) {
        List<ProxyConfig> proxyConfigs = proxyConfigService.getAllProxyConfigs(userId);
        return ResponseEntity.ok(proxyConfigs);
    }

    // 获取单个代理配置
    @GetMapping("/{id}")
    public ResponseEntity<ProxyConfig> getProxyConfigById(@PathVariable Long id) {
        Optional<ProxyConfig> proxyConfig = proxyConfigService.getProxyConfigById(id);
        return proxyConfig.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 获取默认代理配置
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<ProxyConfig> getDefaultProxyConfig(@PathVariable Long userId) {
        Optional<ProxyConfig> proxyConfig = proxyConfigService.getDefaultProxyConfig(userId);
        return proxyConfig.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 获取活跃代理配置
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ProxyConfig> getActiveProxyConfig(@PathVariable Long userId) {
        Optional<ProxyConfig> proxyConfig = proxyConfigService.getActiveProxyConfig(userId);
        return proxyConfig.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 创建代理配置
    @PostMapping("/user/{userId}")
    public ResponseEntity<ProxyConfig> createProxyConfig(@PathVariable Long userId, @RequestBody ProxyConfig proxyConfig) {
        ProxyConfig createdProxyConfig = proxyConfigService.createProxyConfig(userId, proxyConfig);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProxyConfig);
    }

    // 更新代理配置
    @PutMapping("/{id}")
    public ResponseEntity<ProxyConfig> updateProxyConfig(@PathVariable Long id, @RequestBody ProxyConfig proxyConfigDetails) {
        ProxyConfig updatedProxyConfig = proxyConfigService.updateProxyConfig(id, proxyConfigDetails);
        return ResponseEntity.ok(updatedProxyConfig);
    }

    // 删除代理配置
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProxyConfig(@PathVariable Long id) {
        proxyConfigService.deleteProxyConfig(id);
        return ResponseEntity.noContent().build();
    }

    // 设置默认代理配置
    @PutMapping("/{id}/default/user/{userId}")
    public ResponseEntity<ProxyConfig> setDefaultProxyConfig(@PathVariable Long id, @PathVariable Long userId) {
        ProxyConfig proxyConfig = proxyConfigService.setDefaultProxyConfig(id, userId);
        return ResponseEntity.ok(proxyConfig);
    }

    // 测试代理配置
    @PutMapping("/{id}/test")
    public ResponseEntity<ProxyConfig> testProxyConfig(@PathVariable Long id) {
        ProxyConfig proxyConfig = proxyConfigService.testProxyConfig(id);
        return ResponseEntity.ok(proxyConfig);
    }
}