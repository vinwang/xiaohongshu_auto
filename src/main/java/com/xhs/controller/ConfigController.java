package com.xhs.controller;

import com.xhs.dto.SaveAIConfigRequest;
import com.xhs.dto.SaveImageConfigRequest;
import com.xhs.entity.SystemConfig;
import com.xhs.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final SystemConfigService configService;

    @GetMapping
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @GetMapping("/{configKey}")
    public ResponseEntity<SystemConfig> getConfigByKey(@PathVariable String configKey) {
        return configService.getConfigByKey(configKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{configType}")
    public ResponseEntity<List<SystemConfig>> getConfigsByType(@PathVariable String configType) {
        return ResponseEntity.ok(configService.getConfigsByType(configType));
    }

    @PostMapping
    public ResponseEntity<SystemConfig> saveConfig(@RequestBody Map<String, String> request) {
        String configKey = request.get("configKey");
        String configValue = request.get("configValue");
        String configType = request.get("configType");
        String description = request.get("description");

        if (configKey == null || configValue == null) {
            return ResponseEntity.badRequest().build();
        }

        SystemConfig savedConfig = configService.saveConfig(configKey, configValue, configType, description);
        return ResponseEntity.ok(savedConfig);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemConfig> updateConfig(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String configValue = request.get("configValue");

        if (configValue == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SystemConfig updatedConfig = configService.updateConfig(id, configValue);
            return ResponseEntity.ok(updatedConfig);
        } catch (RuntimeException e) {
            log.error("更新配置失败: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ai/config")
    public ResponseEntity<Map<String, Map<String, String>>> getAIConfig() {
        return ResponseEntity.ok(configService.getAllAIConfigs());
    }

    @GetMapping("/ai/config/{provider}")
    public ResponseEntity<Map<String, String>> getAIConfigByProvider(@PathVariable String provider) {
        return ResponseEntity.ok(configService.getAIConfig(provider));
    }

    @PostMapping("/ai/config")
    public ResponseEntity<Map<String, Object>> saveAIConfig(
            @Valid @RequestBody SaveAIConfigRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errors
            ));
        }

        try {
            configService.saveAIConfig(
                    request.getProvider(),
                    request.getApiKey(),
                    request.getModelName(),
                    request.getEndpoint()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "AI配置已保存"
            ));
        } catch (Exception e) {
            log.error("保存AI配置失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "保存失败"
            ));
        }
    }

    @PostMapping("/ai/image-config")
    public ResponseEntity<Map<String, Object>> saveImageConfig(
            @Valid @RequestBody SaveImageConfigRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errors
            ));
        }

        try {
            configService.saveImageConfig(
                    request.getProvider(),
                    request.getApiKey(),
                    request.getModel(),
                    request.getEndpoint()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "生图配置已保存"
            ));
        } catch (Exception e) {
            log.error("保存生图配置失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "保存失败"
            ));
        }
    }
}