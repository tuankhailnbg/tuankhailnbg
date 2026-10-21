<?php
  require_once('../core/config.php'); 
  require_once('../core/head.php');
  session_start();
  
  // Check if the user is logged in
  if (!isset($_SESSION['logger']['username'])) {
      die("Bạn chưa đăng nhập.");
  }
  
  $username = $_SESSION['logger']['username'];
  // Use prepared statements to prevent SQL injection
  $stmt = $config->prepare("SELECT id FROM account WHERE username = ?");
  $stmt->bind_param("s", $username);
  $stmt->execute();
  $result = $stmt->get_result();
  
  if ($result->num_rows > 0) {
    $row_hvd = $result->fetch_assoc();
    $user_id = $row_hvd["id"];
  }

  function qrbank($type, $stk, $accountname, $amount, $comment)
  {
      if ($type == 'MOMO') {
          $result = 'data:image/png;base64,' . base64_encode(file_get_contents("https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=2|99|$stk|||0|0|$amount|$comment|transfer_myqr"));
      } else {
          $result = "https://api.vietqr.io/$type/$stk/0/$comment/vietqr_net_2.jpg?accountName=$accountname";
      }
      return $result;
  }
?>
  
<div class="p-1 mt-1 ibox-content" style="border-radius: 7px; box-shadow: 0px 0px 5px black;">
  <div class="card">
    <div class="card-header">
      <a href="/napthe"><b class="btn btn-action text-white" style="background-color: rgb(243, 146, 101);">THẺ CÀO AUTO</b></a>
      <a href="/atm_bank"><b class="btn btn-action text-white" style="background-color: rgb(101, 160, 243);">ATM AUTO</b></a>
    </div>					
    <div class="table-responsive">
      <div class="card-header">
        <center><img class="mb-3"
        src="<?= qrbank('MBBank','99429111','DOAN CONG SINH',100000,"nap$user_id") ?>"
        width="200px" height="200px"></center>
      </div>
      <center>
        <ul class="list-group mb-2">
          <li class="list-group-item">Số tài khoản: <b id="copySTK11" style="color: green;">99429111</b></li>
          <li class="list-group-item">Chủ tài khoản: <b>DOAN CONG SINH</b></li>
          <li class="list-group-item">Ngân hàng: <b>MBBank</b></li>
          <li class="list-group-item">Nội dung nạp: <b id="copyNoiDung11" style="color: red;"><?php echo("nap$user_id") ?></b></li>
        </ul>
        <center><i><i class="fa fa-spinner fa-spin"></i> Xử lý giao dịch tự động trong vài giây...</i></center>
      </center>
      <hr>
      <div class="table-responsive">
        <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
          <p><i>Chân Thành Cảm Ơn Vì Đã Ủng Hộ Chúng Tôi.</i></p>
        </div>
        <table class="table table-hover table-nowrap">
          <tbody style="border-color: black;">
            <tr>
              <th scope="col">STT</th>
              <th scope="col">Mã GD</th>
              <th scope="col">SỐ TIỀN</th>
              <th scope="col">INGAME</th>
            </tr>
            <?php
              $page = isset($_GET['page']) ? intval($_GET['page']) : 1;
              $recordsPerPage = 5;
              $startFrom = ($page - 1) * $recordsPerPage;
              // Use prepared statements for pagination query
              $stmt = $config->prepare("SELECT * FROM mbbank WHERE comment = ? LIMIT ?, ?");
              $stmt->bind_param("sii", $user_id, $startFrom, $recordsPerPage);
              $stmt->execute();
              $result = $stmt->get_result();
              $hasData = false;
              
              // Get character name
              $stmt1 = $config->prepare("SELECT p.name AS char_name FROM account AS a INNER JOIN player AS p ON a.id = p.account_id WHERE p.name = ?");
              $stmt1->bind_param("s", $username);
              $stmt1->execute();
              $result2 = $stmt1->get_result();
              if ($result2->num_rows > 0) {
                $row_hvd2 = $result2->fetch_assoc();
                $char_name = $row_hvd2["char_name"];
              }

              while ($row = $result->fetch_assoc()) {
                  $hasData = true;
            ?>
            <tr>
              <td><b>#<?=$row['id'];?></b></td>
              <td><b><?=$row['tranId'];?></b></td>
              <td><b><?=$row['amount'];?></b></td>
              <td><b><?=$char_name?></b></td>
            </tr>
            <?php } 
              if (!$hasData) {
                echo '<tr><td colspan="6" align="center"><span style="font-size:100%;"><< Lịch Sử Nạp Trống >></span></td></tr>';
              }
            ?>
          </tbody>
        </table>
        
        <?php
          // Calculate total pages for pagination
          $stmt2 = $config->prepare("SELECT COUNT(*) FROM mbbank WHERE comment = ?");
          $stmt2->bind_param("s", $user_id);
          $stmt2->execute();
          $result2 = $stmt2->get_result();
          $totalRecords = $result2->fetch_row()[0];
          $totalPages = ceil($totalRecords / $recordsPerPage);
        ?>
        
        <!-- Pagination -->
        <div class="pagination">
          <?php
            if ($page > 1) {
                echo '<a href="atm_bank?page=' . ($page - 1) . '"><< Trước</a>';
            }

            for ($i = 1; $i <= $totalPages; $i++) {
                echo '<a href="atm_bank?page=' . $i . '">' . $i . '</a>';
            }

            if ($page < $totalPages) {
                echo '<a href="atm_bank?page=' . ($page + 1) . '">Sau >></a>';
            }
          ?>
        </div>

        <style>
          .pagination {
              display: flex;
              justify-content: center;
          }

          .pagination a {
              color: black;
              padding: 8px 16px;
              text-decoration: none;
              border: 1px solid #ddd;
              margin: 0 4px;
          }

          .pagination a.active {
              background-color: #4CAF50;
              color: white;
          }

          .pagination a:hover:not(.active) {
              background-color: #ddd;
          }
        </style>
      </div>
    </div>
  </div>
</div>

<!-- Disable F12 and Right-Click -->
<script>
  // Disable right-click
  document.addEventListener('contextmenu', function (e) {
      e.preventDefault();
  });

  // Disable F12 (DevTools)
  document.onkeydown = function (e) {
      if (e.keyCode == 123) {  // F12
          return false;
      }
      if (e.ctrlKey && e.shiftKey && e.keyCode == 73) {  // Ctrl + Shift + I
          return false;
      }
  };
</script>

<?php require_once('../core/end.php'); ?>
