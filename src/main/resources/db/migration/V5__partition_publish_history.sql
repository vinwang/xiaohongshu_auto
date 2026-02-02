-- 发布历史表分区配置
-- 按年份分区,便于数据管理和查询优化

-- 首先检查表是否存在,如果存在则先删除(仅用于开发环境)
-- DROP TABLE IF EXISTS `publish_history`;

-- 创建发布历史表(带分区)
CREATE TABLE `publish_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `cover_url` VARCHAR(500),
  `tags` VARCHAR(500),
  `platform` VARCHAR(50) NOT NULL COMMENT '发布平台',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING,SUCCESS,FAILED',
  `error_message` TEXT,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`, `created_at`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_platform` (`platform`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p_max VALUES LESS THAN MAXVALUE
) COMMENT='发布历史表(按年份分区)';

-- 创建自动添加分区的存储过程
DELIMITER $$

CREATE PROCEDURE add_publish_history_partition()
BEGIN
    DECLARE current_year INT;
    DECLARE partition_name VARCHAR(20);
    DECLARE partition_value INT;

    SET current_year = YEAR(CURRENT_DATE);
    SET partition_name = CONCAT('p', current_year);
    SET partition_value = current_year + 1;

    -- 检查分区是否已存在
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.partitions
        WHERE table_schema = DATABASE()
        AND table_name = 'publish_history'
        AND partition_name = partition_name
    ) THEN
        -- 重建分区表(需要删除并重建所有分区)
        SET @sql = CONCAT('ALTER TABLE publish_history REORGANIZE PARTITION p_max INTO (
            PARTITION ', partition_name, ' VALUES LESS THAN (', partition_value, '),
            PARTITION p_max VALUES LESS THAN MAXVALUE
        )');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 创建事件调度器(每年1月1日自动添加新分区)
CREATE EVENT IF NOT EXISTS auto_add_partition
ON SCHEDULE EVERY 1 YEAR
STARTS CONCAT(CURRENT_DATE + INTERVAL 1 YEAR - INTERVAL DAYOFYEAR(CURRENT_DATE) - 1 DAY, ' 00:00:00')
DO CALL add_publish_history_partition();

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;