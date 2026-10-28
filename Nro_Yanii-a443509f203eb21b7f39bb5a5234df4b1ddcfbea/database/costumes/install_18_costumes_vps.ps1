# NRO: Import 18 costumes into LOCAL MariaDB on Windows VPS.
# Requires NRO_DB_PASSWORD environment variable, MariaDB client and dump tools.
# Run from the checked-out feature/costumes-batch-01 branch.
param(
 [Parameter(Mandatory=$true)][string]$ServerFolder,
 [string]$DbUser='nro_app',
 [string]$DbName='ngocrong',
 [string]$DbHost='127.0.0.1',
 [int]$DbPort=3306
)
$ErrorActionPreference='Stop'
if (!$env:NRO_DB_PASSWORD) { throw 'Set environment variable NRO_DB_PASSWORD before running.' }
$mysql= @((Get-Command mariadb.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source),
          (Get-Command mysql.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source),
          'C:\Program Files\MariaDB 10.11\bin\mariadb.exe','C:\xampp\mysql\bin\mysql.exe') |
          Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
$dump= @((Get-Command mariadb-dump.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source),
         (Get-Command mysqldump.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source),
         'C:\Program Files\MariaDB 10.11\bin\mariadb-dump.exe','C:\xampp\mysql\bin\mysqldump.exe') |
         Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (!$mysql -or !$dump) { throw 'MariaDB CLI or dump utility missing.' }
$env:MYSQL_PWD=$env:NRO_DB_PASSWORD
$connection=@("--user=$DbUser","--host=$DbHost","--port=$DbPort","--default-character-set=utf8mb4")
function DbQuery([string]$sql) {
 $result=& $mysql @connection "--database=$DbName" --batch --skip-column-names "--execute=$sql"
 if ($LASTEXITCODE -ne 0) { throw "MariaDB query failed: $sql" }
 return [string]$result
}
$server=(Resolve-Path -LiteralPath $ServerFolder).Path
$assetRoot=Join-Path $server 'resources\normal\image'
if (!(Test-Path $assetRoot)) { throw "Server resource folder missing: $assetRoot" }
$existing=DbQuery "SELECT (SELECT COUNT(*) FROM item_template WHERE id BETWEEN 2154 AND 2171)+(SELECT COUNT(*) FROM part WHERE id BETWEEN 2042 AND 2095)+(SELECT COUNT(*) FROM cai_trang WHERE id_temp BETWEEN 2154 AND 2171)+(SELECT COUNT(*) FROM head_avatar WHERE head_id BETWEEN 2042 AND 2095)"
if ([int]$existing -ne 0) { throw "New item/part ID range already used ($existing rows). Not importing." }
$max=DbQuery "SELECT CONCAT((SELECT MAX(id) FROM item_template),',',(SELECT MAX(id) FROM part))"
if ($max -ne '2153,2041') { throw "Live DB does not match baseline (max IDs $max). Not importing." }
foreach($zoom in 1..4) {
 foreach($id in 32212..32645) {
  $target=Join-Path $assetRoot "$zoom\icon\$id.png"
  if(Test-Path $target) { throw "Image collision: $target. Not importing." }
 }
}
$backup=Join-Path $PSScriptRoot ('backup_'+(Get-Date -Format 'yyyyMMdd_HHmmss'))
New-Item -ItemType Directory -Path $backup -Force | Out-Null
$dbBackup=Join-Path $backup 'ngocrong_before_costumes.sql'
$dumpError=Join-Path $backup 'dump_error.log'
$dumpArgs=@($connection,'--single-transaction',$DbName)
$d=Start-Process -FilePath $dump -ArgumentList $dumpArgs -NoNewWindow -PassThru -Wait -RedirectStandardOutput $dbBackup -RedirectStandardError $dumpError
if ($d.ExitCode -ne 0 -or (Get-Item $dbBackup).Length -lt 1024) { throw "Backup failed: $dumpError" }
Write-Host "Backup created: $dbBackup"
foreach($batch in @('batch01a','batch01b','batch01c')) {
 $file=Join-Path (Join-Path $PSScriptRoot $batch) 'download_assets.ps1'
 & $file -ServerFolder $server
}
foreach($zoom in 1..4) {
 foreach($id in 32212..32645) {
  $file=Join-Path $assetRoot "$zoom\icon\$id.png"
  if(!(Test-Path $file)) { throw "Missing downloaded sprite: $file" }
  $bytes=[IO.File]::ReadAllBytes($file)
  if($bytes.Length -lt 8 -or [Text.Encoding]::ASCII.GetString($bytes,1,3) -ne 'PNG') { throw "Invalid PNG: $file" }
 }
}
[Console]::OutputEncoding=New-Object Text.UTF8Encoding($false)
foreach($batch in @('batch01a','batch01b','batch01c')) {
 $sqlPath=Join-Path (Join-Path $PSScriptRoot $batch) 'install.sql'
 Get-Content -LiteralPath $sqlPath -Encoding UTF8 | & $mysql @connection "--database=$DbName" 
 if ($LASTEXITCODE -ne 0) { throw "MariaDB import failed for $batch; restore from $dbBackup before retrying." }
 Write-Host "Imported: $batch"
}
$result=DbQuery "SELECT CONCAT((SELECT COUNT(*) FROM item_template WHERE id BETWEEN 2154 AND 2171),',',(SELECT COUNT(*) FROM cai_trang WHERE id_temp BETWEEN 2154 AND 2171),',',(SELECT COUNT(*) FROM part WHERE id BETWEEN 2042 AND 2095),',',(SELECT COUNT(*) FROM head_avatar WHERE head_id BETWEEN 2042 AND 2095))"
if ($result -ne '18,18,54,18') { throw "Verification failed: $result" }
Write-Host 'SUCCESS: 18 costumes, 54 parts, 18 avatars, 1,736 PNG files.' -ForegroundColor Green
Write-Host 'Database imported. Server has not been restarted; rebuild/restart and test client.'
