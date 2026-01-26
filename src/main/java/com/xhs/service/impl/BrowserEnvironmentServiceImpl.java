package com.xhs.service.impl;

import com.xhs.entity.BrowserEnvironment;
import com.xhs.entity.User;
import com.xhs.repository.BrowserEnvironmentRepository;
import com.xhs.repository.UserRepository;
import com.xhs.service.BrowserEnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrowserEnvironmentServiceImpl implements BrowserEnvironmentService {

    private final BrowserEnvironmentRepository browserEnvironmentRepository;
    private final UserRepository userRepository;

    @Override
    public List<BrowserEnvironment> getAllBrowserEnvironments(Long userId) {
        return browserEnvironmentRepository.findByUserId(userId);
    }

    @Override
    public Optional<BrowserEnvironment> getBrowserEnvironmentById(Long id) {
        return browserEnvironmentRepository.findById(id);
    }

    @Override
    public Optional<BrowserEnvironment> getDefaultBrowserEnvironment(Long userId) {
        return browserEnvironmentRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    public Optional<BrowserEnvironment> getActiveBrowserEnvironment(Long userId) {
        return browserEnvironmentRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional
    public BrowserEnvironment createBrowserEnvironment(Long userId, BrowserEnvironment environment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        environment.setUser(user);
        
        // 如果这是第一个浏览器环境，设置为默认
        if (browserEnvironmentRepository.findByUserId(userId).isEmpty()) {
            environment.setIsDefault(true);
        }
        
        return browserEnvironmentRepository.save(environment);
    }

    @Override
    @Transactional
    public BrowserEnvironment updateBrowserEnvironment(Long id, BrowserEnvironment environmentDetails) {
        BrowserEnvironment environment = browserEnvironmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("浏览器环境不存在"));

        // 更新浏览器环境信息
        if (environmentDetails.getName() != null) {
            environment.setName(environmentDetails.getName());
        }
        if (environmentDetails.getIsActive() != null) {
            environment.setIsActive(environmentDetails.getIsActive());
        }
        if (environmentDetails.getFingerprint() != null) {
            environment.setFingerprint(environmentDetails.getFingerprint());
        }
        if (environmentDetails.getProxyConfig() != null) {
            environment.setProxyConfig(environmentDetails.getProxyConfig());
        }
        if (environmentDetails.getGeolocationLatitude() != null) {
            environment.setGeolocationLatitude(environmentDetails.getGeolocationLatitude());
        }
        if (environmentDetails.getGeolocationLongitude() != null) {
            environment.setGeolocationLongitude(environmentDetails.getGeolocationLongitude());
        }
        
        return browserEnvironmentRepository.save(environment);
    }

    @Override
    @Transactional
    public void deleteBrowserEnvironment(Long id) {
        BrowserEnvironment environment = browserEnvironmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("浏览器环境不存在"));
        
        // 如果删除的是默认浏览器环境，将另一个浏览器环境设置为默认
        if (environment.getIsDefault()) {
            List<BrowserEnvironment> otherEnvironments = browserEnvironmentRepository.findByUserId(environment.getUser().getId());
            if (!otherEnvironments.isEmpty()) {
                BrowserEnvironment newDefault = otherEnvironments.stream()
                        .filter(e -> !e.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                if (newDefault != null) {
                    newDefault.setIsDefault(true);
                    browserEnvironmentRepository.save(newDefault);
                }
            }
        }
        
        browserEnvironmentRepository.delete(environment);
    }

    @Override
    @Transactional
    public BrowserEnvironment setDefaultBrowserEnvironment(Long id, Long userId) {
        // 将所有浏览器环境的isDefault设置为false
        List<BrowserEnvironment> allEnvironments = browserEnvironmentRepository.findByUserId(userId);
        for (BrowserEnvironment environment : allEnvironments) {
            environment.setIsDefault(false);
        }
        browserEnvironmentRepository.saveAll(allEnvironments);
        
        // 将指定浏览器环境的isDefault设置为true
        BrowserEnvironment environment = browserEnvironmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("浏览器环境不存在"));
        environment.setIsDefault(true);
        
        return browserEnvironmentRepository.save(environment);
    }

    @Override
    public List<BrowserEnvironment> getAllBrowserEnvironments() {
        return browserEnvironmentRepository.findAll();
    }

    @Override
    public long count() {
        return browserEnvironmentRepository.count();
    }
}