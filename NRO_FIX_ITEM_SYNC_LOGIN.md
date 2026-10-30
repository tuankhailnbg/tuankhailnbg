# Sửa lỗi kết nối rồi xoay và thoát khi tải dữ liệu

Ngày 31/10/2026, kiểm tra từ `main` efccc89c.

## Nguyên nhân đã tái hiện

`ItemData` gửi danh sách vật phẩm bằng command -28, có độ dài gói 2 byte.
Bản cũ gửi cố định 750 vật phẩm trước, sau đó gửi toàn bộ phần còn lại trong một gói.
Một lần đo với dữ liệu gốc và danh mục sự kiện cho thấy gói thứ hai dài
180.564 byte, nhưng header chỉ còn 49.492 vì tràn 16 bit. Client đọc thiếu gói
và diễn giải phần còn lại thành lệnh mới. Bản sự kiện tăng phiên bản dữ liệu
vật phẩm nên làm client có cache cũ phải tải lại và gặp lỗi này.

Đã tái hiện qua socket TCP thật với JAR trước sửa: bộ đọc bị ngắt tại vật phẩm 829.
Đây là lỗi giao thức đã xác nhận; vẫn cần kiểm tra lại trên client/VPS thực tế.

## Thay đổi

- Chia gói theo số byte đã mã hóa, tối đa 30.000 byte/gói, giữ nguyên các subtype
  reset/append và thứ tự ID của giao thức hiện có.
- Chuẩn bị đủ dữ liệu trước khi gửi, ghi lỗi thay cho bỏ qua ngoại lệ.
- Đồng bộ thao tác ghi mỗi gói để luồng gửi trực tiếp và luồng hàng đợi không ghi chồng.
- Từ chối gói quá lớn trước khi ghi header, giữ kết nối không bị lệch khung.
- Tăng `vsItem` từ 9 lên 10 để client tải lại danh mục đầy đủ.

## Kiểm tra

- Maven Server `clean package`: 29/29 bài kiểm tra đạt trên Java 17.
- 7 bài mới kiểm tra dữ liệu UTF, đủ 2.421 ID liên tiếp, danh mục nhỏ/rỗng,
  vật phẩm quá dài, gói quá kích thước, ghi đồng thời và giao thức gói lớn cũ.
- Dữ liệu gốc được import vào MariaDB 10.11 trong môi trường riêng.
- Login được biên dịch từ source với các thư viện đóng gói sẵn; mở cổng 3105.
- Server mở cổng 14445. Bộ đọc TCP nhận đủ 2.421 vật phẩm và đọc đúng gói map
  tiếp theo, chứa NPC Sứ Giả Tri Ân.
- Không chạy client Unity/Windows hoặc truy cập VPS của người dùng.

Bộ kiểm tra socket có thể chạy với server test đang mở:

```sh
python scripts/test_nro_item_sync_wire.py --host 127.0.0.1 --port 14445 --items 2421
```

## Cập nhật trên VPS

Bảo trì và dừng server theo quy trình hiện tại, rồi mở CMD:

```bat
cd /d "C:\NRO DATA"
git pull --ff-only origin main
call "CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat"
```

Đóng hẳn client rồi mở lại. Lần đầu cần tải lại dữ liệu.
Bản sửa giao thức này không cần SQL và không cần sửa thông tin kết nối DB.
