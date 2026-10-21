<?php
session_start();
require_once ('core/config.php');
require_once ('core/head.php');
include ('set.php');
if (session_status() == PHP_SESSION_NONE) {
	session_start(); //khởi động phiên làm việc
}

if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

	
//echo "Chức năng đang bảo trì. Vui lòng liên hệ ADMIN";
//exit;

$_alert = null;
$_title = "NRO  - Thanh Toán";
if ($_login == null) {
	header("location:/login.php");
}
?>

<head>

	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<script src="https://unpkg.com/sweetalert/dist/sweetalert.min.js"></script>
	<link rel="stylesheet" href="https://cdn.rawgit.com/daneden/animate.css/v3.1.0/animate.min.css">
	<script src='https://cdn.rawgit.com/matthieua/WOW/1.0.1/dist/wow.min.js'></script>
	<!-- <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.7.6/css/mdb.min.css" /> -->
	<link rel="stylesheet" href="https://cdn.rawgit.com/t4t5/sweetalert/v0.2.0/lib/sweet-alert.css">
	<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.3.1/css/all.css" crossorigin="anonymous">
	<!-- <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css" integrity="sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB" crossorigin="anonymous"> -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" />
	<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>
</head>
<main>
	

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
                <a href="<?php echo $fanpage; ?>" type="submit"
                    class="btn btn-lg btn-dark mx-2 auto-resize" style="<?= $nutdangnhapdangkiTO; ?>" name="submit"
                    onmouseover="<?= $onmouseover; ?>" onmouseout="<?= $onmouseout; ?>">Fanpage</a>
            </div>
        <?php } else { ?>
            <div class="align-items-center my-2 d-flex justify-content-between">
                <a href="/pages/dangnhap.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Donate thẻ cào</a>
                <a href="/pages/dangnhap.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                    style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                    onmouseout="<?= $onmouseout; ?>">Donate ATM</a>
                <a href="<?php echo $fanpage; ?>" type="submit"
                    class="btn btn-lg btn-dark mx-2 auto-resize" style="<?= $nutdangnhapdangkiTO; ?>" name="submit"
                    onmouseover="<?= $onmouseover; ?>" onmouseout="<?= $onmouseout; ?>">Fanpage</a>
            </div>
        <?php } ?>
    </div>
