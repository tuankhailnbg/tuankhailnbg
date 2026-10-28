# NPC Sự Kiện – Giới thiệu bạn bè (Đảo Kamê, map 5)

**Trạng thái: BẢN NHÁP CHƯA BUILD/TEST TRÊN VPS, không tự động triển khai máy chủ.**
Code nằm trên nhánh `feature/nro-event-referral-kame`, không sửa branch `main`.

## Hoạt động

- Sử dụng **NPC ID 73: Sự Kiện** đã có trong `npc_template` (tên/hình có sẵn).
- Xuất hiện ở **map ID 5 – Đảo Kamê**, vị trí dự kiến **x=780, y=408**.
- Khi bấm NPC, mã nhân vật hiện ngay đầu menu: `GT` + ID nhân vật dạng base36, chữ in hoa.
- Menu: `Giới thiệu bạn bè` → `Nhập mã giới thiệu`, `Xem tiến độ`, `Nhận quà`; màn hình đầu cũng có `Nhận quà`.
- Sự kiện **vĩnh viễn**, không phụ thuộc ngày bắt đầu/kết thúc.
- Tài khoản mới được mời nhập mã trong **24 giờ đầu** và chỉ **một mã/tài khoản**.
- Client NRO hiện tại chưa có trường mã giới thiệu trên màn hình tạo account. Bản này hiển thị **ô nhập ngay khi nhân vật mới đăng nhập vào game** (và có thể nhập từ NPC). Muốn nhập trực tiếp trên giao diện đăng ký tài khoản cần sửa riêng client / trang đăng ký.
- Một nhân vật giới thiệu được tính khi **50 tỷ sức mạnh + 10 giờ hoạt động + qua nhiệm vụ 29 (Cell/Xên), sang task ID >= 30**.
- Thời gian chỉ được tính lúc có hoạt động di chuyển/thay đổi sức mạnh/nhiệm vụ; không cộng thời gian offline và không tính thuần AFK (đây là kiểm tra sơ bộ, chưa chống bot tinh vi).
- Tài khoản chung IP với người giới thiệu, hoặc có từ 4 account mới cùng IP trong 3 ngày, được gắn cờ `review_required=1`. Lượt được giữ để admin xem xét, không tự động khóa tài khoản.
- Chỉ các lời mời đủ điều kiện và **không bị giữ kiểm tra** mới tính mốc.

## Phần thưởng Thỏi Vàng (ID vật phẩm 457)

Tại mốc số `n`, 1 <= n <= 20, nhận `100 + (n - 1)*25` Thỏi Vàng.
Tổng thưởng 20 mốc: **6.750 Thỏi Vàng**. Mỗi mốc chỉ nhận 1 lần, có bảng ledger riêng và khóa chính `(inviter_player_id,milestone)`.

## Cải trang mốc 5/10/15/20

| Mốc | Cải trang | HSD | HP/KI/SĐ | Chí mạng | TNSM | Hút HP/KI |
|---|---|---|---|---|---|---|
| 5 | Goku SSJ3 | 7 ngày | 20% | 4% | 10% | Không |
| 10 | Vegito Blue | 7 ngày | 23% | 6% | 13% | 5% |
| 15 | Gogeta Blue | Vĩnh viễn | 25% | 8% | 17% | 8% |
| 20 | Zeno | Vĩnh viễn | 27% | 10% | 20% | 10% |

**Quan trọng:** SQL có `referral_costume_catalog` với `item_template_id=NULL` cho cả bốn mốc.
Các sprite Goku SSJ3, Vegito Blue, Gogeta Blue và Zeno chính xác chưa được xác minh có trong `item_template`/client, nên **cải trang chưa tự phát trong bản này**. Hệ thống giữ quyền nhận thưởng riêng trong `referral_costume_claims` để có thể nhận sau khi gắn ID vật phẩm có asset thật.
Khi gắn ID, service tự tạo option theo bảng JSON, thêm `30` để không giao dịch; với mốc 5 và 10 thêm option `93` = 7 ngày để hệ thống hiện tại tự xử lý HSD. Không nên tự gán ID sai khi chưa kiểm tra client.

## Triển khai thử trên VPS (Windows)

1. **Sao lưu MariaDB** và thư mục `Server` hiện tại.
2. Checkout nhánh tính năng vào máy thử (không ghi đè server production cho đến khi test).
3. Mở MariaDB console đăng nhập database tương ứng `ngocrong`, chạy:
   `SOURCE D:/duong-dan-repo/database/migrations/20261028_referral_event_kame.sql;`
4. Build bằng Maven trong thư mục `Server`, ví dụ:
   `& "C:\tools\maven\bin\mvn.cmd" clean package -DskipTests`
5. Kiểm tra `BUILD SUCCESS` và JAR mới, dùng quy trình khởi động Login/Server BAT hiện có.
6. Đăng nhập nhân vật ở map 5 để xem NPC ID 73, xác minh hiển thị mã GT và ô nhập trên account mới.

## Test tích hợp tối thiểu trước khi merge

1. Tài khoản A tạo nhân vật có mã GT..., đăng nhập NPC để lấy mã.
2. Tạo tài khoản B mới; ô nhập GT...; xác minh `referral_invites` có một dòng mới.
3. B không đủ điều kiện: A chưa được tính lượt.
4. Cho B đạt nhiệm vụ ID >=30, sức mạnh >=50 tỷ, 10 giờ **hoạt động thật**: A được tính 1.
5. Test mốc 1: A nhận 100 Thỏi Vàng ID 457, nhận lại không phát trùng.
6. Tài khoản C cùng IP sẽ bị giữ để admin duyệt; sau khi xác minh mới tính.
7. Test 5/10/15/20 mốc khi đã gắn đúng `item_template_id`, kiểm tra costume 7 ngày/vĩnh viễn.
8. Kiểm tra đóng server bất ngờ ngay lúc phát quà; đây là tình huống rủi ro cần test lưu hành trang và sổ giao dịch.
9. Kiểm tra nhiều phiên/account spam bấm nhận cùng lúc; khóa chính DB phải chặn trùng.

### Admin duyệt lời mời bị giữ

Sau khi điều tra và thấy hợp lệ, chạy trong MariaDB:

```sql
UPDATE referral_invites
SET review_required = 0
WHERE invited_account_id = <ID_ACCOUNT_DA_XAC_MINH>;
```

**Không tự động bỏ cờ cho toàn bộ account cùng IP.**

### Điều còn cần kiểm nghiệm

- Chưa kiểm tra BUILD SUCCESS trong môi trường Maven của VPS.
- Chưa chứng minh gameplay/màn hình đăng ký thực tế với client Unity đang phát hành.
- Chưa có asset và template ID chính xác cho 4 cải trang.
- Giao dịch phát quà hiện khóa theo hành trang + ledger/DB; cần thử tình huống crash và autosave đồng thời trước khi mở công khai.
