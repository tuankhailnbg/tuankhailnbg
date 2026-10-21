[CmdletBinding()]
param(
    [ValidateSet('Check','Login','Game')][string]$Mode = 'Check',
    [string]$ProjectRoot = ''
)
$ErrorActionPreference = 'Stop'

function Find-NroProject {
    param([string]$Requested)
    if (-not [string]::IsNullOrWhiteSpace($Requested)) { return $Requested.Trim().Trim('"') }
    $saved = Join-Path $PSScriptRoot 'project-path.txt'
    if (Test-Path -LiteralPath $saved) {
        $candidate = (Get-Content -LiteralPath $saved -Raw).Trim()
        if (Test-Path -LiteralPath (Join-Path $candidate 'Server\resources\config\server.properties')) { return $candidate }
    }
    $default = 'D:\NRO DATA\Nro_Yanii-a443509f203eb21b7f39bb5a5234df4b1ddcfbea'
    if (Test-Path -LiteralPath (Join-Path $default 'Server\resources\config\server.properties')) { return $default }
    Write-Host 'Nhap duong dan thu muc chua ca Login va Server, khong phai AppLocal.'
    return (Read-Host 'Duong dan').Trim().Trim('"')
}

function Get-ListeningOwner {
    param([int]$Port)
    try { return @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop) }
    catch { return @() }
}

function Get-ConfigPort {
    param([string]$Path, [int]$DefaultPort)
    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -match '^\s*server\.port\s*[=:]\s*(\d+)\s*$') { return [int]$Matches[1] }
    }
    return $DefaultPort
}

function Write-NroPorts {
    param([string]$ReportPath)
    Add-Content -LiteralPath $ReportPath -Value "`r`n[WINDOWS TCP CONNECTIONS]" -Encoding UTF8
    try {
        $rows = @(Get-NetTCPConnection -ErrorAction Stop | Where-Object {
            $_.LocalPort -in @(3306,3105,14445,18888) -or $_.RemotePort -in @(3105,14445)
        })
        foreach ($row in $rows) {
            $name = 'unknown'
            try { $name = (Get-Process -Id $row.OwningProcess -ErrorAction Stop).ProcessName } catch { }
            $text = '{0}:{1} -> {2}:{3} {4} PID={5} PROCESS={6}' -f $row.LocalAddress,$row.LocalPort,$row.RemoteAddress,$row.RemotePort,$row.State,$row.OwningProcess,$name
            Write-Host $text
            Add-Content -LiteralPath $ReportPath -Value $text -Encoding UTF8
        }
        $linked = @($rows | Where-Object { $_.State -eq 'Established' -and ($_.LocalPort -eq 3105 -or $_.RemotePort -eq 3105) })
        $text = 'Established TCP links involving Login 3105: ' + $linked.Count + '. This is transport status, not proof of server registration/authentication.'
        Write-Host $text
        Add-Content -LiteralPath $ReportPath -Value $text -Encoding UTF8
    }
    catch {
        $text = 'Get-NetTCPConnection unavailable. Listening-port fallback:'
        Write-Host $text
        Add-Content -LiteralPath $ReportPath -Value $text -Encoding UTF8
        $output = & netstat.exe -ano | Select-String ':3306\s|:3105\s|:14445\s|:18888\s'
        $output | ForEach-Object {
            Write-Host $_.ToString()
            Add-Content -LiteralPath $ReportPath -Value $_.ToString() -Encoding UTF8
        }
    }
}

