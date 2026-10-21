  <?php 
    require_once('../core/config.php'); 
    require_once('../core/head.php');
?>
        <div class="p-1 mt-1 ibox-content" style="border-radius: 7px; box-shadow: 0px 0px 5px black;">
          <br>
          <main>
            <h1 class="h3 mb-3 font-weight-normal">
                  <center>Bảng Xếp Hạng Đại Gia</center>
              </b>
            </h1>
            <div class="table-responsive">
              <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
                <span class="text-black" style="vertical-align: middle;">Cập nhật 5 phút 1 lần</span>
              </div>
              <table class="table table-hover table-nowrap">
                <tbody style="border-color: black;">
                  <tr>
                    <th scope="col">Top</th>
                    <th scope="col">Nhân vật</th>
                    <th scope="col">Tổng Nạp</th>
                  </tr>
                  <?php
                    $query = "SELECT player.name, SUM(account.tongnap) AS tongnap FROM account JOIN player ON account.id = player.account_id GROUP BY player.name ORDER BY tongnap DESC LIMIT 10";
                    $result = $config->query($query);
                    $stt = 1;
                    if ($result === false) {
                      echo 'Lỗi truy vấn SQL: '.$config->error;
                    } elseif ($result->num_rows > 0) {
                      while ($row = $result->fetch_assoc()) {
                       echo '<tr>
                              <td><b>#'.$stt.'</b></td>
                              <td>'.$row['name'].'</td>
                              <td>'.number_format($row['tongnap']).'</td>
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
            <h1 class="h3 mb-3 font-weight-normal">
    <b>
        <center>Bảng Xếp Hạng Sức Mạnh</center>
    </b>
</h1>
<div class="table-responsive">
    <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
        <span class="text-black" style="vertical-align: middle;">Cập nhật 5 phút 1 lần</span>
    </div>
    <table class="table table-hover table-nowrap">
        <thead>
            <tr>
                <th scope="col">Top</th>
                <th scope="col">Nhân vật</th>
                <th scope="col">Sức Mạnh</th>
                <th scope="col">Hành Tinh</th>
                <th scope="col">Tổng</th>
            </tr>
        </thead>
        <tbody style="border-color: black;">
            <?php
            $countTop = 1;
            $data = mysqli_query($config, "SELECT name, gender, 
                CASE 
                WHEN gender = 1 THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS SIGNED)
                WHEN gender = 2 THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS SIGNED)
                ELSE CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS SIGNED)
                END AS second_value,
                CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS SIGNED) AS second_value,
                CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS SIGNED) AS tongdiem
                FROM player
                ORDER BY tongdiem DESC
                LIMIT 10;");
            if (mysqli_num_rows($data) > 0) { // Check the number of returned results
                while ($row = mysqli_fetch_array($data)) {
            ?>
            <tr class="top_<?php echo $countTop; ?>">
                <td>
                    <b>#<?php echo $countTop++; ?></b>
                </td>
                <td>
                    <?php echo htmlspecialchars($row['name']); ?>
                </td>
                <td>
                    <?php
                    $value = $row['second_value'];

                    if ($value != '') {
                        if ($value > 1000000000) {
                            echo number_format($value / 1000000000, 1, '.', '') . ' Tỷ';
                        } elseif ($value > 1000000) {
                            echo number_format($value / 1000000, 1, '.', '') . ' Tr';
                        } elseif ($value >= 1000) {
                            echo number_format($value / 1000, 1, '.', '') . ' K';
                        } else {
                            echo number_format($value, 0, ',', '');
                        }
                    } else {
                        echo 'Không có chỉ số sức mạnh';
                    }
                    ?>
                </td>
                <td>
                    <?php
                    if ($row['gender'] == 0) {
                        echo "Trái đất";
                    } elseif ($row['gender'] == 1) {
                        echo "Namec";
                    } elseif ($row['gender'] == 2) {
                        echo "Xayda";
                    }
                    ?>
                </td>
                <td>
                    <?php
                    $total = $row['tongdiem'];

                    if ($total > 1000000000) {
                        echo number_format($total / 1000000000, 1, '.', '') . ' Tỷ';
                    } elseif ($total > 1000000) {
                        echo number_format($total / 1000000, 1, '.', '') . ' Tr';
                    } elseif ($total >= 1000) {
                        echo number_format($total / 1000, 1, '.', '') . ' K';
                    } else {
                        echo number_format($total, 0, ',', '');
                    }
                    ?>
                </td>
            </tr>
            <?php
                }
            } else {
                echo '<tr><td colspan="5">Máy Chủ 1 chưa có thống kê bảng xếp hạng!</td></tr>';
            }
            ?>
        </tbody>
    </table>
