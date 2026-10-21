<?php
require_once ('../core/config.php');
require_once ('../core/head.php');
include ('set.php');

$thongbao = null;
session_start();
if (!isset ($_SESSION['logger']['username'])) {
  die ("Bạn chưa đăng nhập.");
}

$query = "SELECT p.name, p.gender, p.data_point, p.data_task, a.username, a.active, a.vnd
    FROM player p
    LEFT JOIN account a ON p.account_id = a.id
    WHERE a.username = '$_username'";

$result = $config->query($query);

if ($result->num_rows > 0) {
  $row = $result->fetch_assoc();
  $tenNhanVat = $row['name'];
  $gioiTinhNhanVat = $row['gender'];
  $chuoiChiSo = $row['data_point'];
  $chuoiNhiemVu = json_decode($row['data_task'], true);
} else {
  echo '<p style="text-align: center; font-size: 20px; color: green; font-weight: bold;">Bạn chưa tạo nhân vật</p>';
  exit;
}
////////////////////////////////////////////////

$nhiemVuQuery = "SELECT name FROM task_main_template WHERE id = ?";
$nhiemVuStatement = $config->prepare($nhiemVuQuery);
$nhiemVuID = $chuoiNhiemVu[0];
$nhiemVuStatement->bind_param("i", $nhiemVuID); // "i" indicates the type of the parameter (integer)
$nhiemVuStatement->execute();

$nhiemVuResult = $nhiemVuStatement->get_result();
$nhiemVuRow = $nhiemVuResult->fetch_assoc();

$tenNhiemVu = $nhiemVuRow ? $nhiemVuRow['name'] : '';

////////////////////////////////////////////////

$chiSo = json_decode($chuoiChiSo, true);

// Khởi tạo các biến để lưu giá trị chỉ số
$sucManh = $tiemNang = $mau = $theLuc = $sucDanhGoc = $giapGoc = $chiMang = '';

// Lấy danh sách các chỉ số cần lấy
$chiSoCanLay = array_intersect_key($chiSo, array_flip(['1', '2', '5', '6', '7', '8', '9']));

// Hiển thị các chỉ số sư phụ nếu có
foreach ($chiSoCanLay as $key => $value) {
  switch ($key) {
    case '1':
      $sucManh = number_format($value);
      break;

    case '2':
      $tiemNang = number_format($value);
      break;

    case '5':
      $mau = number_format($value);
      break;

    case '6':
      $theLuc = number_format($value);
      break;

    case '7':
      $sucDanhGoc = number_format($value);
      break;

    case '8':
      $giapGoc = number_format($value);
      break;

    case '9':
      $chiMang = number_format($value);
      break;
  }
}
?>


<main>
  <div style="background: #ffe9b8; border-radius: 7px; box-shadow: 0px 2px 5px black;" class="pb-1">
    <div class="text-center col-lg-6 col-md-10" style="margin: auto; padding: 30px;">
      <div class="user_name">
        <?php if ($_SESSION['logger']['username']) { ?>
          <center>
            <?php
            if (!$row["gender"]) {
              echo '<img src="../public/images/icon/3.png" />';
            } else {
              echo '<img style="width: 100px;"src="../public/images/icon/' . $row["gender"] . '.png" />';
            }
            ?>
            <br>
          </center>

          <b style="color: #ff0000">
            <?php echo $_SESSION['logger']['username'] ?>
          </b> <br><i class="fa fa-money"></i>
          <b>
            <?= number_format($roww["vnd"]); ?> Coin
          </b>
          <br>
          <a style="font-weight: bold;color: #8C52FF">Học Viện Nro</a>
        <?php } else { ?>
        <?php } ?>
      </div>
      <br>
      <table class="table table-hover table-custom  ">
        <thead>
          <tr>
            <th>Tài Khoản</th>
          </tr>
        </thead>
        <tbody>

          <tr>
            <td>Username</td>
            <td>:
              <?php echo '<span style="color: red; font-weight: bold;">'. $_username . '</span>' ?>
            </td>
          </tr>
          <tr>
            <td>Số Coin</td>
            <td>:
              <?php echo '<span style="font-weight: bold;">'. number_format($row['vnd']) . ' Coin'.'</span>' ?>
            </td>
          </tr>
          <tr>
            <td>Thành viên</td>
            <td>:
              <?php echo ($row['active'] == 0 ?
                '<span style="color: red; font-weight: bold;">Chưa mở thành viên</span>'
                : '<span style="color: green; font-weight: bold;">Đã mở thành viên</span>') ?>
            </td>
          </tr>
        </tbody>
      </table>


      <table class="table table-hover table-custom">
        <thead>
          <tr>
            <th>Nhân Vật</th>
          </tr>
        </thead>
        <tbody>

          <tr>
            <td>Tên</td>
            <td>:
              <?php echo '<span style="color: red; font-weight: bold;">'. $tenNhanVat . '</span>' ?>
            </td>
          </tr>
          <tr>
            <td>Hành Tinh</td>
            <td>:
              <?php echo ($gioiTinhNhanVat == '0' ? 'Trái Đất' : ($gioiTinhNhanVat == '1' ? 'Namec' : ($gioiTinhNhanVat == '2' ? 'Xayda' : 'Không xác định'))) ?>
            </td>
          </tr>
          <tr>
            <td>Nhiệm Vụ</td>
            <td>:
              <?php echo $tenNhiemVu ?>
            </td>
          </tr>
        </tbody>
      </table>

      <table class="table table-hover table-custom  ">
        <thead>
          <tr>
            <th>Chỉ số Hiện Tại</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td><a>Sức mạnh</a></td>
            <td>:
              <?php echo $sucManh ?>
            </td>
          </tr>
          <tr>
            <td><a>Tiềm năng</a></td>
            <td>:
              <?php echo $tiemNang ?>
            </td>
          </tr>
          <tr>
            <td><a>HP Gốc</a></td>
            <td>:
              <?php echo $mau ?>
            </td>
          </tr>
          <tr>
            <td><a>KI Gốc</a></td>
            <td>:
              <?php echo $theLuc ?>
            </td>
          </tr>
          <tr>
            <td><a>Sức Đánh Gốc</a></td>
            <td>:
              <?php echo $sucDanhGoc ?>
            </td>
          </tr>
          <tr>
            <td><a>Giáp Gốc</a></td>
            <td>:
              <?php echo $giapGoc ?>
            </td>
          </tr>
          <tr>
            <td><a>Chí Mạng Gốc</a></td>
            <td>:
              <?php echo $chiMang ?>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div style="line-height: 15px;font-size: 12px;padding-right: 5px;margin-bottom: 8px;padding-top: 2px;"
      class="text-center">
      <span class="text-black" style="vertical-align: middle;"><i class="fa fa-spinner"></i> Thông tin sẽ được cập nhật
        ngay sau khi bạn thoát Game</span>
    </div>
  </div>
</main>

<?php require_once ('../core/end.php'); ?>