<?php
session_start();
include ('cauhinh.php');
include ('config.php');

$linkadmin = 'admin.php';

if (isset ($_POST['tang'])) {
    $error = array();
    $showMess = false;
    $row = array();

    if (!$error) {

        function validate($data)
        {
            $data = trim($data);
            $data = stripslashes($data);
            $data = htmlspecialchars($data);
            return $data;
        }

        $checktk = validate($_POST['checktk']);
        $checktk = strtolower($checktk);
        $hongngoc = validate($_POST['hongngoc']);
        $hongngoc = strtolower($hongngoc);


        $check = "SELECT * FROM account WHERE username = '$checktk'";
        $result = mysqli_query($config, $check);
        $row = mysqli_fetch_array($result);


        if (mysqli_num_rows($result) == 1) {
            $new_vnd_congf = $row['tongnap'] + $hongngoc;
            $tangk = "UPDATE account SET tongnap ='$new_vnd_congf' WHERE username = '$checktk'";
            $result_tangk = mysqli_query($config, $tangk);
			
            $new_vnd_cong = $row['vnd'] + $hongngoc;
            $tang = "UPDATE account SET vnd ='$new_vnd_cong' WHERE username = '$checktk'";
            $result_tang = mysqli_query($config, $tang);
            $hn = $row['vnd'] + $hongngoc;
            header("Location: $linkadmin?error=CỘNG THÀNH CÔNG $hongngoc VNĐ cho tài khoản: $checktk ! Tổng: $hn VNĐ.");
            exit();
        } else {
            header("Location: $linkadmin?error=Tài khoản $checktk không tồn tại !");
            exit();
        }
    }
}


if (isset ($_POST['giam'])) {
    $error = array();
    $showMess = false;
    $row = array();

    if (!$error) {

        function validate($data)
        {
            $data = trim($data);
            $data = stripslashes($data);
            $data = htmlspecialchars($data);
            return $data;
        }

        $checktk = validate($_POST['checktk']);
        $checktk = strtolower($checktk);
        $hongngoc = validate($_POST['hongngoc']);
        $hongngoc = strtolower($hongngoc);


        $check = "SELECT * FROM account WHERE username = '$checktk'";
        $result = mysqli_query($config, $check);
        $row = mysqli_fetch_array($result);


        if (mysqli_num_rows($result) == 1) {
            if ($row['vnd'] == 0 || $hongngoc > $row['vnd']) {
                header("Location: $linkadmin?error=Không còn tiển để trừ");
            } else {

                $new_vnd_tru = $row['vnd'] - $hongngoc;
                $giam = "UPDATE account SET vnd ='$new_vnd_tru' WHERE username = '$checktk'";
                $result_giam = mysqli_query($config, $giam);
                $hn = $row['vnd'] - $hongngoc;
                header("Location: $linkadmin?error=TRỪ THÀNH CÔNG $hongngoc VNĐ cho tài khoản: $checktk ! Tổng: $hn VNĐ.");
                exit();


            }
        } else {
            header("Location:$linkadmin?error=Tài khoản $checktk không tồn tại !");
            exit();
        }
    }
}

if (isset ($_POST['khoa'])) {
    $error1 = array();
    $showMess = false;
    $row = array();

    if (!$error1) {

        function validate($data)
        {
            $data = trim($data);
            $data = stripslashes($data);
            $data = htmlspecialchars($data);
            return $data;
        }

        $checktk = validate($_POST['checktk']);
        $checktk = strtolower($checktk);


        $check = "SELECT * FROM account WHERE username = '$checktk'";
        $result = mysqli_query($config, $check);
        $row = mysqli_fetch_array($result);


        if (mysqli_num_rows($result) == 1) {
            $tang = "UPDATE account SET ban = 1 WHERE username = '$checktk'";
            $result_tang = mysqli_query($config, $tang);
            header("Location: $linkadmin?error1=Đã KHÓA thành công tài khoản: $checktk !");
            exit();
        } else {
            header("Location: $linkadmin?error1=Tài khoản $checktk không tồn tại !");
            exit();
        }
    }
}

