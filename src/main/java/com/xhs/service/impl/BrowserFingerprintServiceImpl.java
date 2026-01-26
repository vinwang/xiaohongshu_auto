package com.xhs.service.impl;

import com.xhs.entity.BrowserFingerprint;
import com.xhs.entity.User;
import com.xhs.repository.BrowserFingerprintRepository;
import com.xhs.repository.UserRepository;
import com.xhs.service.BrowserFingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrowserFingerprintServiceImpl implements BrowserFingerprintService {

    private final BrowserFingerprintRepository browserFingerprintRepository;
    private final UserRepository userRepository;

    @Override
    public List<BrowserFingerprint> getAllBrowserFingerprints(Long userId) {
        return browserFingerprintRepository.findByUserId(userId);
    }

    @Override
    public Optional<BrowserFingerprint> getBrowserFingerprintById(Long id) {
        return browserFingerprintRepository.findById(id);
    }

    @Override
    public Optional<BrowserFingerprint> getDefaultBrowserFingerprint(Long userId) {
        return browserFingerprintRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    public Optional<BrowserFingerprint> getActiveBrowserFingerprint(Long userId) {
        return browserFingerprintRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional
    public BrowserFingerprint createBrowserFingerprint(Long userId, BrowserFingerprint fingerprint) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        fingerprint.setUser(user);
        
        // 如果这是第一个浏览器指纹，设置为默认
        if (browserFingerprintRepository.findByUserId(userId).isEmpty()) {
            fingerprint.setIsDefault(true);
        }
        
        return browserFingerprintRepository.save(fingerprint);
    }

    @Override
    @Transactional
    public BrowserFingerprint updateBrowserFingerprint(Long id, BrowserFingerprint fingerprintDetails) {
        BrowserFingerprint fingerprint = browserFingerprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("浏览器指纹不存在"));

        // 更新浏览器指纹信息
        if (fingerprintDetails.getName() != null) {
            fingerprint.setName(fingerprintDetails.getName());
        }
        if (fingerprintDetails.getUserAgent() != null) {
            fingerprint.setUserAgent(fingerprintDetails.getUserAgent());
        }
        if (fingerprintDetails.getViewportWidth() != null) {
            fingerprint.setViewportWidth(fingerprintDetails.getViewportWidth());
        }
        if (fingerprintDetails.getViewportHeight() != null) {
            fingerprint.setViewportHeight(fingerprintDetails.getViewportHeight());
        }
        if (fingerprintDetails.getScreenWidth() != null) {
            fingerprint.setScreenWidth(fingerprintDetails.getScreenWidth());
        }
        if (fingerprintDetails.getScreenHeight() != null) {
            fingerprint.setScreenHeight(fingerprintDetails.getScreenHeight());
        }
        if (fingerprintDetails.getPlatform() != null) {
            fingerprint.setPlatform(fingerprintDetails.getPlatform());
        }
        if (fingerprintDetails.getTimezone() != null) {
            fingerprint.setTimezone(fingerprintDetails.getTimezone());
        }
        if (fingerprintDetails.getLocale() != null) {
            fingerprint.setLocale(fingerprintDetails.getLocale());
        }
        if (fingerprintDetails.getWebglVendor() != null) {
            fingerprint.setWebglVendor(fingerprintDetails.getWebglVendor());
        }
        if (fingerprintDetails.getWebglRenderer() != null) {
            fingerprint.setWebglRenderer(fingerprintDetails.getWebglRenderer());
        }
        if (fingerprintDetails.getCanvasFingerprint() != null) {
            fingerprint.setCanvasFingerprint(fingerprintDetails.getCanvasFingerprint());
        }
        if (fingerprintDetails.getWebrtcPublicIp() != null) {
            fingerprint.setWebrtcPublicIp(fingerprintDetails.getWebrtcPublicIp());
        }
        if (fingerprintDetails.getWebrtcLocalIp() != null) {
            fingerprint.setWebrtcLocalIp(fingerprintDetails.getWebrtcLocalIp());
        }
        if (fingerprintDetails.getFonts() != null) {
            fingerprint.setFonts(fingerprintDetails.getFonts());
        }
        if (fingerprintDetails.getPlugins() != null) {
            fingerprint.setPlugins(fingerprintDetails.getPlugins());
        }
        if (fingerprintDetails.getIsActive() != null) {
            fingerprint.setIsActive(fingerprintDetails.getIsActive());
        }
        
        return browserFingerprintRepository.save(fingerprint);
    }

    @Override
    @Transactional
    public void deleteBrowserFingerprint(Long id) {
        BrowserFingerprint fingerprint = browserFingerprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("浏览器指纹不存在"));
        
        // 如果删除的是默认浏览器指纹，将另一个浏览器指纹设置为默认
        if (fingerprint.getIsDefault()) {
            List<BrowserFingerprint> otherFingerprints = browserFingerprintRepository.findByUserId(fingerprint.getUser().getId());
            if (!otherFingerprints.isEmpty()) {
                BrowserFingerprint newDefault = otherFingerprints.stream()
                        .filter(f -> !f.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                if (newDefault != null) {
                    newDefault.setIsDefault(true);
                    browserFingerprintRepository.save(newDefault);
                }
            }
        }
        
        browserFingerprintRepository.delete(fingerprint);
    }

    @Override
    @Transactional
    public BrowserFingerprint setDefaultBrowserFingerprint(Long id, Long userId) {
        // 将所有浏览器指纹的isDefault设置为false
        List<BrowserFingerprint> allFingerprints = browserFingerprintRepository.findByUserId(userId);
        for (BrowserFingerprint fingerprint : allFingerprints) {
            fingerprint.setIsDefault(false);
        }
        browserFingerprintRepository.saveAll(allFingerprints);
        
        // 将指定浏览器指纹的isDefault设置为true
        BrowserFingerprint fingerprint = browserFingerprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("浏览器指纹不存在"));
        fingerprint.setIsDefault(true);
        
        return browserFingerprintRepository.save(fingerprint);
    }

    @Override
    public List<BrowserFingerprint> getAllBrowserFingerprints() {
        return browserFingerprintRepository.findAll();
    }

    @Override
    public long count() {
        return browserFingerprintRepository.count();
    }
}