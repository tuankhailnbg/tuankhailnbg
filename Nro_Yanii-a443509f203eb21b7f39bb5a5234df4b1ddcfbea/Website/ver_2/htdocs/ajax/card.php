<?php
// Kiểm tra xem phiên làm việc đã được khởi động hay chưa
if (session_status() == PHP_SESSION_NONE) {
    // Nếu chưa khởi động, tiến hành khởi động phiên làm việc
    session_start();
}

include(__DIR__ ."/../api/config.php");
include_once('../set.php');

if (!isset($_POST['pin'], $_POST['serial'], $_POST['card_type'], $_POST['card_amount'])){
	exit('<script type="text/javascript">toastr.error("Vui Lòng Nhập Đầy Đủ Thông Tin !");</script>');
}

$content = md5(time() . rand(0, 999999).microtime(true));
$seri = $conn->real_escape_string(strip_tags(addslashes($_POST['serial']))); 
$pin = $conn->real_escape_string(strip_tags(addslashes($_POST['pin']))); 
$loaithe = $conn->real_escape_string(strip_tags(addslashes($_POST['card_type']))); 
$menhgia = $conn->real_escape_string(strip_tags(addslashes($_POST['card_amount']))); 
$username = $conn->real_escape_string(strip_tags(addslashes($_username))); // sử dụng biến phiên
$url = "https://thesieutoc.net/chargingws/v2?APIkey=".$apikey."&mathe=".$pin."&seri=".$seri."&type=".$loaithe."&menhgia=".$menhgia."&content=".$content;
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch,CURLOPT_CAINFO, __DIR__ .'/../api/curl-ca-bundle.crt');
curl_setopt($ch,CURLOPT_CAPATH, __DIR__ .'/../api/curl-ca-bundle.crt');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$out = json_decode(curl_exec($ch));
$http_code = 0;
if (isset($out->status)){$http_code = 200;}
curl_close($ch);

if ($http_code == 200){                          
	if ($out->status == '00' || $out->status == 'thanhcong'){
		$conn->query("Insert into trans_log (name,trans_id,amount,pin,seri,type) values ('".$username."','".$content."',".$menhgia.",'".$pin."','".$seri."','".$loaithe."')");
		echo '<script type="text/javascript">swal("Thành Công", "'.$out->msg.'", "success");</script>';							
	} else if ($out->status != '00' && $out->status != 'thanhcong'){
		echo '<script type="text/javascript">toastr.error("'.$out->msg.'");</script>';
	}
} else {
	echo '<script type="text/javascript">toastr.error("Có lỗi máy chủ vui lòng thử lại sau!");</script>';
}
?>
