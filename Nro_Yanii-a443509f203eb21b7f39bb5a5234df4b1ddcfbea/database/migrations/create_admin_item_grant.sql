-- Migration: Create admin_item_grant table for queuing offline admin item grants
CREATE TABLE IF NOT EXISTS `admin_item_grant` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `target_player_id` BIGINT NOT NULL,
  `admin_player_id` BIGINT DEFAULT NULL,
  `item_template_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `options_json` TEXT,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `created_at` DATETIME NOT NULL,
  `delivered_at` DATETIME DEFAULT NULL,
  KEY `idx_target_player_status` (`target_player_id`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