if (isset ($_POST['mtv'])) {
    $error1 = array();
    $showMess = false;
    $row = array();

    if (!$error4) {

        function validate($data)
        {
            $data = trim($data);
            $data = stripslashes($data);
            $data = htmlspecialchars($data);
            return $data;
        }

        $checktk = validate($_POST['checktk']);
        $checktk = strtolower($checktk);


        $check = "SELECT * FROM account WHERE username = '$checktk'";
        $result = mysqli_query($config, $check);
        $row = mysqli_fetch_array($result);


        if (mysqli_num_rows($result) == 1) {
            $tang = "UPDATE account SET active = 1 WHERE username = '$checktk'";
            $result_tang = mysqli_query($config, $tang);
            header("Location: $linkadmin?error4=Đã MỞ TV thành công tài khoản: $checktk !");
            exit();
        } else {
            header("Location: $linkadmin?error4=Tài khoản $checktk không tồn tại !");
            exit();
        }
    }
}



if (isset ($_POST['mokhoa'])) {
    $error1 = array();
    $showMess = false;
    $row = array();

    if (!$error1) {

        function validate($data)
        {
            $data = trim($data);
            $data = stripslashes($data);
            $data = htmlspecialchars($data);
            return $data;
        }

        $checktk = validate($_POST['checktk']);
        $checktk = strtolower($checktk);


        $check = "SELECT * FROM account WHERE username = '$checktk'";
        $result = mysqli_query($config, $check);
        $row = mysqli_fetch_array($result);


        if (mysqli_num_rows($result) == 1) {
            $tang = "UPDATE account SET ban = 0 WHERE username = '$checktk'";
            $result_tang = mysqli_query($config, $tang);
            header("Location: $linkadmin?error1=Đã MỞ KHÓA thành công tài khoản: $checktk !");
            exit();
        } else {
            header("Location: $linkadmin?error1=Tài khoản $checktk không tồn tại !");
            exit();
        }
    }
}

if (isset ($_POST['khoaip'])) {
    $error1 = array();
    $showMess = false;
    $row = array();

    if (!$error3) {

        function validate($data)
        {
            $data = trim($data);
            $data = stripslashes($data);
            $data = htmlspecialchars($data);
            return $data;
        }

        $checktk = validate($_POST['checktk']);
        $checktk = strtolower($checktk);


        $check = "SELECT * FROM account WHERE ip_address = '$checktk'";
        $result = mysqli_query($config, $check);
        $row = mysqli_fetch_array($result);


        if (mysqli_num_rows($result) == 1) {
            $tang = "UPDATE account SET ban = 1 WHERE ip_address = '$checktk'";
            $result_tang = mysqli_query($config, $tang);
            header("Location: $linkadmin?error3=Đã KHÓA thành công tất cả tài khoản có IP: $checktk !");
            exit();
        } else {
            header("Location: $linkadmin?error3=Địa chỉ IP: $checktk không tồn tại !");
            exit();
        }
    }
}

if (isset ($_POST['khoatatca'])) {
    $error2 = array();
    $showMess = false;

    if (!$error2) {

        $tang1 = "UPDATE account 
					SET ban = 1 
					WHERE ip_address IN (SELECT ip_address FROM account WHERE ban = 0 AND ip_address != '127.0.0.1' GROUP BY ip_address HAVING COUNT(*) > 5) 
					AND ban = 0;";
        $result_tang = mysqli_query($config, $tang1);
        header("Location: $linkadmin?error2=Đã KHÓA thành công tất cảt tài khoản clone!");
        exit();

    }
}





