<?php 
    require_once('../core/config.php'); 
    require_once('../core/head.php'); 
    $thongbao = null;

    session_start();
    if (isset($_SESSION['logger']['username'])) {
        echo '<script>window.location.href = "/";</script>';
        exit();
    }
	if(isset($_POST['submit']) && $_POST['username'] !='' && $_POST['password'] != ''){
		$username = mysqli_real_escape_string($config, $_POST['username']);
		$password = mysqli_real_escape_string($config, $_POST['password']);
		$vndd = 0;
		
		if (!preg_match('/^[a-zA-Z0-9]+$/', $username)) {
			$thongbao = 'Tên người dùng không hợp lệ!';
			$script = '
			var thongbao = ' . json_encode($thongbao) . ';
			if (thongbao !== "") {
				Swal.fire({
					title: "Thất Bại",
					html: thongbao,
					icon: "error",
					confirmButtonText: "Đóng",
					allowOutsideClick: () => Swal.getConfirmButton().click()
				}).then((result) => {
					if (result.isConfirmed) {
						window.location.href = "../pages/dangky.php";
					}
				});
			}
			';
			echo '<script>' . $script . '</script>';
		}else if (!preg_match('/^[a-zA-Z0-9]+$/', $password)) {
			$thongbao = 'Mật khẩu không hợp lệ!';
			$script = '
			var thongbao = ' . json_encode($thongbao) . ';
			if (thongbao !== "") {
				Swal.fire({
					title: "Thất Bại",
					html: thongbao,
					icon: "error",
					confirmButtonText: "Đóng",
					allowOutsideClick: () => Swal.getConfirmButton().click()
				}).then((result) => {
					if (result.isConfirmed) {
						window.location.href = "../pages/dangky.php";
					}
				});
			}
			';
			echo '<script>' . $script . '</script>';
		}
		
		$captcha = $_POST['g-recaptcha'];
		if($captcha){
			$thongbao = 'Hãy xác minh captcha!';
			$script = '
			var thongbao = ' . json_encode($thongbao) . ';
			if (thongbao !== "") {
				Swal.fire({
					title: "Thất Bại",
					html: thongbao,
					icon: "error",
					confirmButtonText: "Đóng",
					allowOutsideClick: () => Swal.getConfirmButton().click()
				}).then((result) => {
					if (result.isConfirmed) {
						window.location.href = "../pages/dangky.php";
					}
				});
			}
			';
			echo '<script>' . $script . '</script>';
		} else {
			$stmt = mysqli_prepare($config, "SELECT * FROM account WHERE username = ?");
			mysqli_stmt_bind_param($stmt, "s", $username);
			mysqli_stmt_execute($stmt);
			$result = mysqli_stmt_get_result($stmt);

			if(mysqli_num_rows($result) > 0){
				$thongbao = 'Tài khoản đã tồn tại!';
				$script = '
				var thongbao = ' . json_encode($thongbao) . ';
				if (thongbao !== "") {
					Swal.fire({
						title: "Thất Bại",
						html: thongbao,
						icon: "error",
						confirmButtonText: "Đóng",
						allowOutsideClick: () => Swal.getConfirmButton().click()
					}).then((result) => {
						if (result.isConfirmed) {
							window.location.href = "../pages/dangky.php";
						}
					});
				}
				';
				echo '<script>' . $script . '</script>';
			} else {
				
				if (!preg_match('/^[a-zA-Z0-9]+$/', $username)) {
					$thongbao = 'Tên người dùng không hợp lệ!';
					$script = '
					var thongbao = ' . json_encode($thongbao) . ';
					if (thongbao !== "") {
						Swal.fire({
							title: "Thất Bại",
							html: thongbao,
							icon: "error",
							confirmButtonText: "Đóng",
							allowOutsideClick: () => Swal.getConfirmButton().click()
						}).then((result) => {
							if (result.isConfirmed) {
								window.location.href = "../pages/dangky.php";
							}
						});
					}
					';
					echo '<script>' . $script . '</script>';
				}else if (!preg_match('/^[a-zA-Z0-9]+$/', $password)) {
					$thongbao = 'Mật khẩu không hợp lệ!';
					$script = '
					var thongbao = ' . json_encode($thongbao) . ';
					if (thongbao !== "") {
						Swal.fire({
							title: "Thất Bại",
							html: thongbao,
							icon: "error",
							confirmButtonText: "Đóng",
							allowOutsideClick: () => Swal.getConfirmButton().click()
						}).then((result) => {
							if (result.isConfirmed) {
								window.location.href = "../pages/dangky.php";
							}
						});
					}
					';
					echo '<script>' . $script . '</script>';
				}else {
				
					$stmt = mysqli_prepare($config, "INSERT INTO account (username, password, vnd) VALUES (?, ?, ?)");
					mysqli_stmt_bind_param($stmt, "ssi", $username, $password, $vndd);
					mysqli_stmt_execute($stmt);

					$thongbao = 'Đăng ký thành công!';
					$script = '
					var thongbao = ' . json_encode($thongbao) . ';
					if (thongbao !== "") {
						Swal.fire({
							title: "Thành Công",
							html: thongbao,
							icon: "success",
							confirmButtonText: "Đóng",
							allowOutsideClick: () => Swal.getConfirmButton().click()
						}).then((result) => {
							if (result.isConfirmed) {
								window.location.href = "../pages/dangnhap.php";
							}
						});
					}
					';
					echo '<script>' . $script . '</script>';
				}
			}
		}
	}
?>
<main>
   <div style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
                <form class="text-center col-lg-5 col-md-10" style="margin: auto; padding: 20px;"
                      method="post" action="">
                      <h1 class="h3 mb-3 font-weight-normal text-white" style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Đăng Kí</h1>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username" required="" autofocus=""
                           type="text" class="form-control mt-1" placeholder="Tên tài khoản">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password" required=""
                           type="password" class="form-control mt-1" placeholder="Mật khẩu">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                 
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                  <!--  <center><div class="g-recaptcha" style="margin:10px;" data-sitekey="<?=$site_key;?>"></div></center>-->
                    <div class="text-center mt-1">
						<button class="btn btn-lg btn-dark btn-block" style="<?=$nutdangnhapdangkiTO;?>"
							type="submit" name="submit" onmouseover="<?=$onmouseover;?>" onmouseout="<?=$onmouseout;?>">Đăng Kí</button>
						
					</div>
                </form>
            </div>
      </main>
<?php require_once('../core/end.php'); ?>