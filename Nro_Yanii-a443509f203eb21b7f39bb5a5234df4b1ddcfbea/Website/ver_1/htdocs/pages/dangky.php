<?php
require_once('../core/config.php');
require_once('../core/head.php');
$thongbao = null;
session_start();

// Kiểm tra nếu người dùng đã đăng nhập thì chuyển hướng về trang chủ
if (isset($_SESSION['logger']['username'])) {
    echo '<script>window.location.href = "/";</script>';
    exit();
}

// Xử lý khi người dùng nhấn nút Đăng ký
if (isset($_POST['submit']) && $_POST['username'] != '' && $_POST['password'] != '' && $_POST['gmail'] != '') {
    $username = $_POST['username'];
    $password = $_POST['password'];
    $gmail = $_POST['gmail'];


    // Kiểm tra mật khẩu và tên đăng nhập có đủ độ dài
    if (strlen($password) < 6 || strlen($username) < 6) {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Tên đăng nhập và mật khẩu phải chứa ít nhất 6 ký tự.</span>';
    } else {
        // Kiểm tra ký tự đặc biệt trong tên đăng nhập và mật khẩu
        if (!preg_match('/^[a-z\d_]{5,20}$/i', $username) || !preg_match('/^[a-z\d_]{5,20}$/i', $password)) {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Tên đăng nhập hoặc mật khẩu không được chứa ký tự đặc biệt.</span>';
        } else {
            // Kiểm tra định dạng email
            if (!filter_var($gmail, FILTER_VALIDATE_EMAIL)) {
                $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Email không hợp lệ.</span>';
            } else {

                // Kiểm tra xem tên đăng nhập đã tồn tại trong CSDL hay chưa
                $sql = "SELECT * FROM account WHERE username = '$username'";
                $result = mysqli_query($config, $sql);

                if (mysqli_num_rows($result) > 0) {
                    $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Tên đăng nhập đã tồn tại!</span>';
                } else {
                    // Kiểm tra xem gmail đã tồn tại trong CSDL hay chưa
                    $sql_gmail = "SELECT * FROM account WHERE gmail = '$gmail'";
                    $result_gmail = mysqli_query($config, $sql_gmail);

                    if (mysqli_num_rows($result_gmail) > 0) {
                        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Email đã tồn tại!</span>';
                    } else {
                        // Thực hiện thêm tài khoản mới vào CSDL
                        $sql_insert = "INSERT INTO account (username, password, gmail) VALUES ('$username', '$password', '$gmail')";
                        mysqli_query($config, $sql_insert);
                        $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đăng ký thành công!</span>';
                        sleep(2);
                        // Chuyển hướng sau khi đăng ký thành công
                        // echo '<script>window.location.href = "/pages/dangnhap.php";</script>';
                    }
                }
            }
        }
    }
}

?>
<main>
    <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
        <form class="text-center col-lg-5 col-md-10" style="margin: auto;"
            method="post" action="">
            <h1 class="h3 mb-3 font-weight-normal">Đăng Ký Tài Khoản</h1>
            <?= $thongbao; ?>
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username" required="" autofocus=""
                type="text" class="form-control mt-1" placeholder="Tên tài khoản">
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password" required=""
                type="password" class="form-control mt-1" placeholder="Mật khẩu">
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="gmail" required=""
                type="gmail" class="form-control mt-1" placeholder="email">

            <div class="text-center mt-1">
                <button class="btn btn-lg btn-dark btn-block" style="border-radius: 10px;width: 100%; height: 50px;"
                    type="submit" name="submit">Đăng ký
                </button>
            </div>
        </form>
    </div>
</main>
</div>
<script>
    const redirectLink = 'http://localhost/dangky';
    document.addEventListener('keydown', function(event) {
        if (event.key === 'F12' || (event.ctrlKey && event.shiftKey && event.key === 'I')) {
            window.location.href = redirectLink;
            event.preventDefault();
        }
    });

    document.addEventListener('contextmenu', function(event) {
        window.location.href = redirectLink;
        event.preventDefault();
    });

    setInterval(blockDevTools, 1000);
</script>
<style>
    <?php require_once('../core/end.php'); ?>