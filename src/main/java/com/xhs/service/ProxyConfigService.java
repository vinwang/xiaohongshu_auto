package com.xhs.service;

import com.xhs.entity.ProxyConfig;

import java.util.List;
import java.util.Optional;

public interface ProxyConfigService {

    List<ProxyConfig> getAllProxyConfigs(Long userId);
    Optional<ProxyConfig> getProxyConfigById(Long id);
    Optional<ProxyConfig> getDefaultProxyConfig(Long userId);
    Optional<ProxyConfig> getActiveProxyConfig(Long userId);
    ProxyConfig createProxyConfig(Long userId, ProxyConfig proxyConfig);
    ProxyConfig updateProxyConfig(Long id, ProxyConfig proxyConfigDetails);
    void deleteProxyConfig(Long id);
    ProxyConfig setDefaultProxyConfig(Long id, Long userId);
    ProxyConfig testProxyConfig(Long id);
    List<ProxyConfig> getAllProxyConfigs();
    long count();
}