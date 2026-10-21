<?php
include_once 'cauhinh.php';
$config = mysqli_connect($db_host, $db_user, $db_pass, $db_name);
if (!$config) {
	die ("KHONG THE KET NOI DEN CSDL ! VUI LONG KIEM TRA LAI");
} else {
	mysqli_set_charset($config, 'utf8');
}
function _query($sql)
{
	global $config;
	return mysqli_query($config, $sql);
}
function _fetch($sql)
{
	return mysqli_fetch_array(_query($sql));
}
function isset_sql($txt)
{
	global $config;
	return mysqli_real_escape_string($config, $txt);
}
function _insert($table, $input, $output)
{
	return "INSERT INTO $table($input) VALUES($output)";
}
function _select($select, $from, $where)
{
	return "SELECT $select FROM $from WHERE $where";
}
function _update($tabname, $input_output, $where)
{
	return "UPDATE $tabname SET $input_output WHERE $where";
}

function checklogin()
{
	session_start();
	if ($_SESSION['logger']['username']) {
		return false;
	}
	return true;
}

$_succ = '<div class="success">';
$_err = '<div class="error">';
$_end = '</div>';
function _alert($txt)
{
	// GLOBAL $_succ,$_err,$_end;
	switch ($txt) {
		case 'succ':
			echo '
			<script type="text/javascript">
			
			$(document).ready(function(){
			
			  swal({
					title: "Nạp Thẻ Thành Công",
					text: "Vui lòng chờ hệ thống duyệt thẻ!",
					type: "error",
					confirmButtonText: "OK",
			  })
			});
			
			</script>
			';
			break;

		case 'err':
			echo '
			<script type="text/javascript">
			
			$(document).ready(function(){
			
			  swal({
					title: "Nạp Thẻ Thất bại",
					text: "Nạp sai thông tin hoặc thẻ đã tồn tại trên hệ thống!",
					type: "error",
					confirmButtonText: "OK",
			  })
			});
			
			</script>
			';
			break;
	}
}
function _console($txt)
{
	return "<script>console.log('" . htmlspecialchars($txt) . "')</script>";
}
function _randtxt()
{
	$string = "";
	$chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
	for ($i = 1; $i <= 9; $i++) {
		$position = rand() % strlen($chars);
		$string .= substr($chars, $position, 1);
	}
	return $string;
}

?>