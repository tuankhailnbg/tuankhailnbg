-- ==============================================================================
-- Migration: fix_original_activation_sets.sql
-- Mục đích: Chuẩn hóa 9 Set Kích Hoạt gốc Ngọc Rồng Online (Option IDs 127 - 144)
-- Dự án: Nro Yanii / Ngọc Rồng Online
-- ==============================================================================

-- 1. Sao lưu dữ liệu hiện tại trước khi cập nhật
CREATE TABLE IF NOT EXISTS `item_option_template_backup_skh` AS 
SELECT * FROM `item_option_template` WHERE `id` BETWEEN 127 AND 144;

-- 2. Chuẩn hóa tên 9 Set Kích Hoạt gốc (Options 127 - 135)
-- Trái Đất
UPDATE `item_option_template` SET `NAME` = 'Set Thên Xin Hăng' WHERE `id` = 127;
UPDATE `item_option_template` SET `NAME` = 'Set Kirin' WHERE `id` = 128;
UPDATE `item_option_template` SET `NAME` = 'Set Sôngôku' WHERE `id` = 129;

-- Namếc
UPDATE `item_option_template` SET `NAME` = 'Set Picolo' WHERE `id` = 130;
UPDATE `item_option_template` SET `NAME` = 'Set Ốc Tiêu' WHERE `id` = 131;
UPDATE `item_option_template` SET `NAME` = 'Set Pikkoro Daimao' WHERE `id` = 132;

-- Xayda
UPDATE `item_option_template` SET `NAME` = 'Set Kakarot' WHERE `id` = 133;
UPDATE `item_option_template` SET `NAME` = 'Set Ca Đíc' WHERE `id` = 134;
UPDATE `item_option_template` SET `NAME` = 'Set Nappa' WHERE `id` = 135;

-- 3. Chuẩn hóa mô tả hiệu ứng đủ 5 món (Options 136 - 144)
UPDATE `item_option_template` SET `NAME` = '(5 món +100% sát thương đấm Galick)' WHERE `id` = 136;
UPDATE `item_option_template` SET `NAME` = '(5 món x5 thời gian hóa khỉ)' WHERE `id` = 137;
UPDATE `item_option_template` SET `NAME` = '(5 món +100% HP)' WHERE `id` = 138;
UPDATE `item_option_template` SET `NAME` = '(5 món x2 thời gian chói mắt)' WHERE `id` = 139;
UPDATE `item_option_template` SET `NAME` = '(5 món +150% sát thương Quả Cầu Kênh Khí)' WHERE `id` = 140;
UPDATE `item_option_template` SET `NAME` = '(5 món +100% sát thương Kamejoko)' WHERE `id` = 141;
UPDATE `item_option_template` SET `NAME` = '(5 món +150% KI)' WHERE `id` = 142;
UPDATE `item_option_template` SET `NAME` = '(5 món +100% sát thương Liên Hoàn)' WHERE `id` = 143;
UPDATE `item_option_template` SET `NAME` = '(5 món +150% sát thương Đẻ Trứng)' WHERE `id` = 144;

-- 4. Xác nhận kết quả
SELECT `id`, `NAME`, `TYPE` FROM `item_option_template` WHERE `id` BETWEEN 127 AND 144;
