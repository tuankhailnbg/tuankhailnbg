# BÁO CÁO XỬ LÝ TRIỆT ĐỂ ĐĂNG KÝ TÀI KHOẢN TRONG GAME & TĂNG DROP SET KÍCH HOẠT LÊN 15%

---

## 1. NGUYÊN NHÂN THẬT SỰ KHIẾN NÚT "TẠO TÀI KHOẢN" HIỆN THÔNG BÁO WEB CŨ

### 1.1. Xác định chính xác nguồn gốc thông báo
- **Nguồn gốc phát sinh**: Thông báo yêu cầu đăng ký qua website đến từ **Game Server**, hoàn toàn **không** phải mã cứng (hardcoded) trong Client Unity hay Login Server.
- **Vị trí gốc**: Trong file [Controller.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/server/Controller.java#L805), phương thức `login2(Session session, Message msg)` (xử lý gói tin `-101` `Cmd.LOGIN2` khi người chơi bấm nút "Chơi mới" / "Tạo tài khoản" trên client) trước đây từng chứa:
  ```java
  Service.gI().sendThongBaoOK(session, "Vui lòng đăng ký tài khoản tại www.nronotbuff.com!");
  ```
- **Tại sao lỗi quay lại sau các lần cập nhật server?**:
  1. Trong script khởi động [CHAY_NRO_LOGIN_SERVER_AUTO.bat](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/CHAY_NRO_LOGIN_SERVER_AUTO.bat#L142-L177): Bước 5 chạy `mvn clean package -DskipTests` sinh file `target\TeaMobi-1.0-RELEASE-jar-with-dependencies.jar`, nhưng **không bao giờ copy** đè vào file `Server.jar`. Trong khi đó, bước 7 gọi [Server\run.bat](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/run.bat#L1) chạy trực tiếp lệnh `java -jar Server.jar`. Dẫn đến mỗi lần restart server, JVM tiếp tục nạp bản JAR cũ trên đĩa.
  2. Trong file [CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat](file:///D:/NRO%20DATA/CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat#L8-L9), đường dẫn trỏ tới `%ROOT%\Login` và `%ROOT%\Server` trong khi thư mục thực tế nằm lồng bên trong `Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea`, khiến script VPS không chạy được và người quản trị có thể vô tình chạy file JAR cũ chưa cập nhật.

---

## 2. KIỂM TRA TOÀN BỘ CƠ CHẾ GIAO THỨC ĐĂNG KÝ CLIENT - SERVER

1. **Khi người chơi bấm "Tạo tài khoản" / "Chơi mới"**:
   - Client gửi packet `Cmd.LOGIN2` (`-101`) lên Game Server.
   - [Controller.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/server/Controller.java#L805) gọi [Service.gI().switchToRegisterScr(session)](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/Service.java#L494).
   - Server gửi gói tin `Message(42)` với byte dữ liệu `0` (`writeByte(0)`).
   - Client Unity nhận packet `42` byte `0` và lập tức chuyển màn hình sang form đăng ký nội bộ game (`RegisterScreen` / `LoginScr.actRegister` với các trường `tfUser`, `tfPass`, `passRe` - Nhập lại mật khẩu).
2. **Khi người chơi điền thông tin và bấm [ĐĂNG KÝ]**:
   - Client tự động kiểm tra khớp mật khẩu (`pass == passRe`).
   - Client gửi packet `42` lên Server bao gồm: 7 chuỗi UTF phụ trợ (thông tin thiết bị client) + `username` (readUTF) + `password` (readUTF).
   - [Controller.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/server/Controller.java#L351) (`case 42`) gọi [Service.getInstance().regisAccount(_session, _msg)](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/Service.java#L447).
   - Server đọc tuần tự 7 chuỗi UTF để không lệch packet, đọc `user` và `pass`, kiểm tra tính hợp lệ:
     - Không rỗng, không null.
     - Username dài từ 4 đến 18 ký tự, không chứa khoảng trắng, định dạng `^[a-zA-Z0-9_.-]+$`.
     - Password dài từ 5 đến 18 ký tự, không chứa khoảng trắng.
     - Nếu không hợp lệ: Trả thông báo `Thông tin đăng ký không hợp lệ.`
     - Kiểm tra trùng và thêm vào CSDL qua [AccountDAO.createAccount(user, pass)](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/jdbc/daos/AccountDAO.java#L98).
     - Nếu thành công: Trả thông báo `Đăng ký tài khoản thành công!`, ghi log an toàn (không lộ mật khẩu).
     - Nếu trùng username: Trả thông báo `Tên tài khoản đã tồn tại.`
     - Nếu lỗi kết nối/hệ thống: Trả thông báo `Không thể đăng ký tài khoản. Vui lòng thử lại.`

---

## 3. CÁC TẬP TIN ĐÃ CHỈNH SỬA & ĐÃ XÓA

### 3.1. Các tập tin Java đã chỉnh sửa
1. [AccountDAO.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/jdbc/daos/AccountDAO.java#L98):
   - Thêm từ khóa `synchronized` cho phương thức `createAccount(String user, String password)`.
   - Ngăn chặn triệt để xung đột kết nối JDBC (race condition) trên `connections[3]` khi có nhiều người đăng ký tài khoản cùng 1 mili-giây.
   - Bảo đảm tính toàn vẹn kiểm tra trùng và insert CSDL với PreparedStatement, bắt mã lỗi MariaDB 1062 duplicate key.
2. [Service.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/Service.java#L447-L493):
   - Chuẩn hóa toàn bộ thông báo đăng ký theo đúng yêu cầu nghiệp vụ:
     - Thành công: `"Đăng ký tài khoản thành công!"`
     - Trùng tài khoản: `"Tên tài khoản đã tồn tại."`
     - Sai thông tin: `"Thông tin đăng ký không hợp lệ."`
     - Lỗi hệ thống: `"Không thể đăng ký tài khoản. Vui lòng thử lại."`
   - Bổ sung kiểm tra chặt chẽ: username rỗng, password rỗng, độ dài (4–18 cho user, 5–18 cho pass), không chứa khoảng trắng, ký tự hợp lệ.
3. [RewardService.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/RewardService.java#L44):
   - Cập nhật hằng số: `public static final int SKH_DROP_RATE = 15;`.
   - Cơ chế random độc lập 100%: `ThreadLocalRandom.current().nextInt(100) < SKH_DROP_RATE` cho mỗi quái thường bị tiêu diệt.
   - Thêm điều kiện loại trừ boss: `&& !(mob instanceof BigBoss)` bên cạnh `MOC_NHAN`, `HIRUDEGARN`, `VUA_BACH_TUOC`, `ROBOT_BAO_VE`.
   - Loại bỏ lời gọi `ServerNotify.gI().notify(...)` toàn server tại điểm drop để loại bỏ hoàn toàn hiện tượng nghẽn mạng / giật lag khi nhiều người chơi cùng farm quái ở tỷ lệ 15%.
   - Bảo toàn quyền sở hữu vật phẩm cho người tiêu diệt (`killer.id`), nếu đệ tử đánh chết quái thì tính theo hành tinh và quyền sở hữu của sư phụ (`((Pet) player).master`).

### 3.2. Cải tiến Script khởi động chống chạy nhầm JAR cũ
1. [CHAY_NRO_LOGIN_SERVER_AUTO.bat](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/CHAY_NRO_LOGIN_SERVER_AUTO.bat#L156-L163):
   - Thêm bước tự động đồng bộ: Sau khi Maven build `target/*-jar-with-dependencies.jar`, script tự động copy đè sang `%SERVER_DIR%\Server.jar`. Khi khởi động `run.bat`, luôn luôn chạy bản bytecode mới nhất.
2. [CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat](file:///D:/NRO%20DATA/CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat#L8-L12):
   - Sửa cơ chế phát hiện đường dẫn thông minh: tự động nhận diện cả thư mục gốc và thư mục nhánh `Nro_Yanii-...`.
   - Bổ sung tìm kiếm tự động công cụ Maven tại thư mục người dùng.

### 3.3. Các tập tin web đăng ký cũ đã xóa khỏi dự án
- [Website/ver_1/htdocs/pages/dangky.php](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_1/htdocs/pages/dangky.php) *(đã xóa bằng git rm)*
- [Website/ver_2/htdocs/pages/dangky.php](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_2/htdocs/pages/dangky.php) *(đã xóa bằng git rm)*
- **Lý do xóa**: Đây là hai trang PHP tạo tài khoản cũ qua trình duyệt web không còn sử dụng, chèn mật khẩu trực tiếp vào MariaDB.
- **Bảo toàn tuyệt đối**:
  - Web Admin cổng 8888 (quản lý Buff Đồ và Cộng số dư nạp tiền) hoàn toàn độc lập trong server Java (`AdminWebServer.java`), không bị ảnh hưởng.
  - Trang nạp thẻ, nạp ATM/mbbank, bảng xếp hạng, diễn đàn, đổi mật khẩu, kích hoạt trên Website vẫn được giữ nguyên vẹn.
- **Cập nhật tham chiếu giao diện Web**:
  - Đã loại bỏ nút dẫn sang `/dangky` và `/pages/dangky.php` tại [Website/ver_1/htdocs/core/head.php](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_1/htdocs/core/head.php#L244), [Website/ver_2/htdocs/core/head.php](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_2/htdocs/core/head.php#L510) và [Website/ver_2/htdocs/core/head1.php](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_2/htdocs/core/head1.php#L530).

---

## 4. CHI TIẾT CẤU HÌNH DROP SET KÍCH HOẠT (SKH) 15%

### 4.1. Vị trí cấu hình
- File: [RewardService.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/nro/services/RewardService.java#L44)
- Hằng số:
  ```java
  public static final int SKH_DROP_RATE = 15;
  ```
- Biểu thức tỷ lệ:
  ```java
  ThreadLocalRandom.current().nextInt(100) < SKH_DROP_RATE
  ```
  *(Giá trị từ 0 đến 14 trong tập 100 số nguyên [0..99] => xác suất độc lập chính xác 15.00%).*

### 4.2. Phân bổ Set theo đúng Hành tinh người tiêu diệt
Hệ thống lấy theo `killer.gender`:
- **Trái Đất (gender = 0)**:
  - Set Songoku: Option `129`, Hiệu ứng Option `141`
  - Set Thiên Xin Hăng (Tenshinhan): Option `127`, Hiệu ứng Option `139`
  - Set Krillin (Kirin): Option `128`, Hiệu ứng Option `140`
- **Namếc (gender = 1)**:
  - Set Ốc Tiêu (Option 131 giữ nguyên tên Ốc Tiêu): Option `131`, Hiệu ứng Option `143`
  - Set Pikkoro Daimao: Option `132`, Hiệu ứng Option `144`
  - Set Piccolo: Option `130`, Hiệu ứng Option `142`
- **Xayda (gender = 2)**:
  - Set Nappa: Option `135`, Hiệu ứng Option `138`
  - Set Kakarot: Option `133`, Hiệu ứng Option `136`
  - Set Cadic (Vegeta): Option `134`, Hiệu ứng Option `137`

### 4.3. Random công bằng 1 trong 5 món & chỉ số vật phẩm
- 5 loại món đồ (`slot = Util.nextInt(0, 4)`):
  - Slot 0: **Áo** (Trái Đất: ID 0, Namếc: ID 1, Xayda: ID 2)
  - Slot 1: **Quần** (Trái Đất: ID 6, Namếc: ID 7, Xayda: ID 8)
  - Slot 2: **Găng** (Trái Đất: ID 21, Namếc: ID 22, Xayda: ID 23)
  - Slot 3: **Giày** (Trái Đất: ID 27, Namếc: ID 28, Xayda: ID 29)
  - Slot 4: **Rada** (ID 12 - Rada cấp 1 chuẩn cho cả 3 hành tinh)
- Khởi tạo đầy đủ option cơ bản qua `initBaseOptionClothes`: Áo (giáp), Quần (HP), Găng (sức đánh), Giày (KI), Rada (chí mạng).
- Gắn Option Set: Option tên set (param 1), Option hiệu ứng (param 100), Option không thể giao dịch (30:0), Option rớt từ quái (73:0).
- Không ảnh hưởng đến các bảng drop vàng, ngọc, exp, bí kiếp, thức ăn, capsule kì bí hay Hộp SKH Thần Linh (ID 2083).

---

## 5. KẾT QUẢ BUILD & KIỂM THỬ TỰ ĐỘNG

### 5.1. Kết quả Build Maven
- **Lệnh thực hiện**: `mvn clean package -DskipTests`
- **Kết quả**: `[INFO] BUILD SUCCESS` (Thời gian build: ~14.1s).
- **Artifact đầu ra**:
  - `target/TeaMobi-1.0-RELEASE-jar-with-dependencies.jar` (15,042,442 bytes)
  - Đã đồng bộ sang [Server.jar](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/Server.jar) thành công.

### 5.2. Kết quả chạy bộ kiểm thử toàn diện [TestRegisterAndDrop15Suites.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/scratch/TestRegisterAndDrop15Suites.java)
```
================================================================================
     NRO - TEST REGISTRATION & SKH 15% DROP RATE COMPREHENSIVE SUITE
================================================================================
  [SETUP] Loaded 251 ItemOptionTemplates into Manager

--- [SUITE 1] In-Game Registration Validation & Database Persistence ---
  [PASS] Create Account Valid (Created account id=19, username=testreg_85762)
  [PASS] Account in MariaDB (DB record verified: id=19, user=testreg_85762, pass_len=9)
  [PASS] Account persisted (Account successfully stored in MariaDB)
  [PASS] Duplicate Account Rejected (Result: -2 == -2)
  [PASS] Short user (<4 chars) rejected (len=3 rejected)
  [PASS] Long user (>18 chars) rejected (len>18 rejected)
  [PASS] Short pass (<5 chars) rejected (len=4 rejected)
  [PASS] Long pass (>18 chars) rejected (len>18 rejected)
  [PASS] User with space rejected (Contains space)
  [PASS] Pass with space rejected (Contains space)
  [PASS] Null user rejected (Null user)
  [PASS] Null pass rejected (Null pass)
  [PASS] Empty user rejected (Empty user)
  [PASS] Empty pass rejected (Empty pass)
  [PASS] Invalid chars in user rejected (Special symbols)
  [PASS] Min len user (4) & pass (5) valid (len=4 and 5 accepted)
  [PASS] Max len user (18) & pass (18) valid (len=18 accepted)

--- [SUITE 2] Concurrent Registration Race Condition Test ---
  [PASS] Exactly 1 registration succeeded among 10 concurrent calls (Successes=1, Duplicates=9, Errors=0)
  [PASS] Remaining 9 concurrent calls returned DUPLICATE (Duplicates=9 of 9)
  [PASS] Zero unexpected errors during concurrent insert (Errors=0)
  [PASS] MariaDB contains exactly 1 unique record (Count=1)

--- [SUITE 3] SKH 15% Drop Rate Statistical Simulation (100,000 Kills) ---
  [PASS] RewardService.SKH_DROP_RATE is 15 (SKH_DROP_RATE=15)
  Total Kills: 100000, Total SKH Drops: 15035, Rate: 15.04%
  [PASS] Total drops approx 15% (14.5% - 15.5%) (15.04% within expected statistical range)
  Slot Distribution (Áo, Quần, Găng, Giày, Rada):
    Slot 0 (Áo):   3035 drops (20.19% of drops) -> [PASS]
    Slot 1 (Quần): 3046 drops (20.26% of drops) -> [PASS]
    Slot 2 (Găng): 2966 drops (19.73% of drops) -> [PASS]
    Slot 3 (Giày): 2868 drops (19.08% of drops) -> [PASS]
    Slot 4 (Rada): 3120 drops (20.75% of drops) -> [PASS]
  Set Distribution across all 9 sets:
    Option 127 (Tenshinhan): 1685 drops (11.21%) -> [PASS]
    Option 128 (Krillin):    1722 drops (11.45%) -> [PASS]
    Option 129 (Songoku):    1625 drops (10.81%) -> [PASS]
    Option 130 (Piccolo):    1639 drops (10.90%) -> [PASS]
    Option 131 (Ốc Tiêu):    1716 drops (11.41%) -> [PASS]
    Option 132 (Daimao):     1663 drops (11.06%) -> [PASS]
    Option 133 (Kakarot):    1632 drops (10.85%) -> [PASS]
    Option 134 (Vegeta):     1603 drops (10.66%) -> [PASS]
    Option 135 (Nappa):      1750 drops (11.64%) -> [PASS]

--- [SUITE 4] Planet Gender SKH Mapping & Item Options Verification ---
  [PASS] Earth Set 127 present (Tenshinhan) (Found 127)
  [PASS] Earth Set 128 present (Krillin) (Found 128)
  [PASS] Earth Set 129 present (Songoku) (Found 129)
  [PASS] Earth Effect 139 present (Found 139)
  [PASS] Earth Effect 140 present (Found 140)
  [PASS] Earth Effect 141 present (Found 141)
  [PASS] Namek Set 130 present (Piccolo) (Found 130)
  [PASS] Namek Set 131 present (Ốc Tiêu) (Found 131)
  [PASS] Namek Set 132 present (Pikkoro Daimao) (Found 132)
  [PASS] Namek Effect 142 present (Found 142)
  [PASS] Namek Effect 143 present (Found 143)
  [PASS] Namek Effect 144 present (Found 144)
  [PASS] Saiyan Set 133 present (Kakarot) (Found 133)
  [PASS] Saiyan Set 134 present (Vegeta) (Found 134)
  [PASS] Saiyan Set 135 present (Nappa) (Found 135)
  [PASS] Saiyan Effect 136 present (Found 136)
  [PASS] Saiyan Effect 137 present (Found 137)
  [PASS] Saiyan Effect 138 present (Found 138)
  [PASS] All 3,000 generated items strictly adhered to killer planet gender (3,000/3,000 validated)

--- [SUITE 5] Pet Killer Master Ownership & Gender Assignment ---
  [PASS] Killer resolves to Pet Master (Master ID=88888)
  [PASS] Gender is Master gender (1=Namek) (Gender=1)
  [PASS] Killer is not boss (isBoss=false)

--- [SUITE 6] Boss & Non-Normal Mobs Drop Exclusion ---
  [PASS] Moc Nhan excluded (tempId=0)
  [PASS] Hirudegarn excluded (tempId=70)
  [PASS] Octopus excluded (tempId=71)
  [PASS] Guard Robot excluded (tempId=72)
  [PASS] BigBoss instance excluded (instanceof BigBoss)
  [PASS] Normal Mob eligible for SKH drop (tempId=1)

================================================================================
RESULTS: 65 PASSED, 0 FAILED (100% PASS)
================================================================================
```

### 5.3. Kết quả chạy bộ kiểm thử hồi quy các tính năng hiện tại ([TestVisualBuffAndBalanceSuites.java](file:///D:/NRO%20DATA/Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server/src/main/java/scratch/TestVisualBuffAndBalanceSuites.java))
```
===============================================================
 TỔNG KẾT HỒI QUY: 40 PASSED, 0 FAILED (100% PASS)
===============================================================
- Web Admin cổng 8888: Hoạt động chuẩn xác.
- Tra cứu vật phẩm, buff online/offline, chống trùng thẻ nạp: Đảm bảo toàn vẹn.
- Thỏi vàng 457, Set Ốc Tiêu, Hộp SKH 2083: Giữ nguyên vẹn.
```

---

## 6. HƯỚNG DẪN KHỞI ĐỘNG VÀ ĐỒNG BỘ GIT

### 6.1. Cách khởi động Server bản mới
- **Chạy môi trường Local**:
  Nhấp đúp chuột vào file:
  `D:\NRO DATA\Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea\CHAY_NRO_LOGIN_SERVER_AUTO.bat`
  *(File BAT sẽ tự động tắt tiến trình cũ, build Maven mới, copy file sang Server.jar và mở Login + Game Server).*
- **Chạy môi trường VPS**:
  Sử dụng file:
  `D:\NRO DATA\CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat`

### 6.2. Hướng dẫn Commit & Push Git nếu người dùng muốn đồng bộ lên GitHub
Khi muốn đồng bộ các thay đổi và việc xóa file `dangky.php` lên GitHub:
```bash
git add -A
git commit -m "fix: in-game registration flow, remove legacy web register and set mob SKH drop rate to 15%"
git push origin main
```

### 6.3. Cách Rollback nếu phát sinh sự cố
Nếu cần khôi phục lại trạng thái trước khi sửa:
```bash
git restore --staged .
git restore .
git checkout HEAD -- Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_1/htdocs/pages/dangky.php
git checkout HEAD -- Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Website/ver_2/htdocs/pages/dangky.php
```
