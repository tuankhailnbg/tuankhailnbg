-- ============================================================
-- NRO - DOI 4 CAI TRANG SU KIEN GIOI THIEU SANG BO KHONG NAM TRONG SHOP
-- Database: ngocrong / MariaDB
--
-- Muc tieu:
--   * Moc 5  -> ID 1304: Cai trang Goku SSJ4
--   * Moc 10 -> ID 1305: Cai trang Vegeta SSJ4
--   * Moc 15 -> ID 1306: Cai trang Omega
--   * Moc 20 -> ID 2045: Cai trang Cumber
--
-- Ly do chon:
--   * 4 item da ton tai san trong item_template, TYPE=5.
--   * 4 ID KHONG duoc phep xuat hien trong item_shop.
--   * Ngoai hinh cua 4 item KHONG duoc trung mapping head/body/leg voi item trong shop.
--   * Icon trung frame dau cua head:
--       1304: icon 20371 == head 1345 frame dau 20371
--       1305: icon 20403 == head 1348 frame dau 20403
--       1306: icon 20339 == head 1342 frame dau 20339
--       2045: icon 11379 == head 1387 frame dau 11379
--   * Bo animation player day du:
--       head = 3 frame, body = 17 frame, leg = 14 frame.
--
-- Khong tao item ID moi.
-- Khong sua item_shop.
-- Khong xoa diem gioi thieu / Thoi Vang.
-- Claim cai trang cu khac ID se duoc bo de nguoi choi co the nhan bo moi.
--
-- BAT BUOC: backup DB + dung Server truoc khi import.
-- ============================================================

USE ngocrong;

DELIMITER $$

DROP PROCEDURE IF EXISTS nro_set_referral_nonshop_costumes_20261028$$

