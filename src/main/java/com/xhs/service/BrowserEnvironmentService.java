package com.xhs.service;

import com.xhs.entity.BrowserEnvironment;

import java.util.List;
import java.util.Optional;

public interface BrowserEnvironmentService {

    List<BrowserEnvironment> getAllBrowserEnvironments(Long userId);
    Optional<BrowserEnvironment> getBrowserEnvironmentById(Long id);
    Optional<BrowserEnvironment> getDefaultBrowserEnvironment(Long userId);
    Optional<BrowserEnvironment> getActiveBrowserEnvironment(Long userId);
    BrowserEnvironment createBrowserEnvironment(Long userId, BrowserEnvironment environment);
    BrowserEnvironment updateBrowserEnvironment(Long id, BrowserEnvironment environmentDetails);
    void deleteBrowserEnvironment(Long id);
    BrowserEnvironment setDefaultBrowserEnvironment(Long id, Long userId);
    List<BrowserEnvironment> getAllBrowserEnvironments();
    long count();
}