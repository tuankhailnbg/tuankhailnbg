<!--<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ERROR);
session_start();
unset($_SESSION["errors"]);
$_username = $_SESSION['logger']['username'];
$sql = "SELECT id FROM account WHERE username = '$_username'";
$result = $config->query($sql);

?>
<html lang="en">

<head>

  <link href="path_to_select2_css/select2.min.css" rel="stylesheet" />
  <script src="path_to_jquery/jquery.min.js"></script>
  <script src="path_to_select2_js/select2.min.js"></script>
  <style>
    .select2-icon {
      display: inline-block;
      width: 16px;
      height: 16px;
      margin-right: 5px;
    }
  </style>
  <script>
    $(document).ready(function () {
      function formatOption(option) {
        if (!option.id) {
          return option.text;
        }

        var iconClass = $(option.element).data('icon');
        var iconAlt = $(option.element).data('icon-alt');

        var $option = $('<span><img src="' + iconClass + '" class="select2-icon" alt="' + iconAlt + '" /> ' + option.text + '</span>');

        return $option;
      }

      $('.my-select').select2({
        templateResult: formatOption
      });
    });
  </script>
  <!-- Import SweetAlert2 CSS -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/sweetalert2@11.1.5/dist/sweetalert2.min.css">

  <!-- Import SweetAlert2 JavaScript -->
  <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11.1.5/dist/sweetalert2.min.js"></script>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>
    <?= $tieude; ?>
  </title>
  <link rel="canonical" href="<?= $link_web; ?>" />
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="<?= $iconweb; ?>">
  <!-- bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"></script>
  <!-- icon -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
  <!-- jquery -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
  <script src="https://code.jquery.com/jquery-3.6.1.min.js"></script>
  <!-- mycss -->
  <link rel="stylesheet" href="../public/css/huynhcuong.css?huynhcuong=<?= rand(0, 100000); ?>">
  <script src="//cdn.jsdelivr.net/npm/sweetalert2@11"></script>
  <!-- bootstrap -->
  <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.min.js"></script>
  <!-- Captcha Goole -->
  <script src='https://www.google.com/recaptcha/api.js' async defer></script>
  <style type="text/css">
    #huynhcuong {
      background-color: black;
    }

    #huynhcuong2 {
      padding: 30px;
      background-color: rgba(0, 0, 0, 0.3);
    }

    #custom-hr {
      border: none;
      border-top: 1px solid #000;
      margin: 10px 0;
    }

    #custom-hr2 {
      border: none;
      border-top: 1px solid #000;
      margin: 10px 0;
    }

    <?= $nutdangnhapdangki; ?>
    <?= $nutdownloadfile; ?>
  </style>
</head>

