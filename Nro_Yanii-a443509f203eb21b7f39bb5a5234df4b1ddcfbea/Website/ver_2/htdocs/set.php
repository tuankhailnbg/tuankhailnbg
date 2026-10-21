<?php
// Kiểm tra xem phiên làm việc đã được khởi động hay chưa
if (session_status() == PHP_SESSION_NONE) {
    // Nếu chưa khởi động, tiến hành khởi động phiên làm việc
    session_start();
}

include_once 'cauhinh.php';

date_default_timezone_set('Asia/Ho_Chi_Minh');
$_login = isset($_login) ? $_login : null;
include_once 'config.php';
$_character = isset($_SESSION['player']) ? $_SESSION['player'] : null;
if($_character != null)
{
	$_login = "on";
	$character_arr = _fetch("SELECT * FROM player WHERE username='$_character'");
	if(count($character_arr) <= 0){header("location:/?out");} 
	$_tcoin = htmlspecialchars($_character['vnd']);
}
$_player = isset($_SESSION['player']) ? $_SESSION['player'] : null;
if($_player != null){
	$_login = "on";
	$_tcoin = htmlspecialchars($_player['vnd']);
} else
{
	$_login = null;
}
$_user = isset($_SESSION['account']) ? $_SESSION['account'] : null;
if($_user != null)
{
	$_login = "on";
	$user_arr = _fetch("SELECT * FROM account Where username='$_user'");
	if(count($user_arr) <= 0){header("location:/?out");}
	$_id = $user_arr['id'];
	$_username = htmlspecialchars($user_arr['username']);
	$_password = htmlspecialchars($user_arr['password']); 
	$_coin = $user_arr['vnd'];
	$_ip = htmlspecialchars($user_arr['ip_address']);
	$_vnd_reward = $user_arr['vnd'];
	
	$_status = $user_arr['active'];
	switch ($_status) {
		case '-1':
			$_status_name = '<span style="color:green;font-weight: bold;">Đã kích hoạt';
			break;
		case '0':
			$_status_name = '<span style="color:#007BFF;font-weight: bold;"><a href="/active.php">Kích hoạt ngay</a>';
			break;
		case '1':
			$_status_name = '<span style="color:red;font-weight: bold;">Đang bị khóa';
			break;
	}
}
else
{
	$_login = null;
}
if (isset($_GET['out']))
{
	session_destroy();
	header("location:/");
}
?>
