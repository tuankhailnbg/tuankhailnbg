<?php 
    require_once('../core/config.php'); 
    require_once('../core/head.php'); 
    $thongbao = null;
    session_start();
    if (isset($_SESSION['logger']['username'])) {
        echo '<script>window.location.href = "/";</script>';
        exit();
    }
    if(isset($_POST['submit']) && $_POST['username'] != '' && $_POST['password'] != ''){        
        $username = $_POST['username'];
        $password = $_POST['password'];
        $captcha = $_POST['g-recaptcha-response'];
        
		
            $sql = "SELECT * FROM account WHERE username = '$username' AND password = '$password'";
            $account = mysqli_query($config,$sql);
            if(mysqli_num_rows($account) > 0){
                $_SESSION['logger']['username'] = $username;
                $_SESSION['logger']['password'] = $password;
				$_SESSION['account'] = $username;
                
                $thongbao = 'Đăng nhập thành công';
                
                
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
                            window.location.href = "/";
                        }
                    });
                }
                ';
                echo '<script>' . $script . '</script>';


            }else{
                $thongbao = 'Sai tài khoản hoặc mật khẩu !';
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
                            window.location.href = "../pages/dangnhap.php";
                        }
                    });
                }
                ';

                // Trả về đoạn mã JavaScript dưới dạng chuỗi
                echo '<script>' . $script . '</script>';
            
        }
    }
?>
<main>
<div style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
                <form class="text-center col-lg-5 col-md-10" style="margin: auto; padding: 20px;"
                      method="post" action="">
                      <h1 class="h3 mb-3 font-weight-normal text-white" style="padding-top: 3px; font-weight: bold; text-shadow: 2px 2px 2px #000;">Đăng Nhập</h1>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="username"
                           type="text" class="form-control mt-1" placeholder="Tên tài khoản" autofocus="">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                    <input style="height: 50px; border-radius: 15px; font-weight: bold;" name="password"
                           type="password" class="form-control mt-1" placeholder="Mật khẩu">
                    <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                   
                  <!--  <span style="color: red; font-size: 12px; font-weight: bold;">
                                            </span>
                    <center><div class="g-recaptcha" data-sitekey="<?=$site_key;?>" style="margin:10px;"></div></center>
                    <div class="text-center mt-1">-->
                    <button class="btn btn-lg btn-dark btn-block" style="font-weight: bold; background-color: #faa42e; border-radius: 10px; width: 100%; height: 50px;"
                        type="submit" name="submit" onmouseover="this.style.backgroundColor='#b8f9f1'" onmouseout="this.style.backgroundColor='#faa42e'">Đăng Nhập</button>
                    </div>
                </form>

</div>
</main>
<?php require_once('../core/end.php'); ?>