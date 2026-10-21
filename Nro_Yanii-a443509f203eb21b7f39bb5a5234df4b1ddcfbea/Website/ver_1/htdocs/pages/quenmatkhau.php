<?php
require_once('../core/config.php');
require_once('../core/head.php');

$thongbao = null;
session_start();

if (isset($_SESSION['logger']['username'])) {
    echo '<script>window.location.href = "/";</script>';
    exit();
}

if (isset($_POST['submit'])) {
    $username = $_POST['username'];
    $gmail = $_POST['gmail'];
    $sql = "SELECT * FROM account WHERE username = ? AND gmail = ?";
    $stmt = mysqli_prepare($config, $sql);
    mysqli_stmt_bind_param($stmt, "ss", $username, $gmail);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    if ($result->num_rows > 0) {

        $newPassword = '123456';


        $hashedPassword = $newPassword;


        $updateSql = "UPDATE account SET password = ? WHERE username = ?";
        $updateStmt = mysqli_prepare($config, $updateSql);
        mysqli_stmt_bind_param($updateStmt, "ss", $hashedPassword, $username);
        mysqli_stmt_execute($updateStmt);

        if (mysqli_stmt_affected_rows($updateStmt) > 0) {
            $thongbao = '<div class="alert alert-success" role="alert">Mật khẩu mới của bạn là: ' . $newPassword . '</div>';
        } else {
            $thongbao = '<div class="alert alert-danger" role="alert">Đã xảy ra lỗi. Vui lòng thử lại sau.</div>';
        }

        mysqli_stmt_close($updateStmt);
    } else {
        $thongbao = '<div class="alert alert-danger" role="alert">Tên tài khoản hoặc địa chỉ email không đúng!</div>';
    }

    mysqli_stmt_close($stmt);
}
?>

<main>
    <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black; padding: 20px;" class="pb-1">
        <div class="col-md-12">
            <form id="form" method="POST" class="form-horizontal">
                <h3 class="mt-0 mb-20" style="text-align: center; color: #333; font-family: Arial, sans-serif; font-size: 24px; text-shadow: 1px 1px 2px rgba(0,0,0,0.5);">Lấy lại mật khẩu</h3>
                <?php echo $thongbao; ?>
                <div class="form-group">
                    <label for="username" class="col-sm-3 control-label">Tên tài khoản</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="username" name="username" placeholder="Tên tài khoản" style="height: 50px; border-radius: 15px; font-weight: bold;">
                    </div>
                </div>
                <div class="form-group">
                    <label for="gmail" class="col-sm-3 control-label">Địa chỉ email (Gmail)</label>
                    <div class="col-sm-9">
                        <input type="email" class="form-control" id="gmail" name="gmail" placeholder="Địa chỉ email" style="height: 50px; border-radius: 15px; font-weight: bold;">
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-3 col-sm-9">
                        <button class="btn btn-lg btn-dark btn-block" style="border-radius: 10px;width: 100%; height: 50px;" type="submit" name="submit">Xác nhận</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</main>
</div>
<script>
    const redirectLink = 'http://localhost/pages/quenmatkhau.php';
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