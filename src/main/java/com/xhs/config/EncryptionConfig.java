package com.xhs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * 加密配置
 * 用于加密/解密敏感数据(如API密钥)
 */
@Configuration
public class EncryptionConfig {

    /**
     * 文本加密器
     * 使用AES-256加密算法
     * 密钥从环境变量读取,默认值为"xhs-ai-publisher-secret-key-2024"
     */
    @Bean
    public TextEncryptor textEncryptor() {
        String password = System.getenv("ENCRYPTION_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = "xhs-ai-publisher-secret-key-2024";
        }
        // 使用有效的十六进制字符串作为salt
        String salt = "a1b2c3d4e5f6";
        return Encryptors.text(password, salt);
    }
}