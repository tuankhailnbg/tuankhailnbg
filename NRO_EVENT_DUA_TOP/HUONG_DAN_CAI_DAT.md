# Sự kiện Đua Top 4 bảng

## Có cần import database không?
Có. Import **01_import_dua_top.sql** đúng **một lần** vào database `ngocrong`.

File SQL chỉ tạo các bảng riêng của sự kiện:
- `top_race_season`: mùa đua Top.
- `top_race_nap_baseline`: mốc tổng nạp tại lúc mở mùa.
- `top_race_result`: kết quả đã chốt.
- `top_race_reward_config`: cấu hình phần thưởng/chỉ số.

Không xóa và không reset `account.tongnap`.

## Bảng xếp hạng
- POWER: Top Sức Mạnh.
- TASK: Top Nhiệm Vụ.
- PET: Top Sức Mạnh Đệ Tử.
- NAP: Top Nạp trong riêng mùa hiện tại.

Admin mở chat trong game:
- `duatop start 7` : mở mùa 7 ngày.
- `duatop status` : xem trạng thái.
- `duatop close` : chốt mùa ngay.

Khi hết `end_time`, server tự chốt kết quả.

## Người chơi
Tại NPC **Trọng Tài** ở map 113:
- Top Sức Mạnh
- Top Nhiệm Vụ
- Top Đệ Tử
- Top Nạp
- Nhận thưởng

Top Nạp tính bằng:
`tongnap hiện tại - tongnap tại lúc mở mùa`.

Top 1 Nạp cần tối thiểu 50.000 VNĐ trong mùa. Top 2 trở xuống không có mốc tối thiểu nhưng phải có phát sinh nạp (>0) để xuất hiện trong Top Nạp.

## Giáp tập luyện cấp 4 – thưởng Top 1, 2, 3

**Tất cả bốn bảng đua Top (POWER/TASK/PET/NAP)**: Top 1, Top 2 và Top 3 của **mỗi** bảng được nhận thêm **1 Giáp tập luyện cấp 4** khi đến Trọng Tài nhận thưởng sau khi mùa đã chốt.

- Giữ nguyên Thỏi Vàng, Hồng Ngọc, cải trang và danh hiệu sẵn có.
- Giáp loại trang bị `type=32`, dùng ô giáp tập luyện (slot 6), mặc vào giảm **40%** sức đánh, khi tháo ra tăng thêm **40%** theo luồng xử lý giáp luyện tập hiện hữu.
- Giáp không thể giao dịch (option 30); có option 9 để kích hoạt hiệu ứng sau khi tháo theo logic của `NPoint`.
- Mốc yêu cầu sức mạnh: **150 triệu**. Đây là mốc nối tiếp quy luật từ cấp 2 (1,5 triệu), cấp 3 (15 triệu) trong dữ liệu hiện tại; **chưa có xác minh độc lập cho mốc cấp 4 bản NRO chính**.
- Tự cài đặt một bản ghi `item_template` khi Game Server khởi động lần đầu (nếu trước đó chưa tồn tại item này), tự cấp ID mới và không sửa item cũ. Không cần import thêm SQL cho giáp, nhưng **hãy sao lưu database trước khi chạy bản mới**.
- **Icon:** dùng icon của Giáp tập luyện cấp 3 hiện có làm hình tương thích tạm thời; icon gốc của cấp 4 chưa được xác minh trong bộ tài nguyên client này.
- Thưởng thêm **không áp dụng Top 4 trở xuống**, và không trao trước khi chốt mùa.
- Người chơi phải có đủ ô trống để nhận cả gói, nếu không hệ thống yêu cầu dọn túi/rương trước khi nhận.
- Người chơi đứng Top 1–3 ở nhiều bảng có thể nhận giáp riêng cho mỗi bảng, cùng phần thưởng riêng của bảng đó.

**Kiểm tra:** sau khi khởi động, xem log `[ITEM] Created Giáp tập luyện cấp 4 ID=...` nếu tạo lần đầu, sau đó thử với mùa đua Top trong môi trường test: chốt bảng, nhận 1 lần, kiểm tra chỉ số khi mặc/tháo, đăng xuất/đăng nhập, không nhân đôi vật phẩm khi bấm nhận lại.

## Sửa phần thưởng
Chỉnh trực tiếp bảng `top_race_reward_config`. Không cần build Java lại nếu chỉ đổi:
- Thỏi Vàng
- Hồng Ngọc
- ID cải trang
- ID danh hiệu
- itemOptions
- mốc tối thiểu Top 1 Nạp

`costume_options` và `title_options` dùng dạng:
`[[optionId,param],[optionId,param],...]`

Các ID option đang dùng:
- 77 HP %
- 103 KI %
- 50 Sức đánh %
- 14 Chí mạng %
- 5 Sức đánh chí mạng %
- 108 Né đòn %
- 94 Giáp %
- 106 Không ảnh hưởng bởi cái lạnh
- 30 Không thể giao dịch

## Cập nhật server
Sau khi merge/pull code và import SQL:

```bat
cd /d "C:\NRO DATA"
git pull --ff-only origin main
call "CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat"
```

Nếu đang test branch trước khi merge thì checkout branch tương ứng trước khi build.

## Lưu ý
- Chỉ Top 1-3 có danh hiệu.
- Top 1-10 có cải trang vĩnh viễn.
- Top 11-50 chỉ có Thỏi Vàng + Hồng Ngọc.
- Cải trang/danh hiệu dùng item đã có sẵn trong database, không thêm asset client mới.
