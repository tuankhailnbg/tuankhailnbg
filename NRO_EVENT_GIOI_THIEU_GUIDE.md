# NRO — NPC Sự Kiện tại Đảo Kame: Giới thiệu (Admin cấp điểm)

**Trạng thái: mã nguồn đang trong PR nháp, chưa build và chưa thử trên VPS/client Unity. Không gộp main trước khi test.**

## 1. Menu NPC
NPC **Sự Kiện** (template ID 73, map 5 – Đảo Kame) có **đúng hai chức năng**:

1. **Sự kiện giới thiệu** → mở menu phụ gồm **Bảng xếp hạng giới thiệu**, **Nhận quà giới thiệu**, **Đóng**. Phía trên menu có **mã GT... của nhân vật** và **điểm giới thiệu**. Admin sẽ thấy thêm tùy chọn **Cấp điểm (Admin)**.
2. **Đua top** → chỉ báo "Sự kiện Đua Top đang chuẩn bị", chưa triển khai.

NPC dự kiến tại x=780 y=408 map 5; đã dùng ID 73 và thông tin hình có sẵn trong `npc_template`. Nếu map 5 đã có ID73 thì không tự thêm trùng.

**Sự kiện giới thiệu vĩnh viễn, không có thời hạn kết thúc.**

## 2. Cách tính điểm (CHỈ ADMIN CẤP)

- Tài khoản người chơi mới vẫn có thể nhập **mã GT** của người giới thiệu trong 24 giờ đầu tại popup khi đăng nhập. Dữ liệu chỉ ghi nhận mã/người giới thiệu vào `referral_invites`.
- **Không cộng điểm tự động khi nhập mã, đạt 50 tỷ, chơi 10 giờ, hoàn thành nhiệm vụ Cell**. Các tiêu chí 50 tỷ, 10 giờ, Cell có thể dùng để admin tự xác nhận trước khi cấp.
- **Admin quyết định và nhập số điểm (+1, +2,... hoặc -1)** cho ID/tên nhân vật người giới thiệu.
- Điểm được lưu vào `referral_points`, mọi lần cộng/trừ lưu vào `referral_points_audit` (người cấp, người nhận, điểm trước/sau, lý do và thời gian).
- Admin có thể cấp qua NPC Sự Kiện → Giới thiệu → Cấp điểm (Admin), hoặc **gõ `admin` trong khung chat → Cấp điểm giới thiệu**.
- Hệ thống giới hạn 1–100 điểm mỗi lần thao tác (có thể trừ -1 tới -100), không cho âm, tối đa 100.000; kiểm tra quyền admin ở server.
- Khi cấp điểm nhớ nhập **lý do và ID nhân vật**, ví dụ `Người được mời ID 123 hoàn thành đủ điều kiện`.
- Điểm đã bị trừ không tự thu hồi vật phẩm đã nhận; hãy kiểm duyệt trước khi cấp và xử lý gian lận riêng.

## 3. Bảng xếp hạng giới thiệu
Dùng giao diện TOP sẵn của NRO. Lấy **Top 30 nhân vật theo điểm được Admin cấp**, hỗ trợ cả nhân vật offline, đồng điểm xét thời gian cập nhật điểm rồi ID.

## 4. Giao diện nhận mốc (giống Bò Mộng)
Dùng giao thức `Cmd.ACHIEVEMENT` (bảng "Nhận ngọc miễn phí" ở Bò Mộng).
Danh sách gồm **20 mốc**, mỗi dòng có tên mốc, số điểm hiện có / yêu cầu, số **Thỏi Vàng** và quà cải trang.
- Khi đủ n điểm, dòng n có thể bấm nhận.
- Thỏi Vàng cho mốc n: `100 + 25 × (n - 1)`. Mốc 1=100; 2=125; 5=200; 10=325; 15=450; 20=575.
- Tổng 20 mốc = **6.750 Thỏi Vàng**.
- Mỗi mốc phát 1 lần, lưu `referral_claims` (khóa chính người chơi+mốc); không cộng tự động khi admin cấp điểm.
- Mỗi mốc bấm nhận riêng. Server dùng khóa lưu nhân vật và giao dịch DB, chống gửi lệnh nhận trùng.
- Đây là cách **tận dụng kiểu bảng của Bò Mộng**; icon tiền thưởng mặc định của client có thể vẫn là Hồng Ngọc vì giao thức thành tựu cũ; nội dung dòng đã ghi rõ Thỏi Vàng. Muốn đổi icon riêng cần sửa client.

## 5. Cải trang mốc 5/10/15/20

| Mốc | Cải trang | HSD | HP | KI | Sức đánh | Chí mạng | TNSM | Hút HP/KI |
|---|---|---|---|---|---|---|---|---|
| 5 | Goku SSJ3 | 7 ngày | 20% | 20% | 20% | 4% | 10% | — |
| 10 | Vegito Blue | 7 ngày | 23% | 23% | 23% | 6% | 13% | 5% |
| 15 | Gogeta Blue | Vĩnh viễn | 25% | 25% | 25% | 8% | 17% | 8% |
| 20 | Zeno | Vĩnh viễn | 27% | 27% | 27% | 10% | 20% | 10% |

**Chưa xác định đúng item_template/sprite của 4 cải trang; `referral_costume_catalog.item_template_id` đang là NULL, nên chưa tự phát cải trang.** Khi có đúng item ID và client asset, cập nhật bảng cấu hình để hệ thống phát chỉ số tương ứng và hạn 7 ngày dùng item option 93. Bảng `referral_costume_claims` giữ lịch sử phát. Không tự gán ID ngẫu nhiên khi chưa đối chiếu SQL/assets.

## 6. Cài trên VPS Windows (SAO LƯU trước)

1. Backup MariaDB database `ngocrong` và thư mục Server/Login đang chạy.
2. Lấy code từ nhánh `feature/nro-event-referral-kame` của PR #26 vào bản thử nghiệm; không ghi đè main.
3. Chạy migration lần lượt:
   - `database/migrations/20261028_referral_event_kame.sql`
   - `database/migrations/20261028_referral_manual_points.sql`
4. Trong Server chạy PowerShell: `& "C:\tools\maven\bin\mvn.cmd" clean package -DskipTests`.
5. Nếu `BUILD SUCCESS`, triển khai JAR thử nghiệm và dùng BAT khởi động Login/Server bình thường.
6. Test với ít nhất 2 tài khoản (1 admin): admin nhập cấp 1 điểm cho nhân vật A → bảng top xuất hiện A; A mở bảng nhận mốc 1, bấm nhận 100 Thỏi Vàng → thử bấm lại không được cộng lần hai.
7. Test 5/10/15/20 mốc, túi đầy, logout ngay sau nhận, server restart, admin cấp/trừ, bảng top offline, mở lại bảng Bò Mộng để đảm bảo không lẫn phiên nhận quà.
8. Chỉ merge và đưa lên VPS chính khi pass toàn bộ test.

## 7. SQL kiểm tra admin audit

```sql
SELECT p.id, p.name, rp.points
FROM referral_points rp JOIN player p ON p.id=rp.player_id
ORDER BY rp.points DESC LIMIT 30;

SELECT id, admin_player_id, player_id, delta_points, previous_points,
       current_points, reason, created_at
FROM referral_points_audit ORDER BY id DESC LIMIT 50;
```

**Chưa thử compile Maven/kiểm thử UI trực tiếp.** Chưa có quyền truy cập VPS production và không có client asset chính xác cho 4 cải trang.