CREATE PROCEDURE nro_set_referral_nonshop_costumes_20261028()
BEGIN
    -- --------------------------------------------------------
    -- 1. Bang bat buoc
    -- --------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema=DATABASE() AND table_name='item_template'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bang item_template';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema=DATABASE() AND table_name='cai_trang'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bang cai_trang';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema=DATABASE() AND table_name='part'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bang part';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema=DATABASE() AND table_name='item_shop'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bang item_shop';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema=DATABASE() AND table_name='referral_costume_catalog'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bang referral_costume_catalog';
    END IF;

    -- --------------------------------------------------------
    -- 2. Item phai ton tai dung TYPE=5
    -- --------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=1304 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu cai trang Goku SSJ4 ID 1304';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=1305 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu cai trang Vegeta SSJ4 ID 1305';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=1306 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu cai trang Omega ID 1306';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM item_template WHERE id=2045 AND TYPE=5) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu cai trang Cumber ID 2045';
    END IF;

    -- --------------------------------------------------------
    -- 3. Tuyet doi khong dung item dang co trong shop
    -- --------------------------------------------------------
    IF EXISTS (
        SELECT 1 FROM item_shop
        WHERE temp_id IN (1304,1305,1306,2045)
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='DUNG IMPORT: mot trong 4 ID 1304/1305/1306/2045 dang nam trong item_shop';
    END IF;

    -- --------------------------------------------------------
    -- 4. Mapping ngoai hinh phai dung chinh xac
    -- --------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1 FROM cai_trang
        WHERE id_temp=1304 AND head=1345 AND body=1346 AND leg=1347
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Mapping Goku SSJ4 ID 1304 khong dung';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM cai_trang
        WHERE id_temp=1305 AND head=1348 AND body=1349 AND leg=1350
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Mapping Vegeta SSJ4 ID 1305 khong dung';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM cai_trang
        WHERE id_temp=1306 AND head=1342 AND body=1343 AND leg=1344
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Mapping Omega ID 1306 khong dung';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM cai_trang
        WHERE id_temp=2045 AND head=1387 AND body=1388 AND leg=1389
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Mapping Cumber ID 2045 khong dung';
    END IF;

    -- --------------------------------------------------------
    -- 5. Bo ngoai hinh khong duoc trung voi BAT KY item shop nao
    -- --------------------------------------------------------
    IF EXISTS (
        SELECT 1
        FROM cai_trang c
        JOIN item_shop s ON s.temp_id=c.id_temp
        WHERE c.head=1345 AND c.body=1346 AND c.leg=1347
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Goku SSJ4 dang bi item shop su dung';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM cai_trang c
        JOIN item_shop s ON s.temp_id=c.id_temp
        WHERE c.head=1348 AND c.body=1349 AND c.leg=1350
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Vegeta SSJ4 dang bi item shop su dung';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM cai_trang c
        JOIN item_shop s ON s.temp_id=c.id_temp
        WHERE c.head=1342 AND c.body=1343 AND c.leg=1344
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Omega dang bi item shop su dung';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM cai_trang c
        JOIN item_shop s ON s.temp_id=c.id_temp
        WHERE c.head=1387 AND c.body=1388 AND c.leg=1389
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Cumber dang bi item shop su dung';
    END IF;

    -- --------------------------------------------------------
    -- 6. Kiem tra part dung TYPE + du animation player
    -- head 3 / body 17 / leg 14
    -- --------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM part WHERE id=1345 AND TYPE=0 AND JSON_LENGTH(DATA)=3)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1346 AND TYPE=1 AND JSON_LENGTH(DATA)=17)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1347 AND TYPE=2 AND JSON_LENGTH(DATA)=14) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Part Goku SSJ4 thieu frame animation';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=1348 AND TYPE=0 AND JSON_LENGTH(DATA)=3)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1349 AND TYPE=1 AND JSON_LENGTH(DATA)=17)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1350 AND TYPE=2 AND JSON_LENGTH(DATA)=14) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Part Vegeta SSJ4 thieu frame animation';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=1342 AND TYPE=0 AND JSON_LENGTH(DATA)=3)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1343 AND TYPE=1 AND JSON_LENGTH(DATA)=17)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1344 AND TYPE=2 AND JSON_LENGTH(DATA)=14) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Part Omega thieu frame animation';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=1387 AND TYPE=0 AND JSON_LENGTH(DATA)=3)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1388 AND TYPE=1 AND JSON_LENGTH(DATA)=17)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1389 AND TYPE=2 AND JSON_LENGTH(DATA)=14) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Part Cumber thieu frame animation';
    END IF;

    -- --------------------------------------------------------
    -- 7. Icon phai trung frame dau cua head
    -- --------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1
        FROM item_template i JOIN part p ON p.id=1345
        WHERE i.id=1304 AND i.icon_id=20371
          AND CAST(JSON_UNQUOTE(JSON_EXTRACT(p.DATA,'$[0][0]')) AS UNSIGNED)=20371
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Icon Goku SSJ4 khong trung hinh head';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM item_template i JOIN part p ON p.id=1348
        WHERE i.id=1305 AND i.icon_id=20403
          AND CAST(JSON_UNQUOTE(JSON_EXTRACT(p.DATA,'$[0][0]')) AS UNSIGNED)=20403
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Icon Vegeta SSJ4 khong trung hinh head';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM item_template i JOIN part p ON p.id=1342
        WHERE i.id=1306 AND i.icon_id=20339
          AND CAST(JSON_UNQUOTE(JSON_EXTRACT(p.DATA,'$[0][0]')) AS UNSIGNED)=20339
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Icon Omega khong trung hinh head';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM item_template i JOIN part p ON p.id=1387
        WHERE i.id=2045 AND i.icon_id=11379
          AND CAST(JSON_UNQUOTE(JSON_EXTRACT(p.DATA,'$[0][0]')) AS UNSIGNED)=11379
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Icon Cumber khong trung hinh head';
    END IF;

    -- --------------------------------------------------------
    -- 8. Doi 4 moc event
    -- --------------------------------------------------------
    UPDATE referral_costume_catalog
    SET item_template_id=1304,
        costume_name='Goku SSJ4',
        duration_days=7,
        options_json='[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'
    WHERE milestone=5;

    UPDATE referral_costume_catalog
    SET item_template_id=1305,
        costume_name='Vegeta SSJ4',
        duration_days=7,
        options_json='[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'
    WHERE milestone=10;

    UPDATE referral_costume_catalog
    SET item_template_id=1306,
        costume_name='Omega',
        duration_days=NULL,
        options_json='[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'
    WHERE milestone=15;

    UPDATE referral_costume_catalog
    SET item_template_id=2045,
        costume_name='Cumber',
        duration_days=NULL,
        options_json='[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]'
    WHERE milestone=20;

    -- --------------------------------------------------------
    -- 9. Claim cai trang cua reward cu khac ID se duoc mo lai.
    -- Chi xoa lich su CAI TRANG khac item hien tai.
    -- Khong dong vao referral_claims (Thoi Vang).
    -- --------------------------------------------------------
    DELETE rc
    FROM referral_costume_claims rc
    JOIN referral_costume_catalog cat ON cat.milestone=rc.milestone
    WHERE rc.milestone IN (5,10,15,20)
      AND rc.item_template_id<>cat.item_template_id;

END$$

DELIMITER ;

CALL nro_set_referral_nonshop_costumes_20261028();
DROP PROCEDURE nro_set_referral_nonshop_costumes_20261028;

-- ============================================================
-- KIEM TRA SAU IMPORT
-- ============================================================

-- 4 moc phai ra 1304/1305/1306/2045.
SELECT milestone,item_template_id,costume_name,duration_days,options_json
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
ORDER BY milestone;

-- 4 ID phai khong co trong shop: query nay BAT BUOC tra 0 dong.
SELECT s.id AS shop_row,s.tab_id,s.temp_id,i.NAME
FROM item_shop s
JOIN item_template i ON i.id=s.temp_id
WHERE s.temp_id IN (1304,1305,1306,2045);

-- Mapping + icon.
SELECT i.id,i.NAME,i.icon_id,c.head,c.body,c.leg,c.bag
FROM item_template i
JOIN cai_trang c ON c.id_temp=i.id
WHERE i.id IN (1304,1305,1306,2045)
ORDER BY i.id;

-- Khong co 2 moc dung chung 1 item ID: query nay BAT BUOC tra 0 dong.
SELECT item_template_id,COUNT(*) AS so_moc
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
GROUP BY item_template_id
HAVING COUNT(*)>1;

-- Sau import:
-- 1) Restart Server.
-- 2) Dang nhap lai.
-- 3) Thu nhan moc 5/10/15/20.
