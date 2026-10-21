<?php
include ('set.php');

$stk = '123456';
$tennh = 'Ngân hàng Quân Đội (MB Bank)';
$chutaikhoan = 'abcd';
$password = '';
$token = 'abcd';
$data = curl_get_contents("https://api.web2m.com/historyapimb/$password/$stk/$token");

$MEMO_PREFIX = 'buffalo';