# BÁO CÁO TRIỂN KHAI HỆ THỐNG BUFF ĐỒ TRỰC QUAN & CỘNG SỐ DƯ NẠP TIỀN
**Dự án**: Ngọc Rồng Online (Java 21, Maven, MariaDB)  
**Thời gian hoàn thành**: 23/10/2026  
**Trạng thái**: Hoàn thành xuất sắc 100% – 40/40 Automated Test Cases Passed

---

## 1. TỔNG QUAN HỆ THỐNG ĐÃ NÂNG CẤP

Hệ thống quản trị máy chủ Ngọc Rồng Online đã được nâng cấp toàn diện với 2 phân hệ cốt lõi:

### 1.1. Phân hệ Buff Vật Phẩm Trực Quan (Visual Item Buff System)
- **Quy trình 2 bước (2-Step Multi-Flow)**:
  - **Bước 1 (In-game & Web)**: Nhập Item ID (tự động tra cứu tên, icon, hiển thị thông tin), số lượng (1 - 2.000.000.000), người nhận (hỗ trợ tên nhân vật, Account ID hoặc Player ID kèm tiền tố `acc:` / `pl:`). Tự động kiểm tra trạng thái Online/Offline.
  - **Bước 2 (Giao diện trực quan)**: Bảng Option đầy đủ load động 100% từ bảng `item_option_template` trong MariaDB (251 options). Tích chọn checkbox thực tế, nhập param tương ứng ngay cạnh mỗi option.
- **Tính năng nâng cao**:
  - **11 Danh mục (Category Tabs)**: Phân loại thông minh thành 11 nhóm (Sức đánh, HP, KI, Giáp, Chí mạng, Hút HP/KI, Phản sát thương, Tốc độ/Né đòn, Giảm sát thương, Set Kích Hoạt, Sao pha lê & Khác) mà không làm mất bất kỳ option nào.
  - **Tìm kiếm không dấu (Vietnamese Accentless Search)**: Tìm kiếm tức thì theo ID hoặc tên Option có dấu/không dấu (vd: `suc danh`, `hp`, `set kaioken`, `hut ki`).
  - **Lưu giữ trạng thái lựa chọn**: Chuyển đổi qua lại giữa các tab danh mục không bị mất các option đã chọn trước đó. Không bị trùng lặp option trong payload.
  - **Bảo lưu chỉ số gốc**: Tùy chọn giữ lại chỉ số mặc định của trang bị (`keepDefaultOptions`) khi buff.
  - **Cảnh báo xung đột Set Kích Hoạt**: Phát hiện và cảnh báo trực tiếp nếu tích chọn từ 2 Set Kích Hoạt khác nhau trên cùng 1 trang bị.
  - **Xem trước trực quan (Live Preview)**: Hiển thị ngay diện mạo, danh sách chỉ số và thông tin vật phẩm trước khi quyết định buff.
  - **Phát đồ an toàn (Atomic Delivery)**:
    - **Người chơi Online**: Cấp trực tiếp vào hành trang qua `InventoryService`, tự động đồng bộ packet `-36` xuống client và lưu snapshot an toàn.
    - **Người chơi Offline**: Tự động lưu vào hàng đợi `admin_item_grant` với trạng thái `PENDING`. Người chơi sẽ tự động nhận đủ đồ khi đăng nhập lại vào game.

