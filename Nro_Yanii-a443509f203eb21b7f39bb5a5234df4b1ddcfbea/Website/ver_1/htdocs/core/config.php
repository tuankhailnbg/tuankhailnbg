<?php
global $config;
$serverName = "localhost";
$userName = "root";
$password = "";
$dbName = "ngocrong";

# Cấu hình
$tieude = "Ngọc Rồng Yanii - ";
$server_name = "Ngọc Rồng Yanii";
$link_web = "http:\\nrotuonglai.online";//dùng làm tên miền cũng đc=))
$logo = "../hit/images/logo/logo.gif?w=100%&h=100%";
$gia_mtv = 10000; //giá sẽ trừ vào thỏi vàng để mtv
# Đường dẫn tải phiên bản và box zalo
$java = "/hit/dow/NRO.jar";
$pc = "/hit/dow/NRO.rar";
$adr = "/hit/dow/NRO.apk";
$ios = "/hit/dow/NRO.ipa";
$box_zalo = "";
$fanpage = "";

#DoiThe1s.VN API (Nếu có bán lại thì xoá key đi tránh lộ!)
$partner_id_config = '4509875'; // TẠO Ở AZCARD
$partner_key_config = 'nhập key';  // TẠO Ở AZCARD
#Config API MBBank | STK
$taikhoanmb_config = ''; 
$deviceIdCommon_config = ''; 
$sessionId_config = ''; 
$sotaikhoanmb_config = '';
$chutaikhoan = "";
$urlQRmb_config = "";
$urllogonganhang_config = "https://i.imgur.com/L589m0T.jpg";
# Danh sách từ cấm
$censoredWords = array('sex', 'fuck', 'xxx', '.com', '.net', '.online', 'lồn', 'cặc', 'cc', 'seg', 'duma', 'đụ', 'chịch', 'má', 'địt', 'mẹ', 'cl', 'lm', 'mm'); 

$config = mysqli_connect($serverName, $userName, $password, $dbName);

if (mysqli_connect_errno()) {
    echo "Sai hoặc Chưa kết nối Database!";
    exit();
}


function duxng_time($timestamp) {
    $currentTime = time();
    $diffInSeconds = $currentTime - $timestamp;

    $MINUTE = 60;
    $HOUR = 60 * $MINUTE;
    $DAY = 24 * $HOUR;
    $WEEK = 7 * $DAY;
    $MONTH = 30 * $DAY;
    $YEAR = 365 * $DAY;

    if ($diffInSeconds < $MINUTE) {
        return 'Vừa mới đây';
    } elseif ($diffInSeconds < $HOUR) {
        $minutesAgo = floor($diffInSeconds / $MINUTE);
        return $minutesAgo . ' phút trước';
    } elseif ($diffInSeconds < $DAY) {
        $hoursAgo = floor($diffInSeconds / $HOUR);
        return $hoursAgo . ' giờ trước';
    } elseif ($diffInSeconds < $WEEK) {
        $daysAgo = floor($diffInSeconds / $DAY);
        return $daysAgo . ' ngày trước';
    } elseif ($diffInSeconds < $MONTH) {
        $weeksAgo = floor($diffInSeconds / $WEEK);
        return $weeksAgo . ' tuần trước';
    } elseif ($diffInSeconds < $YEAR) {
        $monthsAgo = floor($diffInSeconds / $MONTH);
        return $monthsAgo . ' tháng trước';
    } else {
        $yearsAgo = floor($diffInSeconds / $YEAR);
        return $yearsAgo . ' năm trước';
    }
}


?>
