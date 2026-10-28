# Costume batch 01

Batch này chỉ thêm **ngoại hình cải trang**, chưa thêm option/chỉ số và chưa đưa vào shop.

## Cải trang

- `2044` — Cải trang Iron Man (part 2042/2043/2044)
- `2050` — Cải trang Black Panther (part 2045/2046/2047)
- `2154` — Cải trang Spider Man (part 2048/2049/2050)
- `2155` — Cải trang Hulk (part 2051/2052/2053)
- `2156` — Cải trang Captain America (part 2054/2055/2056)
- `2157` — Cải trang Gojo (part 2057/2058/2059)
- `2158` — Cải trang Akaza (part 2060/2061/2062)
- `2159` — Cải trang Shinobu Kocho (part 2063/2064/2065)
- `2160` — Cải trang Shinazugawa Sanemi (part 2066/2067/2068)
- `2161` — Cải trang Rengoku (part 2069/2070/2071)

- Iron Man `2044` và Black Panther `2050` là item đã có trong DB Yanii; batch bổ sung part/cai_trang và đổi icon sang asset riêng của batch.
- Các item `2154`–`2161` là item mới, nối tiếp sau item hiện tại `2153`.
- Part mới dùng dải `2042`–`2071`.
- Ảnh mới dùng dải `32212`–`32486`, dưới giới hạn `32767`.
- Không tạo `item_option`; chỉ số để cấu hình sau.
- Không thêm `item_shop`; test mặc ổn trước rồi mới bán/tặng sự kiện.

## Cài đặt

1. Chạy PowerShell tại thư mục này:

```powershell
powershell -ExecutionPolicy Bypass -File .\INSTALL_COSTUMES_BATCH_01.ps1
```

2. Import SQL:

```powershell
mariadb -u root -p ngocrong < ..\..\database\migrations\add_costumes_batch_01.sql
```

3. Build/restart server để reload `part`, `cai_trang`, `head_avatar`, `item_template`.

Batch yêu cầu tăng `DataGame.vsData` và `DataGame.vsItem` để client bỏ cache cũ; branch này đã tăng mỗi version thêm 1.

## Rollback

```powershell
mariadb -u root -p ngocrong < ..\..\database\migrations\rollback_costumes_batch_01.sql
```

Các PNG tải về có thể giữ lại; rollback DB sẽ khiến chúng không còn được sử dụng.
