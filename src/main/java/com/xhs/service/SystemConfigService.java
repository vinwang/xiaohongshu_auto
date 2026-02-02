package com.xhs.service;

import com.xhs.entity.SystemConfig;
import com.xhs.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;
    private final TextEncryptor textEncryptor;

    public List<SystemConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    public Optional<SystemConfig> getConfigByKey(String configKey) {
        return configRepository.findFirstByConfigKeyOrderByUpdatedAtDesc(configKey);
    }

    public List<SystemConfig> getConfigsByType(String configType) {
        return configRepository.findByConfigTypeAndIsActive(configType, true);
    }

    public String getConfigValue(String configKey) {
        return getConfigByKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    /**
     * 获取配置值,如果配置键包含"api_key",则自动解密
     */
    public String getDecryptedConfigValue(String configKey) {
        String value = getConfigByKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
        if (value != null && configKey.contains("api_key")) {
            try {
                return textEncryptor.decrypt(value);
            } catch (Exception e) {
                log.error("解密配置失败: {}", e.getMessage(), e);
                return value;
            }
        }
        return value;
    }

    @Transactional
    public SystemConfig saveConfig(String configKey, String configValue, String configType, String description) {
        Optional<SystemConfig> existingConfig = configRepository.findFirstByConfigKeyOrderByUpdatedAtDesc(configKey);

        // 如果配置键包含"api_key",则加密存储
        String valueToStore = configValue;
        if (configKey.contains("api_key") && configValue != null && !configValue.isEmpty()) {
            try {
                valueToStore = textEncryptor.encrypt(configValue);
            } catch (Exception e) {
                log.error("加密配置失败: {}", e.getMessage(), e);
            }
        }

        if (existingConfig.isPresent()) {
            SystemConfig config = existingConfig.get();
            config.setConfigValue(valueToStore);
            config.setConfigType(configType);
            config.setDescription(description);
            return configRepository.save(config);
        } else {
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(valueToStore);
            newConfig.setConfigType(configType);
            newConfig.setDescription(description);
            newConfig.setIsActive(true);
            return configRepository.save(newConfig);
        }
    }

    @Transactional
    public SystemConfig updateConfig(Long id, String configValue) {
        return configRepository.findById(id)
                .map(config -> {
                    // 如果配置键包含"api_key",则加密存储
                    String valueToStore = configValue;
                    if (config.getConfigKey().contains("api_key") && configValue != null && !configValue.isEmpty()) {
                        try {
                            valueToStore = textEncryptor.encrypt(configValue);
                        } catch (Exception e) {
                            log.error("加密配置失败: {}", e.getMessage(), e);
                        }
                    }
                    config.setConfigValue(valueToStore);
                    return configRepository.save(config);
                })
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
    }

    @Transactional
    public void deleteConfig(Long id) {
        configRepository.deleteById(id);
    }

    public Map<String, String> getAIConfig(String provider) {
        String apiKey = getDecryptedConfigValue("ai." + provider + ".api_key");
        String modelName = getConfigValue("ai." + provider + ".model_name");
        String endpoint = getConfigValue("ai." + provider + ".endpoint");

        return Map.of(
                "provider", provider,
                "apiKey", apiKey != null ? apiKey : "",
                "modelName", modelName != null ? modelName : "",
                "endpoint", endpoint != null ? endpoint : ""
        );
    }

    public Map<String, String> getImageConfig() {
        String provider = getConfigValue("ai.image.provider");
        String apiKey = getDecryptedConfigValue("ai.image.api_key");
        String model = getConfigValue("ai.image.model");
        String endpoint = getConfigValue("ai.image.endpoint");

        return Map.of(
                "provider", provider != null ? provider : "openai",
                "apiKey", apiKey != null ? apiKey : "",
                "model", model != null ? model : "",
                "endpoint", endpoint != null ? endpoint : ""
        );
    }

    public Map<String, Map<String, String>> getAllAIConfigs() {
        Map<String, Map<String, String>> configs = new java.util.HashMap<>();
        String[] providers = {"kimi", "qwen", "openai", "volcengine"};

        for (String provider : providers) {
            configs.put(provider, getAIConfig(provider));
        }

        configs.put("image", getImageConfig());

        return configs;
    }

    @Transactional
    public void saveAIConfig(String provider, String apiKey, String modelName, String endpoint) {
        saveConfig("ai." + provider + ".api_key", apiKey, "AI", provider.toUpperCase() + " API密钥");
        saveConfig("ai." + provider + ".model_name", modelName, "AI", provider.toUpperCase() + " 模型名称");
        saveConfig("ai." + provider + ".endpoint", endpoint, "AI", provider.toUpperCase() + " 端点地址");
    }

    @Transactional
    public void saveImageConfig(String provider, String apiKey, String model, String endpoint) {
        saveConfig("ai.image.provider", provider, "AI", "生图 平台");
        saveConfig("ai.image.api_key", apiKey, "AI", "生图 API密钥");
        saveConfig("ai.image.model", model, "AI", "生图 模型名称");
        saveConfig("ai.image.endpoint", endpoint, "AI", "生图 端点地址");
    }
}