<?php
$_link_download_game = "bạn sửa link download game ở đây";
$_domain = 'hocviennro.online';//điền domain web
$_tientogioithieu = '';
//mysql
$db_host="localhost";
$db_user="root";
$db_pass="";
$db_name="nrovip";
//api
$w_api_momo='';
$w_api_momo_id='';
$w_cuphap_momo='academy'; // cú pháp
$_qrmomo='img/qrmomo.png'; // link ảnh qr code
$_phonemomo='0337766460'; //sđt momo



///
//
function curl_get_contents($url)
{
  $curl = curl_init($url);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($curl, CURLOPT_FOLLOWLOCATION, 1);
  curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 0);
  curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 0);
  $data = curl_exec($curl);
  curl_close($curl);
  return $data;
}

function status($data)
{
    if ($data == 'xuly'){
        $show = '<span class="badge badge-info">Đang xử lý</span>';
    }
    else if ($data == 'hoantat'){
        $show = '<span class="badge badge-success">Hoàn tất</span>';
    }
    else if ($data == 'thatbai'){
        $show = '<span class="badge badge-danger">Thất bại</span>';
    }
    else{
        $show = '<span class="badge badge-warning">Khác</span>';
    }
    return $show;
}
function check_string($data)
{
    return str_replace(array('<',"'",'>','?','/',"\\",'--','eval(','<php'),array('','','','','','','','',''),htmlspecialchars(addslashes(strip_tags($data))));
}
?>



