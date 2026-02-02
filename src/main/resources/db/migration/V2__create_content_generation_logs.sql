-- 内容生成记录表
CREATE TABLE `content_generation_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `prompt` TEXT NOT NULL COMMENT '输入提示词',
  `generated_content` JSON COMMENT '生成的内容JSON',
  `model_name` VARCHAR(50) COMMENT '使用的模型名称',
  `tokens_used` INT COMMENT '使用的token数量',
  `cost` DECIMAL(10,4) COMMENT '花费金额',
  `generation_time` INT COMMENT '生成耗时(毫秒)',
  `status` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT 'SUCCESS,FAILED',
  `error_message` TEXT COMMENT '错误信息',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_model` (`model_name`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容生成记录表';