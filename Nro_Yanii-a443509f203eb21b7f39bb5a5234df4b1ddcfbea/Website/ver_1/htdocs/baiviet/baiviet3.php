<?php 
  require_once('core/config.php'); 
  require_once('core/head.php'); 
?>

<div class="alert alert-warning" style="background-color: #fee8d0;">
    <div class="topic_name">
                <strong>1. Hướng Dẫn Sử Dụng Mod</strong> </br>
                <b style="color:brown">k_X </b>Đổi khu qua khu vực X</br>
                <b style="color:brown">s_X </b>Tốc độ chạy X - mặc định là 8</br>
                <b style="color:brown">c_X </b> Cheat X - mặc định là 2,3 | Nó là tốc độ game nên để < 100 tránh lag</br>
                <b style="color:brown">ts </b>Tàn sát tất cả quái trong Map</br>
                <b style="color:brown">add </b>Thêm quái đang chỉ vào danh sách tàn sát</br>
                <b style="color:brown">addd </b>Thêm tất cả quái đang chỉ vào danh sách tàn sát</br>
                <b style="color:brown">clrm </b>Xóa danh sách tàn sát quái</br>
                <b style="color:brown">dsnv </b>Hiện danh sách Người chơi trong Map</br>
                <b style="color:brown">tbb </b>Hiện thông báo Boss</br>
                <b style="color:brown">lb </b>Hiện đường kẻ tới Boss</br>
                <b style="color:brown">ahs </b>Bật auto hồi sinh</br>
                <b style="color:brown">alogin </b>Bật auto đăng nhập</br>
                <b style="color:brown">xmap </b>Di chuyển đến map đã chọn tự động</br>
                <b style="color:brown">xbg </b>Xóa Background</br>
                <b style="color:brown">gdl </b>Giảm dung lượng game - Nên giảm cả cheat xuống</br>
                <b style="color:brown">xoamap </b>Xóa địa hình của Map</br>

                <p><strong>2. Phím Tắt Mod</strong> </br>
                <b style="color:brown">A </b>Tự động đánh</br>
                <b style="color:brown">C </b>Sử dụng Capsul nhanh</br>
                <b style="color:brown">F </b>Sử dụng Bông Tai nhanh</br>
                <b style="color:brown">J </b>Next map trái </br>
                <b style="color:brown">K </b>Next map giữa</br>
                <b style="color:brown">L </b>Next map phải</br>
                <b style="color:brown">M </b>Mở danh sách khu</br>
                <b style="color:brown">X </b>Mở menu Mod</br>

                <p><img src="images/hdsd.png" style="max-width:100%"></p>

                <strong>3. Lưu ý</strong> </br>
                <b style="color:brown">_ </b>Là dấu cách</br>
                <b style="color:brown">X </b>Là số có giá trị</br>
                
                   <hr>    
        </div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        $('#Noti_Home').modal('show');
    })
</script>
<?php require_once('core/end.php'); ?>