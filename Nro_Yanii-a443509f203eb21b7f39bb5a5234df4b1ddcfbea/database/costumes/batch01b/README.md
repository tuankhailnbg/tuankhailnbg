# Batch 01B - 6 cải trang (chưa có chỉ số)

Source: https://github.com/NDLong1601/nro-server/tree/main/tools/costumes/projects

- 2160 Cải trang Akaza (part 2060/2061/2062; icon 32454)
- 2161 Cải trang Douma (part 2063/2064/2065; icon 32471)
- 2162 Cải trang Kokushibo (part 2066/2067/2068; icon 32486)
- 2163 Cải trang Kibutsuji Muzan (part 2069/2070/2071; icon 32519)
- 2164 Cải trang Rengoku (part 2072/2073/2074; icon 32421)
- 2165 Cải trang Shinobu Kocho (part 2075/2076/2077; icon 32433)

Ảnh mới: 120 ID từ 32406 đến 32525; tải 4 mức x1–x4.

1. Cài batch01a trước; backup database ngocrong và resources.
2. Mở PowerShell tại thư mục Server: powershell -ExecutionPolicy Bypass -File .\\download_assets.ps1 -ServerFolder .
3. Khi ảnh tải xong, import install.sql vào MariaDB ngocrong.
4. Tăng DataGame.vsItem và DataGame.vsData, build Maven, khởi động lại Server; thoát và đăng nhập lại client.
5. Tặng từng item ID và kiểm tra mặc/đứng/chạy/bay/đánh/avatar; chưa đưa lên shop.

Mới tạo file trên GitHub, chưa tự import database VPS, chưa kiểm tra bằng client.
