# Kiểm tra vào map nhưng chỉ di chuyển được

## Lỗi đã xác nhận trong source

Vòng cập nhật Map trước đây đặt sleep trong cùng try với zone.update().
Khi một khu ném ngoại lệ, sleep bị bỏ qua, ngoại lệ bị bỏ qua và vòng lặp lập tức
chạy lại. Khi cập nhật kéo dài hơn một giây, sleep nhận giá trị âm và lại rơi
vào catch. Các đường này có thể làm luồng ngốn CPU; một khu lỗi cũng chặn các
khu phía sau cùng map. Đây là lỗi code đã xác nhận, chưa phải kết luận rằng nó
là nguyên nhân duy nhất trên VPS của người chơi.

Bản sửa giữ nhịp cập nhật ngay khi lỗi, giới hạn log lỗi map mỗi 30 giây và vẫn
cập nhật các khu khác. Interrupt dừng luồng đúng cách. Bộ đọc TCP dùng readFully
để không xử lý gói bị cắt với phần còn thiếu tự điền số 0.

## Theo dõi thao tác thực tế

Sau login có các mốc:

- `ENTER_GAME_OK`: server hoàn tất tải nhân vật và chuẩn bị gửi thông tin vào game.
- `MAP_READY_ACK`: client gửi xác nhận đã tải map.
- `CLIENT_READY_ACK`: client gửi yêu cầu đồng bộ nhân vật sau tải map.
- `[PLAY] ... dir=IN cmd=33`: server nhận yêu cầu mở NPC.
- `[PLAY] ... dir=OUT cmd=32`: server ghi gói menu NPC ra TCP.
- `cmd=32` chiều IN: chọn một mục menu; `cmd=34`: chọn kỹ năng;
  `cmd=-7`: di chuyển; `cmd=29`: danh sách khu; `cmd=44`: chat.

Các dòng IN/OUT chỉ ghi lần đầu cho từng loại thao tác trong mỗi phiên, kèm
phiên bản client, zoom (ở các mốc LOGIN), trạng thái nhân vật và độ dài hàng đợi.
Không ghi payload, nội dung chat, tên đăng nhập hoặc mật khẩu. Bật server.debug
hiện có nếu cần ghi lặp; mặc định không cần bật. Lỗi xử lý gói có session/cmd
thay vì bị bỏ qua. Lỗi xử lý NPC đóng hộp chờ để tránh UI chờ vô hạn.

OUT xác nhận server ghi TCP, chưa chứng minh client đọc/hiển thị đúng gói.
Nếu có cả IN 33 và OUT 32 mà menu vẫn không xuất hiện, cần đối chiếu log client,
đặc biệt lỗi đọc dữ liệu hoặc NullReferenceException. Nếu không có IN khi bấm,
kiểm tra input/kết nối client. Nếu có IN rồi có ngoại lệ server, xử lý theo stack
trace cụ thể. Không suy ra thiếu RAM chỉ từ việc nhân vật còn di chuyển được.

## Kiểm thử

Ngoài build/test Java, script `scripts/test_nro_login_smoke.py` kiểm tra qua TCP
trên MariaDB dùng DB và tài khoản tạm: tải danh mục, vào map Kamê, xác nhận map,
mở NPC 80, chọn Hướng dẫn, chọn kỹ năng rồi tiếp tục thao tác, di chuyển, mở danh
sách khu, đổi khu và nhận lại nội dung chat. Kiểm tra này không chạy Unity hoặc
giả định mọi client MOD có cùng giao thức. Các kiểm thử MapLoop và MessageCollector
bao phủ khu lỗi không lặp bận, không chặn khu khác, dừng bằng interrupt và gói TCP
bị cắt.

## Cập nhật

Sau khi nhập bản sửa vào main, bảo trì theo quy trình hiện tại rồi chạy:

```bat
cd /d "C:\NRO DATA"
git pull --ff-only origin main
call "CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat"
```

Không cần SQL hay đổi file tài khoản/nhân vật. Giữ client hiện tại để so sánh.
Bấm NPC một lần và xem các dòng PLAY cùng lỗi map trong cửa sổ Game Server.
Nếu còn lỗi, gửi đoạn này kèm log client đúng phiên vừa chạy. Bản gọn NRO_Online
lưu log ở DuLieu\Logs; launcher client cũ có thể lưu tại Logs cạnh launcher.
