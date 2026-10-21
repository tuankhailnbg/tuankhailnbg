<?php
    ini_set('display_errors', 1);
    ini_set('display_startup_errors', 1);
    error_reporting(E_ERROR);
    session_start();
    unset($_SESSION["errors"]);
    $username = $_SESSION['logger']['username'];
    $sql_thoivang = "SELECT vnd FROM account WHERE username = '$username'";
      $result1 = $config->query($sql_thoivang);
  
      if ($result1->num_rows > 0) {
          $row1 = $result1->fetch_assoc();
          $vnd = $row1["vnd"];
      }
      $sql = "SELECT id FROM account WHERE username = '$username'";
      $result = $config->query($sql);
      
      if ($result->num_rows > 0) {
          // Lấy id từ kết quả truy vấn
          $row = $result->fetch_assoc();
          $accountId = $row["id"];
      
          // Truy vấn để lấy giá trị giới tính từ bảng Player
          $sql = "SELECT head FROM player WHERE account_id = $accountId";
          $result = $config->query($sql);
      
          if ($result->num_rows > 0) {
              // In ra giá trị giới tính
              $row = $result->fetch_assoc();
          }
      }
?> <html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><?=$tieude;?></title>
    <link rel="canonical" href="<?=$link_web;?>" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/x-icon" href="../hit/images/logo/win.png">
    <!-- bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"></script>
    <!-- icon -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <!-- jquery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
    <script src="https://code.jquery.com/jquery-3.6.1.min.js"></script>
    <!-- mycss -->
    <link rel="stylesheet" href="../hit/css/hoangvietdung.css?hoangvietdung=<?=rand(0,100000);?>">
    <script src="//cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <!-- bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.min.js"></script>
    <style type="text/css">
      #hoangvietdung {
        opacity: 1;
      }
      #hoangvietdung2{
           padding:30px;
           background-color:rgba(0,0,0,0.3);
        }
      #custom-hr {
        border: none;
        border-top: 1px solid #000;
        margin: 10px 0;
      }
      #custom-hr2 {
        border: none;
        border-top: 1px solid #000;
        margin: 2px 0;
      }
    </style>
  </head>
  <body class="girlkun-bg" id="hoangvietdung">
    <div class="container-md p-1 col-sm-12 col-lg-6" style="background: #CCFFFF; border-radius: 7px; border: 1px solid #99FFFF; box-shadow: 0 0 15px #99FFFF;">
      <style>
        #snow {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          pointer-events: none;
          z-index: -70;
        }
      </style>
      <div id="snow"></div>
      <script>
    document.addEventListener('DOMContentLoaded', function () {
        var script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/particles.js/2.0.0/particles.min.js';
        script.onload = function () {
            particlesJS("snow", {
                "particles": {
                    "number": {
                        "value": 75,
                        "density": {
                            "enable": true,
                            "value_area": 400
                        }
                    },
                    "color": {
                        "value": "#FFCC33"
                    },
                    "opacity": {
                        "value": 1,
                        "random": true,
                        "anim": {
                            "enable": false
                        }
                    },
                    "size": {
                        "value": 3,
                        "random": true,
                        "anim": {
                            "enable": true
                        }
                    },
                    "line_linked": {
                        "enable": true
                    },
                    "move": {
                        "enable": true,
                        "speed": 1,
                        "direction": "top",
                        "random": true,
                        "straight": false,
                        "out_mode": "out",
                        "bounce": false,
                        "attract": {
                            "enable": true,
                            "rotateX": 300,
                            "rotateY": 1200
                        }
                    }
                },
                "interactivity": {
                    "events": {
                        "onhover": {
                            "enable": false
                        },
                        "onclick": {
                            "enable": false
                        },
                        "resize": false
                    }
                },
                "retina_detect": true
            });
        }
        document.head.append(script);
    });

