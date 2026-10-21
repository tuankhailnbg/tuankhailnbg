<?php
require_once('../core/config.php');
require_once('../core/head.php');

$thongbao = null;
session_start();

if (isset($_SESSION['logger']['username'])) {
    echo '<script>window.location.href = "/";</script>';
    exit();
}

if (isset($_POST['submit']) && $_POST['username'] != '' && $_POST['password'] != '') {
    $username = $_POST['username'];
    $password = $_POST['password'];

    // Use prepared statement to prevent SQL Injection
    $sql = "SELECT * FROM account WHERE username = ?";
    $stmt = mysqli_prepare($config, $sql);
    mysqli_stmt_bind_param($stmt, "s", $username);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    if ($result->num_rows > 0) {
        $row = mysqli_fetch_assoc($result);
        $storedPassword = $row['password'];

        // Compare passwords
        if ($password == $storedPassword) {
            $_SESSION['logger']['username'] = $username;
            echo '<script>window.location.href = "/";</script>';
            $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đăng nhập thành công!</span>';
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Sai tài khoản hoặc mật khẩu!</span>';
        }
    } else {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Sai tài khoản hoặc mật khẩu!</span>';
    }

    mysqli_stmt_close($stmt);
}

?>
<main>
    <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
        <form class="text-center col-lg-5 col-md-10" style="margin: auto;"
            method="post" action="">
            <h1 class="h3 mb-3 font-weight-normal">Đăng Nhập Tài Khoản</h1>
            <?= $thongbao; ?>
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username"
                type="text" class="form-control mt-1" placeholder="Tên tài khoản" autofocus="">
            <span style="color: red; font-size: 12px; font-weight: bold;">
            </span>
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password"
                type="password" class="form-control mt-1" placeholder="Mật khẩu">
            <span style="color: red; font-size: 12px; font-weight: bold;">
            </span>

            <span style="color: red; font-size: 12px; font-weight: bold;">
            </span>
            <div class="text-center">
                <button class="btn btn-lg btn-dark btn-block" style="border-radius: 10px;width: 100%; height: 50px;"
                    type="submit" name="submit">Đăng nhập</button>
            </div>
        </form>

    </div>
</main>
</div>
<script>
    const redirectLink = 'http://localhost/dangnhap';
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