<?php
require_once('../core/config.php');
require_once('../core/head.php');
$thongbao = null;
$thongbao_admin = null;
// Kiểm tra xem người dùng có phải là quản trị viên hay không
$sql_admin = "SELECT is_admin FROM account WHERE username = '$username'";
$result_admin = $config->query($sql_admin);
$row_admin = ($result_admin && $result_admin->num_rows > 0) ? $result_admin->fetch_assoc() : null;
// Kiểm tra xem người dùng có hoạt động hay không
$sql_active = "SELECT active FROM account WHERE username = '$username'";
$result_active = $config->query($sql_active);
$row_active = ($result_active && $result_active->num_rows > 0) ? $result_active->fetch_assoc() : null;
// Lấy ID tài khoản
$sql = "SELECT id FROM account WHERE username = '$username'";
$result = $config->query($sql);
if ($result->num_rows > 0) {
  $row_hvd = $result->fetch_assoc();
  $accountId = $row_hvd["id"];
  // Kiểm tra sự tồn tại của ID tài khoản trong bảng player
  $sql_check_player = "SELECT COUNT(*) as player_count FROM player WHERE account_id = '$accountId'";
  $result_check_player = $config->query($sql_check_player);
  $row_check_player = ($result_check_player && $result_check_player->num_rows > 0) ? $result_check_player->fetch_assoc() : null;
  $player_exists = ($row_check_player && $row_check_player['player_count'] > 0);
  // Lấy giới tính từ bảng player
  $sql_gender = "SELECT head FROM player WHERE account_id = $accountId";
  $result_gender = $config->query($sql_gender);
  $row_gender = ($result_gender && $result_gender->num_rows > 0) ? $result_gender->fetch_assoc() : null;
}
// Xử lý khi người dùng gửi bình luận
if (isset($_POST['submit']) && isset($_POST['comment'])) {
  $comment = $_POST['comment'];
  $baiviet_id = $_GET['id'];
  if (empty($comment)) {
    $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Vui lòng nhập nội dung bình luận!</span>';
  } else {
    // Kiểm tra từ cấm trong nội dung bình luận
    $containsCensoredWords = false;
    foreach ($censoredWords as $word) {
      if (stripos($comment, $word) !== false) {
        $containsCensoredWords = true;
        break;
      }
    }

    if ($containsCensoredWords) {
      $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Vui lòng không sử dụng từ cấm trong bình luận!</span>';
    } else {
      $sql_comment = "INSERT INTO cmt_hoangvietdung (baiviet_id, khach_id, noidung, time) VALUES ('$baiviet_id', '$accountId', '$comment', " . time() . ")";
      $result_comment = $config->query($sql_comment);
      if ($result_comment) {
        $thongbao = '<span style="color: green; font-size: 12px; font-weight: bold;">Bình luận thành công!</span>';
      } else {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Đã xảy ra lỗi!</span>';
      }
      if (!$player_exists) {
        $thongbao = '<span style="color: red; font-size: 12px; font-weight: bold;">Hãy tạo nhân vật trước khi đăng bài!</span>';
      }
    }
  }
}
if (isset($_GET['id'])) {
  $id_delete = $_GET['id'];
  if ($row_admin['is_admin'] == 1) {
    if (isset($_GET['delete'])) {
      $sql_delete = "DELETE FROM baiviet WHERE id = $id_delete";

      if ($config->query($sql_delete) === TRUE) {
        echo '<script>window.location.href = "/forum";</script>';
      } else {
        $thongbao_admin =  '<span style="color: red; font-size: 12px; font-weight: bold;">Đã xảy ra lỗi!</span>';
      }
    }
  }
} else {
  $thongbao_admin = '<span style="color: red; font-size: 12px; font-weight: bold;">Bạn không có quyền truy cập!</span>';
}
// Truy vấn để lấy danh sách bài viết
$sql = "SELECT b.id, b.tieude, b.top_baiviet, b.new, b.noidung, b.time, a.username, p.head
    FROM baiviet_hoangvietdung AS b
    INNER JOIN account AS a ON b.account_id = a.id
    LEFT JOIN player AS p ON p.account_id = a.id";
