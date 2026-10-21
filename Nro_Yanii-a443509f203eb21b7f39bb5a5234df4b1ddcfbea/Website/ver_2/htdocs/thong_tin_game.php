<?php
require_once ('core/config.php');
require_once ('core/head.php');
?>
<style>
    .announcement {
        background-color: #ffe9b8;
        color: #333;
        font-weight: bold;
        margin-bottom: 10px;
        border-radius: 7px;
        border: 3px solid black;
        padding: 5px;
    }

    .announcement-text {
        font-size: 50px;
        font-weight: bold;
        margin: 0;
    }

    .announcement p {
        margin: 0;
    }

    .announcement-info {
        font-size: 15px;
        margin-top: 10px;
        background-color: white;
        border: 2px solid white;
        padding: 10px;
        border-radius: 5px;
    }

    .ent-info {
        font-size: 12px;
        margin-top: 10px;
        background-color: white;
        border: 2px solid white;
        padding: 10px;
        border-radius: 5px;
    }

    .row1 {
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        /* Canh giữa các thẻ trong dòng */
        padding 10px;
    }
</style>

<main>

    <script>
        window.addEventListener('resize', function () {
            const buttons = document.getElementsByClassName('auto-resize');
            for (let i = 0; i < buttons.length; i++) {
                const button = buttons[i];
                const fontSize = parseInt(window.getComputedStyle(button, null).getPropertyValue('font-size'));
                const buttonWidth = button.offsetWidth;
                const textWidth = button.scrollWidth;
                if (textWidth > buttonWidth) {
                    button.style.fontSize = (fontSize - 1) + 'px';
                }
            }
        });
        window.dispatchEvent(new Event('resize'));
    </script>
    <div class="p-1 mt-1 ibox-content"
        style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black; margin-bottom:10px;">
        <div class="p-1 text-white">
            <?php if ($_SESSION['logger']['username']) { ?>
                <div class="align-items-center my-2 d-flex justify-content-between">
                    <a href="/napthe.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                        style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                        onmouseout="<?= $onmouseout; ?>">Donate thẻ cào</a>
                    <a href="/napatm.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                        style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                        onmouseout="<?= $onmouseout; ?>">Donate ATM</a>
                    <a href="<?php echo $fanpage; ?>" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                        style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                        onmouseout="<?= $onmouseout; ?>">Fanpage</a>
                </div>
            <?php } else { ?>
                <div class="align-items-center my-2 d-flex justify-content-between">
                    <a href="/pages/dangnhap.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                        style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                        onmouseout="<?= $onmouseout; ?>">Donate thẻ cào</a>
                    <a href="/pages/dangnhap.php" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                        style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                        onmouseout="<?= $onmouseout; ?>">Donate ATM</a>
                    <a href="<?php echo $fanpage; ?>" type="submit" class="btn btn-lg btn-dark mx-2 auto-resize"
                        style="<?= $nutdangnhapdangkiTO; ?>" name="submit" onmouseover="<?= $onmouseover; ?>"
                        onmouseout="<?= $onmouseout; ?>">Fanpage</a>
                </div>
            <?php } ?>
        </div>
    </div>

    <div class="p-1 mt-1 ibox-content"
        style="background-color: rgba(57,57,57, 0.7); border-radius: 7px; box-shadow: 0px 2px 5px black;">

        <div class="p-1 text-white">
            <div class="announcement">
                <div class="announcement-title" style="color:white;">
                    <div class="post-item d-flex align-items-center my-2">
                        <div class="post-image"><img src="/public/images/logo/a9.png"
                                style="width: 55px;margin-left:10px ; margin: 8px; height: auto; object-fit: contain;">
                        </div>
                        <div>
                            <a class="fw-bold">Đọc xong ib admin hoặc các key bạc</a>
                            <div class="text-muted font-weight-bold">Để nhận quà<span
                                    style="color: red;"> Cần đọc và hiểu tất cả tính năng </span><span class="fb-comments-count"
                                    data-href="#"></span></div>
                        </div>
                        <div class="post-image">
                            <img src="/public/images/logo/new.gif"
                                style="width: 80% ; margin-left: 10px; margin: 8px; height: auto; object-fit: contain; float: left;">
                        </div>
                    </div>
                </div>
                <div id="info1" class="ent-info">
                    <span style="color: red; font-weight: bold; font-size: 18px;"> >>Quan trọng nhất</span><br>
                    - Sever cày chay 100% người nạp chỉ đi nhanh hơn<br>
					- Admin không bán bất kì thứ gì - cày được tất cả<br>
                    - Tất cả vật phẩm cần thiết đều có thể mua trực tiếp tại NPC Santa <br>
                    - Sever đã fix gần như toàn bộ trick lỏ và bug lỏ <br>
                    - Tiền tệ duy nhất là thỏi vàng (Cách cày mình ghi ở dưới)<br>
                    - Sever ưu tiên tính năng bang hội nên cần có bang để có thể đi nhanh hơn<br>
					- Sever có chuyển sinh đổi hành tinh - chuyển sinh càng cao chỉ số càng khủng<br>
					- Nhiệm vụ nhặt nhóc goku mỗi ngày một khu chỉ có 1 con nên<br>
					cần đổi khu chưa nhặt để có thể hoàn thành nhiệm vụ<br>
					(Đây là tính năng tạo ra để xoá bỏ hoàn toàn các trick làm nhiệm vụ)<br>
					<span style="color: blueviolet; font-weight: bold; font-size: 23px;">Game cày chay - Cày là có<br></span
                    <br><br>
					<span style="color: blue; font-weight: bold; font-size: 18px;">Thông tin chi tiết sever</span><br>
					<span style="color: blue; font-weight: bold; font-size: 15px;"> >>Thông tin Săn đệ</span><br>
					<span style="color: red; font-weight: bold;">Tất cả đệ tử sever đều có thể săn được </span><br>
					-Đệ tử Mabu (10pt): Săn Boss Super Broly nhận trứng nở ra Đệ tử Mabu <br>
                    -Đệ tử Berus (20pt): Giết Boss Whis (Săn Đệ) tại Tương lai nhận Đệ tử Berus <br>
                    -Đệ tử Zeno (30pt): Giết Boss Zeno tại Cold nhận Hồn Zeno và <br>đọc Thông tin Hồn Zeno để biết thêm chi tiết<br>
                    -Đệ tử Ngộ Không (40pt): Giết Boss Ngộ Không tại ngũ hành sơn<br>
                    -Đệ tử itachi (60%): Giết Boss itachi <br>(Cần rất đông thành viên bang mới ăn được)<br>
					-Đệ tử Kaido (80%): Giết Boss Kaido<br>(Cần rất đông thành viên bang mới ăn được)<br>
					-Đệ tử Đệ VIP - Tiên Hắc Ám (100%): Giết Boss Tiên Hắc Ám <br>(Cần rất đông thành viên bang mới ăn được)
					<br><br>

                    <span style="color: blue; font-weight: bold; font-size: 15px;">  >>Các cách kiếm vàng:</span><br>
                    Rất nhiều tính năng cày - phó bản - boss <br>
					-Làm Nhiệm vụ Bò Mộng nhận được Thỏi vàng (Tối đa 450 thỏi 1 ngày)<br>
                    -Up Quái bằng nội tại và danh hiệu có dòng chỉ số +%Vàng <br>(Kiếm được từ 100-2000 thỏi 1 ngày)<br>
                    -Ngọc rồng đen 7 sao - Mỗi thành viên bang sẽ nhận 10 thỏi vàng mỗi giờ<br>(Tối đa 20 thành viên = 4200 thỏi vàng) <br>
                    -Bản đồ kho báu Trương Mỹ Lan (rơi 300-1000 Thỏi vàng) <br>
                    -Con đường rắn độc (300-1500 thỏi vàng 1 lần đi)<br>
                    -Doanh trại (300-1000 thỏi vàng mỗi lần đi) <br>
                    -Đánh boss sẽ có tỉ lệ rơi đồ thần linh, huỷ diệt và thiên sứ rất nhiều <br>
					ae có thể bán trực tiếp cho NPC để nhận được 5 - 100 thỏi vàng mỗi món<br>
					-Vật phẩm nhiệm vụ như Nhẫn thời không có thể bán trực tiếp cho NPC với giá 200 thỏi vàng<br>
                    -Máy Gắp Thú dành cho các tín đồ Gacha có tỉ lệ x10 số thỏi vàng bỏ ra <br>
					(Tạm đóng để anh em tập trung cày)<br>
					-Đi đánh đại hội võ thuật giải siêu cấp (Mở rương nhận thỏi vàng và đồ quý giá)<br>
					-Các bang chủ bang lớn có đủ thành viên sẽ nhận được 2000 thỏi vàng mỗi tuần<br>
					(Tính năng anh em xã đoàn)<br>
					<br><br>
					<span style="color: blue; font-weight: bold; font-size: 15px;"> >>Trang bị</span><br>
                    <span style="color: red; font-weight: bold;">1. Cơ chế Set Kích hoạt:</span><br>
                    <span style="color: red; font-weight: bold;"> - Set Kích hoạt thông thường:</span> <br>
                    -Sét kích hoạt thường các bạn có thể mua trực tiếp tại santa <br>hoặc đánh quái ở 3 map đầu (3 hành tinh)<br>
                    Đồi hoa cúc, Thung lũng tre, Đồi nấm tím, Thị trấn Moori, Đồi hoang, Làng Plant <br>
                    <span style="color: red; font-weight: bold;"> - Set Kích hoạt Thần Linh:</span> <br>
					Cách 1: Đem 3 món đồ huỷ diệt ngẫu nhiên tới đảo Kame <br>gặp Bà Hạt Mít để nâng cấp trang bị Set Kích Hoạt VIP<br>
					(Ngẫu nhiên nhận đượng trang bị Set Kích Hoạt Thường hoặc Thần Linh đúng theo hành tinh của mình)<br>
                    Cách 2: Mang 3 món đồ thiên sứ ngẫu nhiên tới hành tinh Bill<br> gặp NPC Whis để nâng cấp set kích hoạt thiên sứ<br>
					(Ngẫu nhiên nhận đượng trang bị Set Kích Hoạt Thần Linh,Huỷ diệt,Thiên sứ)<br>
                    <br>
					<span style="color: red; font-weight: bold;"> - Set Kích hoạt huỷ diệt:</span> <br>
                    Cách 1:Mang 3 món đồ thiên sứ ngẫu nhiên tới hành tinh Bill<br> gặp NPC Whis để nâng cấp set kích hoạt thiên sứ<br>
					(Ngẫu nhiên nhận đượng trang bị Set Kích Hoạt Thần Linh,Huỷ diệt,Thiên sứ)<br>
					<br>
					<span style="color: red; font-weight: bold;"> Set Kích hoạt Thiên Sứ:</span> <br>
                    Cách 1:Mang 3 món đồ thiên sứ ngẫu nhiên tới hành tinh Bill<br> gặp NPC Whis để nâng cấp set kích hoạt thiên sứ<br>
					(Ngẫu nhiên nhận đượng trang bị Set Kích Hoạt Thần Linh,Huỷ diệt,Thiên sứ)<br>
					<br>
					<br>
                    <span style="color: blue; font-weight: bold; font-size: 15px;"> >>Tính năng hỗ trợ nhiệm vụ</span><br>
                    Về nhà gặp NPC chọn chức năng Skil Nhiệm Vụ <br>
                    Để có thể bỏ qua các nhiệm vụ như: <br>
					- Kết bạn với người Trái đất<br>
					- Kết bạn với người Namex<br>
					- Kết bạn với người Xayda<br>
					- Gia nhập bang có 3 thành viên<br>
					nhiệm vụ heo rừng - bulon có thể tự tạo pt và đánh 1 mình <br>
					
                </div>
            </div>
        </div>
    </div>
</main>

<br>

<?php require_once ('core/end.php'); ?>


<script type="text/javascript">
    $(document).ready(function () {
        $('#Noti_Home').modal('show');
    })
</script>
<?php require_once ('core/end.php'); ?>