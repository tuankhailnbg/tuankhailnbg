# Sự kiện Đua Top NRO

## 1. Có cần import database không?

Có. Import **1 file duy nhất**:

`NRO_DUA_TOP/01_import_dua_top.sql`

File này chỉ tạo thêm 4 bảng mới:

- `dua_top_event`: thời gian/trạng thái mùa.
- `dua_top_recharge_baseline`: mốc `tongnap` đầu mùa để Top Nạp chỉ tính tiền nạp phát sinh.
- `dua_top_reward`: cấu hình quà theo từng khoảng hạng.
- `dua_top_result`: kết quả Top 50 đã chốt + trạng thái đã nhận quà.

Không ALTER các bảng `account` hoặc `player` hiện tại.

## 2. Bảng xếp hạng

- POWER: Top Sức Mạnh, lấy `player.power`.
- TASK: Top Nhiệm Vụ, giữ cách đọc `data_task` giống source hiện tại.
- PET: Top Sức Mạnh Đệ Tử, lấy `player.pet_power`.
- RECHARGE: Top Nạp theo mùa = `account.tongnap - tongnap_start`.

Top Nạp:
- Top 1 phải phát sinh tối thiểu 50.000 VND.
- Top 2 trở xuống không có mốc tối thiểu, nhưng phải có phát sinh nạp > 0.

## 3. Thời gian mặc định

Khi import SQL:
- Bắt đầu: thời điểm import (`NOW()`).
- Kết thúc đua: +14 ngày.
- Hết hạn nhận quà: +21 ngày kể từ lúc import (7 ngày nhận quà).

Muốn đổi thời gian:

```sql
UPDATE dua_top_event
SET start_time = '2026-10-29 00:00:00',
    end_time   = '2026-11-12 23:59:59',
    claim_end  = '2026-11-19 23:59:59'
WHERE name = 'Dua Top Mua 1 - 2026';
```

Nếu đổi `start_time` sau khi đã có người nạp thì cần tạo lại baseline trước khi mở mùa.

## 4. Phần thưởng tiền tệ

### Top Sức Mạnh / Nhiệm Vụ / Đệ Tử

| Hạng | Thỏi Vàng | Hồng Ngọc |
|---|---:|---:|
| 1 | 1000 | 20000 |
| 2 | 700 | 15000 |
| 3 | 500 | 10000 |
| 4-5 | 350 | 7000 |
| 6-10 | 250 | 5000 |
| 11-20 | 150 | 3000 |
| 21-50 | 75 | 1500 |

### Top Nạp

| Hạng | Thỏi Vàng | Hồng Ngọc |
|---|---:|---:|
| 1 | 1500 | 30000 |
| 2 | 1000 | 22000 |
| 3 | 750 | 16000 |
| 4-5 | 500 | 11000 |
| 6-10 | 350 | 7000 |
| 11-20 | 200 | 4000 |
| 21-50 | 100 | 2000 |

## 5. Chỉ số cải trang Top thường

Top 1:
- HP/KI/SĐ 32%
- Chí mạng 10%
- SĐ chí mạng 15%
- Né đòn 5%
- Giáp 5%
- Không ảnh hưởng bởi cái lạnh
- Không thể giao dịch

Top 2:
- HP/KI/SĐ 30%
- Chí mạng 5%
- SĐ chí mạng 10%
- Không ảnh hưởng bởi cái lạnh
- Không thể giao dịch

Top 3:
- HP/KI/SĐ 27%
- Chí mạng 5%
- Không thể giao dịch

Top 4-5: HP/KI/SĐ 24%, CM 4%, SĐCM 5%.

Top 6-10: HP/KI/SĐ 20%, CM 3%, SĐCM 3%.

## 6. Chỉ số cải trang Top Nạp

- Top 1: 36% HP/KI/SĐ, 12% CM, 20% SĐCM, 7% Né, 7% Giáp, kháng lạnh.
- Top 2: 34% HP/KI/SĐ, 10% CM, 18% SĐCM, 6% Né, 6% Giáp, kháng lạnh.
- Top 3: 32% HP/KI/SĐ, 8% CM, 15% SĐCM, 5% Né, 5% Giáp, kháng lạnh.
- Top 4-5: 29% HP/KI/SĐ, 6% CM, 12% SĐCM, 4% Né, 4% Giáp, kháng lạnh.
- Top 6-10: 26% HP/KI/SĐ, 5% CM, 10% SĐCM, 3% Né, 3% Giáp, kháng lạnh.