?>
<!DOCTYPE html>
<html>

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin</title>

    <!---custom css link--->
    <link rel="stylesheet" type="text/css" href="css/style.css">
    <!---custom icon--->
    <link rel="icon" type="image/png" href="img/logo_nro.png" />
    <!---boxicons link--->
    <link rel="stylesheet" href="https://unpkg.com/boxicons@latest/css/boxicons.min.css">

    <!---remixicons link--->
    <link href="https://cdn.jsdelivr.net/npm/remixicon@2.5.0/fonts/remixicon.css" rel="stylesheet">

    <!---google fonts link--->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Permanent+Marker&family=Poppins:wght@300;400;500;600;700;800;900&display=swap"
        rel="stylesheet">
    <style>
        form {
            background-color: #fff;
            padding: 50px;
            max-width: 600px;
            margin: 0 auto;
            border-radius: 30px;
            box-shadow: 0 0 5px rgba(0, 0, 0, 1);

        }

        h2 {
            text-align: center;
            margin-bottom: 25px;
            color: #000000;
            text-transform: uppercase;
            letter-spacing: 2px;
        }

        .hero-text button {
            display: inline-block;
            color: white;
            background: rgba(255, 154, 0, 1);
            border: 1px solid transparent;
            padding: 12px 30px;
            line-height: 1.4;
            font-size: 14px;
            font-weight: 500;
            border-radius: 30px;
            text-transform: uppercase;
            transition: all .55s ease;
        }

        label {
            display: block;
            margin-bottom: 10px;
        }

        input[type="text"],
        input[type="password"] {
            width: 100%;
            padding: 10px;
            border-radius: 30px;
            border: 1px solid #ccc;
            margin-bottom: 20px;
            box-sizing: border-box;
            box-shadow: 0 2px 2px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
        }

        input[type="text"]:focus,
        input[type="password"]:focus {
            transform: translateY(-5px);
            box-shadow: 0 4px 4px rgba(0, 0, 0, 0.2);
        }

        .logo_icon {
            width: 150px;
            display: block;
            margin: 0 auto;
            margin-bottom: 20px;
        }

        .button-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .red-button {
            background-color: red;
            color: white;
            border: none;
            padding: 5px 10px;
            font-size: 1em;
            border-radius: 3px;
            cursor: pointer;
            float: right;
        }

        .custom-table {
            border-collapse: collapse;
            width: 100%;
            margin: 0 auto;
            font-family: Arial, sans-serif;
            font-size: 14px;

        }

        .custom-table th,
        .custom-table td {
            padding: 10px;
            text-align: center;
        }

        .custom-table th {
            font-weight: bold;
        }

        .custom-table tbody tr:hover {
            background-color: #f5f5f5;
        }

        table {
            border-collapse: collapse;
            width: 100%;
        }

        th,
        td {
            text-align: left;
            padding: 8px;
            border: 1px solid #ddd;
        }

        th {
            background-color: #f2f2f2;
        }
    </style>
    </style>

</head>

