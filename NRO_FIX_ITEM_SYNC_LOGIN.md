# Sửa lỗi kết nối rồi xoay và thoát khi tải dữ liệu

## Bản khôi phục sự kiện ngày 31/10/2026

Khôi phục Tri Ân Sư Phụ cùng giftcode Admin `trian11test2026`, đồng thời nhập
bản sửa giao thức từng nằm ở PR #48 (chưa có trong `main` lúc khôi phục trước đó).
Giữ dữ liệu tài khoản/nhân vật và cấu hình kết nối trên VPS.

- Danh mục vật phẩm được chia theo số byte thực tế, mỗi gói tối đa 30.000 byte.
- Dùng hàng đợi an toàn khi nhiều luồng gửi đồng thời và khóa ghi trọn gói TCP.
- Khôi phục phản hồi khi Login gặp lỗi DB và xóa trạng thái chờ khi yêu cầu bị từ chối.
- Thêm thông báo nếu Login không trả lời sau 20 giây.
- Console Server có dòng `[LOGIN] session=... userId=... stage=...`, không chứa mật khẩu.
  Các mốc: `AUTH_REQUEST`, `AUTH_OK`, `ITEM_SYNC_SENT`, `LOAD_CHARACTER`,
  `ENTER_GAME_OK`; ngắt kết nối ghi lại bước cuối. Gửi các dòng này và lỗi của
  cửa sổ Login nếu client vẫn quay về màn hình đăng nhập.

Đã build cả Login và Server bằng Java 21, Server đạt 34 bài kiểm tra.
Phép thử `scripts/test_nro_login_smoke.py` dùng MariaDB 10.11 và tài khoản thường
trong DB tạm: sai mật khẩu nhận thông báo, thử lại trên cùng socket thành công,
nhận đủ 2.421 vật phẩm và dữ liệu map/skill, tải nhân vật, hoàn tất `ENTER_GAME_OK`.
Phép thử này cũng được thêm vào GitHub Actions. Không thay thế việc kiểm tra
Unity trên VPS thực tế.

Sau khi pull/build, đóng hẳn client và mở lại. Nếu cache đã hỏng từ bản trước,
dùng nút **Xóa dữ liệu** trên màn hình đăng nhập rồi tải lại. Đây là cache client;
không xóa bảng SQL hoặc reset tài khoản/nhân vật.

## Bằng chứng và giới hạn chẩn đoán

Với bản SQL gốc trong repo, phép thử mới đo hai gói theo cách chia cũ là
**50.326 byte** và **59.751 byte** khi chưa có vật phẩm tháng 11.
Hai gói này chưa vượt giới hạn 65.535 byte của header 2 byte; vì vậy không thể
khẳng định chính SQL gốc này tái hiện lỗi tràn 180.564 byte đã nêu ở lần điều tra
trước. Database đang chạy trên VPS có thể đã khác bản dump.

Bài kiểm thử hồi quy dùng gói giả lập 180.564 byte xác nhận cơ chế cũ có thể ghi
header bị tràn; bản sửa từ chối gói quá lớn trước khi ghi và vẫn giữ đúng gói kế
sau. Các kiểm thử đồng thời xác nhận bản mới không ghi chồng khung và không mất
2.000 gói khi nhiều luồng đưa dữ liệu vào hàng đợi. Danh mục được chia dưới
30.000 byte cũng tương thích với giới hạn short có dấu của client cũ.

Phép thử TCP thực tế với bản sửa xác nhận đăng nhập, tải toàn bộ danh mục,
map/skill và nhân vật thành công trên môi trường riêng. Điều này kiểm tra cả
luồng ứng dụng thay vì chỉ kiểm tra cổng. **Chưa xác định chắc nguyên nhân trên
VPS khi chưa có log đúng lần client lỗi.** Không coi một cổng LISTENING là bằng
chứng xác thực hoặc tải nhân vật đã hoạt động.

## Cập nhật trên VPS

Bảo trì và dừng server theo quy trình hiện tại, rồi mở CMD:

```bat
cd /d "C:\NRO DATA"
git switch main
git pull --ff-only origin main
call "CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat"
```

BAT build cả Login và Server rồi chép JAR mới. Bản sửa giao thức không cần SQL.
Sự kiện tự tạo bảng; xem `NRO_EVENT_TRI_AN_2026.md` nếu tài khoản DB thiếu quyền
CREATE. Không đổi thông tin kết nối DB khi chưa có lỗi xác nhận.
