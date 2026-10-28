-- SUA LIEN KET CAI TRANG GIOI THIEU - TUONG THICH CLIENT.
-- Khong lien ket lai 2154..2157 nua vi client hien tai co the khong co template cac ID moi.
-- Dung item TYPE=5 da ton tai san trong client/database goc.
-- NEN DUNG SERVER truoc khi import va backup DB.

SELECT id,NAME,TYPE,icon_id,part
FROM item_template
WHERE id IN (904,2117,2142,1276,2154,2155,2156,2157)
ORDER BY id;

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=904 AND i.TYPE=5
SET c.item_template_id=904
WHERE c.milestone=5
  AND (c.item_template_id IS NULL OR c.item_template_id IN (2154,2155,2156,2157));

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=2117 AND i.TYPE=5
SET c.item_template_id=2117
WHERE c.milestone=10
  AND (c.item_template_id IS NULL OR c.item_template_id IN (2154,2155,2156,2157));

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=2142 AND i.TYPE=5
SET c.item_template_id=2142
WHERE c.milestone=15
  AND (c.item_template_id IS NULL OR c.item_template_id IN (2154,2155,2156,2157));

UPDATE referral_costume_catalog c
JOIN item_template i ON i.id=1276 AND i.TYPE=5
SET c.item_template_id=1276
WHERE c.milestone=20
  AND (c.item_template_id IS NULL OR c.item_template_id IN (2154,2155,2156,2157));

SELECT c.milestone,c.costume_name,c.item_template_id,c.duration_days,c.options_json,
       i.NAME AS item_name,i.TYPE AS item_type
FROM referral_costume_catalog c
LEFT JOIN item_template i ON i.id=c.item_template_id
WHERE c.milestone IN (5,10,15,20)
ORDER BY c.milestone;

-- KHONG xoa item_template 2154..2157 de tranh lam hong tinh lien tuc cua Manager.ITEM_TEMPLATES.
