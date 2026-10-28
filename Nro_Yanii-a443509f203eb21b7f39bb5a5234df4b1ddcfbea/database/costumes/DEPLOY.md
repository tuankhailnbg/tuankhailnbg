# Cải trang + sửa mở rộng hành trang/rương (28/10/2026)

**Đã sửa trên branch `feature/costumes-batch-01`, chưa có kết nối DB trực tiếp tới VPS.**

## Đã chuẩn bị

- `install_18_costumes_and_repair_storage.sql`: duy nhất một transaction cho 18 item 2154–2171, 54 part 2042–2095, 18 avatar, 18 mapping; sửa item_shop Santa tab 37 cho template 517/518.
- `batch01a`, `batch01b`, `batch01c`: 434 sprite ID, 4 mức x1–x4 = 1.736 PNG; không ghi đè khác SHA256; có thể chạy lại khi tải dở.
- `DEPLOY_WINDOWS_VPS.cmd`: backup DB trước, tải & kiểm tra ảnh, rồi mới nhập một migration SQL.
- `Server/src/main/java/nro/services/func/ShopService.java`: gửi cập nhật rương ngay khi mua item 518; cập nhật hành trang item 517; mở lại cửa hàng Santa với giá động; code vẫn khóa inventory khi thanh toán.
- `Server/src/main/java/nro/data/DataGame.java`: cache versions hiện là `vsData=7`, `vsItem=8` trên branch.

## Triển khai ở Windows VPS

1. Pull branch `feature/costumes-batch-01` vào bản source Server trên VPS (không tải riêng SQL rồi bỏ qua Java).
2. Dừng Server trước khi nhập database. Đảm bảo `mariadb.exe` và `mariadb-dump.exe` có trong PATH và user DB `nro_app` đủ quyền backup + CREATE PROCEDURE + SELECT/INSERT/UPDATE.
3. Mở thư mục `database/costumes`, chạy `DEPLOY_WINDOWS_VPS.cmd`. Không nhập lại batch01a/b/c sau khi đã chạy SQL tổng. Nếu SQL báo DB đã thay đổi hoặc ID trùng, dừng; không xóa dữ liệu người chơi để ép import.
4. Chạy Maven clean package rồi restart **Login + Server**, thoát client và đăng nhập lại.
5. Kiểm tra item 2154–2171 từng bộ; cho người chơi thử mua ID 517 và 518 với 20, 21, 59, 60 ô. Kiểm tra số ngọc/vàng, tăng ô, hiển thị rương/hành trang, đăng xuất/đăng nhập lại.

## Kết quả mong đợi

- ID 517: hành trang 20→21 ô, tốn 50 ngọc; 21→22 ô, tốn 100 ngọc; không cho vượt 60.
- ID 518: rương 20→21 ô, tốn 25.000.000 vàng; 21→22 ô, tốn 50.000.000 vàng; không cho vượt 60.
- Mua xong client nhận ngay packet hành trang `-36`, và khi mua rương có thêm packet rương `-35`. Cửa hàng Santa hiện giá tiếp theo.
- Không thêm chỉ số mới cho cải trang và không đăng bán cải trang mới.

**Giới hạn:** Không có kết nối trực tiếp đến VPS/MariaDB hoặc phiên client, nên chưa xác nhận import thành công, Maven build thành công hay client thực tế. Cấu trúc SQL và tài nguyên đã được đối chiếu trên source; kiểm thử runtime vẫn cần thực hiện trên VPS.
