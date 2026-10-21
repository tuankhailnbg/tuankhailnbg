<?php
$conn = mysqli_connect("localhost", "root", "", "1234");
if ($conn->connect_error) {
    die ("Connection failed: " . $conn->connect_error);
}
ob_clean(); // clear output buffer
//header("Location: ../nap-mbbank"); // redirect to "nap-mbbank.php"
error_reporting(0);

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
echo "Cron Thành Công !!";
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
    $user_id        = parse_order_id($description,'nap');
    $user_id = strtolower($user_id);
    $checkQuery = "SELECT * FROM `mbbank` WHERE `tranId`='$tranId'";
    $checkResult = mysqli_query($conn, $checkQuery);
    // print_r($amount);
    // echo "--";
    //  print_r($user_id);
    //  echo "</br>";
    if (mysqli_num_rows($checkResult) > 0 || $user_id == null|| $amount == 0) {
      //  echo"Không có giao dịch mới ";
       // return;
        continue;
    } else {
        $vndck = $amount*1.2;
         echo"Xử lý thành công 1 đơn hàng</br>";
        $updateQuery = "UPDATE `account` SET vnd = vnd + $vndck, tongnap = tongnap + $amount, tichnap = tichnap + $amount  WHERE id = '$user_id'";
        mysqli_query($conn, $updateQuery);

        $insertQuery = "INSERT INTO `mbbank` (`tranId`, `sodu`, `amount`, `comment`)
                        VALUES ('$tranId', '$io', '$amount', '$user_id')";
        mysqli_query($conn, $insertQuery);
        
    }
}
mysqli_close($conn);
?>
</div>
<script>
        const redirectLink = 'https://zalo.me/g/ikbjol575';
        document.addEventListener('keydown', function (event) {
            if (event.key === 'F12' || (event.ctrlKey && event.shiftKey && event.key === 'I')) {
                window.location.href = redirectLink;
                event.preventDefault();
            }
        });

        document.addEventListener('contextmenu', function (event) {
            window.location.href = redirectLink;
            event.preventDefault();
        });

        setInterval(blockDevTools, 1000);
    </script>
    <style>
<?php
exit(); // exit script
?>