try {
    $root = Find-NroProject -Requested $ProjectRoot
    if (-not (Test-Path -LiteralPath (Join-Path $root 'Login\server.ini'))) { throw 'Khong tim thay Login\server.ini trong thu muc da chon.' }
    if (-not (Test-Path -LiteralPath (Join-Path $root 'Server\resources\config\server.properties'))) { throw 'Khong tim thay Server\resources\config\server.properties.' }
    $root = (Resolve-Path -LiteralPath $root).Path
    Set-Content -LiteralPath (Join-Path $PSScriptRoot 'project-path.txt') -Value $root -Encoding UTF8
    $java = (Get-Command java.exe -ErrorAction Stop).Source
    $toolJar = Join-Path $PSScriptRoot 'NroCheck.jar'
    $loginJar = Join-Path $root 'Login\target\ServerLogin-1.0-RELEASE-jar-with-dependencies.jar'
    $gameJar = Join-Path $root 'Server\target\TeaMobi-1.0-RELEASE-jar-with-dependencies.jar'
    $reportRoot = Join-Path $PSScriptRoot 'BAO_CAO'
    [void](New-Item -ItemType Directory -Path $reportRoot -Force)
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'

    if ($Mode -eq 'Check') {
        $report = Join-Path $reportRoot ('kiem-tra-' + $stamp + '.txt')
        $classPath = $toolJar + ';' + $loginJar + ';' + $gameJar
        & $java '-Dfile.encoding=UTF-8' '-cp' $classPath 'NroDiagnostics' $root $PSScriptRoot $report
        $checkExit = $LASTEXITCODE
        if (-not (Test-Path -LiteralPath $report)) { Set-Content -LiteralPath $report -Value ('Java checker did not create a report; exit=' + $checkExit) -Encoding UTF8 }
        Write-NroPorts -ReportPath $report
        Write-Host ''
        Write-Host ('Gui file nay de kiem tra: ' + $report)
        exit $checkExit
    }

    $isLogin = $Mode -eq 'Login'
    if ($isLogin) {
        $jar = $loginJar
        $workingDirectory = Join-Path $root 'Login'
        $port = Get-ConfigPort -Path (Join-Path $root 'Login\server.ini') -DefaultPort 3105
        $module = 'login'
        $maximumMemory = '-Xmx512m'
        $minimumMemory = '-Xms128m'
    }
    else {
        $jar = $gameJar
        $workingDirectory = Join-Path $root 'Server'
        $port = Get-ConfigPort -Path (Join-Path $root 'Server\resources\config\server.properties') -DefaultPort 14445
        $module = 'game'
        $maximumMemory = '-Xmx2048m'
        $minimumMemory = '-Xms256m'
    }
    if (-not (Test-Path -LiteralPath $jar)) { throw ('Chua co file build: ' + $jar) }
    $owners = @(Get-ListeningOwner -Port $port)
    if ($owners.Count -gt 0) {
        $ids = ($owners | Select-Object -ExpandProperty OwningProcess -Unique) -join ', '
        throw ('Cong ' + $port + ' dang duoc dung (PID ' + $ids + '). Khong mo them ban sao. Chay 1_KIEM_TRA truoc; neu can khoi dong lai, dung baotri o cua so server cu va cho no tat xong.')
    }
    if (-not $isLogin -and @(Get-ListeningOwner -Port 3105).Count -eq 0) { throw 'Chua thay Login nghe cong 3105. Mo 2_CHAY_LOGIN.cmd truoc, doi dong listening port: 3105, roi mo 3_CHAY_SERVER.cmd.' }
    $log = Join-Path $reportRoot ($module + '-khoi-dong-' + $stamp + '.txt')
    $classPath = $toolJar + ';' + $jar
    Write-Host ('Dang chay dung JAR: ' + $jar)
    Write-Host ('Log: ' + $log)
    Write-Host 'Giu cua so nay mo. Dung baotri de dung binh thuong.'
    Push-Location -LiteralPath $workingDirectory
    try {
        & $java '-Dfile.encoding=UTF-8' $minimumMemory $maximumMemory '-cp' $classPath 'NroStartup' $jar $root $module $log
        $code = $LASTEXITCODE
        Write-Host ('Java da ket thuc; exit=' + $code + '. Xem log ke ca khi exit=0.')
        exit $code
    }
    finally { Pop-Location }
}
catch {
    Write-Host ('LOI: ' + $_.Exception.Message) -ForegroundColor Red
    exit 2
}
