-- NRO: Sự kiện giới thiệu bạn bè vĩnh viễn (map 5).
-- Chạy trên database ngocrong ĐÚNG server (backup DB trước).
-- Không sửa account.gioithieu/xacnhan_gioitheu cũ; dữ liệu mới độc lập.
CREATE TABLE IF NOT EXISTS referral_invites (
    invited_account_id INT NOT NULL PRIMARY KEY,
    invited_player_id BIGINT NOT NULL,
    inviter_player_id BIGINT NOT NULL,
    referral_code VARCHAR(32) NOT NULL,
    active_seconds INT UNSIGNED NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=đang làm, 1=đủ điều kiện',
    review_required TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    counted_at TIMESTAMP NULL DEFAULT NULL,
    KEY idx_inviter_status (inviter_player_id, status),
    KEY idx_invited_player (invited_player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS referral_claims (
    inviter_player_id BIGINT NOT NULL,
    milestone INT NOT NULL,
    gold_bars INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=chờ khôi phục, 1=đã phát',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    claimed_at TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (inviter_player_id, milestone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mốc cải trang giữ lại để cấu hình ID item_template thực tế:
-- 5: Goku SSJ3 (7 ngày); 10: Vegito Blue (7 ngày);
-- 15: Gogeta Blue (vĩnh viễn); 20: Zeno (vĩnh viễn).
-- Cần bổ sung sprite/item_template và xử lý thời hạn trước khi mở nhận cải trang.
CREATE TABLE IF NOT EXISTS referral_costume_catalog (
    milestone INT NOT NULL PRIMARY KEY,
    item_template_id INT NULL,
    costume_name VARCHAR(100) NOT NULL,
    duration_days INT NULL COMMENT 'NULL=vĩnh viễn',
    options_json TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO referral_costume_catalog (milestone,item_template_id,costume_name,duration_days,options_json) VALUES
(5,NULL,'Goku SSJ3',7,'[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'),
(10,NULL,'Vegito Blue',7,'[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'),
(15,NULL,'Gogeta Blue',NULL,'[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'),
(20,NULL,'Zeno',NULL,'[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]');

CREATE TABLE IF NOT EXISTS referral_costume_claims (
    inviter_player_id BIGINT NOT NULL,
    milestone INT NOT NULL,
    item_template_id INT NOT NULL,
    claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (inviter_player_id,milestone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
