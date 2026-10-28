# NRO Yanii — cải trang + mở rộng hành trang/rương đồ (28/10/2026)

## Trạng thái đã xác nhận

- **Mã nguồn:** đã merge vào `main` qua PR #25, commit `f56567bbd4b19c6fcae1ea3a78e42afc7a1c70bb`.
- **Build Java 21/Maven:** GitHub Actions thành công cho mã nguồn đã kiểm tra.
- **SQL & ảnh:** đã đối chiếu 18 item (2154–2171), 54 part (2042–2095), 18 `cai_trang`, 18 `head_avatar`; 434 ID ảnh × 4 mức = 1.736 PNG nguồn tồn tại.
- **VPS:** chưa thể kết nối trực tiếp để nhập DB, chạy Server hoặc test client. Vì vậy **chưa thể xác nhận 18 cải trang mặc được trong game** và **chưa thể xác nhận mua ô thành công trên VPS**.

## Nội dung sửa

- `InventoryService.sendItemBags`: mỗi ô trống phải ghi ID `-1`, không được `continue` bỏ trống payload.
- `ShopService`: mua 517 (túi) và 518 (rương) tăng một ô tối đa 60, giá tăng theo số ô hiện có, cập nhật packet túi/rương và shop, hoàn lại tiền nếu lưu thất bại.
- `PlayerDAO.saveStorageUpgradeOrThrow`: lưu `data_inventory`, `items_bag`, `items_box` ngay khi mua; dùng **kết nối riêng từ pool**, không đóng kết nối lưu dùng chung.
- `Manager` và `PartManager`: truy vấn ORDER BY id ASC; `DataGame`: tăng version cache `vsData=7`, `vsItem=8`.
- 18 cải trang chỉ gồm phần hình, không thêm option/chỉ số và chưa đưa vào shop.

## Một cách triển khai an toàn trên VPS Windows

**Khuyến nghị:** chỉ chọn một bộ cài. Dùng `install_18_costumes_vps.ps1` trong thư mục này; **không nhập lại** các file SQL batch sau khi đã cài.

Yêu cầu: mã nguồn `main` mới nhất đã nằm trên VPS; thư mục `Server` có `resources/normal/image`; MariaDB CLI/dump đã cài, tài khoản DB có quyền SELECT/INSERT/UPDATE và sao lưu.

Lệnh chạy bằng PowerShell, tại thư mục `database/costumes`:

```powershell
$env:NRO_DB_PASSWORD = Read-Host "Mat khau MariaDB cua nro_app"
powershell -ExecutionPolicy Bypass -File .\install_18_costumes_vps.ps1 -ServerFolder "D:\DUONG_DAN_THAT\Server"
Remove-Item Env:NRO_DB_PASSWORD
```

- Script kiểm tra ID, tạo backup DB, kiểm tra không trùng ID ảnh, tải PNG theo x1–x4, nhập SQL batch A/B/C, kiểm tra số dòng.
- Mặc định kết nối `127.0.0.1:3306`, DB `ngocrong`, user `nro_app` (có tham số để thay).
- Nếu có ID mới trên VPS, script từ chối nhập thay vì ghi đè; cần kiểm tra schema/dữ liệu mới trước.
- Script không restart server. Sau import phải build Maven trong `Server`, khởi động lại Server và đăng nhập lại game để cập nhật cache.

**Không chạy** `database/migrations/add_costumes_batch_01.sql` như migration bổ sung: đây là script cũ có lệnh DELETE, không dành cho cài đặt hiện tại.

## Kiểm tra thực tế còn thiếu

Thử mua ID 517 và 518 với 20, 21, 59, 60 ô: giá và số tiền trừ, số ô thêm, mở lại shop, chuyển đồ vào ô mới, đăng xuất/đăng nhập lại; xác nhận không tăng quá 60. Thử mặc 18 cải trang trong các trạng thái đứng, đi, bay, đánh, avatar và icon.

Kết quả build **không thay thế** kiểm thử trực tiếp trên VPS/client.