### 1.2. Phân hệ Cộng Số Dư Nạp Tiền (Admin Balance Credit System)
- **Tra cứu tài khoản thông minh**: Hỗ trợ tìm kiếm theo Player ID, Tên nhân vật, Account ID, hoặc Tên đăng nhập. Cho phép ép kiểu chính xác bằng tiền tố `acc:123` hoặc `pl:123` để tránh trùng lặp giữa Account ID và Player ID.
- **Giao dịch chuẩn ngân hàng (ACID Atomic Transaction)**: Sử dụng `SELECT ... FOR UPDATE` khóa dòng tài khoản trong MariaDB, ngăn chặn tuyệt đối tình trạng race condition khi nhiều admin hoặc script cộng tiền đồng thời.
- **Chống cộng tiền trùng lặp (Idempotent Transaction Code)**: Bắt buộc mỗi giao dịch phải có mã giao dịch duy nhất (`trans_code`). Khóa duy nhất `UNIQUE(trans_code)` trong database đảm bảo nếu admin click nhầm nhiều lần hoặc gửi lại request thì giao dịch lặp sẽ bị từ chối an toàn mà không bị cộng tiền 2 lần.
- **Phân định rõ 2 chế độ**:
  - **Chế độ A - Nạp tiền (DEPOSIT / TOPUP)**: `vnd = vnd + amount`, `tongnap = tongnap + amount`. Cập nhật cả số dư khả dụng và thành tích đua top nạp tiền.
  - **Chế độ B - Hỗ trợ / Điều chỉnh (ADJUST)**: `vnd = vnd + amount`, giữ nguyên `tongnap`. Phù hợp cho việc đền bù, thưởng sự kiện hoặc cộng tiền thử nghiệm mà không làm sai lệch bảng xếp hạng top nạp.
