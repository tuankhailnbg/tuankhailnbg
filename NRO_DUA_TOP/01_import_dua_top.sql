-- NRO - Su kien Dua Top 2026
-- Import file nay vao database ngocrong TRUOC KHI bat dau cho nguoi choi nap/cay top.
-- Mua mac dinh: 14 ngay dua top + 7 ngay nhan thuong.
-- Co the sua start_time/end_time/claim_end trong bang dua_top_event sau khi import.

CREATE TABLE IF NOT EXISTS dua_top_event (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  claim_end DATETIME NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0=draft,1=active,2=finalized,3=closed',
  min_top1_recharge INT NOT NULL DEFAULT 50000,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dua_top_event_status (status,start_time,end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dua_top_recharge_baseline (
  event_id INT NOT NULL,
  account_id INT NOT NULL,
  tongnap_start BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (event_id,account_id),
  KEY idx_dua_top_baseline_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dua_top_reward (
  id INT NOT NULL AUTO_INCREMENT,
  event_id INT NOT NULL,
  category VARCHAR(20) NOT NULL COMMENT 'POWER,TASK,PET,RECHARGE',
  rank_from INT NOT NULL,
  rank_to INT NOT NULL,
  gold_bar INT NOT NULL DEFAULT 0,
  ruby INT NOT NULL DEFAULT 0,
  costume_id INT NOT NULL DEFAULT -1,
  costume_options VARCHAR(500) NOT NULL DEFAULT '',
  title_id INT NOT NULL DEFAULT -1,
  title_options VARCHAR(500) NOT NULL DEFAULT '',
  PRIMARY KEY (id),
  UNIQUE KEY uq_dua_top_reward (event_id,category,rank_from,rank_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dua_top_result (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_id INT NOT NULL,
  category VARCHAR(20) NOT NULL,
  rank_no INT NOT NULL,
  player_id INT NOT NULL,
  account_id INT NOT NULL,
  score BIGINT NOT NULL DEFAULT 0,
  detail VARCHAR(120) NOT NULL DEFAULT '',
  eligible TINYINT NOT NULL DEFAULT 1,
  claimed TINYINT NOT NULL DEFAULT 0,
  claimed_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dua_top_result_rank (event_id,category,rank_no),
  UNIQUE KEY uq_dua_top_result_player (event_id,category,player_id),
  KEY idx_dua_top_result_claim (event_id,player_id,claimed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO dua_top_event(name,start_time,end_time,claim_end,status,min_top1_recharge)
SELECT 'Dua Top Mua 1 - 2026', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), 1, 50000
WHERE NOT EXISTS (
  SELECT 1 FROM dua_top_event WHERE name='Dua Top Mua 1 - 2026'
);

SET @DUA_TOP_EVENT_ID := (
  SELECT id FROM dua_top_event WHERE name='Dua Top Mua 1 - 2026' ORDER BY id DESC LIMIT 1
);

DELETE FROM dua_top_reward WHERE event_id=@DUA_TOP_EVENT_ID;

INSERT INTO dua_top_reward
(event_id,category,rank_from,rank_to,gold_bar,ruby,costume_id,costume_options,title_id,title_options)
VALUES
(@DUA_TOP_EVENT_ID,'POWER',1,1,1000,20000,2147,'77:32;103:32;50:32;14:10;5:15;108:5;94:5;106:1;30:0',1295,'77:14;103:14;50:14;14:5;5:7;30:0'),
(@DUA_TOP_EVENT_ID,'POWER',2,2,700,15000,2146,'77:30;103:30;50:30;14:5;5:10;106:1;30:0',1294,'77:12;103:12;50:12;14:4;5:5;30:0'),
(@DUA_TOP_EVENT_ID,'POWER',3,3,500,10000,2145,'77:27;103:27;50:27;14:5;30:0',1300,'77:10;103:10;50:10;14:3;5:4;30:0'),
(@DUA_TOP_EVENT_ID,'POWER',4,5,350,7000,2142,'77:24;103:24;50:24;14:4;5:5;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'POWER',6,10,250,5000,1304,'77:20;103:20;50:20;14:3;5:3;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'POWER',11,20,150,3000,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'POWER',21,50,75,1500,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'TASK',1,1,1000,20000,2147,'77:32;103:32;50:32;14:10;5:15;108:5;94:5;106:1;30:0',1296,'77:14;103:14;50:14;14:5;5:7;30:0'),
(@DUA_TOP_EVENT_ID,'TASK',2,2,700,15000,2146,'77:30;103:30;50:30;14:5;5:10;106:1;30:0',1307,'77:12;103:12;50:12;14:4;5:5;30:0'),
(@DUA_TOP_EVENT_ID,'TASK',3,3,500,10000,2145,'77:27;103:27;50:27;14:5;30:0',1301,'77:10;103:10;50:10;14:3;5:4;30:0'),
(@DUA_TOP_EVENT_ID,'TASK',4,5,350,7000,2142,'77:24;103:24;50:24;14:4;5:5;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'TASK',6,10,250,5000,1304,'77:20;103:20;50:20;14:3;5:3;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'TASK',11,20,150,3000,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'TASK',21,50,75,1500,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'PET',1,1,1000,20000,2147,'77:32;103:32;50:32;14:10;5:15;108:5;94:5;106:1;30:0',1299,'77:14;103:14;50:14;14:5;5:7;30:0'),
(@DUA_TOP_EVENT_ID,'PET',2,2,700,15000,2146,'77:30;103:30;50:30;14:5;5:10;106:1;30:0',1292,'77:12;103:12;50:12;14:4;5:5;30:0'),
(@DUA_TOP_EVENT_ID,'PET',3,3,500,10000,2145,'77:27;103:27;50:27;14:5;30:0',1308,'77:10;103:10;50:10;14:3;5:4;30:0'),
(@DUA_TOP_EVENT_ID,'PET',4,5,350,7000,2142,'77:24;103:24;50:24;14:4;5:5;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'PET',6,10,250,5000,1304,'77:20;103:20;50:20;14:3;5:3;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'PET',11,20,150,3000,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'PET',21,50,75,1500,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'RECHARGE',1,1,1500,30000,1317,'77:36;103:36;50:36;14:12;5:20;108:7;94:7;106:1;30:0',1293,'77:18;103:18;50:18;14:7;5:10;30:0'),
(@DUA_TOP_EVENT_ID,'RECHARGE',2,2,1000,22000,1316,'77:34;103:34;50:34;14:10;5:18;108:6;94:6;106:1;30:0',1294,'77:16;103:16;50:16;14:6;5:8;30:0'),
(@DUA_TOP_EVENT_ID,'RECHARGE',3,3,750,16000,1315,'77:32;103:32;50:32;14:8;5:15;108:5;94:5;106:1;30:0',1308,'77:14;103:14;50:14;14:5;5:7;30:0'),
(@DUA_TOP_EVENT_ID,'RECHARGE',4,5,500,11000,1306,'77:29;103:29;50:29;14:6;5:12;108:4;94:4;106:1;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'RECHARGE',6,10,350,7000,1305,'77:26;103:26;50:26;14:5;5:10;108:3;94:3;106:1;30:0',-1,''),
(@DUA_TOP_EVENT_ID,'RECHARGE',11,20,200,4000,-1,'',-1,''),
(@DUA_TOP_EVENT_ID,'RECHARGE',21,50,100,2000,-1,'',-1,'');

-- Snapshot tongnap ngay luc import de Top Nap chi tinh so nap PHAT SINH TRONG MUA.
INSERT IGNORE INTO dua_top_recharge_baseline(event_id,account_id,tongnap_start)
SELECT @DUA_TOP_EVENT_ID, id, GREATEST(COALESCE(tongnap,0),0)
FROM account;

-- Kiem tra sau import
SELECT id,name,start_time,end_time,claim_end,status,min_top1_recharge
FROM dua_top_event WHERE id=@DUA_TOP_EVENT_ID;

SELECT category,rank_from,rank_to,gold_bar,ruby,costume_id,title_id
FROM dua_top_reward WHERE event_id=@DUA_TOP_EVENT_ID
ORDER BY FIELD(category,'POWER','TASK','PET','RECHARGE'),rank_from;

SELECT COUNT(*) AS baseline_accounts
FROM dua_top_recharge_baseline WHERE event_id=@DUA_TOP_EVENT_ID;
