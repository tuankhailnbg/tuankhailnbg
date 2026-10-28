-- ============================================================
-- NRO - XOA BRANDING "Nro Yanii" KHOI NOI DUNG HIEN THI
-- Database: ngocrong / MariaDB
--
-- Chi sua text hien thi, KHONG xoa item, KHONG doi ID, KHONG doi option.
-- Sau import can restart Server Java de item_template duoc nap lai.
-- ============================================================

USE ngocrong;

-- 1) Xoa branding khoi mo ta item.
-- Xu ly ca dang:
--   Nro Yanii
--   Nro Yanii, ...
--   ...\r\nNro Yanii
UPDATE item_template
SET description = TRIM(
    REPLACE(
        REPLACE(
            REPLACE(
                REPLACE(description, 'Nro Yanii, ', ''),
                'Nro Yanii,', ''
            ),
            'Nro Yanii', ''
        ),
        CHAR(13), ''
    )
)
WHERE description LIKE '%Nro Yanii%';

UPDATE item_template
SET description = TRIM(REPLACE(description, CHAR(10), ' '))
WHERE description LIKE CONCAT('%', CHAR(10), '%');

-- 2) Xoa branding khoi task placeholder neu co.
UPDATE task_main_template
SET NAME='', detail=''
WHERE NAME LIKE '%Nro Yanii%' OR detail LIKE '%Nro Yanii%';

UPDATE task_sub_template
SET NAME=''
WHERE NAME LIKE '%Nro Yanii%';

-- 3) Kiem tra: tat ca ket qua phai = 0.
SELECT COUNT(*) AS item_con_nro_yanii
FROM item_template
WHERE description LIKE '%Nro Yanii%'
   OR NAME LIKE '%Nro Yanii%';

SELECT COUNT(*) AS task_main_con_nro_yanii
FROM task_main_template
WHERE NAME LIKE '%Nro Yanii%' OR detail LIKE '%Nro Yanii%';

SELECT COUNT(*) AS task_sub_con_nro_yanii
FROM task_sub_template
WHERE NAME LIKE '%Nro Yanii%';

-- Kiem tra rieng Thoi Vang.
SELECT id,NAME,description
FROM item_template
WHERE id=457;
