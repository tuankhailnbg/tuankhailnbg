<?php
  require_once('../core/config.php'); 
  require_once('../core/head.php');
  session_start();
  if (!isset($_SESSION['logger']['username'])) {
      die("Bạn chưa đăng nhập.");
  }
  $username = $_SESSION['logger']['username'];
  $sql = "SELECT id FROM account WHERE username = '$username'";
  $result = $config->query($sql);
  if ($result->num_rows > 0) {
	$row_hvd = $result->fetch_assoc();
	$user_id = $row_hvd["id"];
  }
  
  	?>
	
		<div class="p-1 mt-1 ibox-content" style="border-radius: 7px; box-shadow: 0px 0px 5px black;">
                <div class="card">
                  <div class="card-header">
                    <b>Nạp Tiền ATM(AUTO)</b>
                    <br>
                    <b class="badge" style="background-color: rgb(243, 146, 101);">Tỉ giá quy đổi: 10.000đ = 12.000đ AUTO</b>
                    <a href="/napthe"><b class="badge" style="background-color: rgb(243, 101, 106);">Nạp Qua Thẻ Cào AUTO <b>10k = 8k</b> </b></a>
                            </div>
							<p><i>1.</i>  Số tài khoản:  <b style="color:black;">99429111</b></p>
							<p><i>2.</i>  Chủ tài khoản: <b style="color:black;">DOAN CONG SINH</b></p>
							<p><i>3.</i>  Nhập nội dung: <b style="color:black;"><?php echo "starrail$user_id"; ?>
							</p>
							</b>(Kiểm tra kĩ nội dung, nếu sai vui lòng liên hệ admin để được giải quyết.)</p>
							</p>
                            </p>						
							<p><i>Khi chuyển tiền xong làm mới trang sau 1 - 3 phút để cập nhật Coin.</i></p>
							<div class="table-responsive">
							 <b>--------------------------------------------------------------------------------------------------</b>
							
							<h4>Cách 2: Quét mã QR</h4>
							</p>
							</p>
                            </p>
							<center>
								<img src="../hoangvietdung_public/font/atm.jpg" style="max-width: 250px;">
							<p style="font-size: 12px;">Quét mã QR trên, nhập số tiền bạn cần nạp và nhập lời nhắn <b style="color:black;font-size: 20px;"><?php  echo "starrail$user_id";?></b> </p>
						    </p>
                            </p>						
							<p><i>Khi chuyển tiền xong làm mới trang sau 1 - 3 phút để cập nhật Coin.</i></p>
						<hr>
        		<div class="table-responsive">
              <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
			  <p><i>Chân Thành Cảm Ơn Vì Đã Ủng Hộ Chúng Tôi.</i></p>
				
                <tbody style="border-color: black;">
					</div>
				</div>
				</div>
          
            </div>
            </div>
		<div>
   	</div>
			
<?php require_once('../core/end.php'); ?>