<?php
require_once('../core/config.php');
require_once('../core/head.php');

$thongbao = null;
session_start();

if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

// Kiểm tra nếu form được submit và các trường không trống
if (isset($_POST['submit'])) {
    $username = $_POST['username'];
    $password = $_POST['password'];
    $newgmail = $_POST['gmail'];
    // Kiểm tra định dạng email
    if (!filter_var($newgmail, FILTER_VALIDATE_EMAIL)) {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Email không hợp lệ. Vui lòng nhập lại!</span>';
    } else {
        // Kiểm tra tài khoản và mật khẩu
        $sql = "SELECT * FROM account WHERE username = ?";
        $stmt = mysqli_prepare($config, $sql);
        mysqli_stmt_bind_param($stmt, "s", $username);
        mysqli_stmt_execute($stmt);
        $result = mysqli_stmt_get_result($stmt);

        if ($result->num_rows > 0) {
            $row = mysqli_fetch_assoc($result);
            $storedPassword = $row['password'];

            if ($password == $storedPassword) {
                // Cập nhật email
                $sqlUpdate = "UPDATE account SET gmail = ? WHERE username = ?";
                $stmtUpdate = mysqli_prepare($config, $sqlUpdate);
                mysqli_stmt_bind_param($stmtUpdate, "ss", $newgmail, $username);
                mysqli_stmt_execute($stmtUpdate);
                mysqli_stmt_close($stmtUpdate);

                $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đã cập nhật email. Vui lòng đăng nhập lại!</span>';
            } else {
                $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Mật khẩu không đúng!</span>';
            }
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Không tìm thấy tài khoản!</span>';
        }

        mysqli_stmt_close($stmt);
    }
}
?>
<main>
    <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
        <form class="text-center col-lg-5 col-md-10" style="margin: auto;" method="post" action="">
            <h1 class="h3 mb-3 font-weight-normal">Cập Nhật Email</h1>
            <?= $thongbao; ?>
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username" required="" autofocus="" type="text" class="form-control mt-1" placeholder="Tên tài khoản">
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password" required="" type="password" class="form-control mt-1" placeholder="Mật khẩu">
            <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="gmail" required="" type="text" class="form-control mt-1" placeholder="Email Mới">
            <div class="text-center mt-1">
                <button class="btn btn-lg btn-dark btn-block" style="border-radius: 10px;width: 100%; height: 50px;" type="submit" name="submit">Thực Hiện</button>
            </div>
        </form>
    </div>
</main>
<?php require_once('../core/end.php'); ?>