Không có Xuyên Giáp và không có TN/SM.

## 7. Danh hiệu

Chỉ Top 1-3 nhận danh hiệu. Danh hiệu là item type 39 có thể trang bị ở slot danh hiệu của source hiện tại.

Top thường:
- Top 1: 14% HP/KI/SĐ, 5% CM, 7% SĐCM.
- Top 2: 12% HP/KI/SĐ, 4% CM, 5% SĐCM.
- Top 3: 10% HP/KI/SĐ, 3% CM, 4% SĐCM.

Top Nạp:
- Top 1: 18% HP/KI/SĐ, 7% CM, 10% SĐCM.
- Top 2: 16% HP/KI/SĐ, 6% CM, 8% SĐCM.
- Top 3: 14% HP/KI/SĐ, 5% CM, 7% SĐCM.

Không có Né đòn và Giáp trên danh hiệu.

## 8. Cải trang đang gán

Top thường:
- Top 1: 2147 - CT Black Super Saiyan 5
- Top 2: 2146 - CT Black Goku Rose
- Top 3: 2145 - Cải Trang Black Goku
- Top 4-5: 2142 - Cải Trang Gogeta Ssj4
- Top 6-10: 1304 - Cải trang Goku SSJ4

Top Nạp:
- Top 1: 1317 - Cải trang Super Black Goku Rose
- Top 2: 1316 - Cải trang Fusion Zamasu
- Top 3: 1315 - Cải trang Zamasu
- Top 4-5: 1306 - Cải trang Omega
- Top 6-10: 1305 - Cải trang Vegeta SSJ4

Tất cả đều dùng item/cải trang đã có trong database hiện tại; Java không tạo item_template mới.

## 9. Menu NPC

NPC Trọng Tài map 113 có 6 nút:
1. Top Sức Mạnh
2. Top Nhiệm Vụ
3. Top Đệ Tử
4. Top Nạp
5. Thể lệ / Phần thưởng
6. Nhận thưởng

Server kiểm tra mùa mỗi phút. Khi hết giờ đua:
- Chốt Top 50 vào `dua_top_result`.
- Sau khi chốt, bảng xếp hạng không thay đổi nữa.
- Người chơi nhận quà tại NPC.
- Mỗi category chỉ nhận 1 lần.

## 10. Cách import trên VPS Windows

Trước tiên backup DB.

Ví dụ CMD:

```bat
cd /d "C:\NRO DATA"
mariadb -u root -p ngocrong < NRO_DUA_TOP\01_import_dua_top.sql
```

Hoặc mở HeidiSQL/phpMyAdmin, chọn database `ngocrong`, chạy toàn bộ file SQL.

Sau import, kiểm tra:

```sql
SELECT * FROM dua_top_event ORDER BY id DESC;
SELECT category,rank_from,rank_to,gold_bar,ruby,costume_id,title_id
FROM dua_top_reward
ORDER BY category,rank_from;
SELECT COUNT(*) FROM dua_top_recharge_baseline;
```

Sau đó build/restart server Java.

## 11. Chỉnh quà mà không build Java

Ví dụ đổi quà Top 2 Sức Mạnh:

```sql
UPDATE dua_top_reward
SET gold_bar=800, ruby=16000
WHERE event_id=(SELECT MAX(id) FROM dua_top_event)
  AND category='POWER'
  AND rank_from=2 AND rank_to=2;
```

Option format:

`ID_OPTION:PARAM;ID_OPTION:PARAM;...`

Ví dụ cải trang 32% HP/KI/SĐ:

`77:32;103:32;50:32;30:0`

Các ID đang dùng:
- 77 HP %
- 103 KI %
- 50 Sức đánh %
- 14 Chí mạng %
- 5 Sức đánh chí mạng %
- 108 Né đòn %
- 94 Giáp %
- 106 Không ảnh hưởng bởi cái lạnh
- 30 Không thể giao dịch
