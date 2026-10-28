-- Chuyển sang điểm giới thiệu cấp thủ công từ admin (sự kiện vĩnh viễn).
-- Chạy sau database/migrations/20261028_referral_event_kame.sql.
-- Giữ lịch sử referral_invites cũ nhưng KHÔNG tự cộng điểm từ các lượt đó.
CREATE TABLE IF NOT EXISTS referral_points (
    player_id BIGINT NOT NULL PRIMARY KEY,
    points INT UNSIGNED NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS referral_points_audit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    admin_player_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    delta_points INT NOT NULL,
    previous_points INT NOT NULL,
    current_points INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_referral_player (player_id,created_at),
    KEY idx_referral_admin (admin_player_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
