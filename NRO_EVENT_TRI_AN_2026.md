# Tri Ân Sư Phụ — tháng 11/2026

## Cài đặt

1. Dừng server theo quy trình bảo trì đang dùng, sao lưu database và JAR hiện tại.
2. Lấy nhánh `codex/november-tri-an-2026` hoặc bản `main` sau khi PR sự kiện được nhập.
3. Trong thư mục `Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/Server`, chạy `mvn clean package` bằng JDK 17 hoặc 21.
4. Chép `target/TeaMobi-1.0-RELEASE-jar-with-dependencies.jar` thành `Server.jar`, hoặc dùng BAT tự build hiện có, rồi khởi động lại.
5. Vào đảo Kamê (map 5), gặp **Sứ Giả Tri Ân** ở gần x=600, y=408. NPC giới thiệu cũ ở x=780 giữ nguyên.

JAR đăng ký template sự kiện bằng dữ liệu hình có sẵn và tạo 3 bảng `november2026_*` lúc khởi động. Tài khoản database cần quyền CREATE trên database game. Nếu quyền CREATE bị hạn chế, quản trị viên tự chạy file `Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea/database/migrations/20261030_november_tri_an.sql` trước khi khởi động. File này cũng thêm template vào DB để tiện tra cứu, không ghi đè ID đã tồn tại. Không có thao tác nào kết nối/sửa trực tiếp database VPS trong quá trình phát triển.

**Xung đột ID:** dành ID vật phẩm 2400–2420 (có một số ô dự phòng), NPC 80, option 251. Nếu đã dùng các ID này cho dữ liệu khác, server báo lỗi rõ ràng thay vì ghi đè. Kiểm tra và đổi dải ID đồng bộ trước khi dùng. Không xóa bảng tiến độ hoặc đổi ID khi đã có người chơi nhận quà.

Version dữ liệu client tăng: `vsMap=6`, `vsItem=9`. Client tải lại template NPC/vật phẩm khi kết nối. Hình NPC, icon và sprite cải trang được tái sử dụng từ nguồn hiện có; không cần phân phối sprite mới. Chưa kiểm chứng hình ảnh bằng client Unity đang chạy trên VPS.

## Thời gian (Asia/Ho_Chi_Minh)

| Giai đoạn | Khoảng thời gian |
|---|---|
| Cày, ghép, tặng, nhiệm vụ, mở hộp tự ghép và tính điểm | 00:00 01/11 đến trước 00:00 01/12/2026 |
| Đổi xu, mở hộp thưởng ngày và nhận quà tham gia | 00:00 01/12 đến trước 00:00 04/12/2026 |
| Nhận thưởng top đã chốt | Từ 01/12/2026; không yêu cầu phải online lúc chốt |

Trước ngày bắt đầu, NPC chỉ cho xem thông tin. Điểm và danh sách top không thay đổi sau khi hết tháng. Bảng kết quả được chốt một lần khi có người mở bảng top/nhận thưởng sau thời hạn; thứ tự được quyết định từ dữ liệu đã khóa điểm.

## Vòng chơi

- Quái thường, trừ Mộc nhân/big boss dạng quái: **4% Bút tri ân** và **4% Thiệp trắng**, độc lập. Đệ tử đánh được tính cho sư phụ.
- Boss bị hạ: **30% nhận 1–3 Huy hiệu sư phụ**, ngoài phần thưởng boss thường. Tính một lần mỗi lượt sinh.
- Nguyên liệu rơi trên đất và nhặt vào hành trang; có option không giao dịch.
- **10 Bút + 5 Thiệp trắng → 1 Thiệp tri ân**.
- Tặng 1 thiệp: **10 xu + 10 điểm**. NPC hỗ trợ 1, 10, 100 hoặc tối đa 1.000 thiệp mỗi lần.
- **3 Thiệp + 2 Huy hiệu → 1 hộp tự ghép**. Mở mỗi hộp tự ghép trong tháng 11 được **30 điểm**, không có xu.
- Hộp thưởng nhiệm vụ/500 điểm dùng ID riêng, **không cộng điểm khi mở**.
- Hộp tự ghép phải mở trước khi khóa điểm; NPC hướng dẫn rõ thời hạn này.

## Hộp quà

| Phần thưởng (chọn 1 nhóm) | Tỷ lệ |
|---|---:|
| 3 vật phẩm ngẫu nhiên trong Cuồng nộ/Bổ huyết/Bổ khí/Giáp Xên | 40% |
| 5 đá ngẫu nhiên trong ID 220–224 | 30% |
| 5 mảnh ngẫu nhiên trong ID 1066–1070 | 20% |
| 1 cải trang trong 5 mẫu, 3 ngày | 7% |
| 1 cải trang trong 5 mẫu, 7 ngày | 2,5% |
| 1 cải trang trong 5 mẫu, vĩnh viễn | 0,5% |

Mỗi hộp chỉ ra một loại; số lượng 3/5 là một chồng cùng loại. Tỷ lệ 0,5% là tổng cho cả 5 cải trang (mỗi mẫu 0,1%). Mẫu chọn đều.

## Cải trang

