<?php
require_once('../core/config.php');
require_once('../core/head.php');

session_start();
if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

// Bảo mật session
session_regenerate_id(true);

$thongbao = null;
$username = $_SESSION['logger']['username'];
$gia_mtv = 10000; // Phí kích hoạt thành viên

// Lấy thông tin tài khoản
$sql_active = "SELECT active, vnd FROM account WHERE username = ?";
$stmt = mysqli_prepare($config, $sql_active);
mysqli_stmt_bind_param($stmt, "s", $username);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $active = $row['active'];
    $vnd = $row['vnd'];
} else {
    die("Tài khoản không tồn tại.");
}

if (isset($_POST['submit'])) {
    if ($active == 0) {
        if ($vnd >= $gia_mtv) {
            // Kích hoạt thành viên
            $sql_update = "UPDATE account SET active = 1, vnd = vnd - ? WHERE username = ?";
            $stmt_update = mysqli_prepare($config, $sql_update);
            mysqli_stmt_bind_param($stmt_update, "is", $gia_mtv, $username);
            if (mysqli_stmt_execute($stmt_update)) {
                $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Bạn đã kích hoạt thành công!</span>';
            } else {
                $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Xảy ra lỗi! Vui lòng thử lại.</span>';
            }
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Bạn không đủ số dư. Vui lòng nạp thêm!</span>';
        }
    } else {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Bạn đã kích hoạt thành viên rồi!</span>';
    }
}

?>
<main>
    <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
        <div class="text-center col-lg-5 col-md-10" style="margin: auto;">
            <small>Trạng thái: 
                <?php 
                echo $active == 0
                    ? '<b style="color: red">Tài khoản chưa kích hoạt!</b>'
                    : '<b style="color: green">Tài khoản đã được kích hoạt!</b>';
                ?>
            </small>
            <p><b style="color: black; font-size: 15px;">Phí kích hoạt thành viên là: <b style="color: black; font-size: 19px;">10.000đ</b></b></p>
            <?=$thongbao;?>
            <form method="POST" action="">
                <div class="text-center mt-1">
                    <input class="btn btn-lg btn-dark btn-block" style="border-radius: 10px; width: 100%; height: 50px;" type="submit" name="submit" value="Bấm để kích hoạt thành viên" />
                </div>
            </form>
        </div>
    </div>
</main>
<style>
    /* Bạn có thể thêm các style tại đây */
</style>
<?php require_once('../core/end.php'); ?>
