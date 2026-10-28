-- ============================================================
-- BAO CAO CAI TRANG CO SAN CHUA DUNG TRONG SHOP / QUA SU KIEN
-- Khong sua database. Chi SELECT.
--
-- Tieu chi "co the dung an toan":
--   * item_template.TYPE = 5
--   * co mapping cai_trang head/body/leg
--   * head/body/leg dung TYPE va du 3/17/14 frame
--   * icon item trung frame dau cua head
--   * khong nam trong item_shop
--   * khong dung lam item_exchange cua shop
--   * khong nam trong referral_costume_catalog hien tai
--   * khong nam trong gift_codes
--   * loai cac cai trang da thay phat truc tiep trong NpcFactory:
--       884 Hit, 2052 Cell Max, 2128 Goten, 2129 Fu
--   * loai item co description ghi ro "su kien"
--
-- Day la loc bao thu. Neu code Java co reward dong/ngaunhien khong ghi ID
-- truc tiep thi van can doi chieu them truoc khi chon item cu the.
-- ============================================================

USE ngocrong;

DROP TEMPORARY TABLE IF EXISTS tmp_unused_costumes;

CREATE TEMPORARY TABLE tmp_unused_costumes AS
SELECT
    i.id,
    i.NAME,
    i.description,
    i.icon_id,
    c.head,
    c.body,
    c.leg
FROM item_template i
JOIN cai_trang c ON c.id_temp=i.id
JOIN part ph ON ph.id=c.head AND ph.TYPE=0
JOIN part pb ON pb.id=c.body AND pb.TYPE=1
JOIN part pl ON pl.id=c.leg AND pl.TYPE=2
WHERE i.TYPE=5
  AND c.head>=0 AND c.body>=0 AND c.leg>=0
  AND JSON_LENGTH(ph.DATA)=3
  AND JSON_LENGTH(pb.DATA)=17
  AND JSON_LENGTH(pl.DATA)=14
  AND i.icon_id=CAST(JSON_UNQUOTE(JSON_EXTRACT(ph.DATA,'$[0][0]')) AS UNSIGNED)

  -- Khong nam trong shop / khong lam vat pham doi shop.
  AND NOT EXISTS (
      SELECT 1 FROM item_shop s
      WHERE s.temp_id=i.id OR s.item_exchange=i.id
  )

  -- Khong phai 4 qua referral dang su dung.
  AND NOT EXISTS (
      SELECT 1 FROM referral_costume_catalog r
      WHERE r.item_template_id=i.id
  )

  -- Khong nam trong giftcode.
  AND NOT EXISTS (
      SELECT 1 FROM gift_codes g
      WHERE g.items IS NOT NULL
        AND g.items REGEXP CONCAT('"id"[[:space:]]*:[[:space:]]*', i.id, '([^0-9]|$)')
  )

  -- Costume dang/da duoc phat truc tiep trong NpcFactory.
  AND i.id NOT IN (884,2052,2128,2129)

  -- Loc bao thu cac item duoc danh dau la vat pham su kien.
  AND COALESCE(i.description,'') NOT LIKE '%sự kiện%';

-- Tong so ung vien con lai.
SELECT COUNT(*) AS so_cai_trang_chua_dung_bao_thu
FROM tmp_unused_costumes;

-- Danh sach chi tiet.
SELECT id,NAME,description,icon_id,head,body,leg
FROM tmp_unused_costumes
ORDER BY id;

DROP TEMPORARY TABLE IF EXISTS tmp_unused_costumes;
