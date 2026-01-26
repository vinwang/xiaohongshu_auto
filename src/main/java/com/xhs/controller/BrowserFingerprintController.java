package com.xhs.controller;

import com.xhs.entity.BrowserFingerprint;
import com.xhs.service.BrowserFingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/browser-fingerprints")
@RequiredArgsConstructor
public class BrowserFingerprintController {

    private final BrowserFingerprintService browserFingerprintService;

    // 获取用户的所有浏览器指纹
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BrowserFingerprint>> getAllBrowserFingerprints(@PathVariable Long userId) {
        List<BrowserFingerprint> fingerprints = browserFingerprintService.getAllBrowserFingerprints(userId);
        return ResponseEntity.ok(fingerprints);
    }

    // 获取单个浏览器指纹
    @GetMapping("/{id}")
    public ResponseEntity<BrowserFingerprint> getBrowserFingerprintById(@PathVariable Long id) {
        Optional<BrowserFingerprint> fingerprint = browserFingerprintService.getBrowserFingerprintById(id);
        return fingerprint.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 获取默认浏览器指纹
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<BrowserFingerprint> getDefaultBrowserFingerprint(@PathVariable Long userId) {
        Optional<BrowserFingerprint> fingerprint = browserFingerprintService.getDefaultBrowserFingerprint(userId);
        return fingerprint.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 获取活跃浏览器指纹
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<BrowserFingerprint> getActiveBrowserFingerprint(@PathVariable Long userId) {
        Optional<BrowserFingerprint> fingerprint = browserFingerprintService.getActiveBrowserFingerprint(userId);
        return fingerprint.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 创建浏览器指纹
    @PostMapping("/user/{userId}")
    public ResponseEntity<BrowserFingerprint> createBrowserFingerprint(@PathVariable Long userId, @RequestBody BrowserFingerprint fingerprint) {
        BrowserFingerprint createdFingerprint = browserFingerprintService.createBrowserFingerprint(userId, fingerprint);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFingerprint);
    }

    // 更新浏览器指纹
    @PutMapping("/{id}")
    public ResponseEntity<BrowserFingerprint> updateBrowserFingerprint(@PathVariable Long id, @RequestBody BrowserFingerprint fingerprintDetails) {
        BrowserFingerprint updatedFingerprint = browserFingerprintService.updateBrowserFingerprint(id, fingerprintDetails);
        return ResponseEntity.ok(updatedFingerprint);
    }

    // 删除浏览器指纹
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrowserFingerprint(@PathVariable Long id) {
        browserFingerprintService.deleteBrowserFingerprint(id);
        return ResponseEntity.noContent().build();
    }

    // 设置默认浏览器指纹
    @PutMapping("/{id}/default/user/{userId}")
    public ResponseEntity<BrowserFingerprint> setDefaultBrowserFingerprint(@PathVariable Long id, @PathVariable Long userId) {
        BrowserFingerprint fingerprint = browserFingerprintService.setDefaultBrowserFingerprint(id, userId);
        return ResponseEntity.ok(fingerprint);
    }
}