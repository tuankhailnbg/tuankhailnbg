<?php
require_once('../core/config.php');
require_once('../core/head.php');
$thongbao = null;
session_start();
if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}
$sql_admin = "SELECT is_admin FROM account WHERE username = '$username'";
$result = $config->query($sql_admin);

if ($result && $result->num_rows > 0) {
    $row_admin = $result->fetch_assoc();
}
$sql_active = "SELECT active FROM account WHERE username = '$username'";
$result = $config->query($sql_active);

if ($result && $result->num_rows > 0) {
    $row_active = $result->fetch_assoc();
}
$sql = "SELECT id FROM account WHERE username = '$username'";
$result = $config->query($sql);

if ($result->num_rows > 0) {
    // Lấy id từ kết quả truy vấn
    $row_hvd = $result->fetch_assoc();
    $accountId = $row_hvd["id"];
    // Kiểm tra sự tồn tại của account_id trong player
    $sql_check_player = "SELECT COUNT(*) as player_count FROM player WHERE account_id = '$accountId'";
    $result_check_player = $config->query($sql_check_player);

    if ($result_check_player && $result_check_player->num_rows > 0) {
        $row_check_player = $result_check_player->fetch_assoc();
        $player_exists = $row_check_player['player_count'] > 0;
    } else {
        $player_exists = false;
    }


    // Truy vấn để lấy giá trị giới tính từ bảng Player
    $sql = "SELECT gender FROM player WHERE id = $accountId";
    $result = $config->query($sql);

    if ($result->num_rows > 0) {
        // In ra giá trị giới tính
        $row_hvd = $result->fetch_assoc();
    }
}
if (isset($_POST['submit']) && isset($_POST['tieude']) && isset($_POST['noidung'])) {
    $tieude = $_POST['tieude'];
    $noidung = $_POST['noidung'];
    $account_id = $accountId; // Lấy id của người dùng từ câu truy vấn ban đầu
    // Kiểm tra nội dung bình luận không được bỏ trống
    if (empty($noidung)) {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Vui lòng nhập nội dung bài đăng!</span>';
    } else {
        if ($row_admin['is_admin'] == 1) {
            $new = $_POST['new'];
            $top_baiviet = $_POST['top_baiviet'];

            // Thực hiện truy vấn để lưu bình luận vào cơ sở dữ liệu
            $sql = "INSERT INTO baiviet_hoangvietdung (account_id, top_baiviet, new, tieude, noidung, time) 
                VALUES ('$account_id', '$top_baiviet', '$new', '$tieude', '$noidung', " . time() . ")";
        } else {
            $sql = "INSERT INTO baiviet_hoangvietdung (account_id, top_baiviet, new, tieude, noidung, time) 
                VALUES ('$account_id', 0, 0, '$tieude', '$noidung', " . time() . ")";
        }
        $result = $config->query($sql);
        $baiviet_id_new = $config->insert_id;
        // Thực hiện kiểm tra và thông báo kết quả lưu bình luận
        if ($result) {
            $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đăng bài thành công!</span>';
            echo '<script>window.location.href = "/forum?id=' . $baiviet_id_new . '";</script>';
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Đã xảy ra lỗi!</span>';
        }

        // Kiểm tra sự tồn tại của account_id trong player
        if (!$player_exists) {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Hãy tạo nhân vật trước khi đăng bài!</span>';
        }
    }
}
?>
<div class="p-1 mt-1 alert alert-info" style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 0px 5px black;">
    <div class="alert alert-danger" style="background: #ffe8d1; border-radius: 7px;">
        <center><?= $thongbao; ?></center>
        <form method="POST" action="">
            <b>Tiêu đề</b>
            <input type="text" class=" form-control" style="border-radius: 7px;" placeholder="Tiêu đề (không quá 75 ký tự)" required="" autofocus="" name="tieude">
            <br>
            <b>Nội dung</b>
            <textarea class="form-control" style="border-radius: 7px;" name="noidung" id="" cols="30" rows="10" placeholder="Nội dung (không được quá 256 ký tự)"></textarea>
            <?php if ($row_admin['is_admin'] == 1) { ?>
                <br>
                <h6>*Chức năng dành cho <i><u>Admin</u></i></h6>
                <hr>
                <b>Top bài viết</b>
                <select class="form-control" style="border-radius: 7px;" name="top_baiviet">
                    <option value="0">Không</option>
                    <option value="1">Có</option>
                </select>
                <br>
                <b>Hiện NEW <small>(chức năng hiện icon new ở top!)</small></b>
                <select class="form-control" style="border-radius: 7px;" name="new">
                    <option value="0">Không</option>
                    <option value="1">Có</option>
                </select>
            <?php } ?><br>
            <?php if ($row_active['active'] == 1 && $player_exists) { ?>
                <button class="btn btn-action text-white m-1" name="submit" type="submit" style="border-radius: 7px;">Đăng bài</button>
            <?php } else { ?>
                <span style="color: red; font-size: 12px; font-weight: bold;"><b><i>Hãy tạo nhân vật hoặc kích hoạt trước khi <u>đăng bài</u>!</i></b></span>
            <?php } ?>
        </form>
    </div>
</div>
<?php require_once('../core/end.php'); ?>