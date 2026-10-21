<?php
require_once('../core/config.php');
require_once('../core/head.php');

session_start();
if (!isset($_SESSION['logger']['username'])) {
    die("Bạn chưa đăng nhập.");
}

$username = $_SESSION['logger']['username'];
$sql = "SELECT id FROM account WHERE username = '$username'";
$result = $config->query($sql);
if ($result->num_rows > 0) {
    $row_hvd = $result->fetch_assoc();
    $user_id = $row_hvd["id"];
}

$thongbao = null;

if (isset($_POST['submit'])) {
    if (empty($_POST['telco']) || empty($_POST['amount']) || empty($_POST['serial']) || empty($_POST['code'])) {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Bạn cần nhập đầy đủ thông tin!</span>';
    } else {
        // Thực hiện nạp thẻ
        $partner_id = $partner_id_config; // TẠO Ở DOITHE1S
        $partner_key = $partner_key_config;  // TẠO Ở DOITHE1S
        $dataPost = array();
        $dataPost['request_id'] = rand(100000000, 999999999); // Mã đơn hàng
        $dataPost['code'] = $_POST['code'];
        $dataPost['partner_id'] = $partner_id;
        $dataPost['serial'] = $_POST['serial'];
        $dataPost['telco'] = $_POST['telco'];
        $dataPost['amount'] = $_POST['amount'];
        $dataPost['command'] = 'charging';  // NẠP THẺ
        $dataPost['sign'] = md5($partner_key . $_POST['code'] . $_POST['serial']); // Mã hóa chữ ký

        $data = http_build_query($dataPost);
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://azcard.vn/chargingws/v2');
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $result = curl_exec($ch);
        curl_close($ch);

        $obj = json_decode($result);
        if ($obj->status == 99) {
            // Gửi thẻ thành công, đợi duyệt
            $thongbao = '<span style="color: orange; font-size: 12px; font-weight: bold;">' . $obj->message . '</span>';
            $insert_query = "INSERT INTO napthe (user_nap, telco, serial, code, amount, status) 
                                     VALUES ('$user_id', '{$_POST['telco']}', '{$_POST['serial']}', '{$_POST['code']}', '{$_POST['amount']}', 99)";
            mysqli_query($config, $insert_query);
        } else {
            // Các trạng thái khác
            $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">' . $obj->message . '</span>';
        }
    }
}

// Phân trang
$page = isset($_GET['page']) ? intval($_GET['page']) : 1;
$recordsPerPage = 5;
$startFrom = ($page - 1) * $recordsPerPage;
$query = "SELECT * FROM napthe WHERE user_nap = $user_id LIMIT $startFrom, $recordsPerPage";
$result = mysqli_query($config, $query);
$hasData = false;
?>
<main>
    <div class="p-1 mt-1 ibox-content" style="border-radius: 7px; box-shadow: 0px 0px 5px black;">
        <div class="card">
            <div class="card-header">
                <b>Nạp Tiền Thẻ Cào(AUTO)</b>
                <br>
                <b class="badge" style="background-color: rgb(243, 146, 101);">Tỉ giá quy đổi: 10.000đ = 8.000đ AUTO</b>
                <a href="/atm_bank"><b class="badge" style="background-color: rgb(101, 160, 243);">Nạp Tiền MBank AUTO <b>10k = 12k</b> </b></a>
            </div>
            <div class="card-body">
                <form method="post" action="/napthe">
                    <?= $thongbao; ?>
                    <div class="form-group">
                        <label for="telco"><b>Loại thẻ:</b></label>
                        <select class="form-control mt-1" name="telco" required>
                            <option value="">Chọn loại thẻ</option>
                            <option value="VIETTEL">Viettel</option>
                            <option value="VINAPHONE">Vinaphone</option>
                            <option value="MOBIFONE">Mobifone</option>
                        </select>
                    </div>
                    <div class="form-group mt-2">
                        <label><b>Mã thẻ:</b></label>
                        <input class="form-control mt-1" type="number" name="code" placeholder="Mã thẻ" required>
                    </div>
                    <div class="form-group mt-2">
                        <label><b>Seri thẻ:</b></label>
                        <input class="form-control mt-1" type="number" name="serial" placeholder="Seri thẻ" required>
                    </div>
                    <div class="form-group mt-2">
                        <label><b>Mệnh giá thẻ:</b></label>
                        <select class="form-control mt-1" name="amount" required>
                            <option value="">Chọn mệnh giá thẻ</option>
                            <option value="10000">10,000 VNĐ</option>
                            <option value="20000">20,000 VNĐ</option>
                            <option value="30000">30,000 VNĐ</option>
                            <option value="50000">50,000 VNĐ</option>
                            <option value="100000">100,000 VNĐ</option>
                            <option value="200000">200,000 VNĐ</option>
                            <option value="500000">500,000 VNĐ</option>
                        </select>
                    </div>

                    <div class="form-group mt-2">
                        <button name="submit" type="submit" class="btn btn-action text-white" style="border-radius: 7px;">Gửi thẻ</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <!-- Hiển thị lịch sử nạp thẻ -->
    <div class="table-responsive">
        <table class="table table-hover table-nowrap">
            <tbody>
                <tr>
                    <th scope="col">STT</th>
                    <th scope="col">Nhà Mạng</th>
                    <th scope="col">Seri</th>
                    <th scope="col">Mã</th>
                    <th scope="col">Mệnh Giá</th>
                    <th scope="col">Trạng Thái</th>
                </tr>
                <?php while ($row = mysqli_fetch_assoc($result)) { ?>
                    <tr>
                        <td><b>#<?= $row['id']; ?></b></td>
                        <td><b><?= $row['telco']; ?></b></td>
                        <td><?= $row['serial']; ?></td>
                        <td><?= $row['code']; ?></td>
                        <td><?= number_format($row['amount']); ?> VNĐ</td>
                        <td><b>
                                <?php
                                if ($row["status"] == 99) {
                                    echo '<font color="orange">Thẻ Chờ</font>';
                                } elseif ($row["status"] == 1) {
                                    echo '<font color="green">Thẻ Đúng</font>';
                                } elseif ($row["status"] == 3) {
                                    echo '<font color="red">Thẻ Sai</font>';
                                } elseif ($row["status"] == 2) {
                                    echo '<font color="red">Thẻ Sai Mệnh Giá</font>';
                                }
                                ?>
                            </b></td>
                    </tr>
                <?php } ?>
            </tbody>
        </table>
        <?php
        $totalPages = ceil(mysqli_num_rows(mysqli_query($config, "SELECT * FROM napthe WHERE user_nap = $user_id")) / $recordsPerPage);
        ?>
        <div class="pagination">
            <?php if ($page > 1) {
                echo '<a href="napthe.php?page=' . ($page - 1) . '"><< Trước</a>';
            } ?>
            <?php for ($i = 1; $i <= $totalPages; $i++) {
                echo '<a href="napthe.php?page=' . $i . '" class="' . ($i == $page ? 'active' : '') . '">' . $i . '</a>';
            } ?>
            <?php if ($page < $totalPages) {
                echo '<a href="napthe.php?page=' . ($page + 1) . '">Sau >></a>';
            } ?>
        </div>
    </div>
</main>

<?php require_once('../core/end.php'); ?>