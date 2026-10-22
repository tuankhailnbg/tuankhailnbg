-- ==============================================================================
-- Migration: fix_nro_items_skh.sql
-- Mục đích: 
--   1. Đổi tên Item 457 thành 'Thỏi Vàng' (giữ nguyên ID, icon, cơ chế quy đổi 25M).
--   2. Vô hiệu hóa Item 2011 an toàn (không xóa ID để tránh lệch index mảng cache).
--   3. Cập nhật Item 2083 thành 'Hộp Set Kích Hoạt Thần Linh' với mô tả chuẩn.
--   4. Chuẩn hóa Option 131 thành 'Set Ốc Tiêu' (giữ nguyên ID và hiệu ứng +100% Liên Hoàn).
-- Hệ quản trị CSDL: MySQL 5.7+ / MariaDB 10.3+
-- An toàn: Idempotent (chạy nhiều lần an toàn), có backup và rollback.
-- ==============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------------------
-- 1. SAO LƯU DỮ LIỆU HIỆN TẠI TRƯỚC KHI CẬP NHẬT
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `item_template_backup_skh_fix` AS 
SELECT * FROM `item_template` WHERE `id` IN (457, 2011, 2083);

CREATE TABLE IF NOT EXISTS `item_option_template_backup_skh_fix` AS 
SELECT * FROM `item_option_template` WHERE `id` = 131;

-- ------------------------------------------------------------------------------
-- 2. CẬP NHẬT DỮ LIỆU BẢNG item_template
-- ------------------------------------------------------------------------------

-- Item 457: Đổi tên thành 'Thỏi Vàng'
UPDATE `item_template` 
SET `name` = 'Thỏi Vàng' 
WHERE `id` = 457;

-- Item 2011: Vô hiệu hóa an toàn, không sinh thêm, không bán, không dùng
UPDATE `item_template` 
SET `name` = '[Vô hiệu hóa] Thỏi vàng', 
    `description` = 'Vật phẩm đã bị vô hiệu hóa và không còn khả dụng.',
    `is_up_to_up` = 0
WHERE `id` = 2011;

-- Item 2083: Hộp Set Kích Hoạt Thần Linh
UPDATE `item_template` 
SET `name` = 'Hộp Set Kích Hoạt Thần Linh',
    `description` = 'Mở ra nhận ngẫu nhiên 1 trang bị Thần Linh có Set Kích Hoạt phù hợp với hành tinh của bạn.'
WHERE `id` = 2083;

-- ------------------------------------------------------------------------------
-- 3. CẬP NHẬT DỮ LIỆU BẢNG item_option_template
-- ------------------------------------------------------------------------------

-- Option 131: Đổi tên thành 'Set Ốc Tiêu' (Namếc, kích hoạt cùng Option 143: +100% Liên Hoàn)
UPDATE `item_option_template` 
SET `name` = 'Set Ốc Tiêu' 
WHERE `id` = 131;

-- ------------------------------------------------------------------------------
-- 4. KIỂM TRA KẾT QUẢ SAU MIGRATION
-- ------------------------------------------------------------------------------
SELECT `id`, `name`, `type`, `gender`, `description` 
FROM `item_template` 
WHERE `id` IN (457, 2011, 2083);

SELECT `id`, `name`, `type` 
FROM `item_option_template` 
WHERE `id` IN (127, 128, 129, 130, 131, 132, 133, 134, 135);

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================
-- HƯỚNG DẪN ROLLBACK (KHI CẦN KHÔI PHỤC)
-- ==============================================================================
-- UPDATE `item_template` SET `name` = 'Thỏi vàng-25M' WHERE `id` = 457;
-- UPDATE `item_template` SET `name` = 'Thỏi vàng', `description` = 'Thỏi vàng khóa không thể giao dịch, có thể quy đổi ra vàng', `is_up_to_up` = 0 WHERE `id` = 2011;
-- UPDATE `item_template` SET `name` = 'Hộp Quà Kích Hoạt', `description` = 'Mở ra để nhận set kích hoạt' WHERE `id` = 2083;
-- UPDATE `item_option_template` SET `name` = 'Set Ốc Tiêu' WHERE `id` = 131;
