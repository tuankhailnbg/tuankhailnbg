# Batch 01C - 6 cải trang (chưa có chỉ số)

Source: https://github.com/NDLong1601/nro-server/tree/main/tools/costumes/projects

- 2166 Cải trang Mitsuri Kanroji (part 2078/2079/2080; icon 32609)
- 2167 Cải trang Tengen Uzui (part 2081/2082/2083; icon 32576)
- 2168 Cải trang Tomioka Giyuu (part 2084/2085/2086; icon 32541)
- 2169 Cải trang Yoriichi (part 2087/2088/2089; icon 32636)
- 2170 Cải trang Gyomei Himejima (part 2090/2091/2092; icon 32594)
- 2171 Cải trang Obanai Iguro (part 2093/2094/2095; icon 32558)

Ảnh mới: 120 ID từ 32526 đến 32645; tải 4 mức x1–x4.

1. Cài batch01a và batch01b trước, backup database ngocrong và resources.
2. Mở PowerShell tại thư mục Server: powershell -ExecutionPolicy Bypass -File .\\download_assets.ps1 -ServerFolder .
3. Khi ảnh tải xong, import install.sql vào MariaDB ngocrong.
4. Tăng DataGame.vsItem và DataGame.vsData, build Maven, khởi động lại Server; thoát và đăng nhập lại client.
5. Tặng từng item ID và kiểm tra mặc/đứng/chạy/bay/đánh/avatar; chưa đưa lên shop.

Mới tạo file trên GitHub, chưa tự import database VPS, chưa kiểm tra bằng client.
