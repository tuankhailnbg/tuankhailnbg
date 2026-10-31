# NRO public server – chống DoS ở lớp ứng dụng

> Phạm vi: server Java TCP `14445`, server đăng nhập `3105`, bảng quản trị HTTP.
> Không có sửa đổi giao thức hoặc dữ liệu nhân vật.

## Bảo vệ đã tích hợp

| Mức bảo vệ | Mặc định | Ý nghĩa |
| --- | --- | --- |
| Số phiên TCP đồng thời/IP | `server.maxperip` (mặc định 5) | Người chơi cùng NAT dùng chung giới hạn |
| TCP kết nối mới/IP/10 giây | 20 | Chặn reconnect flood |
| TCP kết nối mới/IP/phút | 60 | Giảm churn kéo dài |
| TCP kết nối mới/toàn server/giây | 150 | Giới hạn tăng số lượng phiên |
| Tổng kết nối đang hoạt động | 1400 | Chặn kiệt tài nguyên JVM |
| Phong tỏa IP tạm thời | 30 giây sau >=30 lần từ chối trong 10 giây | Không khóa vĩnh viễn |
| Gói tin/game session/giây | 180 | Đóng phiên có lưu lượng gói bất thường |
| Timeout bắt tay TCP ban đầu | 15 giây | Chặn socket không gửi gói đầu |
| Xác thực/IP/10 giây | 15 | Giảm truy vấn đăng nhập liên tục |
| Xác thực/IP/phút | 45 | Giảm brute force / database flood |
| Kích thước gói đến | 1024 byte (giới hạn đã có) | Giới hạn cấp phát bộ nhớ theo frame |

Các giới hạn mặc định được chọn tương đối rộng để tránh ảnh hưởng thao tác chơi bình thường. Người chơi dùng chung địa chỉ IP (NAT quán net, trường học, gia đình) có thể gặp hạn mức chung. Theo dõi log trước khi hạ giới hạn.

**Tùy chỉnh**: khai báo các cờ JVM khi chạy `Server`:

```cmd
java -Dnro.guard.connectionsPer10SecPerIp=20 -Dnro.guard.connectionsPerMinutePerIp=60 -Dnro.guard.newConnectionsPerSecondGlobal=150 -Dnro.guard.maxActiveConnectionsGlobal=1400 -Dnro.guard.maxPacketsPerSecond=180 -jar "target\TeaMobi-1.0-RELEASE-jar-with-dependencies.jar"
```

Số phiên đồng thời/IP thay đổi qua `server.maxperip` trong cấu hình server (không phải cờ JVM).

## Thu hẹp bề mặt tấn công

* TCP `14445`: **giữ mở cho người chơi**, hỗ trợ IPv4/IPv6 theo cấu hình VPS.
* TCP `3105` (Login): **chỉ bind 127.0.0.1 mặc định**. Game Server hiện kết nối nội bộ tới `127.0.0.1:3105`. Chỉ khi dùng Login trên máy riêng an toàn mới cấu hình `-Dnro.login.bindAddress=<IP-private>` và giới hạn nguồn tại firewall.
* Bảng quản trị `8181` hoặc cổng trong `Manager.apiPort`: **chỉ bind 127.0.0.1 mặc định**. Mở trên trình duyệt ngay trong VPS: `http://127.0.0.1:8181`. Muốn quản trị từ máy ngoài: ưu tiên VPN / RDP, không nên đặt cổng admin trực tiếp lên Internet. Có thể cấu hình `-Dnro.admin.bindAddress=<IP-private>` nếu đã có mạng riêng và allowlist.
* TCP `3306`: **không để truy cập công cộng nếu MySQL/MariaDB chỉ phục vụ local**. Cấu hình `bind-address=127.0.0.1` trong MariaDB/MySQL hoặc chặn inbound từ Internet ở firewall của nhà cung cấp VPS. Xác minh ứng dụng không có phụ thuộc kết nối database từ xa trước khi đổi.
* TCP `3389` (RDP): không tùy tiện chặn, vì sẽ mất quyền điều khiển VPS. Nên sử dụng VPN hoặc danh sách IP quản trị được phép tại firewall của nhà cung cấp.
* Tránh vô tình mở các cổng quản trị khác qua Windows Firewall, NAT hay security group.

Kiểm tra sau khi khởi động trên CMD VPS:

```cmd
netstat -ano | findstr /C:":3105" /C:":14445" /C:":8181" /C:":3306"
```

Kết quả mong đợi đối với Login và trang Admin đã đổi: lắng nghe `127.0.0.1:<port>` thay vì `0.0.0.0:<port>`. Game `14445` tiếp tục lắng nghe địa chỉ công cộng. TCP `3306` cần bảo vệ riêng.

## Triển khai / khôi phục

1. Tạo bản sao lưu mã nguồn, hai JAR đang hoạt động, file cấu hình và database trước khi triển khai.
2. Chỉ update sau khi cả 2 workflow GitHub Actions build + smoke test thành công; chọn thời gian ít người chơi vì script VPS khởi động lại Java.
3. `git pull --ff-only origin main`, sau đó chạy file `CHAY_NRO_LOGIN_SERVER_AUTO_VPS.bat` trong thư mục mã nguồn.
4. Kiểm tra đăng nhập `CLIENT_READY → AUTH_REQUEST → AUTH_OK → ENTER_GAME_OK`, thử đổi map, đăng xuất, đăng nhập lại, 2–5 client ở chung một IP và quản trị từ localhost.
5. Quan sát dòng `[ANTI_DOS] Dropped ...` / `Packet rate exceeded`. Nếu xuất hiện với người chơi bình thường, điều chỉnh hạn mức trước khi triển khai rộng.
6. Nếu lỗi, khôi phục 2 JAR cùng mã nguồn/config đã sao lưu. Không thay đổi database để thử chống DoS.

## Giới hạn của giải pháp

Các giới hạn Java giúp giảm **DoS lớp ứng dụng**: chiếm socket, spam đăng nhập, flood gói tin. **Không thể chống tràn băng thông** khi nhiều máy gửi lưu lượng lớn tới IP VPS, bởi dữ liệu tấn công đã đi qua mạng trước khi đến Java.

Để bảo vệ server công khai trước DDoS TCP/UDP/SYN flood, yêu cầu **nhà cung cấp VPS chống DDoS ở mạng upstream** hoặc dịch vụ proxy bảo vệ **TCP game tùy biến** trên cổng `14445`. CDN thông thường dành cho HTTP/HTTPS không tự bảo vệ TCP game này. Xác nhận dịch vụ proxy hỗ trợ giao thức raw TCP và cơ chế chuyển tiếp IP trước khi chuyển client.

Không thử tải bằng công cụ flood trên IP công khai. Dùng kiểm thử socket/gói tin quy mô nhỏ trong môi trường riêng và chỉ trong giới hạn hợp pháp được cấp phép.