- **Đồng bộ RAM & Database tức thì**:
  - Khi cộng tiền cho người chơi đang Online: Tự động cập nhật ngay `player.soDuVND`, `session.vnd`, `session.vndBar`, `session.tongnap` trong RAM cùng lúc với database. Người chơi có thể sử dụng số dư mua đồ trong game ngay lập tức mà không cần thoát game hoặc relogin.
  - Sửa đổi an toàn trong [PlayerDAO.subVndBar](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/jdbc/daos/PlayerDAO.java#L1357): Đảm bảo khi người chơi tiêu tiền trong game, số dư trong RAM và database được khấu trừ đồng bộ 100%.
- **Bảng kiểm toán (Audit Trail)**: Mọi giao dịch cộng tiền được lưu vĩnh viễn vào bảng `admin_balance_credit` gồm: admin thực hiện, account_id, player_id, số dư trước/sau, tổng nạp trước/sau, mode, ghi chú, mã giao dịch, thời gian.

### 1.3. Giao diện Web Quản Trị Nhúng (Embedded Glassmorphism Web Admin)
- Do client Ngọc Rồng Online là file binary Unity đã compile (IL2CPP) không có source code UI để sửa canvas trong game, hệ thống đã tích hợp máy chủ Web nội bộ siêu nhẹ dựa trên `com.sun.net.httpserver.HttpServer` chạy trên cổng **8888** song song cùng game server:
  - Hoạt động độc lập, không cần cài đặt thêm Apache, Nginx hay XAMPP.
  - Giao diện **Dark Glassmorphism** hiện đại, font chữ Google Fonts Inter / Outfit, hiệu ứng vi tương tác và chuyển động mượt mà.
  - Tích hợp bảo mật: CSRF Token, Sliding Session Expiration (30 phút không hoạt động), và cơ chế **One-Time Token Login (Đăng nhập 1 lần qua mã tạm thời sinh từ trong game)**.
  - Tích hợp đầy đủ cả 2 chức năng: Buff đồ trực quan (chọn checkbox, nhập param, search không dấu, modal preview) và Cộng số dư nạp tiền.

---

## 2. DANH SÁCH FILE MÃ NGUỒN ĐÃ TẠO MỚI & CHỈNH SỬA

### 2.1. Mã nguồn Java Server (`Server/src/main/java/`)
1. [AdminBalanceService.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/func/AdminBalanceService.java) *(Tạo mới)*:
   - Dịch vụ tra cứu tài khoản đa năng (`lookupAccount`).
   - Xử lý cộng số dư nạp tiền giao dịch khóa dòng (`creditBalance`).
   - Ghi log kiểm toán và tra cứu lịch sử cộng tiền (`getCreditHistory`).
2. [AdminWebAuth.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/func/webadmin/AdminWebAuth.java) *(Tạo mới)*:
   - Xác thực người dùng admin, quản lý session phiên làm việc, chống giả mạo request qua CSRF Token.
   - Cơ chế sinh và xác thực One-Time Auth Token từ chat trong game (`adminweb`).
3. [AdminWebPage.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/func/webadmin/AdminWebPage.java) *(Tạo mới)*:
   - Giao diện Web SPA Dashboard hiện đại, Dark Glassmorphism, Responsive.
   - Bảng checkbox options động 11 tabs, inline param input, Vietnamese accentless search, Live Preview Modal.
4. [AdminWebServer.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/func/webadmin/AdminWebServer.java) *(Tạo mới)*:
   - Máy chủ HTTP Server nhúng trên cổng `8888`.
   - Cung cấp toàn bộ REST API: `/api/options`, `/api/item-info`, `/api/player-info`, `/api/buff`, `/api/balance/credit`, `/api/history/balance`, `/auth-token`.
5. [AdminBuffItemService.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/func/AdminBuffItemService.java) *(Nâng cấp)*:
   - Tích hợp Bước 1 form buff đồ, Bước 2 menu option, nút mở Web Checkbox.
   - Phương thức buff trực tiếp `buffItemDirect()` phục vụ cả Web API và in-game admin.
   - Phương thức gửi link kèm token đăng nhập tự động `sendWebAdminLink()`.
6. [Input.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/func/Input.java) *(Nâng cấp)*:
   - Thêm hằng số `BUFF_STEP_1 = 528` và `ADMIN_CREDIT_FORM = 529`.
   - Hàm tạo popup form `createFormBuffStep1()` và `createFormAdminCredit()`.
7. [ConstNpc.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/consts/ConstNpc.java) *(Nâng cấp)*:
   - Thêm hằng số NPC confirm menu `ADMIN_CONFIRM_CREDIT = 531`.
8. [NpcFactory.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/models/npc/NpcFactory.java) *(Nâng cấp)*:
   - Xử lý Menu Admin (case 1: Cộng số dư nạp tiền).
   - Xử lý Menu Buff Item Options (case 0: Gửi link mở Web Checkbox).
   - Xử lý xác nhận popup nạp tiền `ADMIN_CONFIRM_CREDIT`.
9. [Service.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/Service.java) *(Nâng cấp)*:
   - Bổ sung nút "Cộng số dư" vào Menu Admin NPC.
   - Thêm lệnh chat nhanh cho Admin: `congsodu`, `napsodu`, `buffvnd`, `adminweb`, `webadmin`.
10. [ServerManager.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/server/ServerManager.java) *(Nâng cấp)*:
    - Khởi động `AdminWebServer` tự động trong luồng server và giải phóng tài nguyên khi tắt server.
11. [PlayerDAO.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/jdbc/daos/PlayerDAO.java) *(Nâng cấp)*:
    - Cập nhật hàm `subVndBar` đồng bộ tức thì `player.soDuVND`, `session.vnd`, `session.vndBar` trong RAM.
12. [Client.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/server/Client.java) & [Session.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/server/io/Session.java) *(Nâng cấp)*:
    - Bổ sung hàm gỡ bỏ và constructor hỗ trợ kiểm thử tự động an toàn không cần kết nối socket thật.
13. [TestVisualBuffAndBalanceSuites.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/scratch/TestVisualBuffAndBalanceSuites.java) *(Tạo mới)*:
    - Bộ test tự động kiểm thử 7 phân hệ với 40 bài test chi tiết.

### 2.2. Cơ sở dữ liệu MariaDB (`database/migrations/`)
1. [add_admin_balance_credit.sql](file:///D:/NRO%20DATA/database/migrations/add_admin_balance_credit.sql) *(Tạo mới & Đã thực thi)*:
   - Tạo bảng `admin_balance_credit` lưu vết lịch sử nạp tiền với khóa duy nhất `UNIQUE KEY uk_trans_code (trans_code)`.
   - Tạo index hỗ trợ tìm kiếm nhanh theo `account_id`, `player_id`, `created_at`.
   - Bổ sung cột `vnd` và `tongnap` cho bảng `account` nếu chưa tồn tại.

---

## 3. HƯỚNG DẪN SỬ DỤNG CHI TIẾT DÀNH CHO ADMIN

### 3.1. Chức năng Buff Vật Phẩm Trực Quan

#### Cách 1: Sử dụng Giao diện Web Quản Trị (Khuyến nghị)
1. Trong game, tài khoản Admin chat: `adminweb` hoặc `webadmin` (hoặc mở menu Buff Item -> Chọn **Mở Web (Checkbox)**).
2. Server sẽ tạo một mã Token sử dụng 1 lần và gửi thông báo kèm đường dẫn đăng nhập dạng:  
   `http://localhost:8888/auth-token?token=...`
3. Truy cập đường link trên trình duyệt:
   - Nhập ID vật phẩm (vd: `457` - Thỏi Vàng, `555` - Áo Thần Trái Đất). Hệ thống sẽ tự tra cứu và hiển thị tên vật phẩm.
   - Nhập số lượng cần buff.
   - Nhập người nhận (Tên nhân vật, hoặc `acc:ID` / `pl:ID`).
   - Tích chọn các Option mong muốn qua 11 tabs danh mục (Sức đánh, HP, Set Kích Hoạt...).
   - Nhập chỉ số (Param) ngay cạnh mỗi Option.
   - Có thể tích chọn **Bảo lưu chỉ số mặc định** của vật phẩm.
   - Bấm nút **Xem trước (Preview)** để kiểm tra lại toàn bộ chỉ số trước khi cấp.
   - Bấm nút **Xác nhận Buff Đồ**: Hệ thống sẽ cấp ngay vào hành trang nếu người chơi Online, hoặc lưu vào hàng đợi nếu người chơi Offline.

#### Cách 2: Sử dụng Form trong Game
1. Mở Menu Admin (gõ chat `buffitem` hoặc chat `menuadmin`).
2. Chọn **Buff Vật Phẩm (Trực Quan)**:
   - **Ô 1**: Nhập ID vật phẩm (vd: `457`).
   - **Ô 2**: Nhập số lượng.
   - **Ô 3**: Nhập tên hoặc ID người nhận.
3. Sau khi xác nhận Bước 1, server mở menu Bước 2:
   - Nhấn **Mở Web (Checkbox)** để chuyển sang giao diện web chọn nhanh hàng trăm option.
   - Hoặc chọn các option phổ biến được cài sẵn trong danh sách in-game và bấm **Hoàn tất Buff Đồ**.

---

### 3.2. Chức năng Cộng Số Dư Nạp Tiền (Cộng VNĐ)

#### Cách 1: Qua lệnh Chat nhanh trong Game
Admin có thể gõ một trong các lệnh chat:
- `congsodu`
- `napsodu`
- `buffvnd`

Hệ thống sẽ hiển thị bảng Form gồm 4 trường thông tin:
1. **Người nhận**: Nhập tên nhân vật (vd: `trumxdhp`), username tài khoản (vd: `admin2`), hoặc `acc:ID` / `pl:ID`.
2. **Số tiền VNĐ**: Nhập số tiền nguyên dương (vd: `50000` cho 50.000 VNĐ).
3. **Mã giao dịch**: Mã đơn nạp hoặc mã chuyển khoản ngân hàng (vd: `MB123456789`). Nếu để trống, server sẽ tự sinh mã duy nhất dạng `NAP_timestamp`.
4. **Ghi chú**: Lý do nạp (vd: `Nạp thẻ ATM 50k`).

Sau khi nhấn OK, một hộp thoại xác nhận chi tiết sẽ hiện lên hiển thị: Tên tài khoản, tên nhân vật, số dư hiện tại, số tiền cộng, số dư mới. Admin bấm **Đồng ý Nạp** để hoàn tất.

#### Cách 2: Qua Giao diện Web Admin
1. Mở trang quản trị Web Admin (`http://localhost:8888`).
2. Chuyển sang tab **Cộng Số Dư Nạp Tiền**.
3. Điền thông tin:
   - Tài khoản / Người chơi nhận.
   - Số tiền (VNĐ).
   - Mã giao dịch (Trans Code).
   - Chọn chế độ:
     - **Chế độ A (Nạp tiền / Topup)**: Cộng số dư + Tăng tổng nạp tính đua top.
     - **Chế độ B (Hỗ trợ / Điều chỉnh)**: Chỉ cộng số dư, không tăng tổng nạp.
   - Ghi chú giao dịch.
4. Bấm **Xác nhận Cộng Tiền**. Kết quả và lịch sử giao dịch sẽ được cập nhật tức thì trên bảng lịch sử kiểm toán phía dưới.

---

## 4. KẾT QUẢ KIỂM THỬ TỰ ĐỘNG (AUTOMATED TEST RESULTS)

Toàn bộ hệ thống đã được chạy kiểm thử tự động thông qua class [TestVisualBuffAndBalanceSuites.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/scratch/TestVisualBuffAndBalanceSuites.java) kết nối trực tiếp với MariaDB.

### Kết quả tổng hợp:
```
===============================================================
 BẮT ĐẦU KIỂM THỬ HỆ THỐNG BUFF ĐỒ TRỰC QUAN & CỘNG SỐ DƯ NẠP TIỀN
===============================================================
[INIT] Nạp danh sách Item & Item Option Template từ MariaDB... HOÀN TẤT. (Loaded 251 options, 2154 items)

--- [TEST 1] Kiểm tra Phân loại Option & Tìm kiếm không dấu ---
  [PASS] Nhóm 'Sức đánh' có ít nhất 1 option (thực tế: 33)
  [PASS] Nhóm 'HP' có ít nhất 1 option (thực tế: 9)
  [PASS] Nhóm 'KI' có ít nhất 1 option (thực tế: 14)
  [PASS] Nhóm 'Giáp' có ít nhất 1 option (thực tế: 4)
  [PASS] Nhóm 'Chí mạng' có ít nhất 1 option (thực tế: 2)
  [PASS] Nhóm 'Set Kích Hoạt' có ít nhất 9 option (127-135) (thực tế: 27)
  [PASS] Nhóm 'Sao pha lê' có ít nhất 1 option (thực tế: 4)
  [PASS] Chuẩn hóa không dấu: suc danh chi mang +100%

--- [TEST 2] Kiểm tra Giữ lựa chọn Option, Không trùng lặp, Cảnh báo SKH ---
  [PASS] Số option được chọn duy nhất là 3 (không bị nhân đôi option 77)
  [PASS] Param option 77 được cập nhật thành 120
  [PASS] Phát hiện xung đột khi chọn 2 set kích hoạt khác nhau: Xung đột: Phát hiện 2 Set Kích Hoạt trên cùng 1 vật phẩm!
  [PASS] Cấu hình 1 set kích hoạt hợp lệ không bị cảnh báo xung đột

--- [TEST 3] Kiểm tra Tra cứu Vật phẩm & Xử lý ID không hợp lệ ---
  [PASS] Tra cứu thành công ID 457: Thỏi Vàng
  [PASS] ID 999999 không tồn tại trả về null hợp lệ

--- [TEST 4] Kiểm tra Cộng số dư Chế độ A (Nạp) và Chế độ B (Điều chỉnh) ---
  [PASS] Cộng lần 1 (50.000đ) thành công: Cộng thành công 50 k VNĐ cho tài khoản u_test_bal (N/A)!
  [PASS] Số dư sau lần 1 = 60.000 VNĐ (thực tế: 60000)
  [PASS] Tổng nạp sau lần 1 = 50.000 VNĐ (thực tế: 50000)
  [PASS] Cộng lần 2 (20.000đ) thành công: Cộng thành công 20 k VNĐ cho tài khoản u_test_bal (N/A)!
  [PASS] Số dư sau lần 2 = 80.000 VNĐ (thực tế: 80000)
  [PASS] Tổng nạp sau lần 2 = 70.000 VNĐ (thực tế: 70000)
  [PASS] Điều chỉnh Chế độ B (+15.000đ) thành công: Cộng thành công 15 k VNĐ cho tài khoản u_test_bal (N/A)!
  [PASS] Số dư sau điều chỉnh = 95.000 VNĐ (thực tế: 95000)
  [PASS] Tổng nạp không đổi = 70.000 VNĐ (thực tế: 70000)

--- [TEST 5] Kiểm tra Chống cộng tiền trùng (Idempotent) & Giới hạn an toàn ---
  [PASS] Giao dịch ban đầu thành công, balance = 50000
  [PASS] Giao dịch trùng mã bị TỪ CHỐI an toàn (isDuplicate=true)
  [PASS] Số dư giữ nguyên không bị cộng lần 2 (thực tế: 50000)
  [PASS] Từ chối số tiền âm (-5.000)
  [PASS] Từ chối số tiền bằng 0
  [PASS] Từ chối số tiền cộng gây tràn số (Integer.MAX_VALUE)

--- [TEST 6] Kiểm tra Đồng bộ RAM Online & Khấu trừ số dư an toàn ---
  [PASS] Cộng tiền cho player online thành công
  [PASS] RAM player.soDuVND cập nhật ngay lập tức thành 150.000 (thực tế: 150000)
  [PASS] RAM session.vnd cập nhật ngay lập tức thành 150.000 (thực tế: 150000)
  [PASS] RAM session.tongnap cập nhật ngay lập tức thành 300.000 (thực tế: 300000)
  [PASS] Khấu trừ thành công: Số dư sau khi mua đồ: 110.000 VNĐ trong RAM
  [PASS] Database và RAM đồng bộ hoàn hảo sau khi trừ: 110000 VNĐ

--- [TEST 7] Kiểm tra Cấp vật phẩm Trực quan Online & Offline Queue ---
  [PASS] Buff online thành công: Đã buff thành công 5x Thỏi Vàng cho pl_online_buff_test (ONLINE)!
  [PASS] Hành trang người chơi nhận được vật phẩm Thỏi Vàng x5
  [PASS] Buff offline thành công tạo pending grant: Đã lưu yêu cầu buff 1x Áo Thần Linh cho trumxdhp (OFFLINE - Sẽ nhận khi đăng nhập)!
  [PASS] Bảng admin_item_grant lưu đúng bản ghi PENDING cho nhân vật offline
  [PASS] Từ chối buff Item ID không tồn tại (999999)
===============================================================
 TỔNG KẾT: 40 PASSED, 0 FAILED (100% HOÀN HẢO)
===============================================================
```

Đồng thời, bộ kiểm thử hồi quy `TestSkhAndItemsAllSuites` (8 bài kiểm thử: Thỏi Vàng 457, Rương 2083, Set Kích Hoạt 131 Ốc Tiêu, Drop Rate) tiếp tục đạt kết quả **100% PASS**, xác nhận không xảy ra bất kỳ xung đột hay lỗi hồi quy nào.

---

## 5. HƯỚNG DẪN CHẠY MÁY CHỦ TRÊN VPS HOẶC LOCAL

1. File `.jar` chính thức đã được đóng gói và cập nhật sẵn tại:  
   `D:\NRO DATA\Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea\Server\Server.jar`
2. Để khởi chạy máy chủ:
   - Sử dụng script tự động: chạy file `CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat` có sẵn trong thư mục.
   - Hoặc chạy trực tiếp qua lệnh:
     ```cmd
     java -jar Server.jar
     ```
3. Khi Server khởi động, máy chủ Web Admin sẽ tự động lắng nghe tại:
   `http://localhost:8888` (hoặc `http://<IP_VPS>:8888` trên môi trường VPS).
