-- Chay trong HeidiSQL/phpMyAdmin tren DB ngocrong (khong thay doi du lieu).
SELECT DATABASE() AS current_database, CURRENT_USER() AS database_user, VERSION() AS mariadb_version;
SHOW GRANTS;
SELECT table_name FROM information_schema.tables
 WHERE table_schema=DATABASE() AND table_name IN
 ('referral_invites','referral_points','referral_points_audit',
 'referral_claims','referral_costume_catalog','referral_costume_claims')
 ORDER BY table_name;
SELECT table_name, column_name, data_type
 FROM information_schema.columns
 WHERE table_schema=DATABASE()
 AND table_name IN ('referral_points','referral_claims','referral_costume_catalog',
 'referral_costume_claims','referral_invites','referral_points_audit')
 ORDER BY table_name,ordinal_position;
SELECT id,NAME,TYPE,icon_id FROM item_template WHERE id BETWEEN 2154 AND 2157;
-- Neu 6 bang core khong du, chay 2 migration core trong database/migrations
-- 20261028_referral_event_kame.sql, 20261028_referral_manual_points.sql.
-- Chi chay costume migration khi da sao luu DB va xac nhan ID 2154..2157 chua xung dot.
