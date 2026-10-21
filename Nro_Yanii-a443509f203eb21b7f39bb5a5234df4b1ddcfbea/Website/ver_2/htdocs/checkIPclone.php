<?php
    require_once('core/config.php'); 
    require_once('core/head.php'); 
    include('set.php'); 
    $thongbao = null;
    session_start();
    if (!isset($_SESSION['logger']['username'])) {
        die("Bạn chưa đăng nhập.");
    }

    // Lấy username từ session
    $username = $_SESSION['logger']['username'];
    
?> 


<main>
  <div style="background: #ffe9b8; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
    <div class="text-center col-lg-5 col-md-10" style="margin: auto;padding: 20px;">
    
    <span style="color:green;font-weight:bold;"><i class="fa fa-wifi"></i> Địa chỉ IP hiện tại <br><?php echo $_ip; ?></span>   
    <br> 
    <table>
            <thead class="thead-dark">      
                <tr>
                    <th>Các tài khoản cùng IP :</th>
                </tr>
                <tr>
                    <th>Tên</th>
                    
                    <th>Địa chỉ IP</th>
                </tr>
            </thead>
            <tbody>
                <?php
                $data = _query(_select("player.*", "player INNER JOIN account ON account.id = player.account_id", "account.ip_address = '$_ip' AND account.ban = 0"));
                while ($row = mysqli_fetch_array($data)) {
                ?>
                <tr>
                    <td><?php echo htmlspecialchars($row['name']); ?></td>                   
                    <td><?php echo $_ip; ?></td>
                </tr>
                <?php } ?>
            </tbody>
        </table>
        <br>
        <span style="color:red;font-weight:bold;"><i class="fa fa-exclamation-triangle" aria-hidden="true"></i> Được vận hành bởi Buffalo Team</span>
    </div>
    <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
            <span class="text-black" style="vertical-align: middle;">Địa chỉ IP là gì ? <a href="https://vi.wikipedia.org/wiki/%C4%90%E1%BB%8Ba_ch%E1%BB%89_IP" class="text-blue">Xem Tại Đây<a></span>
        </div>
  </div>
</main> 


<?php require_once('core/end.php');  ?>