<body class="girlkun-bg" id="huynhcuong">
  <div class="container-md p-1 col-sm-12 col-lg-6" style="<?= $maunen; ?>"">
      <style>

        .btn-navbar{
            background-color: #5E17EB;
            border-color: black;
            text-shadow: 1px 1px 0 black;
        }
        .btn-navbar:hover,
        .btn-navbar:focus {
            background-color:#5500FF;
            border-color: 1px solid black;
            font-weight: bold;
        }
        .image-container {
          max-width: 500px;
          margin-left: auto;
          margin-right: auto;
        }
       
        .table-custom, .table-custom tr, .table-custom td, .table-custom th {
          border: none !important;
        }
        .table-custom th, .table-custom td:first-child {
          width: 150px;
        }
        .image-container img {
          width: 100%;
          height: auto;
        }
        
        @keyframes blink {
          0% { color: black; }
          50% { color: red; }
          100% { color: black; }
        }
        @keyframes flashing {
          0% {
            border-color: black;
          }
          50% {
            border-color: yellow;
          }
          100% {
            border-color: black;
          }
        }
        @keyframes flashing1 {
          0% {
            border-color: black;
          }
          50% {
            border-color: red;
          }
          100% {
            border-color: black;
          }
        }
        @keyframes flashing2 {
          0% {
            border-color: black;
          }
          50% {
            border-color: #9CF100;
          }
          100% {
            border-color: black;
          }
        }
        @keyframes stretch {
          0% {
            transform: scale(1);
          }
          50% {
            transform: scale(1.2);
          }
          100% {
            transform: scale(1);
          }
        }
        .text-black1 {
          animation: blink 0.5s infinite;
        }

        @keyframes blinking {
          0% { color: #FF0000; }
          50% { color: #FFFFFF; }
          100% { color: #FF0000; }
        }

        .blinking-link {
          animation: blinking 0.5s infinite;
        }
      </style>


<style>


.section1{
    position: relative;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background: url(./bg0.jpg);
    background-position-x: center;
    background-size: cover;
}
.span1{
    position: absolute;
    top: 50%;
    left: 50%;
    width: 4px;
    height: 4px;
    background: #fff;
    border-radius: 50%;
    box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1), 0 0 0 8px rgba(255, 255, 255, 0.1), 0 0 20px rgba(255, 255, 255, 1)  ;
    animation:  animate 1s linear infinite;
}
.span1::before{
    content: '';
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 300px;
    height: 1px;
    background: linear-gradient(90deg, #fff, transparent);
}
@keyframes animate {
    0%
    {
        transform: rotate(315deg) translateX(0);
        opacity: 1;
    }
    70%
    {
        opacity: 1;

    }
    100%
    {
        transform: rotate(315deg) translateX(-1500px);
        opacity: 0;

    }
    
}
.span1:nth-child(1){
    top: 0;
    right: 0;
    left:initial;
    animation-delay:0 ;
    animation-duration: 1s;
}

.span1:nth-child(2){
    top: 0;
    right: 80px;
    left:initial;
    animation-delay:0.2s;
    animation-duration: 3s;
}

.span1:nth-child(3){
    top: 80px;
    right: 0px;
    left:initial;
    animation-delay:0.4s ;
    animation-duration: 2s;
}

.span1:nth-child(4){
    top: 0;
    right: 180px;
    left:initial;
    animation-delay:0.6s;
    animation-duration: 1.5s;
}

.span1:nth-child(5){
    top: 0;
    right: 400px;
    left:initial;
    animation-delay:0.8s;
    animation-duration: 2.5s;
}

.span1:nth-child(6){
    top: 0;
    right: 600px;
    left:initial;
    animation-delay:1s ;
    animation-duration: 3s;
}
.span1:nth-child(7 ){
    top: 300px;
    right: 0px;
    left:initial;
    animation-delay:1s ;
    animation-duration: 1.75s;
}

.span1:nth-child(8){
    top: 0px;
    right: 700px;
    left:initial;
    animation-delay:1.4s ;
    animation-duration: 1.25s;
}

.span1:nth-child(9){
    top: 0px;
    right: 1000px;
    left:initial;
    animation-delay:0.75s ;
    animation-duration: 2.25s;
}

.span1:nth-child(10){
    top: 0px;
    right: 1000px;
    left:initial;
    animation-delay:2.75s ;
    animation-duration: 2.25s;
}
.decorative-section {
    position: fixed;
    overflow: hidden;
}


</style>




      <main>
        <!-- header -->
        <div style=" background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;"
    class="pb-1 ">
    <!-- logo -->
    <style>
      .hovered img {
        transform: scale(1.1);
      }

      a img {
        transition: transform 0.3s ease;
      }

      .marquee {
        overflow: hidden;
        white-space: nowrap;
        width: 50%;
        margin: 0 auto;
        text-align: center;
        padding-top: 10px;
        padding-bottom: 10px;
        font-weight: bold;
      }

      .marquee span {
        display: inline-block;
        animation: marquee 2s linear infinite;
        padding: 0 20px;
        color: white;
      }

      @keyframes marquee {
        0% {
          transform: translateX(100%);
        }

        100% {
          transform: translateX(-100%);
        }
      }

      @keyframes blink {
        0% {
          color: #FFCF00;
        }

        50% {
          color: white;
        }

        100% {
          color: #FFCF00;
        }
      }

      .marquee span {
        animation: marquee 7s linear infinite, blink 1s ease-in-out infinite;
      }
    </style>
    <div class="p-xs">
      <div class="image-container">
        <a href="/index.php" onmouseover="addHoverClass(this)" onmouseout="removeHoverClass(this)">
          <center><img style="width:80%; padding-bottom:10px;animation: stretch 2s infinite alternate;"
              src="<?= $logohead; ?>" alt="Logo"></center>
        </a>
      </div>
    </div>
    <script>
      function addHoverClass(element) {
        element.classList.add("hovered");
      }

      function removeHoverClass(element) {
        element.classList.remove("hovered");
      }
    </script>
    <!-- navbar -->
    <div class="row text-center p-3 pb-1 pt-1" style="border-radius: 10px; display: flex; justify-content: center;">
      <a href="/index.php" class="btn btn-navbar text-white col-4" style="<?= $maunavbar1; ?>"
        onmouseover="this.style.backgroundColor='#bbf9f1'" onmouseout="this.style.backgroundColor='#000'">
        <i class="fa fa-home" style="display: inline-block; margin-right: 0.5rem;"></i> Trang chủ
      </a>
      <a href="/bxh.php" class="btn btn-navbar text-white col-4" style="<?= $maunavbar1; ?>"
        onmouseover="this.style.backgroundColor='#bbf9f1'" onmouseout="this.style.backgroundColor='#000'">
        <i class="fa fa-group" style="display: inline-block; margin-right: 0.5rem;"></i> Top Server
      </a>
      <a href="<?php echo $linkbox; ?>" target="_blank" class="btn btn-navbar text-white col-4"
        style="<?= $maunavbar1; ?>" onmouseover="this.style.backgroundColor='#bbf9f1'"
        onmouseout="this.style.backgroundColor='#000'">
        <i class="fa fa-comments" style="display: inline-block; margin-right: 0.5rem;"></i> Group Zalo
      </a>
    </div>

    <!-- download -->
    <div class="text-center mt-2">
      <!-- <a href="<?= $jar; ?>" target="_blank" class="btn btn-download text-white" style="border-radius: 10px; width: 100px;">
                <i class="fa fa-android"></i> JAR </a> -->
      <a href="<?= $pc; ?>" target="_blank" class="btn btn-download text-white"
        style="border-radius: 10px; width: 100px;">
        <i class="fa fa-windows"></i>Phiên bản PC</a>
      <a href="<?= $adr; ?>" target="_blank" class="btn btn-download text-white"
        style="border-radius: 10px; width: 100px;">
        <i class="fa fa-android"></i>Phiên bản APK</a>
      <a href="<?= $ios; ?>" target="_blank" class="btn btn-download text-white"
        style="border-radius: 10px; width: 100px;">
        <i class="fa fa-apple"></i>Phiên bản IOS</a>
      <a href="<?= $adr2; ?>" target="_blank" class="btn btn-download text-white"
        style="border-radius: 10px; width: 100px;">
        <i class="fa fa-android"></i>Bản APK Phụ</a>
    </div>

    <div class="marquee">
      <span>Chào mừng bạn đến với Học Viện Nro</span>
    </div>
    <!--
      <div class="marquee">
        <span>Chào mừng bạn đến với Ngọc Rồng Fire. Mọi chi tiết vui lòng liên hệ Admin qua Zalo !</span>
      </div>
      -->
  </div>
  <!--body-->

  <div class="col text-center mt-2 ">
    <div class="user_name">
      <?php if ($_SESSION['logger']['username']) { ?>
        <center>
          <?php
          if (!$row["gender"]) {
            echo '<img src="../public/images/icon/3.png" />';
          } else {
            echo '<img style="width: 150px;" src="../public/images/icon/' . $row["gender"] . '.png" />';
          }
          ?>
          <br>
        </center>
        <label>
          <a style="color: white;">Chào,</a>
        </label>
        <b style="color: #ff0000">
          <?php echo $_SESSION['logger']['username'] ?>
        </b> - <i class="fa fa-money" style="color:white;"></i>
        <b style="color: white;">
          <?= number_format($roww["vnd"]); ?> VNĐ
        </b>
        <br>
      <?php } else { ?>
      <?php } ?>
    </div>



    <?php if ($_SESSION['logger']['username']) { ?>
      <a href="/pages/doimatkhau.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-address-card"></i> Đổi Mật Khẩu </a>

      <a href="/pages/kichhoat.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-check-circle-o"></i> Kích Hoạt </a>
      <a href="/pages/profile.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-user-plus"></i> Nhân vật </a>
      <a href="/checkIPclone.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-credit-card"></i> Kiểm Tra IP </a>
      <!-- <a href="/lichsunap.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-credit-card"></i> Lịch Sử Nạp </a> -->
      <a href="/danhsach_gift.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-money"></i> Giftcode riêng </a>
      <!--<a href="/pages/doigift.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-money"></i> Nhận Giftcode </a>-->
      <a href="/pages/dangxuat.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-sign-in"></i> Đăng Xuất </a> <br>
    <?php } else { ?>
      <a href="/pages/dangnhap.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-sign-in"></i> Đăng nhập </a>
      <a href="/pages/dangky.php" class="btn btn-action m-1 text-black" style="border-radius: 10px;">
        <i class="fa fa-user-plus"></i> Đăng ký </a>
    <?php } ?>

  </div>