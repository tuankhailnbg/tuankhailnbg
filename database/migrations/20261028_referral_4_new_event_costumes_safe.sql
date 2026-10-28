-- ============================================================
-- NRO - 4 CAI TRANG EVENT MOI - BAN DATABASE-ONLY AN TOAN
-- Tao cho source: tuankhailnbg/tuankhailnbg
-- Database: ngocrong / MariaDB
--
-- Muc tieu:
--   * Tao 4 item_template ID MOI, khong dung lai ID shop.
--   * ID duoc cap lien tuc tu MAX(id)+1 de tuong thich code Java
--     dang dung Manager.ITEM_TEMPLATES.get(id).
--   * Chi dung icon + head/body/leg DA TON TAI trong client/database hien tai.
--   * Khong chen part/icon la tu source ngoai vao client cua ban.
--   * Gan 4 cai trang vao moc gioi thieu 5 / 10 / 15 / 20.
--
-- 4 ngoai hinh client da co:
--   Whis       : head 505, body 506, leg 507
--   Champa     : head 511, body 512, leg 513
--   Tapion     : head 481, body 482, leg 483
--   Vua Vegeta : head 36,  body 37,  leg 38
--
-- Icon su dung deu la icon da co trong DB/client:
--   Whis       : 9979  (icon Avatar Thien Su)
--   Champa     : 12793 (icon Kid Bill)
--   Tapion     : 20160 (icon Trunks)
--   Vua Vegeta : 20403 (icon Vegeta SSJ4)
--
-- NGUON THAM KHAO PUBLIC:
--   https://github.com/baodubaiiii/NgocRongOnline_Data
--   https://github.com/baodubaiiii/DataNgocRongOnline
--
-- LUU Y:
--   1) BACKUP database ngocrong truoc khi import.
--   2) Import file nay 1 LAN.
--   3) Restart SERVER JAVA hoan toan sau import.
--   4) Khong can them client asset moi vi cac part/icon deu da co.
-- ============================================================

USE ngocrong;

DELIMITER $$

DROP PROCEDURE IF EXISTS nro_install_4_event_costumes_safe_20261028$$

