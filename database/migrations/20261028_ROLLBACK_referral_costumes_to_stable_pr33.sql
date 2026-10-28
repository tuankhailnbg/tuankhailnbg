-- ============================================================
-- ROLLBACK 4 CAI TRANG EVENT MOI -> TRANG THAI STABLE SAU PR #33
-- Muc tieu:
--   * Khoi phuc mapping moc 5/10/15/20 ve item client da ho tro.
--   * Khoi phuc code nhan qua cu, khong xoa diem gioi thieu/Thoi Vang.
--   * Chuyen cac item event moi da phat trong player JSON ve item stable.
--   * KHONG xoa item_template moi de KHONG tao khoang trong ID.
--
-- BAT BUOC: DUNG SERVER + BACKUP DATABASE TRUOC KHI IMPORT.
-- ============================================================

USE ngocrong;

DELIMITER $$

DROP PROCEDURE IF EXISTS nro_rollback_referral_new_costumes_to_pr33$$

CREATE PROCEDURE nro_rollback_referral_new_costumes_to_pr33()
BEGIN
    DECLARE v_whis INT DEFAULT NULL;
    DECLARE v_champa INT DEFAULT NULL;
    DECLARE v_tapion INT DEFAULT NULL;
    DECLARE v_vegeta INT DEFAULT NULL;

    -- 1) Kiem tra 4 item stable bat buoc phai ton tai.
    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=904 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback dung: thieu item stable ID 904';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=2117 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback dung: thieu item stable ID 2117';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=2142 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback dung: thieu item stable ID 2142';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=1276 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rollback dung: thieu item stable ID 1276';
    END IF;

    -- 2) Tim ID 4 item event moi theo TEN, khong doan ID.
    SELECT MAX(id) INTO v_whis
    FROM item_template
    WHERE NAME='Cải Trang Whis Event' AND TYPE=5;

    SELECT MAX(id) INTO v_champa
    FROM item_template
    WHERE NAME='Cải Trang Champa Event' AND TYPE=5;

    SELECT MAX(id) INTO v_tapion
    FROM item_template
    WHERE NAME='Cải Trang Tapion Event' AND TYPE=5;

    SELECT MAX(id) INTO v_vegeta
    FROM item_template
    WHERE NAME='Cải Trang Vua Vegeta Event' AND TYPE=5;

    -- 3) Neu da phat item event moi, chuyen temp_id trong player JSON ve item stable.
    -- Giu nguyen option, quantity, create_time.
    IF v_whis IS NOT NULL THEN
        UPDATE player SET
            items_bag  = REPLACE(items_bag,  CONCAT('"temp_id":',v_whis), '"temp_id":904'),
            items_body = REPLACE(items_body, CONCAT('"temp_id":',v_whis), '"temp_id":904'),
            items_box  = REPLACE(items_box,  CONCAT('"temp_id":',v_whis), '"temp_id":904'),
            pet_body   = REPLACE(pet_body,   CONCAT('"temp_id":',v_whis), '"temp_id":904')
        WHERE items_bag  LIKE CONCAT('%"temp_id":',v_whis,'%')
           OR items_body LIKE CONCAT('%"temp_id":',v_whis,'%')
           OR items_box  LIKE CONCAT('%"temp_id":',v_whis,'%')
           OR pet_body   LIKE CONCAT('%"temp_id":',v_whis,'%');

        UPDATE referral_costume_claims
        SET item_template_id=904
        WHERE milestone=5 AND item_template_id=v_whis;
    END IF;

    IF v_champa IS NOT NULL THEN
        UPDATE player SET
            items_bag  = REPLACE(items_bag,  CONCAT('"temp_id":',v_champa), '"temp_id":2117'),
            items_body = REPLACE(items_body, CONCAT('"temp_id":',v_champa), '"temp_id":2117'),
            items_box  = REPLACE(items_box,  CONCAT('"temp_id":',v_champa), '"temp_id":2117'),
            pet_body   = REPLACE(pet_body,   CONCAT('"temp_id":',v_champa), '"temp_id":2117')
        WHERE items_bag  LIKE CONCAT('%"temp_id":',v_champa,'%')
           OR items_body LIKE CONCAT('%"temp_id":',v_champa,'%')
           OR items_box  LIKE CONCAT('%"temp_id":',v_champa,'%')
           OR pet_body   LIKE CONCAT('%"temp_id":',v_champa,'%');

        UPDATE referral_costume_claims
        SET item_template_id=2117
        WHERE milestone=10 AND item_template_id=v_champa;
    END IF;

    IF v_tapion IS NOT NULL THEN
        UPDATE player SET
            items_bag  = REPLACE(items_bag,  CONCAT('"temp_id":',v_tapion), '"temp_id":2142'),
            items_body = REPLACE(items_body, CONCAT('"temp_id":',v_tapion), '"temp_id":2142'),
            items_box  = REPLACE(items_box,  CONCAT('"temp_id":',v_tapion), '"temp_id":2142'),
            pet_body   = REPLACE(pet_body,   CONCAT('"temp_id":',v_tapion), '"temp_id":2142')
        WHERE items_bag  LIKE CONCAT('%"temp_id":',v_tapion,'%')
           OR items_body LIKE CONCAT('%"temp_id":',v_tapion,'%')
           OR items_box  LIKE CONCAT('%"temp_id":',v_tapion,'%')
           OR pet_body   LIKE CONCAT('%"temp_id":',v_tapion,'%');

        UPDATE referral_costume_claims
        SET item_template_id=2142
        WHERE milestone=15 AND item_template_id=v_tapion;
    END IF;

    IF v_vegeta IS NOT NULL THEN
        UPDATE player SET
            items_bag  = REPLACE(items_bag,  CONCAT('"temp_id":',v_vegeta), '"temp_id":1276'),
            items_body = REPLACE(items_body, CONCAT('"temp_id":',v_vegeta), '"temp_id":1276'),
            items_box  = REPLACE(items_box,  CONCAT('"temp_id":',v_vegeta), '"temp_id":1276'),
            pet_body   = REPLACE(pet_body,   CONCAT('"temp_id":',v_vegeta), '"temp_id":1276')
        WHERE items_bag  LIKE CONCAT('%"temp_id":',v_vegeta,'%')
           OR items_body LIKE CONCAT('%"temp_id":',v_vegeta,'%')
           OR items_box  LIKE CONCAT('%"temp_id":',v_vegeta,'%')
           OR pet_body   LIKE CONCAT('%"temp_id":',v_vegeta,'%');

        UPDATE referral_costume_claims
        SET item_template_id=1276
        WHERE milestone=20 AND item_template_id=v_vegeta;
    END IF;

    -- 4) Khoi phuc catalog dung nhu trang thai stable PR #33.
    UPDATE referral_costume_catalog
    SET item_template_id=904,
        costume_name='Goku SSJ3',
        duration_days=7,
        options_json='[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'
    WHERE milestone=5;

    UPDATE referral_costume_catalog
    SET item_template_id=2117,
        costume_name='Vegito Blue',
        duration_days=7,
        options_json='[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'
    WHERE milestone=10;

    UPDATE referral_costume_catalog
    SET item_template_id=2142,
        costume_name='Gogeta Blue',
        duration_days=NULL,
        options_json='[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'
    WHERE milestone=15;

    UPDATE referral_costume_catalog
    SET item_template_id=1276,
        costume_name='Zeno',
        duration_days=NULL,
        options_json='[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]'
    WHERE milestone=20;

    -- 5) Chuyen claim legacy 2154..2157 neu van con.
    UPDATE referral_costume_claims SET item_template_id=904
    WHERE milestone=5 AND item_template_id=2154;
    UPDATE referral_costume_claims SET item_template_id=2117
    WHERE milestone=10 AND item_template_id=2155;
    UPDATE referral_costume_claims SET item_template_id=2142
    WHERE milestone=15 AND item_template_id=2156;
    UPDATE referral_costume_claims SET item_template_id=1276
    WHERE milestone=20 AND item_template_id=2157;

    -- 6) Chuyen legacy 2154..2157 trong player JSON (neu con).
    UPDATE player
    SET items_bag = REPLACE(REPLACE(REPLACE(REPLACE(items_bag,
        '"temp_id":2154','"temp_id":904'),
        '"temp_id":2155','"temp_id":2117'),
        '"temp_id":2156','"temp_id":2142'),
        '"temp_id":2157','"temp_id":1276'),
        items_body = REPLACE(REPLACE(REPLACE(REPLACE(items_body,
        '"temp_id":2154','"temp_id":904'),
        '"temp_id":2155','"temp_id":2117'),
        '"temp_id":2156','"temp_id":2142'),
        '"temp_id":2157','"temp_id":1276'),
        items_box = REPLACE(REPLACE(REPLACE(REPLACE(items_box,
        '"temp_id":2154','"temp_id":904'),
        '"temp_id":2155','"temp_id":2117'),
        '"temp_id":2156','"temp_id":2142'),
        '"temp_id":2157','"temp_id":1276'),
        pet_body = REPLACE(REPLACE(REPLACE(REPLACE(pet_body,
        '"temp_id":2154','"temp_id":904'),
        '"temp_id":2155','"temp_id":2117'),
        '"temp_id":2156','"temp_id":2142'),
        '"temp_id":2157','"temp_id":1276')
    WHERE items_bag  REGEXP '"temp_id":215[4-7]'
       OR items_body REGEXP '"temp_id":215[4-7]'
       OR items_box  REGEXP '"temp_id":215[4-7]'
       OR pet_body   REGEXP '"temp_id":215[4-7]';

END$$

DELIMITER ;

CALL nro_rollback_referral_new_costumes_to_pr33();
DROP PROCEDURE nro_rollback_referral_new_costumes_to_pr33;

-- KIEM TRA SAU ROLLBACK
SELECT milestone,item_template_id,costume_name,duration_days,options_json
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
ORDER BY milestone;

SELECT milestone,item_template_id,COUNT(*) AS claim_count
FROM referral_costume_claims
WHERE milestone IN (5,10,15,20)
GROUP BY milestone,item_template_id
ORDER BY milestone,item_template_id;

-- 4 item event moi KHONG bi xoa khoi item_template/cai_trang.
-- Ly do: xoa se tao khoang ID, co the lam sai ITEM_TEMPLATES.get(id).
-- Chung chi duoc bo khoi catalog va khong con duoc phat.