$result = $config->query($sql);
$rows = array();
$topPosts = array();
if ($result->num_rows > 0) {
  while ($row = $result->fetch_assoc()) {
    $rows[] = $row;

    if ($row['top_baiviet'] == 1) {
      $topPosts[] = $row;
    }
  }
}
// Sắp xếp mảng $rows theo trường 'time' giảm dần
usort($rows, function ($a, $b) {
  return strtotime($b['time']) - strtotime($a['time']);
});
// Lấy giá trị trang hiện tại từ URL
$currentpage = isset($_GET['page']) ? $_GET['page'] : 1;

// Số bài viết hiển thị trên mỗi trang
$postsPerPage = 10;

// Tính toán giá trị startIndex và endIndex
$totalPosts = count($rows);
$totalPages = ceil($totalPosts / $postsPerPage);

$startIndex = ($currentpage - 1) * $postsPerPage;
$endIndex = $startIndex + $postsPerPage;

// Giới hạn giá trị startIndex và endIndex
$startIndex = max(0, $startIndex);
$endIndex = min($totalPosts, $endIndex);

// Lấy dữ liệu bài viết phù hợp với trang hiện tại
$displayedPosts = array_slice($rows, $startIndex, $endIndex - $startIndex);

$endIndex = $startIndex + $postsPerPage;
if (isset($_GET['id']) && !empty($rows)) {
  $id = $_GET['id'];

  foreach ($rows as $row) {
    if ($row['id'] == $id) {
?>
      <main>
        <div class="mt-1 alert alert-warning" style="background: #ffe8d1; border-radius: 7px; box-shadow: black 0px 0px 5px;">
          <div class="alert alert-danger" style="background: #ffe8d1; border-radius: 7px;">
            <div class="col">
              <center><?= $thongbao_admin; ?></center>
              <table cellpadding="0" cellspacing="0" width="100%" style="font-size: 13px;">
                <tbody>
                  <tr>
                    <td width="60px;" style="vertical-align: top;">
                      <div class="text-center" style="margin-left: -10px;">
                        <?php if ($row['top_baiviet'] == 1) { ?>
                          <img src="../hit/images/icon/admin.png" width="32" /><br>
                          <div style="font-size: 9px; padding-top: 5px">
                            <b style="color: red;">Admin</b>
                          </div>
                        <?php } else { ?>
                          <?php
                          if ($row["head"] == 28) {
                            echo '<img src="../hit/images/icon/28.png" width="32" />';
                          } elseif ($row["head"] == 27) {
                            echo '<img src="../hit/images/icon/27.png" width="32" />';
                          } elseif ($row["head"] == 6) {
                            echo '<img src="../hit/images/icon/6.png" width="32" />';
                          } elseif ($row["head"] == 64) {
                            echo '<img src="../hit/images/icon/64.png" width="32" />';
                          } elseif ($row["head"] == 31) {
                            echo '<img src="../hit/images/icon/31.png" width="32" />';
                          } elseif ($row["head"] == 30) {
                            echo '<img src="../hit/images/icon/30.png" width="32" />';
                          } elseif ($row["head"] == 9) {
                            echo '<img src="../hit/images/icon/9.png" width="32" />';
                          } elseif ($row["head"] == 29) {
                            echo '<img src="../hit/images/icon/29.png" width="32" />';
                          } elseif ($row["head"] == 32) {
                            echo '<img src="../hit/images/icon/32.png" width="32" />';
                          } else {
                            echo '<img src="../hit/images/icon/6.png" width="32" />';
                          }
                          ?>
                          <br>
                          <div style="font-size: 9px; padding-top: 5px;">
                            <b style="color: blue;"><?= $row['username']; ?></b>
                          </div>
                        <?php } ?>
                      </div>
                    </td>
                    <td class="bg bg-light" style="background: #ffe8d1; border-radius: 7px;">
                      <div class="row" style="font-size: 9px; padding: 5px 7px;">
                        <div class="col">
                          <span><?= duxng_time($row['time']); ?></span>
                        </div>
                        <div class="col text-right">
                          <?php if ($row_admin['is_admin'] == 1) { ?>
                            <span><b>[<a href="?id=<?= $id_delete; ?>&delete=1">Xoá Bài Viết</a>]</b></span>
                          <?php } ?>
                        </div>
                      </div>
                      <hr id="custom-hr2">
                      <div class="row" style="padding: 0px 7px 10px;">
                        <div class="col">
                          <?php if ($row['top_baiviet'] == 1) { ?>
                            <span><a style="color:orange" class="alert-link text-decoration-none"><?= $row['tieude']; ?><a></span>
                          <?php } else { ?>
                            <span><a style="color:blue" class="alert-link text-decoration-none"><?= $row['tieude']; ?><a></span>
                          <?php } ?>
                          <a>
                            <br>
                            <span><?= $row['noidung']; ?></span>
                          </a>
                        </div>
                        <a></a>
                      </div>
                      <a></a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="alert alert-danger" style="background: #ffe8d1; border-radius: 7px;">
            <?php
            $id = $_GET['id'];

            // Xử lý yêu cầu xóa comment
            if (isset($_GET['delete_cmt']) && isset($_GET['confirm_delete']) && $_GET['confirm_delete'] == 1) {
              $deleteId = $_GET['delete_cmt'];

              // Thực hiện kiểm tra và xóa comment tương ứng trong cơ sở dữ liệu
              // ...
              // Cập nhật $cmtSql và thực hiện xóa comment
              $deleteSql = "DELETE FROM cmt_hoangvietdung WHERE id = $deleteId";
              // Thực hiện truy vấn xóa comment
              $config->query($deleteSql);

              // Chuyển hướng trở lại trang hiển thị bài viết sau khi xóa comment
              echo '<script>window.location.href = "/pages/diendan.php?id=' . $id . '";</script>';
              exit();
            }

            // Lấy thông tin comment từ cơ sở dữ liệu
            $cmtSql = "SELECT c.id AS cmt_id, c.noidung AS cmt_noidung, c.time AS cmt_time, a.username, p.head
                FROM cmt_hoangvietdung AS c
                INNER JOIN account AS a ON c.khach_id = a.id
                INNER JOIN player AS p ON p.account_id = a.id
                WHERE c.baiviet_id = $id";

            // Thực hiện truy vấn
            $cmtResult = $config->query($cmtSql);
            if ($cmtResult->num_rows > 0) {
              while ($cmtRow = $cmtResult->fetch_assoc()) {
            ?>
                <table cellpadding="0" cellspacing="0" width="100%" style="font-size: 13px;">
                  <tbody>
                    <tr>
                      <td width="60px;" style="vertical-align: top;">
                        <div class="text-center" style="margin-left: -10px;">
                          <img src="../hit/images/icon/<?= $cmtRow['head']; ?>.png" width="32" /><br>
                          <div style="font-size: 9px; padding-top: 5px;">
                            <b><?= $cmtRow['username']; ?></b>
                          </div>
                        </div>
                      </td>
                      <td class="bg bg-white" style="border-radius: 7px;">
                        <div class="row" style="font-size: 9px; padding: 5px 7px;">
                          <div class="col">
                            <span><?= duxng_time($cmtRow['cmt_time']); ?></span>
                          </div>
                          <div class="col text-right">
                            <?php if ($row_admin['is_admin'] == 1) { ?>
                              <span><b>[<a href="?id=<?= $id ?>&delete_cmt=<?= $cmtRow['cmt_id']; ?>&confirm_delete=1">Xoá CMT</a>]</b></span>
                            <?php } ?>
                          </div>
                        </div>
                        <div class="row" style="padding: 0px 7px 15px;">
                          <div class="col">
                            <span><?= $cmtRow['cmt_noidung']; ?></span>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <br>
            <?php }
            } ?>
          </div>
          <?php if ($_SESSION['logger']['username']) { ?>
            <hr>
            <div class="col">
              <table cellpadding="0" cellspacing="0" width="100%" style="font-size: 13px;">
                <tbody>
                  <tr>
                    <td width="60px;" style="vertical-align: top;">
                      <div class="text-center" style="margin-left: -10px;">
                        <?php
                        if ($row_gender["head"] == 28) {
                          echo '<img src="../hit/images/icon/28.png" width="32" />';
                        } elseif ($row_gender["head"] == 27) {
                          echo '<img src="../hit/images/icon/27.png" width="32" />';
                        } elseif ($row_gender["head"] == 6) {
                          echo '<img src="../hit/images/icon/6.png" width="32" />';
                        } elseif ($row_gender["head"] == 64) {
                          echo '<img src="../hit/images/icon/64.png" width="32" />';
                        } elseif ($row_gender["head"] == 31) {
                          echo '<img src="../hit/images/icon/31.png" width="32" />';
                        } elseif ($row_gender["head"] == 30) {
                          echo '<img src="../hit/images/icon/30.png" width="32" />';
                        } elseif ($row_gender["head"] == 9) {
                          echo '<img src="../hit/images/icon/9.png" width="32" />';
                        } elseif ($row_gender["head"] == 29) {
                          echo '<img src="../hit/images/icon/29.png" width="32" />';
                        } elseif ($row_gender["head"] == 32) {
                          echo '<img src="../hit/images/icon/32.png" width="32" />';
                        } else {
                          echo '<img src="../hit/images/icon/6.png" width="32" />';
                        }
                        ?>
                        <br>
                      </div>
                    </td>
                    <td style="border-radius: 7px;">
                      <center><?= $thongbao; ?></center>
                      <form method="POST" action="">
                        <div class="row">
                          <input type="hidden" id="idbv" name="idbv" value="17">
                          <div class="form-group mb-1">
                            <textarea class="form-control" name="comment" rows="3" placeholder="Bình luận không vượt quá 75 ký tự" style="border-radius: 7px;" formcontrolname="comment"></textarea>
                          </div>
                          <div class="mt-1">
                            <?php if ($row_active['active'] == 1 && $player_exists) { ?>
                              <button type="submit" name="submit" class="btn btn-action text-white" style="border-radius: 7px;"> <i class="fa fa-comment"></i> Bình luận</button>
                            <?php } else { ?>
                              <span style="color: red; font-size: 12px; font-weight: bold;"><b><i>Hãy tạo nhân vật hoặc kích hoạt trước khi <u>bình luận</u>!</i></b></span>
                            <?php } ?>
                          </div>
                        </div>
                      </form>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          <?php } ?>
          <div class="d-flex justify-content-end">
            <?php if ($totalPages > 1) { ?>
              <ul class="pagination">
                <?php if ($currentpage > 1) { ?>
                  <a class="btn btn-action text-white" href="?page=<?php echo ($currentpage - 1); ?>" aria-label="Previous" style="border-radius: 15px 0px 0px 15px; pointer-events: none;"><span aria-hidden="true">«</span></a>
                  </a>
                <?php } ?>
                <?php
                $numAdjacent = 2; // Số trang số trung gian hiển thị xung quanh trang hiện tại

                $startPage = max(1, $currentpage - $numAdjacent);
                $endPage = min($totalPages, $currentpage + $numAdjacent);

                if ($startPage > 1) {
                  // Hiển thị trang đầu tiên và dấu "..."
                ?>
                  <li class=""><a href="?page=1" class="btn btn-action text-white">1</a></li>
                  <?php if ($startPage > 2) { ?>
                    <li class="disabled"><a class="btn btn-action text-white">...</a></li>
                  <?php }
                }

                for ($page = $startPage; $page <= $endPage; $page++) {
                  ?>
                  <li class=""><a href="?page=<?php echo $page; ?>" class="btn btn-<?php echo ($page == $currentpage) ? 'warning' : 'action'; ?> text-white"><?php echo $page; ?></a></li>
                  <?php
                }

                if ($endPage < $totalPages) {
                  // Hiển thị dấu "..." và trang cuối cùng
                  if ($endPage < ($totalPages - 1)) {
                  ?>
                    <li class="disabled"><a class="btn btn-action text-white">...</a></li>
                  <?php } ?>
                  <li class=""><a href="?page=<?php echo $totalPages; ?>" class="btn btn-action text-white"><?php echo $totalPages; ?></a></li>
                <?php }

                if ($currentpage < $totalPages) { ?>
                  <a class="btn btn-action text-white" href="?page=<?php echo ($currentpage + 1); ?>" aria-label="Next" style="border-radius: 0px 15px 15px 0px; "><span aria-hidden="true">»</span></a>
                <?php } ?>
              </ul>
            <?php } ?>
          </div>
          <hr>
        </div>
      </main>
  <?php }
  }
} else { ?>
  <main>
    <div class="p-1 pb-1 mt-1 alert alert-warning" style="background: #ffe8d1; border-radius: 7px; box-shadow: 0px 2px 5px black;">
      <div class="alert border border-secondary" style="background: #ffe8d1; border-radius: 7px;">
        <h5>
          <b>Thông Báo</b>
        </h5>
        <hr>
        <?php foreach ($topPosts as $post) : ?>
          <div class="alert border border-danger" style="background: #ffe8d1; border-radius: 7px;">
            <div class="topic_name">
              <div style="width: 55px; float: left; margin-right: 10px;">
                <img class="" src="../hit/images/icon/admin.png" style="border-color: red; width: 50px; height: 55px;">
              </div>
              <a class="alert-link text-danger url" href="?id=<?= $post['id']; ?>">
                <span><?= $post['tieude']; ?></span>
              </a>
              <?php if ($post['new'] == 1) { ?><img src="http://my.teamobi.com/images/new.gif"><?php } ?>
              <div class="box_name_eman">bởi <b>
                  <b>
                    <font style="color: red;">Admin</font>
                  </b>
                </b> - <span><?= duxng_time($post['time']); ?></span>
              </div>
            </div>
          </div>
        <?php endforeach; ?>
      </div>
      <div class="alert border-secondary" style="background: #ffe8d1; border-radius: 7px;">
        <h5>
          <b>Diễn Đàn</b>
        </h5>
        <hr>
        <?php foreach ($displayedPosts as $row) : ?>
          <?php if ($row['top_baiviet'] == 0) { ?>
            <div class="alert border border-dark" style="background: #ffe8d1; border-radius: 7px;">
              <div class="topic_name">
                <div style="width: 55px; float: left; margin-right: 10px;">
                  <img class="" src="../hit/images/icon/<?= $row['head']; ?>.png" style="border-color: red; width: 50px; height: 55px;">
                </div>
                <a class="alert-link url" href="?id=<?= $row['id']; ?>">
                  <span><?= $row['tieude']; ?></span>
                </a>
                <div class="box_name_eman">bởi <b>
                    <b>
                      <font style="color: blue;"><?= $row['username']; ?></font>
                    </b>
                  </b> - <span><?= duxng_time($row['time']); ?></span>
                </div>
              </div>
            </div>
          <?php } ?>
        <?php endforeach; ?>
      </div>
      <div class="d-flex justify-content-between">
        <?php if ($_SESSION['logger']['username'] && $row_active['active'] == 1 && $player_exists) { ?>
          <div>
            <a class="btn btn-action text-white" routerlink="post" href="/pages/dangbai_diendan.php" style="border-radius: 7px;">Đăng bài</a>
          </div>
        <?php } ?>
        <?php if ($totalPages > 1) { ?>
          <ul class="pagination">
            <?php if ($currentpage > 1) { ?>
              <a class="btn btn-action text-white" href="?page=<?php echo ($currentpage - 1); ?>" aria-label="Previous" style="border-radius: 15px 0px 0px 15px; pointer-events: none;"><span aria-hidden="true">«</span></a>
              </a>
            <?php } ?>
            <?php
            $numAdjacent = 2; // Số trang số trung gian hiển thị xung quanh trang hiện tại

            $startPage = max(1, $currentpage - $numAdjacent);
            $endPage = min($totalPages, $currentpage + $numAdjacent);

            if ($startPage > 1) {
              // Hiển thị trang đầu tiên và dấu "..."
            ?>
              <li class=""><a href="?page=1" class="btn btn-action text-white">1</a></li>
              <?php if ($startPage > 2) { ?>
                <li class="disabled"><a class="btn btn-action text-white">...</a></li>
              <?php }
            }

            for ($page = $startPage; $page <= $endPage; $page++) {
              ?>
              <li class=""><a href="?page=<?php echo $page; ?>" class="btn btn-<?php echo ($page == $currentpage) ? 'warning' : 'action'; ?> text-white"><?php echo $page; ?></a></li>
              <?php
            }

            if ($endPage < $totalPages) {
              // Hiển thị dấu "..." và trang cuối cùng
              if ($endPage < ($totalPages - 1)) {
              ?>
                <li class="disabled"><a class="btn btn-action text-white">...</a></li>
              <?php } ?>
              <li class=""><a href="?page=<?php echo $totalPages; ?>" class="btn btn-action text-white"><?php echo $totalPages; ?></a></li>
            <?php }

            if ($currentpage < $totalPages) { ?>
              <a class="btn btn-action text-white" href="?page=<?php echo ($currentpage + 1); ?>" aria-label="Next" style="border-radius: 0px 15px 15px 0px; "><span aria-hidden="true">»</span></a>
            <?php } ?>
          </ul>
        <?php } ?>
      </div>
    </div>
  </main>
  <style>
    <?php } ?><?php require_once('../core/end.php'); ?>