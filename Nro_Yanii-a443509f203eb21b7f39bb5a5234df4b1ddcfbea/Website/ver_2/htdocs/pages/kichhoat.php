<?php
    require_once('../core/config.php'); 
    require_once('../core/head.php'); 
    $thongbao = null;
    session_start();
    if (!isset($_SESSION['logger']['username'])) {
        die("Bạn chưa đăng nhập.");
    }

    // Lấy username từ session
    $username = $_SESSION['logger']['username'];

    $sql_active = "SELECT active FROM account WHERE username = '$username'";
    $result = $config->query($sql_active);

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $active = $row["active"];
    }
    $sql = "SELECT id FROM account WHERE username = '$username'";
    $result = $config->query($sql);

    if ($result->num_rows > 0) {
          // Lấy id từ kết quả truy vấn
          $row = $result->fetch_assoc();
          $accountId = $row["id"];
      
          // Truy vấn để lấy giá trị giới tính từ bảng Player
          $sql = "SELECT vnd FROM account WHERE id = $accountId";
          $result = $config->query($sql);
      
          if ($result->num_rows > 0) {
              $row = $result->fetch_assoc();
              $vnd = $row['vnd'];
          }
      }
    if (isset($_POST['submit'])) {
        if ($active == 0) {
            if ($vnd >= $gia_mtv) {
            $sql = "UPDATE account SET account.active = 1, account.vnd = account.vnd - $gia_mtv WHERE account.username = '$username'";
                $result = $config->query($sql);
                if ($result === TRUE) {
                    $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Bạn đã kích hoạt thành công!</span>';
                } else {
                    $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Xảy ra lỗi!</span>';
                }
            } else {
                $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Không đủ tiền, vui lòng nạp!</span>';
            }
        } else {
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Bạn đã kích hoạt thành viên rồi!</span>';
        }
    }
    
    
?> 


<main>
  <div style="background: #ffe9b8; font-size: 15px; border-radius: 7px; box-shadow: 0px 2px 5px black;padding: 20px;" class="pb-1">
    <div class="text-center col-lg-5 col-md-10" style="margin: auto;">
        <?php 
            if ($active == 0) {
                echo '<b style="color: green">Phí kích hoạt: </b>';
                echo '<b style="color: black">20.000 VNĐ</b><br>';
            } else {
                echo '<b style="color: green">Bạn đã là thành viên chính thức</b><br>';
            }
        ?> <br>
      <small>Trạng thái: 
        <?php 
            if ($active == 0) {
                echo '<b style="color: red">Chưa kích hoạt!</b>';
            } else {
                echo '<b style="color: green">Đã kích hoạt!</b>';
            }
        ?> 
      </small>
      <br> <?=$thongbao;?> <form method="POST" action="">
        <div class="text-center mt-1">
          <input class="btn btn-lg btn-dark btn-block" style="border-radius: 10px;width: 100%; font-size: 15px; height: 50px;" type="submit" name="submit" value="Bấm để kích hoạt thành viên" />
        </div>
      </form>
    </div>
  </div>
</main> 


<?php require_once('../core/end.php'); ?>