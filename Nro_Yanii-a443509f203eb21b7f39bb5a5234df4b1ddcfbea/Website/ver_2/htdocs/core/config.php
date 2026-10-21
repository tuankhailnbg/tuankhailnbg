<?php
// Dev By Huỳnh Cường

#config-------------------------------------------------------------
global $config;
$serverName = "localhost";
$userName = "root";
$password = "";
$dbName = "ngocrong";


#tên server---------------------------------------------------------
$tieude = "Trang Chủ - Học Viện Nro";
$ten_server = "Học Viện Nro";
$link_web = "";
//Được vận hành bởi:
$doingu = "Admin: NRO";

#Link boxZalo-------------------------------------------------------
$linkbox = "https://zalo.me/g/ryipvr696";

#Link fanpage-------------------------------------------------------
$fanpage = "link fanpage ";

#logo---------------------------------------------------------------
//head
$logohead = "../public/images/logo/logowebgif.gif";
//end
$logoend = "../public/images/logo/logowebgif.gif";
//icon-web
$iconweb = "../public/images/logo/icon.png";
	
	
#Mở thành viên------------------------------------------------------
$gia_mtv = 20000; //giá sẽ trừ vào số dư để mtv

#Nhận giftcode------------------------------------------------------
$gia_nhan_gift = 500000;

#Màu nền------------------------------------------------------------
$maunen ="
	z-index: 1;  
	position: relative;
	border-radius: 7px; 
	border: 2px solid #E75F47; 
	box-shadow: 0 0 15px #E75F47; 
	background-color: #ffb900;
	padding: 10px;
";

#Màu thanh công cụ ( -1- )------------------------------------------

$maunavbar1 ="
	padding: 0.5rem 1rem; 
	text-align: center; 
	box-shadow: 0 0 7px #E75F47; 
	border-color: #E75F47; 
	text-decoration: none; 
	background-color: #000; 
	color: #fff; font-size: 0.9rem; 
	white-space: nowrap;
";

#Nút Download File---------------------------------------------------
$nutdownloadfile = "    
	.btn-download {
		background-color: #E75F47;
		border-color: black;
		text-shadow: 1px 1px 0 black;
	}
	.btn-download:hover,
	.btn-download:focus {
		background-color: #b8f9f1;
		border-color: 1px solid black;
		font-weight: bold;
	}
";

#Nút đăng nhập đăng kí index------------------------------------------
$nutdangnhapdangki = "   
	.btn-action {
		background-color: #f4a058;
		border-color: black;
		font-weight: bold;
	}

	.btn-action:hover,
	.btn-action:focus {
		background-color: #ffca85 ;
		border-color: black;  
		font-weight: bold;
	}
";

#Nút đăng kí TO-------------------------------------------------------
$nutdangnhapdangkiTO = "
	font-weight: bold; 
	background-color: #E75F47; 
	border-radius: 10px; 
	width: 100%; 
	height: 50px;  
	white-space: nowrap; 
	overflow: hidden;
";

$onmouseover = "
	this.style.backgroundColor='#b8f9f1'
";
#---> ( Chỉ thay cái này )
$onmouseout = "
	this.style.backgroundColor='#E75F47'
";
#Link download + box chat---------------------------------------------
$jar = "nhap link jar";
$pc = "link pc";
$adr = "link apk";
$ios = "link ios";
$adr2 = "link apk2";

#Captcha Google - config----------------------------------------------
$site_key = "6LcZMLAqAAAAAElhnnOdA2tHWlUwaFXsSnHOe4Ty";
$select_key = "6LcZMLAqAAAAAFMfqD9huvKsgEGVILp9q-VatUDV";
$config = mysqli_connect($serverName, $userName, $password, $dbName);
if (mysqli_connect_errno()) {
    echo "Sai hoặc Chưa kết nối Database!";
    exit();
}
?>