</div>

			<h1 class="h3 mb-3 font-weight-normal">
                  <center>Bảng Xếp Hạng Nhiệm Vụ</center>
            </h1>
            <div class="table-responsive">
              <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;" class="text-center">
                <span class="text-black" style="vertical-align: middle;">Cập nhật 5 phút 1 lần</span>
              </div>
              <table class="table table-hover table-nowrap">
                <tbody style="border-color: black;">
                  <tr>
                    <th scope="col">Top</th>
                    <th scope="col">Nhân vật</th>
                    <th scope="col">Nhiệm Vụ Chính</th>
                    <th scope="col">Nhiệm Vụ Phụ</th>
                    <th scope="col">Nhiệm Vụ Con</th>
                  </tr>
                  <?php
						$stt = 1;
						$data = mysqli_query($config,"SELECT name, 
            JSON_EXTRACT(data_task, '$[0]') AS second_value,
            JSON_EXTRACT(data_task, '$[1]') AS third_value,
            JSON_EXTRACT(data_task, '$[2]') AS four_value
     FROM player
     ORDER BY CAST(JSON_EXTRACT(data_task, '$[0]') AS UNSIGNED) DESC,
              CAST(JSON_EXTRACT(data_task, '$[1]') AS UNSIGNED) DESC,
              CAST(JSON_EXTRACT(data_task, '$[2]') AS UNSIGNED) DESC
     LIMIT 10;
     
						");
						while ($row = mysqli_fetch_array($data)) {
                       echo '<tr>
                              <td><b>#'.$stt.'</b></td>
                              <td>'.$row['name'].'</td>
                              <td>'.number_format($row['second_value']).'</td>
                              <td>'.number_format($row['third_value']).'</td>
                              <td>'.number_format($row['four_value']).'</td>
                            </tr>';
                        $stt++;
                      }
						?>
						</tbody>
              </table>
            </div>
          </main>
        </div>
      </main>
      <div class="modal right fade" id="Noti_Home" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true" data-mdb-backdrop="static" data-mdb-keyboard="true">
    <div class="modal-dialog modal-side modal-bottom-right ">
        <div class="modal-content">
            <div class="modal-header" style="background-color: #2c2c2c; color: #FFF; text-align: center;">
                <img src="../hit/images/logo/logo.gif" style="display: block; margin-left: auto; margin-right: auto; max-width: 250px;">
            </div>
            <div class="modal-body">
            <center><p style="padding: 10px">
                    <b style="color:red"><u>Thông Báo</u></b><br>
                    Tham gia Ngọc Rồng Tương Lai   trên các nền tảng mạng xã hội nhé.!<br><br>
                    <a href="link zalo" class="btn btn-download" style="border-radius: 10px; color: #FFFFFF;" target="_blank"><b>Box Zalo</b></a>
					<a href="link zalo" class="btn btn-download" style="border-radius: 10px; color: #FFFFFF;" target="_blank"><b>Nhóm FaceBook</b></a>
                </p>
          </center>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        $('#Noti_Home').modal('show');
    })
</script>
<?php require_once('../core/end.php'); ?>