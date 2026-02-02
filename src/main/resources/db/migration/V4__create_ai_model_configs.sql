-- AI模型配置表
CREATE TABLE `ai_model_configs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `model_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '模型名称: kimi,qwen,openai,volcengine',
  `provider` VARCHAR(50) NOT NULL COMMENT '提供商: OPENAI,CLAUDE,LOCAL,VOLCENGINE',
  `api_endpoint` VARCHAR(255) COMMENT 'API端点URL',
  `api_key_encrypted` VARCHAR(255) COMMENT '加密的API密钥',
  `model_parameters` JSON COMMENT '模型参数配置',
  `rate_limit` INT DEFAULT 100 COMMENT '速率限制(每分钟请求数)',
  `cost_per_1k_tokens` DECIMAL(10,4) COMMENT '每1K token花费',
  `enabled` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_provider` (`provider`),
  INDEX `idx_enabled` (`enabled`),
  INDEX `idx_model_name` (`model_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';