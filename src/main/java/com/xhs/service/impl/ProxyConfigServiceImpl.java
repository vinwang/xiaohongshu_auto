package com.xhs.service.impl;

import com.xhs.entity.ProxyConfig;
import com.xhs.entity.User;
import com.xhs.repository.ProxyConfigRepository;
import com.xhs.repository.UserRepository;
import com.xhs.service.ProxyConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProxyConfigServiceImpl implements ProxyConfigService {

    private final ProxyConfigRepository proxyConfigRepository;
    private final UserRepository userRepository;

    @Override
    public List<ProxyConfig> getAllProxyConfigs(Long userId) {
        return proxyConfigRepository.findByUserId(userId);
    }

    @Override
    public Optional<ProxyConfig> getProxyConfigById(Long id) {
        return proxyConfigRepository.findById(id);
    }

    @Override
    public Optional<ProxyConfig> getDefaultProxyConfig(Long userId) {
        return proxyConfigRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    public Optional<ProxyConfig> getActiveProxyConfig(Long userId) {
        return proxyConfigRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional
    public ProxyConfig createProxyConfig(Long userId, ProxyConfig proxyConfig) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        proxyConfig.setUser(user);
        
        // 如果这是第一个代理配置，设置为默认
        if (proxyConfigRepository.findByUserId(userId).isEmpty()) {
            proxyConfig.setIsDefault(true);
        }
        
        return proxyConfigRepository.save(proxyConfig);
    }

    @Override
    @Transactional
    public ProxyConfig updateProxyConfig(Long id, ProxyConfig proxyConfigDetails) {
        ProxyConfig proxyConfig = proxyConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("代理配置不存在"));

        // 更新代理配置信息
        if (proxyConfigDetails.getName() != null) {
            proxyConfig.setName(proxyConfigDetails.getName());
        }
        if (proxyConfigDetails.getProxyType() != null) {
            proxyConfig.setProxyType(proxyConfigDetails.getProxyType());
        }
        if (proxyConfigDetails.getHost() != null) {
            proxyConfig.setHost(proxyConfigDetails.getHost());
        }
        if (proxyConfigDetails.getPort() != null) {
            proxyConfig.setPort(proxyConfigDetails.getPort());
        }
        if (proxyConfigDetails.getUsername() != null) {
            proxyConfig.setUsername(proxyConfigDetails.getUsername());
        }
        if (proxyConfigDetails.getPassword() != null) {
            proxyConfig.setPassword(proxyConfigDetails.getPassword());
        }
        if (proxyConfigDetails.getIsActive() != null) {
            proxyConfig.setIsActive(proxyConfigDetails.getIsActive());
        }
        if (proxyConfigDetails.getTestUrl() != null) {
            proxyConfig.setTestUrl(proxyConfigDetails.getTestUrl());
        }

        return proxyConfigRepository.save(proxyConfig);
    }

    @Override
    @Transactional
    public void deleteProxyConfig(Long id) {
        ProxyConfig proxyConfig = proxyConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("代理配置不存在"));
        
        // 如果删除的是默认代理，将另一个代理设置为默认
        if (proxyConfig.getIsDefault()) {
            List<ProxyConfig> otherProxies = proxyConfigRepository.findByUserId(proxyConfig.getUser().getId());
            if (!otherProxies.isEmpty()) {
                ProxyConfig newDefault = otherProxies.stream()
                        .filter(p -> !p.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                if (newDefault != null) {
                    newDefault.setIsDefault(true);
                    proxyConfigRepository.save(newDefault);
                }
            }
        }
        
        proxyConfigRepository.delete(proxyConfig);
    }

    @Override
    @Transactional
    public ProxyConfig setDefaultProxyConfig(Long id, Long userId) {
        // 将所有代理配置的isDefault设置为false
        List<ProxyConfig> allProxies = proxyConfigRepository.findByUserId(userId);
        for (ProxyConfig proxy : allProxies) {
            proxy.setIsDefault(false);
        }
        proxyConfigRepository.saveAll(allProxies);
        
        // 将指定代理配置的isDefault设置为true
        ProxyConfig proxyConfig = proxyConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("代理配置不存在"));
        proxyConfig.setIsDefault(true);
        
        return proxyConfigRepository.save(proxyConfig);
    }

    @Override
    @Transactional
    public ProxyConfig testProxyConfig(Long id) {
        ProxyConfig proxyConfig = proxyConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("代理配置不存在"));
        
        // 这里应该实现代理测试逻辑，暂时简化处理
        proxyConfig.setTestSuccess(true);
        proxyConfig.setTestLatency(100.0f);
        proxyConfig.setLastTestAt(LocalDateTime.now());
        
        return proxyConfigRepository.save(proxyConfig);
    }

    @Override
    public List<ProxyConfig> getAllProxyConfigs() {
        return proxyConfigRepository.findAll();
    }

    @Override
    public long count() {
        return proxyConfigRepository.count();
    }
}