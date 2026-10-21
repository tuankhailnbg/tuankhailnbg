<?php 

    require_once('../core/config.php'); 
    require_once('../core/head.php'); 
    $thongbao = null;
    if (!isset($_SESSION['logger']['username'])) {
        die("Bạn chưa đăng nhập.");
    }
    session_start();
	if(isset($_POST['submit']) && $_POST['username'] != '' && $_POST['password'] != '' && $_POST['newpassword'] != ''){
		$username = $_POST['username'];
		$password = $_POST['password'];
		$newpassword = $_POST['newpassword'];
		$captcha = $_POST['g-recaptcha-response'];
		
		$username = mysqli_real_escape_string($config, $username);
		$password = mysqli_real_escape_string($config, $password);
		$newpassword = mysqli_real_escape_string($config, $newpassword);
		
		$sql = "SELECT * FROM account WHERE username = '$username' AND password = '$password'";
		$old = mysqli_query($config, $sql);
		
		if(mysqli_num_rows($old) > 0){
			$sql1 = "UPDATE account SET password = '$newpassword' WHERE username = '$username'";
			mysqli_query($config, $sql1);
			
			$thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Đã đổi mật khẩu, hãy thoát ra vào lại!</span>';
			echo '<script>window.location.href = "/pages/dangxuat.php";</script>';
		} else {
			$thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Hãy nhập đúng tài khoản mật khẩu!</span>';
		}
	} else {
		$thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Hãy điền đầy đủ thông tin!</span>';
	}

?>
<main>
	  <!-- header -->
<div style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
                <form class="text-center col-lg-5 col-md-10" style="margin: auto;padding:20px;"
                      method="post" action="">
                <center><h1 class="h3 mb-3 font-weight-normal text-white" style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Đổi Mật Khẩu</h1></center>
                    <?=$thongbao;?>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username" required="" autofocus=""
                           type="text" class="form-control mt-1" placeholder="Tên tài khoản">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password" required=""
                           type="password" class="form-control mt-1" placeholder="Mật khẩu">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="newpassword" required=""
                           type="newpassword" class="form-control mt-1" placeholder="Mật khẩu Mới">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                    <div class="text-center mt-1">
                        <button class="btn btn-lg btn-dark btn-block" style="<?=$nutdangnhapdangkiTO;?>"
                                type="submit" name="submit" onmouseover="<?=$onmouseover;?>" onmouseout="<?=$onmouseout;?>">Thực Hiện</button>
                                
                    </div>
                </form>
            </div>
      </main>
<?php require_once('../core/end.php'); ?>