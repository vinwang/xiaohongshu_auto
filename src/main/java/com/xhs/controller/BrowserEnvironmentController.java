package com.xhs.controller;

import com.xhs.entity.BrowserEnvironment;
import com.xhs.service.BrowserEnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/browser-environments")
@RequiredArgsConstructor
public class BrowserEnvironmentController {

    private final BrowserEnvironmentService browserEnvironmentService;

    // 获取所有浏览器环境
    @GetMapping
    public ResponseEntity<List<BrowserEnvironment>> getAllBrowserEnvironments() {
        // 返回第一个用户的所有环境作为示例
        List<BrowserEnvironment> environments = browserEnvironmentService.getAllBrowserEnvironments(1L);
        return ResponseEntity.ok(environments);
    }

    // 获取用户的所有浏览器环境
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BrowserEnvironment>> getAllBrowserEnvironmentsByUser(@PathVariable Long userId) {
        List<BrowserEnvironment> environments = browserEnvironmentService.getAllBrowserEnvironments(userId);
        return ResponseEntity.ok(environments);
    }

    // 获取单个浏览器环境
    @GetMapping("/{id}")
    public ResponseEntity<BrowserEnvironment> getBrowserEnvironmentById(@PathVariable Long id) {
        Optional<BrowserEnvironment> environment = browserEnvironmentService.getBrowserEnvironmentById(id);
        return environment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 获取默认浏览器环境
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<BrowserEnvironment> getDefaultBrowserEnvironment(@PathVariable Long userId) {
        Optional<BrowserEnvironment> environment = browserEnvironmentService.getDefaultBrowserEnvironment(userId);
        return environment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 获取活跃浏览器环境
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<BrowserEnvironment> getActiveBrowserEnvironment(@PathVariable Long userId) {
        Optional<BrowserEnvironment> environment = browserEnvironmentService.getActiveBrowserEnvironment(userId);
        return environment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 创建浏览器环境
    @PostMapping("/user/{userId}")
    public ResponseEntity<BrowserEnvironment> createBrowserEnvironment(@PathVariable Long userId, @RequestBody BrowserEnvironment environment) {
        BrowserEnvironment createdEnvironment = browserEnvironmentService.createBrowserEnvironment(userId, environment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEnvironment);
    }

    // 更新浏览器环境
    @PutMapping("/{id}")
    public ResponseEntity<BrowserEnvironment> updateBrowserEnvironment(@PathVariable Long id, @RequestBody BrowserEnvironment environmentDetails) {
        BrowserEnvironment updatedEnvironment = browserEnvironmentService.updateBrowserEnvironment(id, environmentDetails);
        return ResponseEntity.ok(updatedEnvironment);
    }

    // 删除浏览器环境
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrowserEnvironment(@PathVariable Long id) {
        browserEnvironmentService.deleteBrowserEnvironment(id);
        return ResponseEntity.noContent().build();
    }

    // 设置默认浏览器环境
    @PutMapping("/{id}/default/user/{userId}")
    public ResponseEntity<BrowserEnvironment> setDefaultBrowserEnvironment(@PathVariable Long id, @PathVariable Long userId) {
        BrowserEnvironment environment = browserEnvironmentService.setDefaultBrowserEnvironment(id, userId);
        return ResponseEntity.ok(environment);
    }
}