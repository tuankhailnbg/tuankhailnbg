-- FIX CLIENT FREEZE KHI BAM/MAC CAI TRANG GIOI THIEU
-- BAT BUOC: DUNG SERVER + BACKUP DATABASE TRUOC KHI CHAY.
--
-- Nguyen nhan: server da tao item moi 2154..2157 nhung client hien tai co the
-- khong co template cac ID nay. Trieu chung: icon trong, bam item lag/do.
--
-- Chuyen sang item TYPE=5 da co san trong client:
-- 2154 -> 904
-- 2155 -> 2117
-- 2156 -> 2142
-- 2157 -> 1276
--
-- Giu NGUYEN option, quantity, create_time trong JSON cua player.
-- KHONG xoa item_template 2154..2157 de tranh lam hong index lien tuc.

DELIMITER $$
DROP PROCEDURE IF EXISTS nro_fix_referral_costume_client_freeze_20261028$$
CREATE PROCEDURE nro_fix_referral_costume_client_freeze_20261028()
BEGIN
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=904 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu item client-compatible ID 904';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=2117 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu item client-compatible ID 2117';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=2142 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu item client-compatible ID 2142';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM item_template WHERE id=1276 AND TYPE=5) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu item client-compatible ID 1276';
  END IF;
END$$
DELIMITER ;

CALL nro_fix_referral_costume_client_freeze_20261028();
DROP PROCEDURE nro_fix_referral_costume_client_freeze_20261028;

-- 1) Sua catalog de cac lan nhan sau khong phat item ID moi.
UPDATE referral_costume_catalog SET item_template_id=904  WHERE milestone=5;
UPDATE referral_costume_catalog SET item_template_id=2117 WHERE milestone=10;
UPDATE referral_costume_catalog SET item_template_id=2142 WHERE milestone=15;
UPDATE referral_costume_catalog SET item_template_id=1276 WHERE milestone=20;

-- 2) Chuyen lich su claim sang ID client-compatible.
UPDATE referral_costume_claims SET item_template_id=904  WHERE milestone=5  AND item_template_id=2154;
UPDATE referral_costume_claims SET item_template_id=2117 WHERE milestone=10 AND item_template_id=2155;
UPDATE referral_costume_claims SET item_template_id=2142 WHERE milestone=15 AND item_template_id=2156;
UPDATE referral_costume_claims SET item_template_id=1276 WHERE milestone=20 AND item_template_id=2157;

-- 3) Chuyen item da phat trong tui / dang mac / ruong / de tu.
-- org.json.simple luu JSON dang "temp_id":<id>, REPLACE chi thay dung gia tri temp_id.
UPDATE player
SET items_bag = REPLACE(REPLACE(REPLACE(REPLACE(items_bag,
    '"temp_id":2154','"temp_id":904'),
    '"temp_id":2155','"temp_id":2117'),
    '"temp_id":2156','"temp_id":2142'),
    '"temp_id":2157','"temp_id":1276')
WHERE items_bag LIKE '%"temp_id":2154%'
   OR items_bag LIKE '%"temp_id":2155%'
   OR items_bag LIKE '%"temp_id":2156%'
   OR items_bag LIKE '%"temp_id":2157%';

UPDATE player
SET items_body = REPLACE(REPLACE(REPLACE(REPLACE(items_body,
    '"temp_id":2154','"temp_id":904'),
    '"temp_id":2155','"temp_id":2117'),
    '"temp_id":2156','"temp_id":2142'),
    '"temp_id":2157','"temp_id":1276')
WHERE items_body LIKE '%"temp_id":2154%'
   OR items_body LIKE '%"temp_id":2155%'
   OR items_body LIKE '%"temp_id":2156%'
   OR items_body LIKE '%"temp_id":2157%';

UPDATE player
SET items_box = REPLACE(REPLACE(REPLACE(REPLACE(items_box,
    '"temp_id":2154','"temp_id":904'),
    '"temp_id":2155','"temp_id":2117'),
    '"temp_id":2156','"temp_id":2142'),
    '"temp_id":2157','"temp_id":1276')
WHERE items_box LIKE '%"temp_id":2154%'
   OR items_box LIKE '%"temp_id":2155%'
   OR items_box LIKE '%"temp_id":2156%'
   OR items_box LIKE '%"temp_id":2157%';

UPDATE player
SET pet_body = REPLACE(REPLACE(REPLACE(REPLACE(pet_body,
    '"temp_id":2154','"temp_id":904'),
    '"temp_id":2155','"temp_id":2117'),
    '"temp_id":2156','"temp_id":2142'),
    '"temp_id":2157','"temp_id":1276')
WHERE pet_body LIKE '%"temp_id":2154%'
   OR pet_body LIKE '%"temp_id":2155%'
   OR pet_body LIKE '%"temp_id":2156%'
   OR pet_body LIKE '%"temp_id":2157%';

-- 4) Sua giftcode test neu da tao.
UPDATE gift_codes
SET items=REPLACE(REPLACE(REPLACE(REPLACE(items,
    '"id":2154','"id":904'),
    '"id":2155','"id":2117'),
    '"id":2156','"id":2142'),
    '"id":2157','"id":1276')
WHERE code='nrotest4ct2026';

-- 5) Kiem tra ket qua.
SELECT milestone,costume_name,item_template_id,duration_days
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
ORDER BY milestone;

SELECT id,NAME,TYPE,icon_id
FROM item_template
WHERE id IN (904,2117,2142,1276)
ORDER BY id;

SELECT COUNT(*) AS players_still_have_legacy_costume_in_bag
FROM player
WHERE items_bag LIKE '%"temp_id":2154%'
   OR items_bag LIKE '%"temp_id":2155%'
   OR items_bag LIKE '%"temp_id":2156%'
   OR items_bag LIKE '%"temp_id":2157%';

SELECT COUNT(*) AS players_still_have_legacy_costume_on_body
FROM player
WHERE items_body LIKE '%"temp_id":2154%'
   OR items_body LIKE '%"temp_id":2155%'
   OR items_body LIKE '%"temp_id":2156%'
   OR items_body LIKE '%"temp_id":2157%';
