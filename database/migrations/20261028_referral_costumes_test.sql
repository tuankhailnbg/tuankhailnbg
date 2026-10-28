-- NRO: cai trang moc gioi thieu - ban TUONG THICH CLIENT.
-- KHONG tao item_template moi 2154..2157 nua.
-- Ly do: client hien tai co the khong co template item moi, dan den icon trong va lag/do khi bam item.
-- Dung cac item TYPE=5 da ton tai trong client/database goc:
--   moc 5  -> 904  (Cai trang Black Goku SSJ White)
--   moc 10 -> 2117 (Cai Trang Goku Ultra)
--   moc 15 -> 2142 (Cai Trang Gogeta Ssj4)
--   moc 20 -> 1276 (Zamasu Toi Thuong)
-- Chi so phan thuong van lay tu referral_costume_catalog / options_json.
-- Chay SAU 20261028_referral_event_kame.sql va 20261028_referral_manual_points.sql.

DELIMITER $$
DROP PROCEDURE IF EXISTS nro_install_referral_costumes_20261028$$
CREATE PROCEDURE nro_install_referral_costumes_20261028()
BEGIN
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=904 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu costume client ID 904';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=2117 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu costume client ID 2117';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=2142 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu costume client ID 2142';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=1276 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu costume client ID 1276';
  END IF;

  IF NOT EXISTS(SELECT 1 FROM cai_trang WHERE id_temp=904) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu mapping cai_trang ID 904';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM cai_trang WHERE id_temp=2117) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu mapping cai_trang ID 2117';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM cai_trang WHERE id_temp=2142) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu mapping cai_trang ID 2142';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM cai_trang WHERE id_temp=1276) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu mapping cai_trang ID 1276';
  END IF;
END$$
DELIMITER ;

CALL nro_install_referral_costumes_20261028();
DROP PROCEDURE nro_install_referral_costumes_20261028;

-- Gan cac moc vao item client da biet.
UPDATE referral_costume_catalog SET item_template_id=904  WHERE milestone=5;
UPDATE referral_costume_catalog SET item_template_id=2117 WHERE milestone=10;
UPDATE referral_costume_catalog SET item_template_id=2142 WHERE milestone=15;
UPDATE referral_costume_catalog SET item_template_id=1276 WHERE milestone=20;

-- Giftcode test cung chi dung item client da biet.
UPDATE gift_codes SET items='[{"id":904,"quantity":1,"options":[{"id":77,"param":20},{"id":103,"param":20},{"id":50,"param":20},{"id":14,"param":4},{"id":101,"param":10},{"id":30,"param":0},{"id":93,"param":7}]},{"id":2117,"quantity":1,"options":[{"id":77,"param":23},{"id":103,"param":23},{"id":50,"param":23},{"id":14,"param":6},{"id":101,"param":13},{"id":8,"param":5},{"id":30,"param":0},{"id":93,"param":7}]},{"id":2142,"quantity":1,"options":[{"id":77,"param":25},{"id":103,"param":25},{"id":50,"param":25},{"id":14,"param":8},{"id":101,"param":17},{"id":8,"param":8},{"id":30,"param":0}]},{"id":1276,"quantity":1,"options":[{"id":77,"param":27},{"id":103,"param":27},{"id":50,"param":27},{"id":14,"param":10},{"id":101,"param":20},{"id":8,"param":10},{"id":30,"param":0}]}]'
WHERE code='nrotest4ct2026';

INSERT INTO gift_codes(type,code,gold,gem,ruby,items,status,active,expires_at,created_at,updated_at)
SELECT 1,'nrotest4ct2026',0,0,0,'[{"id":904,"quantity":1,"options":[{"id":77,"param":20},{"id":103,"param":20},{"id":50,"param":20},{"id":14,"param":4},{"id":101,"param":10},{"id":30,"param":0},{"id":93,"param":7}]},{"id":2117,"quantity":1,"options":[{"id":77,"param":23},{"id":103,"param":23},{"id":50,"param":23},{"id":14,"param":6},{"id":101,"param":13},{"id":8,"param":5},{"id":30,"param":0},{"id":93,"param":7}]},{"id":2142,"quantity":1,"options":[{"id":77,"param":25},{"id":103,"param":25},{"id":50,"param":25},{"id":14,"param":8},{"id":101,"param":17},{"id":8,"param":8},{"id":30,"param":0}]},{"id":1276,"quantity":1,"options":[{"id":77,"param":27},{"id":103,"param":27},{"id":50,"param":27},{"id":14,"param":10},{"id":101,"param":20},{"id":8,"param":10},{"id":30,"param":0}]}]',0,0,NULL,NOW(),NOW()
WHERE NOT EXISTS(SELECT 1 FROM gift_codes WHERE code='nrotest4ct2026');

SELECT id,TYPE,NAME,icon_id FROM item_template WHERE id IN (904,2117,2142,1276) ORDER BY id;
SELECT id_temp,head,body,leg FROM cai_trang WHERE id_temp IN (904,2117,2142,1276) ORDER BY id_temp;
SELECT milestone,costume_name,item_template_id,duration_days,options_json
FROM referral_costume_catalog WHERE milestone IN (5,10,15,20) ORDER BY milestone;
SELECT code,items,status,type FROM gift_codes WHERE code='nrotest4ct2026';
