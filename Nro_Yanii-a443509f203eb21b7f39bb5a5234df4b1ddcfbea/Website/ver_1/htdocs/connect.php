<?php
$ip_sv = "localhost";
$dbname_sv = "nro";
$user_sv = "root";
$pass_sv = "";

//GMT +7
date_default_timezone_set('Asia/Ho_Chi_Minh');

// Create connection
$conn = new mysqli($ip_sv, $user_sv, $pass_sv, $dbname_sv);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>