CREATE PROCEDURE nro_install_4_event_costumes_safe_20261028()
BEGIN
    DECLARE v_min_id INT DEFAULT 0;
    DECLARE v_max_id INT DEFAULT 0;
    DECLARE v_count_id INT DEFAULT 0;

    DECLARE v_whis INT;
    DECLARE v_champa INT;
    DECLARE v_tapion INT;
    DECLARE v_vegeta INT;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'item_template'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu bang item_template';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'cai_trang'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu bang cai_trang';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'part'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu bang part';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'referral_costume_catalog'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu referral_costume_catalog. Import migration referral event truoc.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM item_template
        WHERE NAME IN (
            'Cải Trang Whis Event',
            'Cải Trang Champa Event',
            'Cải Trang Tapion Event',
            'Cải Trang Vua Vegeta Event'
        )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '4 cai trang event da ton tai. Khong import file nay lan 2.';
    END IF;

    SELECT MIN(id), MAX(id), COUNT(*)
    INTO v_min_id, v_max_id, v_count_id
    FROM item_template;

    IF v_min_id <> 0 OR v_count_id <> (v_max_id + 1) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'item_template dang co khoang trong ID. Khong tu tao ID moi de tranh loi ITEM_TEMPLATES.get(id).';
    END IF;

    SET v_whis   = v_max_id + 1;
    SET v_champa = v_max_id + 2;
    SET v_tapion = v_max_id + 3;
    SET v_vegeta = v_max_id + 4;

    IF v_vegeta > 32767 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ID item moi vuot gioi han short 32767 cua server Java.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE icon_id = 9979) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Client/DB thieu icon 9979 cho Whis';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE icon_id = 12793) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Client/DB thieu icon 12793 cho Champa';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE icon_id = 20160) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Client/DB thieu icon 20160 cho Tapion';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE icon_id = 20403) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Client/DB thieu icon 20403 cho Vua Vegeta';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=505 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=506 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=507 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu part Whis 505/506/507 trong client/database';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=511 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=512 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=513 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu part Champa 511/512/513 trong client/database';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=481 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=482 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=483 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu part Tapion 481/482/483 trong client/database';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=36 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=37 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=38 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Thieu part Vua Vegeta 36/37/38 trong client/database';
    END IF;

    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=505 AND body=506 AND leg=507) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ngoai hinh Whis da duoc mot item cai trang khac su dung.';
    END IF;

    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=511 AND body=512 AND leg=513) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ngoai hinh Champa da duoc mot item cai trang khac su dung.';
    END IF;

    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=481 AND body=482 AND leg=483) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ngoai hinh Tapion da duoc mot item cai trang khac su dung.';
    END IF;

    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=36 AND body=37 AND leg=38) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ngoai hinh Vua Vegeta da duoc mot item cai trang khac su dung.';
    END IF;

    INSERT INTO item_template
        (id, TYPE, gender, NAME, description, icon_id, part, is_up_to_up, power_require)
    VALUES
        (v_whis,   5, 3, 'Cải Trang Whis Event',
         'Cải trang độc quyền sự kiện giới thiệu', 9979, -1, 0, 0),

        (v_champa, 5, 3, 'Cải Trang Champa Event',
         'Cải trang độc quyền sự kiện giới thiệu', 12793, -1, 0, 0),

        (v_tapion, 5, 3, 'Cải Trang Tapion Event',
         'Cải trang độc quyền sự kiện giới thiệu', 20160, -1, 0, 0),

        (v_vegeta, 5, 3, 'Cải Trang Vua Vegeta Event',
         'Cải trang độc quyền sự kiện giới thiệu', 20403, -1, 0, 0);

    INSERT INTO cai_trang (id_temp, head, body, leg, bag)
    VALUES
        (v_whis,   505, 506, 507, -1),
        (v_champa, 511, 512, 513, -1),
        (v_tapion, 481, 482, 483, -1),
        (v_vegeta, 36,  37,  38,  -1);

    INSERT INTO referral_costume_catalog
        (milestone, item_template_id, costume_name, duration_days, options_json)
    VALUES
        (5, v_whis, 'Whis Event', 7,
         '[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'),

        (10, v_champa, 'Champa Event', 7,
         '[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'),

        (15, v_tapion, 'Tapion Event', NULL,
         '[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'),

        (20, v_vegeta, 'Vua Vegeta Event', NULL,
         '[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]')
    ON DUPLICATE KEY UPDATE
        item_template_id = VALUES(item_template_id),
        costume_name = VALUES(costume_name),
        duration_days = VALUES(duration_days),
        options_json = VALUES(options_json);

END$$

DELIMITER ;

CALL nro_install_4_event_costumes_safe_20261028();
DROP PROCEDURE nro_install_4_event_costumes_safe_20261028;

SELECT
    id, TYPE, gender, NAME, icon_id, part
FROM item_template
WHERE NAME IN (
    'Cải Trang Whis Event',
    'Cải Trang Champa Event',
    'Cải Trang Tapion Event',
    'Cải Trang Vua Vegeta Event'
)
ORDER BY id;

SELECT
    c.id_temp,
    i.NAME,
    c.head,
    c.body,
    c.leg,
    c.bag
FROM cai_trang c
JOIN item_template i ON i.id = c.id_temp
WHERE i.NAME IN (
    'Cải Trang Whis Event',
    'Cải Trang Champa Event',
    'Cải Trang Tapion Event',
    'Cải Trang Vua Vegeta Event'
)
ORDER BY c.id_temp;

SELECT
    milestone,
    item_template_id,
    costume_name,
    duration_days,
    options_json
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
ORDER BY milestone;

-- Optional:
-- DELETE FROM referral_costume_claims
-- WHERE milestone IN (5,10,15,20);

-- SAU IMPORT:
-- 1) Tat Server Java.
-- 2) Build lai neu source vua cap nhat.
-- 3) Chay Server Java lai TU DAU de Manager load item_template + cai_trang moi.
