param([string]$ServerFolder = (Get-Location).Path)
$ErrorActionPreference='Stop'
$source='https://raw.githubusercontent.com/NDLong1601/nro-server/main/data/icon'
$map=@'
25104,32406
25107,32407
25108,32408
25109,32409
25110,32410
25112,32411
25113,32412
25114,32413
25115,32414
25116,32415
25117,32416
25118,32417
25119,32418
25120,32419
25121,32420
25122,32421
25126,32422
25127,32423
25189,32424
25197,32425
25198,32426
25199,32427
25200,32428
25201,32429
25203,32430
25204,32431
25206,32432
25207,32433
25208,32434
25209,32435
25210,32436
25211,32437
25212,32438
25213,32439
25214,32440
25215,32441
25225,32442
26002,32443
26014,32444
26018,32445
26019,32446
26022,32447
26023,32448
26024,32449
26025,32450
26034,32451
26035,32452
26036,32453
26037,32454
26038,32455
26039,32456
26042,32457
26043,32458
26044,32459
26045,32460
26047,32461
26048,32462
26049,32463
26050,32464
26051,32465
26052,32466
26053,32467
26054,32468
26055,32469
26056,32470
26057,32471
26058,32472
26059,32473
26060,32474
26061,32475
26062,32476
26063,32477
26064,32478
26065,32479
26066,32480
26069,32481
26070,32482
26074,32483
26075,32484
26076,32485
26077,32486
26078,32487
26080,32488
26081,32489
26082,32490
26083,32491
26084,32492
26085,32493
26089,32494
26090,32495
26091,32496
26092,32497
26093,32498
26094,32499
26095,32500
26096,32501
26097,32502
26098,32503
26099,32504
26100,32505
26121,32506
26124,32507
26125,32508
26127,32509
26129,32510
26130,32511
26131,32512
26132,32513
26133,32514
26134,32515
26135,32516
26136,32517
26138,32518
26139,32519
26140,32520
26167,32521
26168,32522
26169,32523
26170,32524
26171,32525
'@ | ConvertFrom-Csv -Header OldId,NewId
$downloaded = 0
$reused = 0
foreach($z in 1..4){
  $dir = Join-Path $ServerFolder "resources/normal/image/$z/icon"
  New-Item -ItemType Directory -Force -Path $dir | Out-Null
  foreach($i in $map){
    $dest = Join-Path $dir "$($i.NewId).png"
    $tmp = "$dest.download"
    $url = "$source/x$z/$($i.OldId).png"
    try {
      Invoke-WebRequest -Uri $url -OutFile $tmp -UseBasicParsing -TimeoutSec 45
      $bytes = [IO.File]::ReadAllBytes($tmp)
      if($bytes.Length -lt 8 -or [Text.Encoding]::ASCII.GetString($bytes,1,3) -ne 'PNG'){
        throw "Invalid PNG: $url"
      }
      if(Test-Path $dest) {
        if((Get-FileHash $tmp -Algorithm SHA256).Hash -ne (Get-FileHash $dest -Algorithm SHA256).Hash){
          throw "Image ID collision with different content: $dest"
        }
        $reused++
      } else {
        Move-Item -LiteralPath $tmp -Destination $dest
        $downloaded++
      }
    } finally {
      if(Test-Path $tmp) { Remove-Item -LiteralPath $tmp -Force }
    }
  }
  Write-Host "Verified x$z: $($map.Count) PNGs"
}
Write-Host "Asset verification passed. New=$downloaded ExistingValid=$reused"
