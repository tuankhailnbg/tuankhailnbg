<p align="center">
  <a href="https://github.com/yanii7905/Nro_Yanii">
    <img src="https://media0.giphy.com/media/t2UyODDiTuENGVtd78/200.webp?cid=ecf05e476bw62k1t5xzmype20ehyuyh2ihk9gzmaq2xfj8i8&rid=200.webp&ct=g" alt="Logo" width="200" height="200">
  </a>

  <h1 align="center">NRO - YANII OFFLINE</h1>

  <p align="center">
    Trò chơi ngoại tuyến tái hiện nguyên tác Ngọc Rồng Online. Hóa thân thành anh hùng thuộc 3 hành tinh: Trái Đất, Xayda, Namếc, luyện tập nâng cao sức mạnh và chiến đấu chống lại các thế lực hắc ám.
    <br />
    <br />
    <a href="#-hướng-dẫn-nhanh"><strong>Khám phá ngay »</strong></a>
    <br />
    <br />
    <a href="https://github.com/yanii7905/Nro_Yanii/issues">Báo lỗi / Đóng góp</a>
  </p>
</p>

![Repo Size](https://img.shields.io/github/repo-size/yanii7905/Nro_Yanii?color=blue&style=flat-square)
![Languages](https://img.shields.io/github/languages/count/yanii7905/Nro_Yanii?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-success?style=flat-square)

---

## 📌 Mục lục
- [Giới thiệu dự án](#-giới-thiệu-dự-án)
- [Tính năng nổi bật](#-tính-năng-nổi-bật)
- [Hướng dẫn nhanh](#-hướng-dẫn-nhanh)

---

## 🌐 Giới thiệu dự án

- **Thể loại:** Hành động, nhập vai trực tiếp (Action RPG).
- **Lối chơi:** Dễ làm quen, điều khiển mượt mà, tối ưu hóa đồ họa trên nhiều nền tảng.
- **Cốt truyện:** Bám sát nguyên tác, chạm trán toàn bộ các nhân vật quen thuộc từ Bunma, Quy Lão Kame, Tau Pay Pay cho đến các ác nhân sừng sỏ như Fide, Xên, Bô-lếch...

---

## ⚡ Tính năng nổi bật

- 🥊 Tham gia Đại hội võ thuật tranh tài cao thấp.
- 🐉 Săn ngọc rồng thu thập ước nguyện riêng.
- 🏰 Khám phá hệ thống doanh trại độc lập, phó bản hấp dẫn.
- 🌐 Hỗ trợ tích hợp Web đăng ký tài khoản quản lý tiện lợi qua XAMPP.

---

## 🚀 Hướng dẫn nhanh

Để khởi chạy và trải nghiệm mượt mà hệ thống, bạn thực hiện lần lượt theo các bước sau:

### Bước 1: Chuẩn bị môi trường & Cài đặt JDK 21
1. Cài đặt sẵn **JDK 21** trên máy tính của bạn.
2. Cấu hình biến môi trường cho Java:
   - Nhấn phím `Windows + S`, tìm kiếm từ khóa **environment variables** và chọn **Edit the system environment variables**.
   - Nhấp vào nút **Environment Variables...** ở góc dưới bên phải.
   - Tại mục **System variables**, nhấn **New...** để tạo biến mới:
     - **Variable name:** `JAVA_HOME`
     - **Variable value:** `C:\Program Files\Java\jdk-21`
   - Tìm biến `Path` trong danh sách System variables, chọn nó rồi nhấn **Edit...**, chọn **New** và thêm vào:
     - `;%JAVA_HOME%\bin`
3. Khởi động và bật cụm dịch vụ **Apache / MySQL** trong ứng dụng **XAMPP**.

### Bước 2: Cấu hình Database
- Truy cập vào `phpMyAdmin`, tạo mới một cơ sở dữ liệu (Database) với tên `ngocrong`.
- Tiến hành **Import** file cơ sở dữ liệu (`.sql`) đi kèm vào database vừa tạo.

### Bước 3: Khởi động Server
1. Nhấp đúp chuột vào file `Run.bat` nằm trong thư mục **Sever**
2. Tiếp tục nhấp đúp chuột vào file `Run.bat` nằm trong thư mục **Login**  để hệ thống hoàn tất tải dữ liệu.
**Lưu Ý:** Chạy của bên Sever trước rồi lập tức chạy của bên Login

### Bước 4: Trải nghiệm game
- Truy cập vào đường dẫn thư mục `AppLocal/NroYanii.exe` để mở Client và bắt đầu bước chân vào thế giới Ngọc Rồng!

---

## 💡 Thông tin thêm

* Dự án phục vụ mục đích lưu trữ mã nguồn cá nhân, học tập và trải nghiệm ngoại tuyến. 
* Mọi đóng góp hoặc báo lỗi xin vui lòng tạo [Issue](https://github.com/yanii7905/Nro_Yanii/issues) để được hỗ trợ.