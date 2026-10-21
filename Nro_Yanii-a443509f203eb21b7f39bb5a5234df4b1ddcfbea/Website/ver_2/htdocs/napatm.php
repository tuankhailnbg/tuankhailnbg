<?php
require_once ('core/config.php');
require_once ('core/head.php');
require_once ('core/khaibaobankmb.php');
include ('set.php');

if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}


unset($_SESSION["errors"]);
$_username = $_SESSION['logger']['username'];
$sqlk = "SELECT id FROM account WHERE username = '$_username'";
$resultk = $config->query($sqlk);

if ($resultk->num_rows > 0) {
    // Lấy id từ kết quả truy vấn
    $rowk = $resultk->fetch_assoc();
    $accountId = $rowk["id"];
}
?>
<style>
    .container {
        border: 1px solid black;
        background-color: white;
        padding: 20px;
        text-align: center;
    }

    .textinfo {
        border: 1px solid black;
        background-color: antiquewhite;
        padding: 10px;
        text-align: center;
        font-weight: bold;
    }

    .label {
        font-weight: bold;
        margin-bottom: 0px;
    }

    .value {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 10px;
    }

    .value span {
        margin-right: 15px;
    }

    .red-text {
        color: red;
    }
</style>

<div class="p-1 mt-1 ibox-content"
    style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black; margin-bottom:10px;">
    <div class="p-1 text-white">
        <?php if ($_SESSION['logger']['username']) { ?>
            <div class="align-items-center my-2 d-flex justify-content-between">
                <a href="/napthe.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Donate thẻ cào</a>
                <a href="/napatm.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Donate ATM</a>
                <a href="<?php echo $fanpage; ?>" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Fanpage</a>
            </div>
        <?php } else { ?>
            <div class="align-items-center my-2 d-flex justify-content-between">
                <a href="/pages/dangnhap.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Donate thẻ cào</a>
                <a href="/pages/dangnhap.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Donate ATM</a>
                <a href="<?php echo $fanpage; ?>" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Fanpage</a>
            </div>
        <?php } ?>
    </div>
</div>

<script type="text/javascript"> new WOW().init(); </script>
<div class="p-1 mt-1  "
    style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">
    <div class="atm-body">
        <form method="POST" action="#" id="myform">
            <center>
                <h1 class="h3 mb-3 font-weight-normal text-white"
                    style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Thông tin Chuyển khoản
                </h1>
            </center>
            <div class="container">
				
                <div class="value">
                    <span class="label">- Nội dung: </span>
                    <span id="noidung">
                        <strong class="red-text">
                            <?php echo $MEMO_PREFIX . $accountId; ?>
                        </strong>
                    </span>
                </div>

                <div class="value">
                    <span class="label">- Số tài khoản: </span>
                    <span id="stk">
                        <?php echo $stk; ?>
                    </span>
                </div>

                <div class="value">
                    <span class="label">- Ngân hàng: </span>
                    <span id="tennh">
                        <?php echo $tennh; ?>
                    </span>
                </div>

                <div class="value">
                    <span class="label">- Chủ tài khoản: </span>
                    <span id="chutaikhoan">
                        <?php echo $chutaikhoan; ?>
                    </span>
                </div>

                <img src="https://img.vietqr.io/image/M?amount=<AMOUNT>&addInfo=<?php echo $MEMO_PREFIX . $accountId; ?>
                            " style="max-width: 25%; height: auto; margin: 20px auto;" alt="QR Code">

				<br>
				<div style="color: dodgerblue; font-weight: bold; font-size: 15px;">
                    *Lưu ý : Chuyển Khoản vui lòng Load lại trang hiện tại đến khi được cộng tiền</div>
            </div>
        </form>
        <form method="POST" action="#" id="myform">
            <center>
                <h1 class="h4 mb-3 font-weight-normal text-white"
                    style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Bảng giá nạp ATM
                </h1>
            </center>
            <div class="textinfo">
                - 10.000đ - 12.000 Coin <br>
                - 20.000đ - 24.000 Coin <br>
                - 50.000đ - 60.000 Coin <br>
                - 100.000đ - 120.000 Coin <br>
                - 200.000đ - 240.000 Coin <br>
                - 500.000đ - 600.000 Coin <br>
                - 1.000.000đ - 1.300.000 Coin <br>
                - 2.000.000đ - 2.800.000 Coin <br>
                - 5.000.000đ - 7.500.000 Coin
            </div>
        </form>
        <label class="text-white">- Lưu ý: Chuyển đúng nội dung bao gồm cả DẤU CÁCH </label>
        <br>
        <div class="text-white">- Chuyển khoản ít nhất 1.000Đ mới Thành công</div>
        <div class="text-white">- Hãy Kiểm Tra Kĩ Thông Tin Trước Khi Chuyển khoản</div>
        <div class="text-white">- Chuyển khoản đúng nội dung và Thành công mới có lịch sử Nạp ATM</div>
        <div class="text-white">- Quá 30 Phút Thẻ Chưa Duyệt Hãy Báo Ngay Cho Admin Để Được Hỗ Trợ Nhanh Nhất!</div>
    </div>
</div>

<br>

