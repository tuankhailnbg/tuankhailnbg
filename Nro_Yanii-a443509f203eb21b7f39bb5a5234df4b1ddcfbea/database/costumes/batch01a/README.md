# Batch 01A - 6 cải trang (chưa có chỉ số)

Source: https://github.com/NDLong1601/nro-server/tree/main/tools/costumes/projects

- 2154 Cải trang Captain America (part 2042/2043/2044; icon 32245)
- 2155 Cải trang Spider Man (part 2045/2046/2047; icon 32277)
- 2156 Cải trang Hulk (part 2048/2049/2050; icon 32309)
- 2157 Cải trang Iron Man (part 2051/2052/2053; icon 32341)
- 2158 Cải trang Black Parten (part 2054/2055/2056; icon 32212)
- 2159 Cải trang Gojo (part 2057/2058/2059; icon 32374)

Ảnh mới: 194 ID từ 32212 đến 32405; tải 4 mức x1–x4.

1. Backup database ngocrong và resources.
2. Mở PowerShell tại thư mục Server, chạy: powershell -ExecutionPolicy Bypass -File .\\download_assets.ps1 -ServerFolder .
3. Khi ảnh tải xong, import install.sql vào MariaDB ngocrong.
4. Tăng DataGame.vsItem và DataGame.vsData, build Maven, khởi động lại Server; thoát và đăng nhập lại client để refresh cache.
5. Tặng từng item ID và kiểm tra mặc/đứng/chạy/bay/đánh/avatar; chưa đưa lên shop.

Mới tạo file trên GitHub, chưa tự import database VPS, chưa kiểm tra bằng client.
