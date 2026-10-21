<?php

function newgift($id)
{
require_once ('core/config.php');
include ('set.php');

if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

unset($_SESSION["errors"]);
$_username = $_SESSION['logger']['username'];
$sqlk = "SELECT id FROM account WHERE username = '$_username'";
$resultk = $config->query($sqlk);

$queryt = "SELECT p.name, p.id
    FROM player p
    LEFT JOIN account a ON p.account_id = a.id
    WHERE a.username = '$_username'";

$resultt = $config->query($queryt);
if ($resultt->num_rows > 0) {
    $rowt = $resultt->fetch_assoc();
    $player_id = $rowt['id'];
  } else {
    echo '<p style="text-align: center; font-size: 20px; color: green; font-weight: bold;">Bạn chưa tạo nhân vật</p>';
    exit;
  }

if ($resultk->num_rows > 0) {
    // Lấy id từ kết quả truy vấn
    $rowk = $resultk->fetch_assoc();
    $accountId = $rowk["id"];
}

session_start();
function generateRandomString($length)
{
    $characters = 'abcdefghijklmnopqrstuvwxyz0123456789';
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, strlen($characters) - 1)];
    }
    return $randomString;
}
if ($id == 1){
    $gift = '[{"quantity":999,"options":[{"param":1,"id":30}],"id":1215},{"quantity":10,"options":[{"param":1,"id":73}],"id":579},{"quantity":5,"options":[{"param":1,"id":73}],"id":899},{"quantity":1,"options":[{"param":2000,"id":0},{"param":30,"id":50},{"param":500,"id":101},{"param":500,"id":100},{"param":1,"id":30}],"id":1087}]';
} else {
    $gift = '[{"quantity":1000,"options":[{"param":1,"id":73}],"id":457},{"quantity":10,"options":[{"param":1,"id":30}],"id":1460},{"quantity":20,"options":[{"param":1,"id":73}],"id":1479},{"quantity":5,"options":[{"param":1,"id":73}],"id":1015},{"quantity":1,"options":[{"param":100,"id":50},{"param":1,"id":74},{"param":3,"id":93}],"id":823}]';
}

$exists = true;

    // Tạo chuỗi ngẫu nhiên
    $randomString = generateRandomString(10);
    while ($exists) {
        $sql = "SELECT * FROM member_gift WHERE coded = '$randomString'";
        $result = $config->query($sql);

        if ($result->num_rows > 0) {
            // Nếu chuỗi đã tồn tại, tạo chuỗi ngẫu nhiên mới
            $randomString = generateRandomString(10);
        } else {
            $sqll = "INSERT INTO `member_gift` (`player_idd`, `typed`, `coded`, `itemsd`, `statusd`) 
                VALUES ('$player_id', 1, '$randomString', '$gift', '0')";
            $result11 = mysqli_query($config, $sqll);
            
            $exists = false;
        }
    }
}

?>