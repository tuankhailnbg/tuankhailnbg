-- NRO: 4 ID cai trang event + giftcode thu nghiem (server client assets HIEN CO)
-- Source baseline ngocrong.sql: item_template ID lien tuc 0..2153. 
-- QUAN TRONG: ItemService.getTemplate(id) = ITEM_TEMPLATES.get(id), vi vay bat buoc ID lien tuc.
-- Migration CHI chay khi chua co ID 2154..2157; khong ghi de vat pham hien huu.
-- Chay SAU 20261028_referral_event_kame.sql va 20261028_referral_manual_points.sql.
-- Khong ghep nham: cac MODEL (head/body/leg) dang dung la sprite TẠM DE XEM TEST:
-- Goku SSJ3: model Black Goku SSJ White (904), icon SSJ3 White (1145).
-- Vegito Blue: model Goku Ultra (2117).
-- Gogeta Blue: model Gogeta SSJ4 (2142).
-- Zeno: model Zamasu Toi Thuong (1276).
-- De hien thi DUNG 4 nhan vat can bo sung sprites head/body/leg + icon cho client sau.
-- De tranh cap nham mau, script kiem tra tu dong ID data truoc khi INSERT.

DELIMITER $$
DROP PROCEDURE IF EXISTS nro_install_referral_costumes_20261028$$
CREATE PROCEDURE nro_install_referral_costumes_20261028()
BEGIN
  DECLARE v_count INT;
  DECLARE v_max INT;
  SELECT COUNT(*), COALESCE(MAX(id),-1) INTO v_count,v_max FROM item_template;
  IF v_count <> v_max+1 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT='item_template ID khong lien tuc: chua the cai cai trang';
  END IF;
  IF EXISTS(
    SELECT 1 FROM item_template WHERE id BETWEEN 2154 AND 2157
      AND NAME NOT IN ('Cải Trang Goku SSJ3 - GT','Cải Trang Vegito Blue - GT',
                       'Cải Trang Gogeta Blue - GT','Cải Trang Zeno - GT')
  ) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT='ID 2154-2157 da co vat pham khac, KHONG ghi de';
  END IF;
  IF v_max < 2153 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT='Du lieu item_template thieu ID baseline 2153';
  END IF;
  -- Neu DB da co ID lon hon 2157 thi day khong la ban chua cai; dung de kiem tra.
  IF v_max > 2157 AND NOT EXISTS(
     SELECT 1 FROM item_template WHERE id=2154 AND NAME='Cải Trang Goku SSJ3 - GT'
  ) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT='DB da co vat pham moi: can chon ID tuong thich truoc khi cai';
  END IF;

  INSERT INTO item_template (id,TYPE,gender,NAME,description,icon_id,part,is_up_to_up,power_require)
  VALUES
(2154, 5, 3, 'Cải Trang Goku SSJ3 - GT', 'Cai trang gioi thieu - giao dien test', 10711, -1, 0, 0),
(2155, 5, 3, 'Cải Trang Vegito Blue - GT', 'Cai trang gioi thieu - giao dien test', 18532, -1, 0, 0),
(2156, 5, 3, 'Cải Trang Gogeta Blue - GT', 'Cai trang gioi thieu - giao dien test', 14047, -1, 0, 0),
(2157, 5, 3, 'Cải Trang Zeno - GT', 'Cai trang gioi thieu - giao dien test', 14015, -1, 0, 0)
  ON DUPLICATE KEY UPDATE id=VALUES(id);

  INSERT INTO cai_trang(id_temp,head,body,leg,bag)
  VALUES
(2154,906,880,881,-1),
(2155,1053,1054,1055,-1),
(2156,1434,1435,1436,-1),
(2157,838,839,840,-1)
  ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),leg=VALUES(leg),bag=VALUES(bag);
END$$
DELIMITER ;
CALL nro_install_referral_costumes_20261028();
DROP PROCEDURE nro_install_referral_costumes_20261028;

-- Lien ket 4 moc gioi thieu voi template moi; id trung hop se bi ngan o tren.
UPDATE referral_costume_catalog SET item_template_id=2154 WHERE milestone=5;
UPDATE referral_costume_catalog SET item_template_id=2155 WHERE milestone=10;
UPDATE referral_costume_catalog SET item_template_id=2156 WHERE milestone=15;
UPDATE referral_costume_catalog SET item_template_id=2157 WHERE milestone=20;

-- Code thu nghiem: moi nhan vat nhan 1 lan; dung trong game tai "Ma Qua Tang".
-- Phat 4 cai trang cung luc voi chi so thuc te, 7 ngay cho moc 5 va 10.
INSERT INTO gift_codes(type,code,gold,gem,ruby,items,status,active,expires_at,created_at,updated_at)
SELECT 1,'nrotest4ct2026',0,0,0,'[{"id":2154,"quantity":1,"options":[{"id":77,"param":20},{"id":103,"param":20},{"id":50,"param":20},{"id":14,"param":4},{"id":101,"param":10},{"id":30,"param":0},{"id":93,"param":7}]},{"id":2155,"quantity":1,"options":[{"id":77,"param":23},{"id":103,"param":23},{"id":50,"param":23},{"id":14,"param":6},{"id":101,"param":13},{"id":8,"param":5},{"id":30,"param":0},{"id":93,"param":7}]},{"id":2156,"quantity":1,"options":[{"id":77,"param":25},{"id":103,"param":25},{"id":50,"param":25},{"id":14,"param":8},{"id":101,"param":17},{"id":8,"param":8},{"id":30,"param":0}]},{"id":2157,"quantity":1,"options":[{"id":77,"param":27},{"id":103,"param":27},{"id":50,"param":27},{"id":14,"param":10},{"id":101,"param":20},{"id":8,"param":10},{"id":30,"param":0}]}]',0,0,NULL,NOW(),NOW()
WHERE NOT EXISTS(SELECT 1 FROM gift_codes WHERE code='nrotest4ct2026');

SELECT id, TYPE, NAME, icon_id FROM item_template WHERE id BETWEEN 2154 AND 2157;
SELECT id_temp,head,body,leg FROM cai_trang WHERE id_temp BETWEEN 2154 AND 2157;
SELECT code,items,status,type FROM gift_codes WHERE code='nrotest4ct2026';
