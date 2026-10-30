-- NRO: CHỈ bổ sung 3 kẹo TNSM mới, KHÔNG tạo/khôi phục vật phẩm gốc.
-- Giữ nguyên bùa 213/671/672, buff 381-384, thức ăn 663-667.
-- Sao lưu DB ngocrong trước khi chạy trên VPS.
-- Nếu ID 2154-2156 đã bị chiếm dụng, SQL không ghi đè; kiểm tra SELECT cuối file.
-- Java UseItem.java trên repo xử lý hiệu ứng 30 phút cho ba kẹo này.
USE `ngocrong`;
START TRANSACTION;

-- Ba món ăn THÊM RIÊNG cho server NRO của bạn; không phải vật phẩm của TeaMobi.
-- Dùng icon Bùa Trí Tuệ hiện có để không đòi thêm tài nguyên ảnh.
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 2154,29,3,'Kẹo TNSM x2','Ăn để nhân 2 TNSM trong 30 phút; không cộng dồn với bùa trí tuệ khác',1403,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=2154)
AND NOT EXISTS (SELECT 1 FROM `item_template` WHERE `NAME`='Kẹo TNSM x2');
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 2155,29,3,'Kẹo TNSM x3','Ăn để nhân 3 TNSM trong 30 phút; không cộng dồn với bùa trí tuệ khác',1406,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=2155)
AND NOT EXISTS (SELECT 1 FROM `item_template` WHERE `NAME`='Kẹo TNSM x3');
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 2156,29,3,'Kẹo TNSM x4','Ăn để nhân 4 TNSM trong 30 phút; không cộng dồn với bùa trí tuệ khác',1407,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=2156)
AND NOT EXISTS (SELECT 1 FROM `item_template` WHERE `NAME`='Kẹo TNSM x4');
COMMIT;

-- Kết quả 3 kẹo phải đúng ID + TYPE + NAME. Nếu khác, DỪNG và không cấp item.
SELECT `id`,`TYPE`,`NAME`,`icon_id`,
 CASE WHEN (`id`=2154 AND `NAME`='Kẹo TNSM x2' AND `TYPE`=29)
 OR (`id`=2155 AND `NAME`='Kẹo TNSM x3' AND `TYPE`=29)
 OR (`id`=2156 AND `NAME`='Kẹo TNSM x4' AND `TYPE`=29)
 THEN 'OK' ELSE 'XUNG_DOT_ID' END AS `trang_thai`
FROM `item_template`
WHERE `id` IN (2154,2155,2156)
ORDER BY `id`;
