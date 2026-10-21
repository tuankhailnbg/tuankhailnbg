<?php
require_once('../core/config.php');
require_once('../core/head.php');

$thongbao = null;
session_start();

if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

// Kiểm tra nếu form được submit và các trường không trống
if (isset($_POST['submit']) && !empty($_POST['username']) && !empty($_POST['password']) && !empty($_POST['newpassword'])) {
    $username = $_POST['username'];
    $password = $_POST['password'];
    $newpassword = $_POST['newpassword'];
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
            // Update password
            $sqlUpdate = "UPDATE account SET password = ? WHERE username = ?";
            $stmtUpdate = mysqli_prepare($config, $sqlUpdate);
            mysqli_stmt_bind_param($stmtUpdate, "ss", $newpassword, $username);
            mysqli_stmt_execute($stmtUpdate);
            mysqli_stmt_close($stmtUpdate);

            // Hiển thị thông báo thành công và ngừng tải lại trang
            $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đã đổi mật khẩu, hãy thoát ra và đăng nhập lại!</span>';
            // Sau khi thành công, không cần tải lại trang, chỉ cần hiển thị thông báo
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Mật khẩu cũ không đúng!</span>';
        }
    } else {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Không tìm thấy tài khoản!</span>';
    }

    mysqli_stmt_close($stmt);
}

?>
<main>
    <!-- header -->
    <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
        <form class="text-center col-lg-5 col-md-10" style="margin: auto;" method="post" action="">
            <h1 class="h3 mb-3 font-weight-normal">Đổi Mật Khẩu</h1>
            <?= $thongbao; ?>
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username" required="" autofocus=""
                type="text" class="form-control mt-1" placeholder="Tên tài khoản">
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password" required=""
                type="password" class="form-control mt-1" placeholder="Mật khẩu cũ">
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="newpassword" required=""
                type="password" class="form-control mt-1" placeholder="Mật khẩu mới">
            <div class="text-center mt-1">
                <button class="btn btn-lg btn-dark btn-block" style="border-radius: 10px;width: 100%; height: 50px;" type="submit" name="submit">Thực Hiện</button>
            </div>
        </form>
    </div>
</main>

<style>
    /* Custom styles */
    /* Additional CSS can go here */
</style>

<?php require_once('../core/end.php'); ?>