</div>

	<script type="text/javascript"> new WOW().init(); </script>
	<div class="p-1 mt-1  "
		style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">
		<div class="card-body">
			<form method="POST" action="#" id="myform">
				<tbody>
					<center>
						<h1 class="h3 mb-3 font-weight-normal text-white"
							style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Nạp Thẻ Tự Động
						</h1>
					</center>
					<label class="text-white">Tài Khoản: </label><br>
					<input type="text" class="form-control form-control-alternative"
						style="background-color: #DCDCDC; font-weight: bold; color: #696969" name="username" value="<?php echo $_username; ?>
						" readonly required>

					<label class="text-white">Loại thẻ:</label>
					<select class="form-control form-control-alternative" name="card_type" required>
						<option value="" class="text-white">Chọn loại thẻ</option>
						<?php
						$cdurl = curl_init("https://thesieutoc.net/card_info.php");
						curl_setopt($cdurl, CURLOPT_FAILONERROR, true);
						curl_setopt($cdurl, CURLOPT_FOLLOWLOCATION, true);
						curl_setopt($cdurl, CURLOPT_RETURNTRANSFER, true);
						curl_setopt($cdurl, CURLOPT_CAINFO, __DIR__ . '/api/curl-ca-bundle.crt');
						curl_setopt($cdurl, CURLOPT_CAPATH, __DIR__ . '/api/curl-ca-bundle.crt');
						$obj = json_decode(curl_exec($cdurl), true);
						curl_close($cdurl);
						$length = count($obj);
						for ($i = 0; $i < $length; $i++) {
							if ($obj[$i]['status'] == 1) {
								echo '<option value="' . $obj[$i]['name'] . '">' . $obj[$i]['name'];// . ' (chiết khấu 10%)</option> '
							}
						}
						?>
					</select>
					<label class="text-white">Mệnh giá:</label>
					<select class="form-control form-control-alternative" name="card_amount" required>
						<option value="">Chọn mệnh giá</option>
						<option value="10000">10.000đ - 10.000 Coin</option>
						<option value="20000">20.000đ - 20.000 Coin</option>
						<option value="30000">30.000đ - 30.000 Coin</option>
						<option value="50000">50.000đ - 50.000 Coin</option>
						<option value="100000">100.000đ - 100.000 Coin</option>
						<option value="200000">200.000đ - 200.000 Coin</option>
						<option value="300000">300.000đ - 300.000 Coin</option>
						<option value="500000">500.000đ - 500.000 Coin</option>
						<option value="1000000">1.000.000đ - 1.000.000 Coin</option>
					</select>
					<label class="text-white">Số seri:</label>
					<input type="text" class="form-control form-control-alternative" name="serial" required />
					<label class="text-white">Mã thẻ:</label>
					<input type="text" class="form-control form-control-alternative" name="pin" required /><br>
					<div>
						<center><button type="submit" class="btn btn-lg btn-dark btn-block"
								style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
								onmouseout="<?= $onmouseout; ?>">NẠP NGAY</button></center>

				</tbody>
			</form>
			<script type="text/javascript">


				$("#myform").submit(function (e) {
					$("#status").html("<img src='/load.gif' width='30%' />");
					e.preventDefault();
					$.ajax({
						url: "./ajax/card.php",
						type: 'post',
						data: $("#myform").serialize(),
						success: function (data) {
							$("#status").html(data);
							document.getElementById("myform").reset();
							$("#load_hs").load("./ajax/history.php");
						}
					});

				});


			</script><br><br>

			<label class="text-white">- Lưu ý: Sai mệnh giá sẽ mất thẻ ! </label>
			<br>
			<label class="text-white">- Nạp tự động 100% </label>
			<div class="text-white">- Hãy Kiểm Tra Kĩ Thông Tin Trước Khi Nạp</div>
			<div class="text-white">- Nạp Sai Mệnh Giá, admin cũng không nhận được gì nên không hỗ trợ được</div>
			<div class="text-white">- Quá 30 Phút Thẻ Chưa Duyệt Hãy Báo Ngay Cho Admin Để Được Hỗ Trợ Nhanh Nhất!</div>

			</form>
		</div>
	</div>
	</div>
	<br>
	<div class="p-1 mt-1"
		style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">
		<div class="card-body">
			<center>
				<h1 class="h3 mb-3 font-weight-normal text-white"
					style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;"
					>Lịch Sử Donate Thẻ Cào</h1>
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
								Loại thẻ</th>
							<th
								style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
								Mệnh Giá</th>
							<th
								style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
								Mã Seri</th>
							<th
								style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
								Mã PIN</th>
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
						$query = "SELECT *
								  FROM trans_log
								  WHERE trans_log.name = '" . $_SESSION['logger']['username'] . "'";
						$result = $config->query($query);
						$stt = 1;
						if ($result === false) {
							echo 'Lỗi truy vấn SQL: ' . $config->error;
						} elseif ($result->num_rows > 0) {
							while ($row = $result->fetch_assoc()) {
								$status = '';
								$statusColor = '';

								if ($row['status'] == 1) {
									$status = 'Thành công';
									$statusColor = 'green';
								} elseif ($row['status'] == 2) {
									$status = 'Không thành công';
									$statusColor = 'red';
								} elseif ($row['status'] == 0) {
									$status = 'Đang xử lý';
									$statusColor = 'yellow';
								}

								echo '<tr>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $stt . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $row['name'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $row['type'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . number_format($row['amount']) . '<sup>đ</sup></td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $row['seri'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $row['pin'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $row['date'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white; color: ' . $statusColor . '; font-weight:bold;">' . $status . '</td>
							</tr>';
								$stt++;
							}
						} else {
							echo ' <tr>
							  <td colspan="8" align="center"><span style="font-size:100%;font-weight: bold; text-shadow: 2px 2px 2px #000; color:white;"><< Bạn Chưa Từng Nạp Thẻ >></span></td>
							</tr>';
						}
						?>
					</tbody>
				</table>
			</div>
		</div>
	</div>


	<div id="status"></div>

	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.4/jquery.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.0.0/crypto-js.min.js"></script>
	<!-- Code made in tui 127.0.0.1 -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
		integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
		crossorigin="anonymous"></script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js"
		integrity="sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T"
		crossorigin="anonymous"></script>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/2.9.2/umd/popper.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.6.0/js/bootstrap.min.js"></script>
	<script src="https://www.google.com/recaptcha/api.js" async defer></script>
</main>
<?php require_once ('core/end.php'); ?>