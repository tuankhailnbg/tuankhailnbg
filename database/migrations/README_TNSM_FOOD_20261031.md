# NRO – Vật phẩm buff & kẹo TNSM (31/10/2026)

## Dữ liệu gốc đang có
- Bùa Trí Tuệ x2 (ID 213), x3 (671), x4 (672): hiệu quả theo thời gian, mua bùa ở Bà Hạt Mít, **không phải đồ ăn**.
- Các consumable có mã xử lý Java sẵn: Cuồng nộ (381), Bổ huyết (382), Bổ khí (383), Giáp Xên (384), Bánh Pudding (663), Xúc xích (664), Kem dâu (665), Mì ly (666), Sushi (667).
- NRO gốc 2026 có Nước Mía Khổng Lồ (+10% HP), Nước Mía Thơm (+10% HP, 10% chí mạng) và Nước Mía Sầu Riêng (+10% HP, 10% giáp, 10 sức đánh) trong sự kiện hè World Cup tháng 6. **Không đưa vào SQL** vì server bạn chưa có xử lý sử dụng các món đó; chỉ chèn template mà không viết logic sẽ tạo đồ ăn vô tác dụng.
- NRO gốc có các đợt x3 TNSM toàn server, **không đồng nghĩa** có một món ăn x3 chính thức.

## Ba đồ ăn mới TÙY CHỈNH trên server
| ID | Tên | Hiệu quả | Thời gian |
|---:|---|---|---|
| 2154 | Kẹo TNSM x2 | X2 TNSM | 30 phút |
| 2155 | Kẹo TNSM x3 | X3 TNSM | 30 phút |
| 2156 | Kẹo TNSM x4 | X4 TNSM | 30 phút |

Các món này **không phải hàng gốc TeaMobi**. Logic tận dụng chính xác Player.charms.addTimeCharms(213/671/672, 30). Chặn ăn khi đang còn hiệu lực một trong các bùa TNSM, không mất kẹo khi bị chặn. Bùa tự hết hạn sau 30 phút; việc cộng TNSM vẫn qua NPoint.calSucManhTiemNang, giống bùa cũ. Biểu tượng dùng lại icon hiện hữu 1403/1406/1407.

## Triển khai trên VPS
1. Sao lưu DB ngocrong và mã nguồn trước.
2. Git pull repo chính, chạy SQL trong phpMyAdmin tại db ngocrong.
3. Kiểm tra SELECT cuối file: cả 3 món phải là OK, tránh trùng ID nếu DB VPS mới hơn file SQL gửi ngày 22/10.
4. Build Maven server và khởi động lại **Server Java** (nếu không build, chỉ import SQL sẽ không kích hoạt đồ ăn mới).
5. Cấp thử một món vào túi bằng công cụ admin rồi nhấn sử dụng. Kiểm tra trừ 1, hiện thông báo, thời gian hết hạn 30 phút, TNSM tăng.
6. Thử sử dụng tiếp món khác khi buff chưa hết: phải báo chặn và **không mất item**.

Không phát tự động cho toàn server. Không sửa shop/NPC, không đè các item cũ. Mọi thay đổi trong UseItem.java và SQL cần cùng phiên bản.

Nguồn tham chiếu: `ngocrong.sql` do chủ server cung cấp; `UseItem.java`, `Charms.java`, `NPoint.java` của repo hiện tại; https://forum.ngocrongonline.com/post/su-kien-he-world-cup-19526308 .
