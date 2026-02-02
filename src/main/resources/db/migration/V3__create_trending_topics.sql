-- 热点数据表
CREATE TABLE `trending_topics` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category` VARCHAR(50) NOT NULL COMMENT '分类: weibo,baidu,toutiao,bilibili',
  `topic` VARCHAR(200) NOT NULL COMMENT '话题标题',
  `hot_score` INT COMMENT '热度值',
  `source` VARCHAR(50) NOT NULL COMMENT '数据来源',
  `trend_rank` INT COMMENT '排名',
  `metadata` JSON COMMENT '元数据',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category`),
  INDEX `idx_hot_score` (`hot_score`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_category_created` (`category`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热点数据表';