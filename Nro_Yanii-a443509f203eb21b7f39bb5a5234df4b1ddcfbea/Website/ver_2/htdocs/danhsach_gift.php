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

$_alert = null;
$_title = "Học Viện Nro - Thanh Toán";
if ($_login == null) {
	header("location:/login.php");
}

$queryt = "SELECT p.name, p.id
    FROM player p
    LEFT JOIN account a ON p.account_id = a.id
    WHERE a.username = '$_username'";

$resultt = $config->query($queryt);

if ($resultt->num_rows > 0) {
	$rowt = $resultt->fetch_assoc();
	$player_id = $rowt['id'];
} else {
	echo '<p style="text-align: center; font-size: 20px; color: green; font-weight: bold;">Bạn chưa tạo nhân vật</p>';
	exit;
}
?>
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

	<div class="atm-body">
		<center>
			<h1 class="h3 mb-3 font-weight-normal text-white"
				style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Danh sách Giftcode</h1>
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
							Mã Giftcode</th>
						<th
							style="padding: 8px; text-align: left; border: 1px solid black; background-color: #b8f9f1;font-weight: bold;text-shadow: 2px 2px 2px #fff;">
							Trạng Thái</th>
					</tr>
				</thead>
				<tbody>
					<?php
					$queryk = "SELECT *
								  FROM member_gift
								  WHERE member_gift.player_idd = '" . $player_id . "'";
					$resultk = $config->query($queryk);

					$stt = 1;
					if ($resultk === false) {
						echo 'Lỗi truy vấn SQL: ' . $config->error;
					} elseif ($resultk->num_rows > 0) {
						while ($rowk = $resultk->fetch_assoc()) {
							$statusk = '';
							$statusColork = '';

							$queryCheckCoded = "SELECT * FROM member_gift_lichsu WHERE coded = '" . $rowk['coded'] . "'";
							$resultCheckCoded = $config->query($queryCheckCoded);
							if ($resultCheckCoded->num_rows > 0) {
								$statusk = 'Đã sử dụng';
								$statusColork = 'green';
							} else {
								$statusk = 'Chưa sử dụng';
								$statusColork = 'red';
							}

							echo '<tr>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $stt . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white;">' . $rowk['coded'] . '</td>
							  <td style="padding: 8px; text-align: left; border: 1px solid black;background-color: white; color: ' . $statusColork . '; font-weight:bold;">' . $statusk . '</td>
							</tr>';
							$stt++;
						}
					} else {
						echo ' <tr>
							  <td colspan="8" align="center"><span style="font-size:100%;font-weight: bold; text-shadow: 2px 2px 2px #000; color:white;"><< Bạn không có Giftcode riêng >></span></td>
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