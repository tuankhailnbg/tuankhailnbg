<?php
$config = mysqli_connect("localhost", "root", "", "1234");
if ($config->connect_error) {
    die("Connection failed: " . $config->connect_error);
}
ob_clean(); // clear output buffer
//header("Location: ../nap-mbbank"); // redirect to "nap-mbbank.php"
error_reporting(0);
echo "cron atm";
function parse_order_id($des, $MEMO_PREFIX)
{
    $re = '/'.$MEMO_PREFIX.'\d+/im';
    preg_match_all($re, $des, $matches, PREG_SET_ORDER, 0);
    if (count($matches) == 0) {
        return null;
    }
    // Print the entire match result
    $orderCode = $matches[0][0];
    $prefixLength = strlen($MEMO_PREFIX);
    $orderId = intval(substr($orderCode, $prefixLength));
    return $orderId ;
}
// Lấy dữ liệu từ API Mbbank
$url = "https://api.sieuthicode.net/historyapimbbank/b2bc18fef5dc2372389ed2e460ec11ef";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$data = curl_exec($ch);
curl_close($ch);

$response = json_decode($data, true);
$tranList = $response['TranList'];
$count = count($tranList);

for ($x = 0; $x < $count; $x++) {
    $tranId = $tranList[$x]['refNo'];
    $io = $tranList[$x]['availableBalance'];
    $amount = $tranList[$x]['creditAmount'];
    $description = $tranList[$x]['description'];
 //   $user_id = isset(explode(' ', $description)[1]) ? explode(' ', $description)[1] : "-"; 
    $user_id        = parse_order_id($description,'Kid');
    $user_id = strtolower($user_id);
    $checkQuery = "SELECT * FROM `mbbank` WHERE `tranId`='$tranId'";
    $checkResult = mysqli_query($config, $checkQuery);
    print_r($amount);
    echo "--";
     print_r($user_id);
     echo "</br>";
      $soxudc =  $amount *1.2;
    if (mysqli_num_rows($checkResult) > 0) {
        echo"Không có giao dịch mới ";
        return;;
    } else {
         echo"Xử lý thành công 1 đơn hàng</br>";
        $updateQuery = "UPDATE `account` SET vnd = vnd + $soxudc, tongnap = tongnap + $amount, tichnap = tichnap + $amount  WHERE id = '$user_id'";
        mysqli_query($config, $updateQuery);

        $insertQuery = "INSERT INTO `mbbank` (`tranId`, `sodu`, `amount`, `comment`)
                        VALUES ('$tranId', '$io', '$amount', '$user_id')";
        mysqli_query($config, $insertQuery);
        
        
    }
}
mysqli_close($config);
?>

<?php
exit(); // exit script
?>