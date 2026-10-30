-- NRO: bổ sung/khôi phục vật phẩm buff + kẹo ăn TNSM x2/x3/x4
-- Chạy trong database ngocrong bằng phpMyAdmin, sau khi SAO LƯU.
-- Cam kết: KHÔNG UPDATE/DELETE item cũ, KHÔNG ghi đè ID đã có.
-- SQL sẽ không thêm kẹo nếu ID 2154-2156 đang được sử dụng.
-- Quan trọng: code Java mới tương ứng đã nằm trên GitHub.
USE `ngocrong`;
START TRANSACTION;

-- Khôi phục template chuẩn đang thiếu, giữ nguyên bản đã có.
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 213,13,3,'Bùa Trí Tuệ','Tiềm năng và sức mạnh nhận được gấp đôi khi bùa có hiệu lực',1403,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=213);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 671,13,3,'Bùa Trí Tuệ x3','Tiềm năng và sức mạnh nhận được gấp ba khi bùa có hiệu lực',1406,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=671);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 672,13,3,'Bùa Trí Tuệ x4','Tiềm năng và sức mạnh nhận được gấp bốn khi bùa có hiệu lực',1407,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=672);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 381,29,3,'Cuồng nộ','Trong vòng tối đa 10 phút +100% sức đánh gốc',2754,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=381);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 382,29,3,'Bổ huyết','Trong vòng tối đa 10 phút +100% HP',2755,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=382);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 383,29,3,'Bổ khí','Trong vòng tối đa 10 phút +100% KI',2756,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=383);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 384,29,3,'Giáp Xên bọ hung','Trong vòng tối đa 10 phút giảm 50% sát thương',2757,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=384);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 663,29,3,'Bánh Pudding','Ăn để tăng 10% sức đánh cho sư phụ và đệ tử tối đa 10 phút',6324,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=663);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 664,29,3,'Xúc xích','Ăn để tăng 10% sức đánh cho sư phụ và đệ tử tối đa 10 phút',6325,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=664);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 665,29,3,'Kem dâu','Ăn để tăng 10% sức đánh cho sư phụ và đệ tử tối đa 10 phút',6326,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=665);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 666,29,3,'Mì ly','Ăn để tăng 10% sức đánh cho sư phụ và đệ tử tối đa 10 phút',6327,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=666);
INSERT INTO `item_template` (`id`,`TYPE`,`gender`,`NAME`,`description`,`icon_id`,`part`,`is_up_to_up`,`power_require`)
SELECT 667,29,3,'Sushi','Ăn để tăng 10% sức đánh cho sư phụ và đệ tử tối đa 10 phút',6328,-1,0,0
WHERE NOT EXISTS (SELECT 1 FROM `item_template` WHERE `id`=667);

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
