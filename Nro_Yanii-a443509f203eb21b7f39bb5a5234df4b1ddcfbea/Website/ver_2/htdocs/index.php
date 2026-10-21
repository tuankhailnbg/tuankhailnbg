<?php
require_once ('core/config.php');
require_once ('core/head.php');
?>
<style>
	.announcement {
		background-color: #ffe9b8;
		color: #333;
		font-weight: bold;
		margin-bottom: 10px;
		border-radius: 7px;
		border: 3px solid black;
		padding: 5px;
	}

	.announcement-text {
		font-size: 50px;
		font-weight: bold;
		margin: 0;
	}

	.announcement p {
		margin: 0;
	}

	.announcement-info {
		font-size: 15px;
		display: none;
		margin-top: 10px;
		background-color: white;
		border: 2px solid white;
		padding: 10px;
		border-radius: 5px;
	}

	.announcement-info.active {
		display: block;
	}

	.ent-info {
		font-size: 12px;
		display: none;
		margin-top: 10px;
		background-color: white;
		border: 2px solid white;
		padding: 10px;
		border-radius: 5px;
	}

	.ent-info.active {
		display: block;
	}

	.row1 {
		display: flex;
		flex-wrap: wrap;
		justify-content: center;
		/* Canh giữa các thẻ trong dòng */
		padding 10px;
	}
</style>
<script>
	function toggleInfo(id) {
		var info = document.getElementById('info' + id);
		info.classList.toggle('active');
	}
</script>

