param([string]$ServerFolder = (Get-Location).Path)
$ErrorActionPreference='Stop'
$source='https://raw.githubusercontent.com/NDLong1601/nro-server/main/data/icon'
$csv = @'
25064,32526
25067,32527
25068,32528
25069,32529
25070,32530
25072,32531
25073,32532
25074,32533
25075,32534
25076,32535
25077,32536
25078,32537
25079,32538
25080,32539
25081,32540
25082,32541
25084,32542
25085,32543
25087,32544
25088,32545
25089,32546
25090,32547
25092,32548
25093,32549
25094,32550
25095,32551
25096,32552
25097,32553
25098,32554
25099,32555
25100,32556
25101,32557
25102,32558
25123,32559
25124,32560
25125,32561
25129,32562
25132,32563
25133,32564
25134,32565
25135,32566
25137,32567
25138,32568
25139,32569
25140,32570
25141,32571
25143,32572
25144,32573
25145,32574
25146,32575
25147,32576
25149,32577
25150,32578
25151,32579
25152,32580
25153,32581
25154,32582
25155,32583
25157,32584
25158,32585
25159,32586
25160,32587
25161,32588
25162,32589
25163,32590
25164,32591
25165,32592
25166,32593
25167,32594
25169,32595
25172,32596
25173,32597
25174,32598
25175,32599
25177,32600
25178,32601
25179,32602
25180,32603
25181,32604
25183,32605
25184,32606
25185,32607
25186,32608
25187,32609
25216,32610
25219,32611
25220,32612
25221,32613
25300,32614
25301,32615
26004,32616
26005,32617
26006,32618
26008,32619
26009,32620
26012,32621
26013,32622
26015,32623
26016,32624
26017,32625
26101,32626
26109,32627
26110,32628
26111,32629
26112,32630
26113,32631
26114,32632
26115,32633
26116,32634
26118,32635
26119,32636
26120,32637
26161,32638
26162,32639
26163,32640
26164,32641
26165,32642
26166,32643
26173,32644
26174,32645
'@
$map = $csv | ConvertFrom-Csv -Header OldId,NewId
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
