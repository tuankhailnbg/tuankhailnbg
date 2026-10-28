-- ============================================================
-- NRO - 4 CAI TRANG EVENT MOI - AN TOAN NGOAI HINH
-- Database: ngocrong / MariaDB
--
-- Quy tac:
--   * Tao 4 item ID moi lien tuc tu MAX(item_template.id)+1.
--   * Khong chen vao item_shop.
--   * Khong dung lai mapping cai_trang da co.
--   * Chi dung bo part co du frame dung/chay/danh cho player.
--   * Icon lay tu frame dau cua head, nen dong bo voi ngoai hinh.
--
-- Bo part da doi chieu tren ngocrong.sql:
--   Chi Chi : 1330/1331/1332 - 3/3, 17/17, 14/14 frame that
--   Champa  : 511/512/513    - 2/3, 16/17, 13/14 frame that
--   Cađíc   : 645/646/647    - 2/3, 16/17, 13/14 frame that
--   Bill    : 508/509/510    - 2/3, 16/17, 13/14 frame that
--
-- KHONG DUNG cac bo da xac dinh thieu animation:
--   Tapion 481/482/483, Vua Vegeta 36/37/38, Whis 505/506/507.
--
-- Backup DB truoc khi import. Restart Server Java sau import.
-- ============================================================

USE ngocrong;

DELIMITER $$

DROP PROCEDURE IF EXISTS nro_install_4_event_costumes_safe_20261028$$
CREATE PROCEDURE nro_install_4_event_costumes_safe_20261028()
BEGIN
    DECLARE v_min_id INT DEFAULT 0;
    DECLARE v_max_id INT DEFAULT 0;
    DECLARE v_count_id INT DEFAULT 0;

    DECLARE v_chichi INT;
    DECLARE v_champa INT;
    DECLARE v_cadic INT;
    DECLARE v_bill INT;

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
        WHERE table_schema=DATABASE() AND table_name='referral_costume_catalog'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Thieu referral_costume_catalog. Import migration referral event truoc.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM item_template
        WHERE NAME IN (
            'Cải Trang Chi Chi Event',
            'Cải Trang Champa Event',
            'Cải Trang Cađíc Event',
            'Cải Trang Bill Event',
            'Cải Trang Whis Event',
            'Cải Trang Tapion Event',
            'Cải Trang Vua Vegeta Event'
        )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Cai trang event da ton tai. Neu da import ban cu, dung file fix_referral_event_costume_mapping.sql.';
    END IF;

    SELECT MIN(id),MAX(id),COUNT(*)
    INTO v_min_id,v_max_id,v_count_id
    FROM item_template;

    IF v_min_id<>0 OR v_count_id<>(v_max_id+1) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='item_template co khoang trong ID, dung import de tranh sai ITEM_TEMPLATES.get(id).';
    END IF;

    SET v_chichi=v_max_id+1;
    SET v_champa=v_max_id+2;
    SET v_cadic=v_max_id+3;
    SET v_bill=v_max_id+4;

    IF v_bill>32767 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='ID item moi vuot gioi han short 32767.';
    END IF;

    -- Kiem tra part + TYPE.
    IF NOT EXISTS (SELECT 1 FROM part WHERE id=1330 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1331 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=1332 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bo part Chi Chi 1330/1331/1332';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=511 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=512 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=513 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bo part Champa 511/512/513';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=645 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=646 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=647 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bo part Cadic 645/646/647';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM part WHERE id=508 AND TYPE=0)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=509 AND TYPE=1)
       OR NOT EXISTS (SELECT 1 FROM part WHERE id=510 AND TYPE=2) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Thieu bo part Bill 508/509/510';
    END IF;

    -- Khong dung chung ngoai hinh voi cai trang dang ton tai.
    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=1330 AND body=1331 AND leg=1332) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Chi Chi da duoc item cai trang khac su dung';
    END IF;
    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=511 AND body=512 AND leg=513) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Champa da duoc item cai trang khac su dung';
    END IF;
    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=645 AND body=646 AND leg=647) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Cadic da duoc item cai trang khac su dung';
    END IF;
    IF EXISTS (SELECT 1 FROM cai_trang WHERE head=508 AND body=509 AND leg=510) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ngoai hinh Bill da duoc item cai trang khac su dung';
    END IF;

    INSERT INTO item_template
        (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require)
    VALUES
        (v_chichi,5,3,'Cải Trang Chi Chi Event',
         'Cải trang độc quyền sự kiện giới thiệu',12141,-1,0,0),
        (v_champa,5,3,'Cải Trang Champa Event',
         'Cải trang độc quyền sự kiện giới thiệu',4879,-1,0,0),
        (v_cadic,5,3,'Cải Trang Cađíc Event',
         'Cải trang độc quyền sự kiện giới thiệu',6027,-1,0,0),
        (v_bill,5,3,'Cải Trang Bill Event',
         'Cải trang độc quyền sự kiện giới thiệu',4847,-1,0,0);

    INSERT INTO cai_trang(id_temp,head,body,leg,bag)
    VALUES
        (v_chichi,1330,1331,1332,-1),
        (v_champa,511,512,513,-1),
        (v_cadic,645,646,647,-1),
        (v_bill,508,509,510,-1);

    INSERT INTO referral_costume_catalog
        (milestone,item_template_id,costume_name,duration_days,options_json)
    VALUES
        (5,v_chichi,'Chi Chi Event',7,
         '[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'),
        (10,v_champa,'Champa Event',7,
         '[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'),
        (15,v_cadic,'Cađíc Event',NULL,
         '[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'),
        (20,v_bill,'Bill Event',NULL,
         '[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]')
    ON DUPLICATE KEY UPDATE
        item_template_id=VALUES(item_template_id),
        costume_name=VALUES(costume_name),
        duration_days=VALUES(duration_days),
        options_json=VALUES(options_json);

    -- Bao dam 4 ID moi khong bi dua vao shop.
    IF EXISTS (
        SELECT 1 FROM item_shop
        WHERE temp_id IN (v_chichi,v_champa,v_cadic,v_bill)
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT='Phat hien ID event bi dua vao item_shop';
    END IF;
END$$
DELIMITER ;

CALL nro_install_4_event_costumes_safe_20261028();
DROP PROCEDURE nro_install_4_event_costumes_safe_20261028;

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
-- Sau import: TAT HAN Server Java, build/chay lai de nap item_template + cai_trang.