<main>

	<script>
		window.addEventListener('resize', function () {
			const buttons = document.getElementsByClassName('auto-resize');
			for (let i = 0; i < buttons.length; i++) {
				const button = buttons[i];
				const fontSize = parseInt(window.getComputedStyle(button, null).getPropertyValue('font-size'));
				const buttonWidth = button.offsetWidth;
				const textWidth = button.scrollWidth;
				if (textWidth > buttonWidth) {
					button.style.fontSize = (fontSize - 1) + 'px';
				}
			}
		});
		window.dispatchEvent(new Event('resize'));
	</script>
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

	<div class="p-1 mt-1 ibox-content"
		style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">

		<div class="p-1 text-white">
			<h5 class="h3 mb-3 font-weight-normal text-white"
				style="text-align:center;padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Bảng Thông
				Tin</h5>

			<div class="announcement">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a9.png"
								style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
						</div>
						<div>
							<a class="fw-bold " href="/thong_tin_game.php">Đọc xong liên hệ ADMIN Nhận Quà</a>
							<div class="text-muted font-weight-bold"> Đọc xong ib nhận <span
									style="color: red;"> Thỏi vàng </span><span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
			</div>
			
			

			<div class="announcement" onclick="toggleInfo(1)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a1.png"
								style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
						</div>
						<div>
							<a class="fw-bold " href="#">Đua Top Hàng Tuần</a>
							<div class="text-muted font-weight-bold">Đăng bởi <span
									style="color: red;">Admin</span><span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
				<div id="info1" class="ent-info">
				
					
					<span style="color: blue; font-weight: bold; font-size: 20px;">THƯỞNGNHIỆM VỤ</span><br>
					<span style="color: blue;">Ai xong all nhiệm vụ có thể liên hệ admin nhận 1000 thỏi vàng </span> <br> 
					<span style="color: red;">(Bất cứ ai xong nhiệm vụ có thể ib ad nhận quà)</span> <br> <br>
					
					<span style="color: blue; font-weight: bold; font-size: 20px;">TOP CHUYỂN SINH HÀNG TUẦN</span><br>				
					- TOP 1 :100 Tờ tiền 10k (Item sự kiện)
		            <br>
					- TOP 2: 50 Tờ tiền 10k (Item sự kiện)
					<br>
					- Top 3: 20 Tờ tiền 10k (Item sự kiện)
					<br>
					- Top 4-10: 5 Tờ tiền 10k (Item sự kiện)
					<br>
					<span style="color: red;">(Chốt Top vào 23h ngày 8/9/2024)</span> <br> <br>

					<span style="color: blue; font-weight: bold; font-size: 20px;">TOP DONATE THÁNG 8</span><br>
				
					-TOP 1 : 250 Tờ 10k - 1 Hộp thánh tôn (Mở ngẫu nhiên 1 món)+ Thẻ Đổi Tên(Có thể GD)<br>
					-TOP 2 : 200 Tờ 10k - 1 Hộp thánh tôn (Mở ngẫu nhiên 1 món)+ Thẻ Đổi Tên(Có thể GD)<br>
					-TOP 3 : 150 Tờ 10k - 1 Hộp thánh tôn (Mở ngẫu nhiên 1 món)+ Thẻ Đổi Tên(Có thể GD)<br>
					-Top 4 : 100 Tờ 10k (Item sự kiện)<br>
					-Top 5-10 :50 Tờ 10k (Item sự kiện)<br>
					<span style="color: red;">(Chốt top 23h 15 Tháng 9)</span> <br>
					<br><br><br>
					<span style="color: blue; font-weight: bold; font-size: 15px;"> ADMIN </span> <br>
					
					- Anh em vui lòng liên hệ đúng người để tránh bị scam

				</div>
			</div>
			
			<div class="announcement" onclick="toggleInfo(2)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a2.png"
								style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
						</div>
						<div>
							<a class="fw-bold " href="#">Giftcode dùng chung</a>
							<div class="text-muted font-weight-bold">Không cần mở thành viên<br><span
									style="color: red;"><br>!!!</span><span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
				<div id="info2" class="announcement-info">
					<div class="table-responsive">
						<div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;"
							class="text-center">
						</div>
						<table class="table table-hover table-custom  " style="text-align: center;">
							<thead>
								<tr>
									<th>Giftcode</th>
								</tr>
							</thead>
							<tbody>
								<?php
								$query = "SELECT *
											FROM gift_codes
											WHERE gift_codes.active = 0;";
								$result = $config->query($query);
								$stt = 1;
								if ($result === false) {
									echo 'Lỗi truy vấn SQL: ' . $config->error;
								} elseif ($result->num_rows > 0) {
									while ($row = $result->fetch_assoc()) {
										echo '<tr>
											  <td>' . $row['code'] . '</td>
											</tr>';
										$stt++;
									}
								} else {
									echo ' <tr>
											  <td colspan="3" align="center"><span style="font-size:100%;"><< Lịch Sử Trống >></span></td>
											</tr>';
								}
								?>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="announcement" onclick="toggleInfo(3)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a10.png"
								style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
						</div>
						<div>
							<a class="fw-bold " href="#">Giftcode cho tài khoản đã Mở thành viên</a>
							<div class="text-muted font-weight-bold">Đăng bởi <span
									style="color: red;">Admin</span><span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
				<div id="info3" class="announcement-info">
					<div class="table-responsive">
						<div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;"
							class="text-center">
						</div>
						<table class="table table-hover table-custom  " style="text-align: center;">
							<thead>
								<tr>
									<th>Giftcode</th>
								</tr>
							</thead>
							<tbody>
								<?php
								$query = "SELECT *
											FROM gift_codes
											WHERE gift_codes.active = 1;";
								$result = $config->query($query);
								$stt = 1;
								if ($result === false) {
									echo 'Lỗi truy vấn SQL: ' . $config->error;
								} elseif ($result->num_rows > 0) {
									while ($row = $result->fetch_assoc()) {
										echo '<tr>
											  <td>' . $row['code'] . '</td>
											</tr>';
										$stt++;
									}
								} else {
									echo ' <tr>
											  <td colspan="3" align="center"><span style="font-size:100%;"><< Lịch Sử Trống >></span></td>
											</tr>';
								}
								?>
							</tbody>
						</table>
					</div>
				</div>
			</div>


			<!-- <div class="announcement" onclick="toggleInfo(4)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a6.png"
								style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
						</div>
						<div>
							<a class="fw-bold " href="#">Cách nhận Giftcode Riêng</a>
							<div class="text-muted font-weight-bold">Đăng bởi <span
									style="color: red;">Admin</span><span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
				<div id="info4" class="ent-info">
					<span style="color: blue; font-weight: bold; font-size: 16px;">Mỗi lần nạp mệnh giá trên 50.000đ sẽ
						nhận được 1 Gifcode (Nhập code ở mục Nhập code riêng tại NPC nhà)</span><br>
					<span style="color: red; font-weight: bold; font-size: 15px;">(Xem Giftcode tại Giftcode riêng phía trên)</span><br><br>
					***Vật phẩm Giftcode bao gồm :
					<br>
					-10 Hộp đồ Huỷ diệt : mở ngẫu nhiên được 1 món Huỷ diệt
					<br>
					-15 Rương sao pha lê VIP
					<br>
					-5 Viên ngọc rồng siêu cấp <br>
					<span style="color: red;">(Chốt Top vào 21h Ngày 06/04)</span> <br> <br>
				</div>
			</div> -->
		</div>
	</div>

	<!-- -->
	<br>

	<div class="p-1 mt-1 ibox-content"
		style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">

		<div class="p-1 text-white">
			<h5 class="h3 mb-3 font-weight-normal text-white"
				style="text-align:center;padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Download
				Game</h5>
			<!--
					<div class="announcement" onclick="toggleInfo(3)">
						
						<div class="announcement-title" style="color:white;">
							<div class="post-item d-flex align-items-center my-2">
								<div class="post-image"><img src="/public/images/logo/3.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
								<div>
									<a class="fw-bold " href="#">Các Bản Mod Khác</a>
									<div class="text-muted font-weight-bold">Tải về tại đây !<span class="fb-comments-count" data-href="#"></span></div>
								</div>
								<div class="post-image">
									<img src="/public/images/logo/hot.gif" style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
								</div>
							</div>
						</div>
						<div id="info3" class="announcement-info ">
							<br>
							<div class="text-center row1">
								<div class="announcement col-5">
									<div class="announcement-title" style="color:white;">
										<div class="post-item d-flex align-items-center my-2">
											<div class="post-image"><img src="/public/images/logo/dow3.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
											<div>
												<a class="fw-bold " href="https://www.mediafire.com/file/oaf6vphbu9y9auq/Jar_230_Fire.jar/file">Phiên Bản JAR 230</a>
											</div>
											
										</div>
									</div>
								</div>
								<div class="col-1"></div>
								<div class="announcement col-5">
									<div class="announcement-title" style="color:white;">
										<div class="post-item d-flex align-items-center my-2">
										<div class="post-image"><img src="/public/images/logo/dow2.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
											
											<div>
												<a class="fw-bold " href="https://install.appcenter.ms/orgs/nghiamod/apps/nrofire/distribution_groups/mod">Phiên Bản IOS App center</a>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="text-center row1">
								<div class="announcement col-5">
									<div class="announcement-title" style="color:white;">
										<div class="post-item d-flex align-items-center my-2">
										<div class="post-image"><img src="/public/images/logo/dow4.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
											
											<div>
												<a class="fw-bold " href="https://www.mediafire.com/file/s45prbnuy8vw19r/KOI_230_PC.rar/file">Phiên Bản Koi 230 PC</a>
											</div>
										</div>
									</div>
								</div>
								<div class="col-1"></div>
								<div class="announcement col-5">
									<div class="announcement-title" style="color:white;">
										<div class="post-item d-flex align-items-center my-2">
										<div class="post-image"><img src="/public/images/logo/dow1.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
											
											<div>
												<a class="fw-bold " href="https://www.mediafire.com/file/4oyudkcku4p87e6/Koi_APK_230.apk/file">Phiên Bản Koi 230 APK</a>
											</div>
										</div>
									</div>
								</div>
							</div>
							<!--
							<div class="text-center row1">
								<div class="announcement col-5">
									<div class="announcement-title" style="color:white;">
										<div class="post-item d-flex align-items-center my-2">
										<div class="post-image"><img src="/public/images/logo/dow4.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
											
											<div>
												<a class="fw-bold " href="https://www.file.io/g3tv/download/KlXMed2Saotv">Phiên Bản VuDang PC</a>
											</div>
										</div>
									</div>
								</div>
								<div class="col-1"></div>
								<div class="announcement col-5">
									<div class="announcement-title" style="color:white;">
										<div class="post-item d-flex align-items-center my-2">
										<div class="post-image"><img src="/public/images/logo/dow1.png" style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;" ></div>
											
											<div>
												<a class="fw-bold " href="https://file.io/KlXMed2Saotv">Phiên Bản VuDang APK</a>
											</div>
										</div>
									</div>
								</div>
							</div>
							
							
						</div>
					</div>-->
			<div class="text-center row1">
				<div class="announcement col-5">
					<div class="announcement-title" style="color:white;">
						<div class="post-item d-flex align-items-center my-2">
							<div class="post-image"><img src="/public/images/logo/dow1.png"
									style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
							</div>
							<div>
								<a class="fw-bold " href="<?= $adr; ?>">Phiên Bản Android</a>
							</div>

						</div>
					</div>
				</div>
				<div class="col-1"></div>
				<div class="announcement col-5">
					<div class="announcement-title" style="color:white;">
						<div class="post-item d-flex align-items-center my-2">
							<div class="post-image"><img src="/public/images/logo/dow2.png"
									style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
							</div>

							<div>
								<a class="fw-bold " href="<?= $ios; ?>">Phiên bản IOS</a>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="text-center row1">
				<div class="announcement col-5">
					<div class="announcement-title" style="color:white;">
						<div class="post-item d-flex align-items-center my-2">
							<div class="post-image"><img src="/public/images/logo/dow4.png"
									style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
							</div>

							<div>
								<a class="fw-bold " href="<?= $pc; ?>">Bản PC Window</a>
							</div>
						</div>
					</div>
				</div>
				<!-- <div class="col-1"></div>
				<div class="announcement col-5">
					<div class="announcement-title" style="color:white;">
						<div class="post-item d-flex align-items-center my-2">
							<div class="post-image"><img src="/public/images/logo/dow1.png"
									style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
							</div>
							<div>
								<a class="fw-bold " href="<?= $adr2; ?>">Bản Android Phụ</a>
							</div>

						</div>
					</div>
				</div> -->
			</div>
			<!-- <div class="announcement" onclick="toggleInfo(4)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a4.png"
								style="width: 55px; margin: 8px; height: auto; object-fit: contain;"></div>
						<div>
							<a href="https://install.appcenter.ms/users/sonthai062k4-gmail.com/apps/esign-social/distribution_groups/esign%20social%20by%20dts"
								class="fw-bold " href="#">Tải về Esign Cho IOS</a>
							<div class="text-muted font-weight-bold">Xem tại đây !<span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
			</div>

			<div class="announcement" onclick="toggleInfo(5)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a5.png"
								style="width: 55px; margin: 8px; height: auto; object-fit: contain;"></div>
						<div>
							<a class="fw-bold " href="https://www.youtube.com/watch?v=7FG5krhno7g">Hướng dẫn cài đặt cho
								IOS Esign</a>
							<div class="text-muted font-weight-bold">Xem tại đây !<span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
			</div>

			<div class="announcement" onclick="toggleInfo(6)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a6.png"
								style="width: 55px; margin: 8px; height: auto; object-fit: contain;"></div>
						<div>
							<a class="fw-bold " href="https://www.youtube.com/watch?v=vNoAoMcqGSk">Hướng dẫn cài đặt qua
								Scarlet</a>
							<div class="text-muted font-weight-bold">Xem tại đây !<span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
			</div>

			<div class="announcement" onclick="toggleInfo(7)">
				<div class="announcement-title" style="color:white;">
					<div class="post-item d-flex align-items-center my-2">
						<div class="post-image"><img src="/public/images/logo/a7.png"
								style="width: 55px; margin: 8px; height: auto; object-fit: contain;"></div>
						<div>
							<a class="fw-bold " href="https://youtu.be/B_DSCZwu7BI?si=FLR7HICPcPUV4bMh">Hướng dẫn Fix
								không xác minh</a>
							<div class="text-muted font-weight-bold">Xem tại đây !<span class="fb-comments-count"
									data-href="#"></span></div>
						</div>
						<div class="post-image">
							<img src="/public/images/logo/new.gif"
								style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
						</div>
					</div>
				</div>
			</div> -->
		</div>
</main>

<br>

<?php require_once ('core/end.php'); ?>


<script type="text/javascript">
	$(document).ready(function () {
		$('#Noti_Home').modal('show');
	})
</script>
<?php require_once ('core/end.php'); ?>