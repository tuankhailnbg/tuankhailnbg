-- Migration: Tạo bảng admin_balance_credit và các index hỗ trợ cho chức năng Cộng số dư nạp tiền & Audit
-- Đảm bảo an toàn, idempotent (chạy nhiều lần không lỗi, không ghi đè dữ liệu cũ)

CREATE TABLE IF NOT EXISTS `admin_balance_credit` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `trans_code` VARCHAR(64) NOT NULL,
    `account_id` INT NOT NULL,
    `player_id` BIGINT DEFAULT NULL,
    `player_name` VARCHAR(50) DEFAULT NULL,
    `admin_username` VARCHAR(50) NOT NULL,
    `balance_before` INT NOT NULL,
    `amount` INT NOT NULL,
    `balance_after` INT NOT NULL,
    `tongnap_before` INT NOT NULL,
    `tongnap_after` INT NOT NULL,
    `mode` VARCHAR(20) NOT NULL DEFAULT 'DEPOSIT', -- 'DEPOSIT' (Chế độ A: Xác nhận nạp tiền) hoặc 'ADJUST' (Chế độ B: Điều chỉnh số dư)
    `note` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    UNIQUE KEY `uk_trans_code` (`trans_code`),
    KEY `idx_account_id` (`account_id`),
    KEY `idx_player_id` (`player_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
