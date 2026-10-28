-- NRO - Su kien dua Top 4 bang
-- POWER: Suc manh
-- TASK : Nhiem vu
-- PET  : Suc manh de tu
-- NAP  : Nap trong mua
--
-- Import file nay 1 lan vao database ngocrong.
-- KHONG reset account.tongnap. Moi mua se luu baseline de tinh phan nap tang them.

CREATE TABLE IF NOT EXISTS top_race_season (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    finalized_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_top_race_season_status (status, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS top_race_nap_baseline (
    season_id INT NOT NULL,
    account_id INT NOT NULL,
    baseline_tongnap BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (season_id, account_id),
    KEY idx_top_race_nap_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS top_race_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    season_id INT NOT NULL,
    race_type VARCHAR(16) NOT NULL,
    rank_no INT NOT NULL,
    player_id INT NOT NULL,
    account_id INT NOT NULL,
    score BIGINT NOT NULL DEFAULT 0,
    detail VARCHAR(255) DEFAULT NULL,
    reward_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=chua nhan,1=da nhan,2=khong du dieu kien',
    claimed_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_top_race_result_rank (season_id, race_type, rank_no),
    UNIQUE KEY uk_top_race_result_player (season_id, race_type, player_id),
    KEY idx_top_race_result_claim (season_id, player_id, reward_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS top_race_reward_config (
    id INT NOT NULL AUTO_INCREMENT,
    race_type VARCHAR(16) NOT NULL,
    rank_from INT NOT NULL,
    rank_to INT NOT NULL,
    gold_bar INT NOT NULL DEFAULT 0,
    ruby INT NOT NULL DEFAULT 0,
    costume_item_id INT NOT NULL DEFAULT -1,
    costume_options TEXT DEFAULT NULL COMMENT 'JSON [[optionId,param],...]',
    title_item_id INT NOT NULL DEFAULT -1,
    title_options TEXT DEFAULT NULL COMMENT 'JSON [[optionId,param],...]',
    min_score BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_top_race_reward (race_type, rank_from, rank_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Xoa cau hinh mau cu de import lai file nay khong bi trung.
DELETE FROM top_race_reward_config WHERE race_type IN ('POWER','TASK','PET','NAP');

-- =========================================================
-- 3 BANG TOP THUONG: POWER / TASK / PET
-- Cai trang:
-- 1: 1317 Super Black Goku Rose
-- 2: 1316 Fusion Zamasu
-- 3: 1276 Zamasu Toi Thuong
-- 4-5: 1304 Goku SSJ4
-- 6-10: 1305 Vegeta SSJ4
--
-- Danh hieu:
-- 1: 1294 De Vuong
-- 2: 1295 Bo La Trum
-- 3: 1300 Fan Cung
-- =========================================================

INSERT INTO top_race_reward_config
(race_type,rank_from,rank_to,gold_bar,ruby,costume_item_id,costume_options,title_item_id,title_options,min_score)
VALUES
('POWER',1,1,1000,20000,1317,'[[77,32],[103,32],[50,32],[14,10],[5,15],[108,5],[94,5],[106,0],[30,0]]',1294,'[[77,14],[103,14],[50,14],[14,5],[5,7],[30,0]]',0),
('POWER',2,2,700,15000,1316,'[[77,30],[103,30],[50,30],[14,5],[5,10],[106,0],[30,0]]',1295,'[[77,12],[103,12],[50,12],[14,4],[5,5],[30,0]]',0),
('POWER',3,3,500,10000,1276,'[[77,27],[103,27],[50,27],[14,5],[30,0]]',1300,'[[77,10],[103,10],[50,10],[14,3],[5,4],[30,0]]',0),
('POWER',4,5,350,7000,1304,'[[77,24],[103,24],[50,24],[14,3],[30,0]]',-1,NULL,0),
('POWER',6,10,250,5000,1305,'[[77,20],[103,20],[50,20],[14,2],[30,0]]',-1,NULL,0),
('POWER',11,20,150,3000,-1,NULL,-1,NULL,0),
('POWER',21,50,75,1500,-1,NULL,-1,NULL,0),

('TASK',1,1,1000,20000,1317,'[[77,32],[103,32],[50,32],[14,10],[5,15],[108,5],[94,5],[106,0],[30,0]]',1294,'[[77,14],[103,14],[50,14],[14,5],[5,7],[30,0]]',0),
('TASK',2,2,700,15000,1316,'[[77,30],[103,30],[50,30],[14,5],[5,10],[106,0],[30,0]]',1295,'[[77,12],[103,12],[50,12],[14,4],[5,5],[30,0]]',0),
('TASK',3,3,500,10000,1276,'[[77,27],[103,27],[50,27],[14,5],[30,0]]',1300,'[[77,10],[103,10],[50,10],[14,3],[5,4],[30,0]]',0),
('TASK',4,5,350,7000,1304,'[[77,24],[103,24],[50,24],[14,3],[30,0]]',-1,NULL,0),
('TASK',6,10,250,5000,1305,'[[77,20],[103,20],[50,20],[14,2],[30,0]]',-1,NULL,0),
('TASK',11,20,150,3000,-1,NULL,-1,NULL,0),
('TASK',21,50,75,1500,-1,NULL,-1,NULL,0),

('PET',1,1,1000,20000,1317,'[[77,32],[103,32],[50,32],[14,10],[5,15],[108,5],[94,5],[106,0],[30,0]]',1294,'[[77,14],[103,14],[50,14],[14,5],[5,7],[30,0]]',0),
('PET',2,2,700,15000,1316,'[[77,30],[103,30],[50,30],[14,5],[5,10],[106,0],[30,0]]',1295,'[[77,12],[103,12],[50,12],[14,4],[5,5],[30,0]]',0),
('PET',3,3,500,10000,1276,'[[77,27],[103,27],[50,27],[14,5],[30,0]]',1300,'[[77,10],[103,10],[50,10],[14,3],[5,4],[30,0]]',0),
('PET',4,5,350,7000,1304,'[[77,24],[103,24],[50,24],[14,3],[30,0]]',-1,NULL,0),
('PET',6,10,250,5000,1305,'[[77,20],[103,20],[50,20],[14,2],[30,0]]',-1,NULL,0),
('PET',11,20,150,3000,-1,NULL,-1,NULL,0),
('PET',21,50,75,1500,-1,NULL,-1,NULL,0);

-- =========================================================
-- TOP NAP
-- Cai trang:
-- 1: 2117 Goku Ultra
-- 2: 2045 Cumber
-- 3: 2115 Kid Bill
-- 4-5: 2120 Kid Mabu
-- 6-10: 2119 Kid Sen
--
-- Top 1 phai nap toi thieu 50.000 VND trong mua.
-- =========================================================

INSERT INTO top_race_reward_config
(race_type,rank_from,rank_to,gold_bar,ruby,costume_item_id,costume_options,title_item_id,title_options,min_score)
VALUES
('NAP',1,1,1500,30000,2117,'[[77,36],[103,36],[50,36],[14,12],[5,20],[108,7],[94,7],[106,0],[30,0]]',1293,'[[77,18],[103,18],[50,18],[14,7],[5,10],[30,0]]',50000),
('NAP',2,2,1000,22000,2045,'[[77,34],[103,34],[50,34],[14,10],[5,18],[108,6],[94,6],[106,0],[30,0]]',1308,'[[77,16],[103,16],[50,16],[14,6],[5,8],[30,0]]',0),
('NAP',3,3,750,16000,2115,'[[77,32],[103,32],[50,32],[14,8],[5,15],[108,5],[94,5],[106,0],[30,0]]',1294,'[[77,14],[103,14],[50,14],[14,5],[5,7],[30,0]]',0),
('NAP',4,5,500,11000,2120,'[[77,29],[103,29],[50,29],[14,6],[5,12],[108,4],[94,4],[106,0],[30,0]]',-1,NULL,0),
('NAP',6,10,350,7000,2119,'[[77,26],[103,26],[50,26],[14,5],[5,10],[108,3],[94,3],[106,0],[30,0]]',-1,NULL,0),
('NAP',11,20,200,4000,-1,NULL,-1,NULL,0),
('NAP',21,50,100,2000,-1,NULL,-1,NULL,0);