<div class="p-1 mt-1" style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">
    <div class="atm-body">
        <center>
            <h1 class="h3 mb-3 font-weight-normal text-white"
                style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Lịch Sử Chuyển khoản</h1>
        </center>

        <div class="table-responsive">
            <table style="border-collapse: collapse; width: 100%;">
                <thead>
                    <tr>
                        <th
                            style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
                            STT</th>
                        <th
                            style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
                            Tài Khoản</th>
                        <th
                            style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
                            Mệnh Giá</th>
                        <th
                            style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
                            Thời Gian</th>
                        <th
                            style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
                            Trạng Thái</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $queryk = "SELECT *
								  FROM mb_bank
								  WHERE mb_bank.username = '" . $_SESSION['logger']['username'] . "'";
                    $resultk = $config->query($queryk);
                    $stt = 1;
                    if ($resultk === false) {
                        echo 'Lỗi truy vấn SQL: ' . $config->error;
                    } elseif ($resultk->num_rows > 0) {
                        while ($rowk = $resultk->fetch_assoc()) {
                            $statusk = '';
                            $statusColork = '';

                            if ($rowk['status'] == 1) {
                                $statusk = 'Thành công';
                                $statusColork = 'green';
                            } elseif ($rowk['status'] == 0) {
                                $statusk = 'Đang xử lý';
                                $statusColork = 'yellow';
                            }

                            echo '<tr>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $stt . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $rowk['username'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $rowk['amount'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $rowk['time'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white; color: ' . $statusColork . '; font-weight:bold;">' . $statusk . '</td>
							</tr>';
                            $stt++;
                        }
                    } else {
                        echo ' <tr>
							  <td colspan="8" align="center"><span style="font-size:100%;font-weight: bold; text-shadow: 2px 2px 2px #000; color:white;"><< Bạn Chưa Từng Nạp ATM >></span></td>
							</tr>';
                    }
                    ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<br>



<?php
session_start();
function parse_order_id($des)
{
    global $MEMO_PREFIX;
    $re = '/' . $MEMO_PREFIX . '\d+/im';
    preg_match_all($re, $des, $matches, PREG_SET_ORDER, 0);
    if (count($matches) == 0)
        return null;
    $orderCode = $matches[0][0];
    $prefixLength = strlen($MEMO_PREFIX);
    $orderId = substr($orderCode, $prefixLength);
    return strval($orderId);
}

$data = json_decode($data, true);
foreach ($data['data'] as $mb) {
    $sotien = $mb['creditAmount'];
    $magd = explode('\\', $mb['refNo'])[0];
    $noidung = $mb['description'];
    $id = parse_order_id($noidung);

    $account = "SELECT * FROM account WHERE id = '$id'";
    $result = mysqli_query($config, $account);
    $row = mysqli_fetch_array($result);

    $napatm = "SELECT * FROM `mb_bank` WHERE `tid` = '$magd'";
    $result1 = mysqli_query($config, $napatm);
    $row1 = mysqli_fetch_array($result1);

    if ($id) {
        if ($row['id']) {
            if ($sotien >= 1000) {
                if (!$row1) {
                    if ($sotien <= 500000) {
                        $real_amount = $sotien + $sotien * 20 / 100;
                    } elseif ($sotien > 500000 && $sotien <= 2000000) {
                        $real_amount = $sotien + $sotien * 30 / 100;
                    } elseif ($sotien > 2000000 && $sotien <= 5000000) {
                        $real_amount = $sotien + $sotien * 40 / 100;
                    } else {
                        $real_amount = $sotien + $sotien * 50 / 100;
                    }
                    $sqll = "INSERT INTO `mb_bank` (`tid`, `description`, `amount`, `username`, `status`) 
                VALUES ('$magd', '$noidung', '$sotien', '" . $row['username'] . "', '0')";
                    $result11 = mysqli_query($config, $sqll);
                    $create = ($result11 !== false);
                }
                if ($create) {
                    if ($sotien <= 500000) {
                        $real_amount = $sotien + $sotien * 20 / 100;
                    } elseif ($sotien > 500000 && $sotien <= 2000000) {
                        $real_amount = $sotien + $sotien * 30 / 100;
                    } elseif ($sotien > 2000000 && $sotien <= 5000000) {
                        $real_amount = $sotien + $sotien * 40 / 100;
                    } else {
                        $real_amount = $sotien + $sotien * 50 / 100;
                    }
                    $sql = "UPDATE account
                INNER JOIN mb_bank ON account.username = mb_bank.username
                SET account.vnd = account.vnd + $real_amount
                WHERE account.username = '" . $row['username'] . "' AND mb_bank.status = 0";
                    $result = mysqli_query($config, $sql);
					
                    $sqlk = "UPDATE account
                INNER JOIN mb_bank ON account.username = mb_bank.username
                SET account.tongnap = account.tongnap + $sotien
                WHERE account.username = '" . $row['username'] . "' AND mb_bank.status = 0";
                    $resultk = mysqli_query($config, $sqlk);

                    $sqlz = "UPDATE `mb_bank` SET `status` = '1'";
                    $resultz = mysqli_query($config, $sqlz);
                }
            }
        }
    }
}
?>