<body>



    <div class="hero-text" ">
            <form method=" POST" style="padding-top:10px;padding-bottom:10px;>
                <?php
                $data = "SELECT SUM(player.tong_nap) AS total_vnd
						FROM player INNER JOIN account ON account.id = player.account_id
						WHERE account.is_admin = 0 
						AND player.name != 'toansoi';"; // <<<<<<<<<<<<============= Buff tài khoản thì thêm vào đây tự trừ ra
                $result = mysqli_query($config, $data);
                $row = mysqli_fetch_array($result);
                
                $dataf = "SELECT SUM(amount) AS atm FROM mb_bank WHERE status = 1;";
                $resultf = mysqli_query($config, $dataf);
                $rowf = mysqli_fetch_array($resultf);

                $datak = "SELECT COUNT(*) as total FROM account WHERE active = 1 AND account.is_admin = 0";
                $resultk = mysqli_query($config, $datak);
                $rowk = mysqli_fetch_array($resultk);
                $totalActiveAccounts = $rowk['total'];

                $data1 = "SELECT SUM(amount) AS total_the FROM trans_log WHERE status = 1;";
                $result1 = mysqli_query($config, $data1);
                $row1 = mysqli_fetch_array($result1);
                
                $darut = 0;
                $tongdoangthu = ($row1['total_the'] * 80/100 ) + $rowf['atm'] - $darut
                

                    ?>
                <input type=" hidden" name="_token" value="JEGpj39vMoqzUAPDoHWTY8Y4jJiy4t0mhPST9nds">
				<h2 style="color: red;">Quản lý Server</h2>
        <center>
            <h3>Tổng Doanh Thu: <a style="color:#73F400;">+
                    <?php echo number_format($tongdoangthu) ?><sup>đ</sup>
                </a></h3>
        </center>
        <center>
            <h3>Tổng Thẻ Cào: <a style="color:#0CC0DF;">+
                    <?php echo number_format($row1['total_the'] * 80 / 100) ?><sup>đ</sup>
                </a></h3>
        </center>
        <center>
            <h3>Tổng ATM: <a style="color:#FF3131;">+
                    <?php echo number_format($rowf['atm']) ?><sup>đ</sup>
                </a></h3>
        </center>
        <!-- <center>
            <h3>Hiện Còn: <a style="color:#73F400;">+
                    <?php echo number_format($tongdoangthu) ?><sup>đ</sup>
                </a></h3>
        </center>
        <center>
            <h3>Đã Rút: <a style="color:#FF3131;">-
                    <?php echo number_format($darut) ?><sup>đ</sup>
                </a></h3>
        </center> -->
        <center>
            <h3>Tổng Tài khoản MTV: <a style="color:#0CC0DF;">
                    <?php echo $totalActiveAccounts ?> Tài khoản
                </a></h3>
        </center>
        </form>
    </div>



    <br>
    <br>

    <div class="hero-text">
        <form method="POST">
            <input type="hidden" name="_token" value="JEGpj39vMoqzUAPDoHWTY8Y4jJiy4t0mhPST9nds">
            <h2>Buff VND</h2>
            <div class="mb-3 form-group">
                <label for="username" class="form-label">Tên tài khoản:</label>
                <input type="text" class="form-control" name="checktk" required>
            </div>
            <div class="mb-3 form-group">
                <label for="username" class="form-label">Số VNĐ:</label>
                <input type="text" class="form-control" name="hongngoc" required>
            </div>
            <div style="  display: flex;justify-content: center;align-items: center; ">
                <button type="submit" style="margin-right: 10px; background: green;" class="submit" name="tang">+
                    VNĐ</button>
                <button type="submit" style="margin-left: 10px; background: red;" class="submit" name="giam">-
                    VNĐ</button>
            </div>
            <div style="margin-top: 20px; padding:20px">
                <?php if (isset ($_GET['error'])) { ?>
                    <div class="alert alert-danger w-50 p-3 " role="alert">
                        <?php echo $_GET['error']; ?>
                    </div>
                <?php } ?>
            </div>

        </form>
    </div>



    <br>
    <br>

    <div class="hero-text">
        <form method="POST">
            <input type="hidden" name="_token" value="JEGpj39vMoqzUAPDoHWTY8Y4jJiy4t0mhPST9nds">
            <h2>Mở Thành viên</h2>
            <div class="mb-3 form-group">
                <label for="username" class="form-label">Tên tài khoản:</label>
                <input type="text" class="form-control" name="checktk" required>
            </div>
            <br>
            <br>
            <div style="  display: flex;justify-content: center;align-items: center; ">
                <button type="submit" style="margin-right: 10px; background: yellow;color:black;" class="submit"
                    name="mtv">Mở Thành Viên</button>
            </div>
            <div style="margin-top: 20px; padding:20px">
                <?php if (isset ($_GET['error4'])) { ?>
                    <div class="alert alert-danger w-50 p-3 " role="alert">
                        <?php echo $_GET['error4']; ?>
                    </div>
                <?php } ?>
            </div>

        </form>
    </div>

    <br>
    <br>

    <div class="hero-text">
        <form method="POST">
            <input type="hidden" name="_token" value="JEGpj39vMoqzUAPDoHWTY8Y4jJiy4t0mhPST9nds">
            <h2>Ban Account</h2>
            <div class="mb-3 form-group">
                <label for="username" class="form-label">Tên tài khoản:</label>
                <input type="text" class="form-control" name="checktk" required>
            </div>
            <br>
            <br>
            <div style="  display: flex;justify-content: center;align-items: center; ">
                <button type="submit" style="margin-right: 10px; background: green;" class="submit" name="mokhoa">Mở
                    khóa</button>
                <button type="submit" style="margin-left: 10px; background: red;" class="submit"
                    name="khoa">Khóa</button>
            </div>
            <div style="margin-top: 20px; padding:20px">
                <?php if (isset ($_GET['error1'])) { ?>
                    <div class="alert alert-danger w-50 p-3 " role="alert">
                        <?php echo $_GET['error1']; ?>
                    </div>
                <?php } ?>
            </div>

        </form>
    </div>

    <br>
    <br>

    <form method="POST">

        <h4 class="card-header" style="text-align:center;color:var(--main-color)">
            Kiểm Tra Thỏi Vàng
        </h4>
        <div class="table-responsive">
            <table class="table table-hover table-custom  " style="text-align: center;">
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>Tên</th>
                        <th>Hiện Có</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $query = "SELECT name, gender, player.thoi_vang AS tv
									FROM player
									INNER JOIN account ON account.id = player.account_id
									WHERE account.is_admin = 0 AND account.ban = 0 
									ORDER BY player.thoi_vang DESC
									LIMIT 20;";
                    $result = $config->query($query);
                    $stt = 1;
                    if ($result === false) {
                        echo 'Lỗi truy vấn SQL: ' . $config->error;
                    } elseif ($result->num_rows > 0) {
                        while ($row = $result->fetch_assoc()) {
                            echo '<tr>
									  <td>' . $stt . '</td>
									  <td>' . $row['name'] . '</td>
									  <td>' . number_format($row['tv'], 0, '.', '.') . '	[Thỏi vàng]</td>
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
    </form>

    <br>
    <br>

    <form method="POST">

        <h4 class="card-header" style="text-align:center;color:var(--main-color)">
            Kiểm Tra Hồng Ngọc
        </h4>
        <div class="table-responsive">
            <table class="table table-hover table-custom  " style="text-align: center;">
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>Tên</th>
                        <th>Hiện Có</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $query = "SELECT name, gender, CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_inventory, ',', 3), ',', -1) AS UNSIGNED) AS hn
									FROM player
									INNER JOIN account ON account.id = player.account_id
									WHERE account.is_admin = 0 AND account.ban = 0 
									ORDER BY CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_inventory, ',', 3), ',', -1) AS UNSIGNED) DESC
									LIMIT 10;";
                    $result = $config->query($query);
                    $stt = 1;
                    if ($result === false) {
                        echo 'Lỗi truy vấn SQL: ' . $config->error;
                    } elseif ($result->num_rows > 0) {
                        while ($row = $result->fetch_assoc()) {
                            echo '<tr>
									  <td>' . $stt . '</td>
									  <td>' . $row['name'] . '</td>
									  <td>' . number_format($row['hn'], 0, '.', '.') . '	[Hồng Ngọc]</td>
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
    </form>

    <br>
    <br>

    <div class="hero-text">
        <form method="POST">
            <input type="hidden" name="_token" value="JEGpj39vMoqzUAPDoHWTY8Y4jJiy4t0mhPST9nds">
            <h2>Ban Theo 1 IP</h2>
            <div class="mb-3 form-group">
                <label for="username" class="form-label">Nhập IP cần ban:</label>
                <input type="text" class="form-control" name="checktk" required>
            </div>
            <br>
            <br>
            <div style="  display: flex;justify-content: center;align-items: center; ">
                <button type="submit" style="margin-left: 10px; background: red;" class="submit" name="khoaip">Khóa tất
                    cả</button>
            </div>
            <div style="margin-top: 20px; padding:20px">
                <?php if (isset ($_GET['error3'])) { ?>
                    <div class="alert alert-danger w-50 p-3 " role="alert">
                        <?php echo $_GET['error3']; ?>
                    </div>
                <?php } ?>
            </div>

        </form>
    </div>

    <br>
    <br>
    <form method="POST">

        <h4 class="card-header" style="text-align:center;color:var(--main-color)">
            Check IP Clone
        </h4>
        <div class="table-responsive">
            <table class="table table-hover table-bordered " style="text-align: center;">
                <thead class="thead-dark">
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>MTV</th>
                        <th>IP Address</th>


                    </tr>
                </thead>
                <tbody>
                    <?php
                    $countTop1 = 0;
                    $data = "SELECT * FROM account WHERE ip_address IN (
							SELECT ip_address FROM account WHERE ban = 0 AND ip_address != '127.0.0.1' GROUP BY ip_address HAVING COUNT(*) > 5
						) AND ban = 0 ORDER BY ip_address;";
                    $result = mysqli_query($config, $data);
                    while ($row = mysqli_fetch_array($result)) {
                        ?>
                        <tr>
                            <?php $countTop1++; ?>
                            <td>
                                <?php echo htmlspecialchars($row['id']); ?>
                            </td>
                            <td>
                                <?php echo htmlspecialchars($row['username']); ?>
                            </td>
                            <td>
                                <?php echo htmlspecialchars($row['active']); ?>
                            </td>
                            <td>
                                <?php echo htmlspecialchars($row['ip_address']); ?>
                            </td>
                        </tr>
                    <?php } ?>
                    <tr>
                </tbody>
                <div class="button-container">
                    <h5> Đang có:
                        <?php echo $countTop1; ?> tài khoản
                    </h5>
                    <button class="red-button" type="submit" class="submit" name="khoatatca">Khóa tất cả</button>
                </div>
                <?php if (isset ($_GET['error2'])) { ?>
                    <div class="alert alert-danger w-50 p-3 " role="alert">
                        <?php echo $_GET['error2']; ?>
                    </div>
                <?php } ?>
            </table>
        </div>
    </form>

    <br>
    <br>


    </section>


    <!---######################################################--->


    <!---######################################################--->
    <!---scrollreveal effect--->
    <script src="https://unpkg.com/scrollreveal"></script>

    <!---custom js link--->
    <script src="js/script.js"></script>



</body>

</html>

<div>