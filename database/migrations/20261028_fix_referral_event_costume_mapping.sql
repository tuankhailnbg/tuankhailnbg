-- ============================================================
-- FIX SU KIEN GIOI THIEU: KHOI PHUC 4 CAI TRANG EVENT MOI
-- Dung khi:
--   * Da import 20261028_referral_4_new_event_costumes_safe.sql
--   * Nhưng Java cu da doi catalog ve item shop 904/2117/2142/1276
--   * Hoac icon event dang dung icon khong giong hinh dang.
--
-- File nay:
--   * KHONG tao ID moi
--   * KHONG xoa item
--   * KHONG reset Thoi Vang
--   * KHONG xoa lich su claim
--   * Chi tim dung 4 item event theo TEN, sua icon va gan lai catalog.
--
-- Backup DB truoc khi import.
-- ============================================================

USE ngocrong;

-- 1) Phai co dung 4 item event, moi ten chi 1 dong.
SELECT id, NAME, TYPE, icon_id
FROM item_template
WHERE NAME IN (
  'Cải Trang Whis Event',
  'Cải Trang Champa Event',
  'Cải Trang Tapion Event',
  'Cải Trang Vua Vegeta Event'
)
ORDER BY id;

-- 2) Icon lấy trực tiếp từ frame đầu của HEAD tương ứng:
-- Whis head 505 -> small image 4843
-- Champa head 511 -> small image 4879
-- Tapion head 481 -> small image 4665
-- Vua Vegeta head 36 -> small image 549
UPDATE item_template
SET icon_id=4843
WHERE NAME='Cải Trang Whis Event' AND TYPE=5;

UPDATE item_template
SET icon_id=4879
WHERE NAME='Cải Trang Champa Event' AND TYPE=5;

UPDATE item_template
SET icon_id=4665
WHERE NAME='Cải Trang Tapion Event' AND TYPE=5;

UPDATE item_template
SET icon_id=549
WHERE NAME='Cải Trang Vua Vegeta Event' AND TYPE=5;

-- 3) Đảm bảo mapping hình dáng đúng từng nhân vật.
-- Chỉ insert nếu chưa có id_temp tương ứng.
INSERT INTO cai_trang(id_temp,head,body,leg,bag)
SELECT id,505,506,507,-1 FROM item_template
WHERE NAME='Cải Trang Whis Event' AND TYPE=5
ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),leg=VALUES(leg),bag=VALUES(bag);

INSERT INTO cai_trang(id_temp,head,body,leg,bag)
SELECT id,511,512,513,-1 FROM item_template
WHERE NAME='Cải Trang Champa Event' AND TYPE=5
ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),leg=VALUES(leg),bag=VALUES(bag);

INSERT INTO cai_trang(id_temp,head,body,leg,bag)
SELECT id,481,482,483,-1 FROM item_template
WHERE NAME='Cải Trang Tapion Event' AND TYPE=5
ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),leg=VALUES(leg),bag=VALUES(bag);

INSERT INTO cai_trang(id_temp,head,body,leg,bag)
SELECT id,36,37,38,-1 FROM item_template
WHERE NAME='Cải Trang Vua Vegeta Event' AND TYPE=5
ON DUPLICATE KEY UPDATE head=VALUES(head),body=VALUES(body),leg=VALUES(leg),bag=VALUES(bag);

-- 4) Gắn lại đúng 4 item EVENT vào catalog.
-- Không dùng lại item shop.
UPDATE referral_costume_catalog c
JOIN item_template i ON i.NAME='Cải Trang Whis Event' AND i.TYPE=5
SET c.item_template_id=i.id,
    c.costume_name='Whis Event',
    c.duration_days=7,
    c.options_json='[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'
WHERE c.milestone=5;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.NAME='Cải Trang Champa Event' AND i.TYPE=5
SET c.item_template_id=i.id,
    c.costume_name='Champa Event',
    c.duration_days=7,
    c.options_json='[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'
WHERE c.milestone=10;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.NAME='Cải Trang Tapion Event' AND i.TYPE=5
SET c.item_template_id=i.id,
    c.costume_name='Tapion Event',
    c.duration_days=NULL,
    c.options_json='[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'
WHERE c.milestone=15;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.NAME='Cải Trang Vua Vegeta Event' AND i.TYPE=5
SET c.item_template_id=i.id,
    c.costume_name='Vua Vegeta Event',
    c.duration_days=NULL,
    c.options_json='[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]'
WHERE c.milestone=20;

-- 5) Kiểm tra xung đột ID và mapping.
-- Kết quả đúng:
--   * 4 id khác nhau
--   * TYPE=5
--   * mỗi id_temp chỉ có 1 mapping
--   * catalog 5/10/15/20 trỏ đúng 4 item event
SELECT
    i.id,
    i.NAME,
    i.TYPE,
    i.icon_id,
    c.head,
    c.body,
    c.leg,
    c.bag
FROM item_template i
LEFT JOIN cai_trang c ON c.id_temp=i.id
WHERE i.NAME IN (
  'Cải Trang Whis Event',
  'Cải Trang Champa Event',
  'Cải Trang Tapion Event',
  'Cải Trang Vua Vegeta Event'
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
HAVING COUNT(*) > 1;

-- Nếu SELECT cuối trả 0 dòng => 4 mốc không dùng trùng item ID.
--
-- Sau import:
-- 1) git pull origin main
-- 2) build lại Java
-- 3) TẮT HẲN Server và chạy lại để nạp item_template/cai_trang mới.
-- 4) Không cần xóa referral_costume_claims:
--    Java mới sẽ tự cho nhận lại nếu claim cũ là item ID khác với catalog hiện tại.
