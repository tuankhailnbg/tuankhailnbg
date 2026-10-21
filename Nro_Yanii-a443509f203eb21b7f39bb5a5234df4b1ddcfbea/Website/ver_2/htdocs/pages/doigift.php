<?php
require_once ('../core/config.php');
require_once ('../core/head.php');
require_once ('../giftcode_mtv.php');
$thongbao = null;
session_start();
if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

// Lấy username từ session
$username = $_SESSION['logger']['username'];

$sql_active = "SELECT active FROM account WHERE username = '$username'";
$result = $config->query($sql_active);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $active = $row["active"];
}
$sql = "SELECT id FROM account WHERE username = '$username'";
$result = $config->query($sql);

if ($result->num_rows > 0) {
    // Lấy id từ kết quả truy vấn
    $row = $result->fetch_assoc();
    $accountId = $row["id"];

    // Truy vấn để lấy giá trị giới tính từ bảng Player
    $sql = "SELECT vnd FROM account WHERE id = $accountId";
    $result = $config->query($sql);

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $vnd = $row['vnd'];
    }
}

$querytt = "SELECT p.name, p.id
    FROM player p
    LEFT JOIN account a ON p.account_id = a.id
    WHERE a.username = '$_username'";

$resultt = $config->query($querytt);
if ($resultt->num_rows > 0) {
    $rowt = $resultt->fetch_assoc();
    $player_id = $rowt['id'];
} else {
    echo '<p style="text-align: center; font-size: 20px; color: green; font-weight: bold;">Bạn chưa tạo nhân vật</p>';
    exit;
}

if (isset($_POST['submit'])) {
    if ($vnd >= $gia_nhan_gift) {
        $sql = "UPDATE account SET account.vnd = account.vnd - $gia_nhan_gift WHERE account.username = '$username'";
        $result = $config->query($sql);
        if ($result === TRUE) {
            newgift(2);
            $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đã nhận 1 Giftcode. Vui lòng vào mục Giftcode Riêng để xem!</span>';
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Xảy ra lỗi!</span>';
        }
    } else {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Không đủ tiền, vui lòng nạp!</span>';
    }
}


?>


<main>
    <div style="background: #ffe9b8; font-size: 15px; border-radius: 7px; box-shadow: 0px 2px 5px black;padding: 20px;"
        class="pb-1">
        <div class="text-center col-lg-5 col-md-10" style="margin: auto;">
            <?php
            echo '<b style="color: green">Bạn muốn dùng: </b>';
            echo '<b style="color: black">500.000 VNĐ </b>';
            echo '<b style="color: green">để nhận 1 Giftcode cá nhận không?</b><br>';
            ?> <br>
            <small style="color: red; font-weight: bold; font-size: 15px;">Phần quà Giftcode: <br>
            </small>
            <?php
            echo '<b style="color: blue; font-size: 13px">-1.000 Thỏi vàng</b> <br>';
            echo '<b style="color: blue; font-size: 13px">-10 Hộp đồ Huỷ diệt random</b> <br>';
            echo '<b style="color: blue; font-size: 13px">-20 Rương Sao pha lê VIP</b> <br>';
            echo '<b style="color: blue; font-size: 13px">-5 Ngọc rồng siêu cấp</b> <br>';
            echo '<b style="color: blue; font-size: 13px">-1 Túi quà đeo lưng 3 Ngày (Dành cho sự kiện)</b>';
            ?>
            <br>
            <?= $thongbao; ?>
            <form method="POST" action="">
                <div class="text-center mt-1">
                    <input class="btn btn-lg btn-dark btn-block"
                        style="border-radius: 10px;width: 100%; font-size: 15px; height: 50px;" type="submit"
                        name="submit" value="Bấm để đổi" />
                </div>
            </form>
        </div>
    </div>
</main>


<?php require_once ('../core/end.php'); ?>