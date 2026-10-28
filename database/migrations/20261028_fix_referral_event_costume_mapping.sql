-- ============================================================
-- FIX NGOAI HINH 4 CAI TRANG EVENT DA IMPORT BAN CU
--
-- Dung khi database da co 4 item event cu:
--   Whis Event / Champa Event / Tapion Event / Vua Vegeta Event
--
-- File nay GIU NGUYEN item ID da cap, chi sua:
--   * ten item
--   * icon
--   * head/body/leg
--   * referral_costume_catalog
--
-- Khong tao them ID, khong xoa Thoi Vang, khong xoa claim.
-- Sau import BAT BUOC restart Server Java.
-- ============================================================

USE ngocrong;

DELIMITER $$

DROP PROCEDURE IF EXISTS nro_fix_referral_costume_animation_20261028$$
CREATE PROCEDURE nro_fix_referral_costume_animation_20261028()
BEGIN
    DECLARE v_m5 INT DEFAULT NULL;
    DECLARE v_m10 INT DEFAULT NULL;
    DECLARE v_m15 INT DEFAULT NULL;
    DECLARE v_m20 INT DEFAULT NULL;
    DECLARE v_count INT DEFAULT 0;

    -- Tim dung 4 ID event cu/moi theo ten. Moi nhom phai chi co 1 item.
    SELECT COUNT(*),MIN(id) INTO v_count,v_m5
    FROM item_template
    WHERE TYPE=5 AND NAME IN ('Cải Trang Whis Event','Cải Trang Chi Chi Event');
    IF v_count<>1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Khong xac dinh duoc duy nhat item moc 5 (Whis/Chi Chi Event)';
    END IF;

    SELECT COUNT(*),MIN(id) INTO v_count,v_m10
    FROM item_template
    WHERE TYPE=5 AND NAME='Cải Trang Champa Event';
    IF v_count<>1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Khong xac dinh duoc duy nhat item moc 10 Champa Event';
    END IF;

    SELECT COUNT(*),MIN(id) INTO v_count,v_m15
    FROM item_template
    WHERE TYPE=5 AND NAME IN ('Cải Trang Tapion Event','Cải Trang Cađíc Event');
    IF v_count<>1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Khong xac dinh duoc duy nhat item moc 15 (Tapion/Cadic Event)';
    END IF;

    SELECT COUNT(*),MIN(id) INTO v_count,v_m20
    FROM item_template
    WHERE TYPE=5 AND NAME IN ('Cải Trang Vua Vegeta Event','Cải Trang Bill Event');
    IF v_count<>1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Khong xac dinh duoc duy nhat item moc 20 (Vua Vegeta/Bill Event)';
    END IF;

    IF v_m5=v_m10 OR v_m5=v_m15 OR v_m5=v_m20
       OR v_m10=v_m15 OR v_m10=v_m20 OR v_m15=v_m20 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='4 moc event dang bi trung item ID';
    END IF;

    -- Tuyet doi khong sua item shop.
    IF EXISTS (
        SELECT 1 FROM item_shop
        WHERE temp_id IN (v_m5,v_m10,v_m15,v_m20)
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Mot trong 4 item event dang nam trong shop. Dung fix de tranh anh huong shop.';
    END IF;

    -- Kiem tra 4 bo part an toan.
    IF NOT EXISTS (SELECT 1 FROM part WHERE id=1330 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1331 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1332 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu part Chi Chi 1330/1331/1332';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM part WHERE id=511 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=512 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=513 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu part Champa 511/512/513';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM part WHERE id=645 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=646 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=647 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu part Cadic 645/646/647';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM part WHERE id=508 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=509 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=510 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu part Bill 508/509/510';
    END IF;

    -- Khong duoc trung ngoai hinh voi item cai_trang khac.
    IF EXISTS (
        SELECT 1 FROM cai_trang
        WHERE head=1330 AND body=1331 AND leg=1332 AND id_temp<>v_m5
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Bo Chi Chi da bi item cai trang khac su dung';
    END IF;
    IF EXISTS (
        SELECT 1 FROM cai_trang
        WHERE head=511 AND body=512 AND leg=513 AND id_temp<>v_m10
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Bo Champa da bi item cai trang khac su dung';
    END IF;
    IF EXISTS (
        SELECT 1 FROM cai_trang
        WHERE head=645 AND body=646 AND leg=647 AND id_temp<>v_m15
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Bo Cadic da bi item cai trang khac su dung';
    END IF;
    IF EXISTS (
        SELECT 1 FROM cai_trang
        WHERE head=508 AND body=509 AND leg=510 AND id_temp<>v_m20
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Bo Bill da bi item cai trang khac su dung';
    END IF;

    -- Sua template nhưng GIU NGUYEN ID.
    UPDATE item_template
    SET NAME='Cải Trang Chi Chi Event',
        description='Cải trang độc quyền sự kiện giới thiệu',
        icon_id=12141,part=-1
    WHERE id=v_m5 AND TYPE=5;

    UPDATE item_template
    SET NAME='Cải Trang Champa Event',
        description='Cải trang độc quyền sự kiện giới thiệu',
        icon_id=4879,part=-1
    WHERE id=v_m10 AND TYPE=5;

    UPDATE item_template
    SET NAME='Cải Trang Cađíc Event',
        description='Cải trang độc quyền sự kiện giới thiệu',
        icon_id=6027,part=-1
    WHERE id=v_m15 AND TYPE=5;

    UPDATE item_template
    SET NAME='Cải Trang Bill Event',
        description='Cải trang độc quyền sự kiện giới thiệu',
        icon_id=4847,part=-1
    WHERE id=v_m20 AND TYPE=5;

    INSERT INTO cai_trang(id_temp,head,body,leg,bag)
    VALUES
        (v_m5,1330,1331,1332,-1),
        (v_m10,511,512,513,-1),
        (v_m15,645,646,647,-1),
        (v_m20,508,509,510,-1)
    ON DUPLICATE KEY UPDATE
        head=VALUES(head),body=VALUES(body),leg=VALUES(leg),bag=VALUES(bag);

    UPDATE referral_costume_catalog
    SET item_template_id=v_m5,costume_name='Chi Chi Event',duration_days=7,
        options_json='[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'
    WHERE milestone=5;

    UPDATE referral_costume_catalog
    SET item_template_id=v_m10,costume_name='Champa Event',duration_days=7,
        options_json='[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'
    WHERE milestone=10;

    UPDATE referral_costume_catalog
    SET item_template_id=v_m15,costume_name='Cađíc Event',duration_days=NULL,
        options_json='[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'
    WHERE milestone=15;

    UPDATE referral_costume_catalog
    SET item_template_id=v_m20,costume_name='Bill Event',duration_days=NULL,
        options_json='[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]'
    WHERE milestone=20;
END$$
DELIMITER ;

CALL nro_fix_referral_costume_animation_20261028();
DROP PROCEDURE nro_fix_referral_costume_animation_20261028;

-- Kiem tra sau fix.
SELECT i.id,i.NAME,i.TYPE,i.icon_id,c.head,c.body,c.leg,c.bag
FROM item_template i
JOIN cai_trang c ON c.id_temp=i.id
WHERE i.NAME IN (
    'Cải Trang Chi Chi Event',
    'Cải Trang Champa Event',
    'Cải Trang Cađíc Event',
    'Cải Trang Bill Event'
)
ORDER BY i.id;

SELECT milestone,item_template_id,costume_name,duration_days,options_json
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
ORDER BY milestone;

SELECT item_template_id,COUNT(*) AS so_moc_dung_chung_id
FROM referral_costume_catalog
WHERE milestone IN (5,10,15,20)
GROUP BY item_template_id
HAVING COUNT(*)>1;

-- SELECT cuoi phai tra 0 dong.
-- Sau import: TAT HAN Server Java -> build/chay lai -> dang nhap lai client.
