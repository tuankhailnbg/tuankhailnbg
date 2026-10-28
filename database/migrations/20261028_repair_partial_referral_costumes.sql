-- Sửa lỗi import dở: 4 item 2154..2157 đã được thêm vào item_template,
-- nhưng import dừng ở UPDATE referral_costume_catalog (#1146).
-- YÊU CẦU: đã import 20261028_referral_event_kame.sql và
-- 20261028_referral_manual_points.sql, đã backup DB trước khi thao tác.
-- KHÔNG ghi đè vật phẩm khác ID / tên. Không thay đổi túi đồ người chơi.
-- Sau khi chạy, RESTART Server để nạp template item mới vào RAM.

-- Đối chiếu item gốc có tồn tại và đúng loại cải trang TYPE=5:
SELECT id, NAME, TYPE, icon_id, part FROM item_template
WHERE id BETWEEN 2154 AND 2157 ORDER BY id;

-- Chỉ gắn những item đúng ID, đúng tên và TYPE=5.
-- Nếu catalog đang gắn một ID khác thì giữ nguyên, không tự ghi đè.
UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=2154 AND i.TYPE=5 AND i.NAME='Cải Trang Goku SSJ3 - GT'
SET c.item_template_id=2154
WHERE c.milestone=5 AND c.item_template_id IS NULL;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=2155 AND i.TYPE=5 AND i.NAME='Cải Trang Vegito Blue - GT'
SET c.item_template_id=2155
WHERE c.milestone=10 AND c.item_template_id IS NULL;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=2156 AND i.TYPE=5 AND i.NAME='Cải Trang Gogeta Blue - GT'
SET c.item_template_id=2156
WHERE c.milestone=15 AND c.item_template_id IS NULL;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=2157 AND i.TYPE=5 AND i.NAME='Cải Trang Zeno - GT'
SET c.item_template_id=2157
WHERE c.milestone=20 AND c.item_template_id IS NULL;

-- Kỳ vọng: 4 dòng có item_template_id lần lượt 2154,2155,2156,2157.
SELECT c.milestone,c.costume_name,c.item_template_id,c.duration_days,c.options_json,
       i.NAME AS item_name,i.TYPE AS item_type
FROM referral_costume_catalog c
LEFT JOIN item_template i ON i.id=c.item_template_id
WHERE c.milestone IN (5,10,15,20)
ORDER BY c.milestone;

-- Nếu NULL sau UPDATE: thiếu item hoặc item khác tên/TYPE. Không đoán ID.
-- Kiểm tra lịch sử phát, KHÔNG XÓA/RESET bảng referral_costume_claims.
