package com.xhs.service;

import com.xhs.entity.BrowserFingerprint;

import java.util.List;
import java.util.Optional;

public interface BrowserFingerprintService {

    List<BrowserFingerprint> getAllBrowserFingerprints(Long userId);
    Optional<BrowserFingerprint> getBrowserFingerprintById(Long id);
    Optional<BrowserFingerprint> getDefaultBrowserFingerprint(Long userId);
    Optional<BrowserFingerprint> getActiveBrowserFingerprint(Long userId);
    BrowserFingerprint createBrowserFingerprint(Long userId, BrowserFingerprint fingerprint);
    BrowserFingerprint updateBrowserFingerprint(Long id, BrowserFingerprint fingerprintDetails);
    void deleteBrowserFingerprint(Long id);
    BrowserFingerprint setDefaultBrowserFingerprint(Long id, Long userId);
    List<BrowserFingerprint> getAllBrowserFingerprints();
    long count();
}