| ID | Tên | SĐ | HP | KI | Dòng riêng | Hình gốc |
|---:|---|---:|---:|---:|---|---:|
| 2410 | Học Trò Chiến Binh | +32% | +25% | +25% | Chí mạng +8% | 1304 — Goku SSJ4 |
| 2411 | Ảnh Phong Học Giả | +26% | +27% | +27% | Né +18%, tốc độ +10% | 1305 — Vegeta SSJ4 |
| 2412 | Sư Phụ Kim Cang | +24% | +30% | +24% | Giáp +32%, phản sát thương 10% | 1306 — Omega |
| 2413 | Đệ Tử Sinh Lực | +25% | +32% | +32% | Hồi thêm 3% HP và KI mỗi 30 giây | 1276 — Zamasu |
| 2414 | Thần Tài Tu Luyện | −50% | −50% | −50% | +150% lượng vàng từ quái, +20% SM/TN, hút 5% sát thương đánh quái thành HP | 2129 — Fu |

Giá: 3 ngày = 50 xu; 7 ngày = 150 xu; 30 ngày = 500 xu; vĩnh viễn = 1.500 xu. Các thời hạn có cùng chỉ số. Không giới hạn số lần đổi nếu đủ xu/chỗ trống. Mọi mẫu không giao dịch. Gói 30 xu: 3 Cuồng nộ + 3 Bổ huyết + 3 Bổ khí.

Thần Tài dùng option 251 riêng cho giảm chỉ số, không dùng option 156 vốn gộp thêm SM/TN. Giảm HP/KI/SĐ sau khi tính các bonus khác, clamp HP/KI hiện tại theo giới hạn mới, tháo ra trở về chỉ số bình thường. Bonus vàng làm tăng **lượng vàng thường** trong luồng rơi quái lên 2,5 lần khi không có bonus khác; không thay tỷ lệ rơi Thỏi Vàng/Hồng Ngọc hay vàng từ nguồn khác. Giảm sát thương có thể làm tốc độ cày chậm hơn.

## Nhiệm vụ ngày và đua top

- Hạ 500 quái: 5 Bút + 3 Thiệp trắng.
- Hạ 3 boss: 2 Huy hiệu.
- Tặng 5 thiệp: 1 hộp thưởng ngày.
- Mỗi mốc nhận một lần/ngày, reset theo ngày Việt Nam; tiến độ và cờ nhận lưu trong DB.
- Đạt 500 điểm tích lũy: nhận 3 hộp thưởng, một lần cả sự kiện.
- Xếp hạng theo điểm giảm dần, thời điểm đạt mốc tăng dần, cuối cùng ID nhân vật tăng dần. NPC hiển thị vị trí của bản thân, kể cả ngoài Top 30.

| Hạng | Thưởng |
|---|---|
| 1 | Chọn 1 cải trang Vinh Danh vĩnh viễn, ID 2415–2419, hình khác mẫu thường; 1 thẻ danh hiệu lưu niệm vĩnh viễn |
| 2–3 | Chọn 1 cải trang thường vĩnh viễn; thẻ danh hiệu lưu niệm 30 ngày |
| 4–10 | Chọn 1 cải trang thường 30 ngày; 3 Cuồng nộ + 5 Đá lục bảo |
| 11–30 | Chọn 1 cải trang thường 7 ngày; 3 Cuồng nộ + 5 Đá lục bảo |

Hình Vinh Danh lần lượt tái sử dụng ID 2142, 904, 985, 1270, 2045. Chỉ số giữ nguyên các hướng chơi; không cộng vượt trần 32%. Danh hiệu ID 2420 là **thẻ lưu niệm trong túi**, tên hiển thị trên vật phẩm/NPC; chưa bổ sung banner chữ mới trên đầu nhân vật vào client. Quà top nhận một lần, lưu cờ nhận cùng giao dịch lưu túi.

## Lưu dữ liệu và kiểm tra

- Giao dịch ghép/đổi/nhận quà khóa dòng tiến độ bằng `SELECT ... FOR UPDATE`, lưu hành trang và xu/điểm trong cùng transaction InnoDB.
- Chặn lưu nhân vật đồng thời, hủy giao dịch trao đổi đang mở; nếu túi đầy/thiếu nguyên liệu/lưu DB thất bại thì rollback và phục hồi bản sao túi.
- Các vật phẩm có hạn dùng option 93 và thời điểm tạo theo cơ chế sẵn có.
- Các bảng top dùng bản chốt riêng, không phụ thuộc việc người chơi online lúc hết tháng.
- `mvn clean package` chạy các bài kiểm thử event và các bài hồi quy boss. Kiểm thử SQL dùng H2 chế độ MySQL; chưa chạy import trên MariaDB/MySQL của VPS.

### Kiểm tra trong game sau khi cập nhật

1. NPC mới và NPC giới thiệu cũ cùng hiện ở đảo Kamê.
2. Ngày trước 01/11 chỉ xem hướng dẫn; từ 01/11 quái bắt đầu rơi nguyên liệu.
3. Hành trang hiện đúng tên/icon; mỗi cải trang mặc lên hiện hình và các dòng chỉ số.
4. Ghép, tặng, đổi xong thoát vào lại vẫn còn nguyên xu/điểm/vật phẩm.
5. Túi đầy không mất nguyên liệu hoặc xu; nhận nhiệm vụ/top lần hai bị từ chối.
6. Bộ Thần Tài giảm đủ HP/KI/SĐ, tháo ra phục hồi chỉ số; vàng thường từ quái tăng đúng bonus.

Không thay đồng hồ VPS để thử mốc thời gian. Các bài unit/integration test đã có Clock giả lập riêng cho trước/sau tháng.