</script>


      <main>
        <!-- header -->
        <div style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
          <!-- logo -->
          <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
            <img height="12" src="../hit/images/icon/12.png" style="vertical-align: middle;">
            <span class="text-black" style="vertical-align: middle;">Dành cho người chơi trên 12 tuổi. Chơi quá 180 phút mỗi ngày sẽ có hại sức khỏe.</span>
          </div>
          <div class="p-xs">
            <a href="/">
              <img src="<?=$logo;?>" style="display: block;margin-left: auto;margin-right: auto;max-width: 250px;">
            </a>
          </div>
          <!-- download -->
          <div class="text-center mt-2">
              <a href="<?=$java;?>" target="_blank" class="btn btn-download text-white" style="border-radius: 10px; width: 100px;">
              <i class="fa fa-download"></i> JAR </a>
              <a href="<?=$pc;?>" target="_blank" class="btn btn-download text-white m-1" style="border-radius: 10px;width: 100px;">
              <i class="fa fa-windows"></i> PC </a>
              <a href="<?=$adr;?>" target="_blank" class="btn btn-download text-white" style="border-radius: 10px;width: 100px;">
              <i class="fa fa-android"></i> APK </a>
              <a href="<?=$ios;?>" target="_blank" class="btn btn-download text-white" style="border-radius: 10px;width: 100px;">
              <i class="fa fa-apple"></i> IOS  </a>
              <a href="<?=$box_zalo;?>" target="_blank" class="btn btn-download text-white" style="border-radius: 10px;width: 100px;">
              <i class="fa fa-telegram"></i> Zalo </a>
              <a href="/pages/bangxephang.php" target="_blank" class="btn btn-download text-white" style="border-radius: 10px;width: 100px;">
              <i class="fa fa-bar-chart"></i> BXH </a>
		        	<a href="<?=$fanpage;?>" target="_blank" class="btn btn-download text-white" style="border-radius: 10px;width: 120px;">
              <i class="fa fa-facebook"></i> Fanpage </a>  

 

            <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
              <span class="text-black" style="vertical-align: middle;">Tải phiên bản phù hợp để có trải nghiệm tốt.</span>
            </div>
          </div>
        </div>
        <!--body-->
        <div class="col text-center mt-2">
          <div class="user_name"> <?php if ($_SESSION['logger']['username']) { ?>
          <center>
            <?php
            if ($row["head"] == 28) {
                echo '<img src="../hit/images/icon/28.png" width="60" />';
            } elseif ($row["head"] == 27) {
                echo '<img src="../hit/images/icon/27.png" width="60" />';
            } elseif ($row["head"] == 6) {
                echo '<img src="../hit/images/icon/6.png" width="60" />';
            } elseif ($row["head"] == 64) {
                echo '<img src="../hit/images/icon/64.png" width="60" />';
            } elseif ($row["head"] == 31) {
                echo '<img src="../hit/images/icon/31.png" width="60" />';
            } elseif ($row["head"] == 30) {
                echo '<img src="../hit/images/icon/30.png" width="60" />';
            } elseif ($row["head"] == 9) {
                echo '<img src="../hit/images/icon/9.png" width="60" />';
            } elseif ($row["head"] == 29) {
                echo '<img src="../hit/images/icon/29.png" width="60" />';
            } elseif ($row["head"] == 32) {
                echo '<img src="../hit/images/icon/32.png" width="60" />';
            } else {
                echo '<img src="../hit/images/icon/6.png" width="60" />';
            }
            ?>
            <br>
          </center>
            <label>
              <a style="color: black">Chào,</a>
            </label>
            <b style="color: #9A0000"> <?php echo $_SESSION['logger']['username']?> </b> - <i class="fa fa-money"></i>
            <b> <?=number_format($vnd);?><sup>VNĐ</sup></b>
            <br>
            <u>Hãy tận dụng các chức năng dưới đây!</u> <?php } else { ?> <?php } ?>
          </div> <?php if ($_SESSION['logger']['username']) { ?> 
          <a href="/napthe" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-credit-card"></i> Nạp Tiền </a>
          <a href="/doimatkhau" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-address-card"></i> Đổi Password </a>
            <a href="/pages/doigmail.php" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-google"></i> Đổi Gmail </a>
          <a href="/active" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-check-circle-o"></i> Kích Hoạt </a>
            <a href="/forum" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-globe"></i> Diễn Đàn </a>
          <a href="/dangxuat" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-sign-in"></i> Đăng Xuất </a>
             <br>

            <?php } else { ?> 
          <a href="/dangnhap" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-sign-in"></i> Đăng nhập </a>
          <a href="/dangky" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-user-plus"></i> Đăng ký </a>
          <a href="/forum" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-globe"></i> Diễn Đàn </a> 
            <a href="/pages/quenmatkhau.php" class="btn btn-action m-1 text-white" style="border-radius: 10px;">
            <i class="fa fa-key"></i> Quên Mật Khẩu </a>



            <?php 
          } ?>
        </div>