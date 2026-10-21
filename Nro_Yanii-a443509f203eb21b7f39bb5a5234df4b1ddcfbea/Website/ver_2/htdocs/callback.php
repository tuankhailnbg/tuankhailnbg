<?php
include (__DIR__ . "/api/config.php");
$validate = ValidateCallback($_POST);
if ($validate != false) { //Nếu xác thực callback đúng thì chạy vào đây.
	$status = $validate['status']; //Trạng thái thẻ nạp, thẻ thành công = thanhcong , Thẻ sai, thẻ sai mệnh giá = thatbai
	$serial = $validate['serial']; //Số serial của thẻ.
	$pin = $validate['pin']; //Mã pin của thẻ.
	$card_type = $validate['card_type']; //Loại thẻ. vd: Viettel, Mobifone, Vinaphone.
	$amount = $validate['amount']; //Mệnh giá của thẻ. nếu bạn sài thêm hàm sai mệnh giá vui lòng sử dụng thêm hàm này tự cập nhật mệnh giá thật kèm theo desc là mệnh giá củ
	$content = $validate['content']; // id transaction 
	$result = $conn->query("SELECT * FROM `trans_log` WHERE status = 0 AND trans_id = '{$content}' AND pin = '{$pin}' AND seri = '{$serial}' AND type = '{$card_type}'");

	if ($result->num_rows > 0) {
		$result = $result->fetch_array(MYSQLI_ASSOC);
		print_r($result);
		if ($status == 'thanhcong') {
			//Xử lý nạp thẻ thành công tại đây.
			$price = $amount;
			$pricert = $price * (100/100);
			$conn->query("UPDATE `account` SET vnd = vnd + {$pricert} WHERE username = '{$result['name']}'");
			$conn->query("UPDATE `account` SET tongnap = tongnap + {$pricert} WHERE username = '{$result['name']}'");
			$conn->query("UPDATE `trans_log` SET `status` = 1 WHERE `id` = {$result['id']}"); // chuyển cho kết quả thành công      
		} else if ($status == 'saimenhgia') {
			//Xử lý nạp thẻ sai mệnh giá tại đây.
			$conn->query("UPDATE `trans_log` SET status = 3, `amount` = {$amount} WHERE `id` = {$result['id']}"); // thất bại   
		} else {
			//Xử lý nạp thẻ thất bại tại đây.
			$conn->query("UPDATE `trans_log` SET status = 2 WHERE `id` = {$result['id']}"); // thất bại   
		}

		# Lưu log Nạp Thẻ 
		$file = "card.log";
		$fh = fopen($file, 'a') or die ("cant open file");
		fwrite($fh, "Tai khoan: " . $result['name'] . ", data: " . json_encode($_POST));
		fwrite($fh, "\r\n");
		fclose($fh);
	}
}

function ValidateCallback($out)
{ //Hàm kiểm tra callback từ sever
	if (
		isset (
		$out['status'],
		$out['serial'],
		$out['pin'],
		$out['card_type'],
		$out['amount'],
		$out['content'],
	)
	) {
		return $out; //xác thực thành công, return mảng dữ liệu từ sever trả về.

	} else {
		return false; //Xác thực callback thất bại.
	}
}