[CmdletBinding()]
param([switch]$CheckOnly)

$ErrorActionPreference = 'Stop'

function Test-NroPort {
    param([string]$ServerAddress, [int]$ServerPort)
    $connection = New-Object System.Net.Sockets.TcpClient
    try {
        $pending = $connection.BeginConnect($ServerAddress, $ServerPort, $null, $null)
        if (-not $pending.AsyncWaitHandle.WaitOne(1500)) { return $false }
        $connection.EndConnect($pending)
        return $true
    }
    catch { return $false }
    finally { $connection.Close() }
}

function Get-NroConfigServers {
    param([string]$Json)
    if (-not $Json.TrimStart().StartsWith('[')) { throw 'server.json phai la danh sach may chu trong dau [ ].' }
    # Assign the parser result first, then enumerate explicitly. Windows PowerShell
    # 5.1 and PowerShell 7 do not emit JSON arrays in the same way.
    $parsed = ConvertFrom-Json -InputObject $Json
    $count = 0
    foreach ($entry in $parsed) {
        if ($null -eq $entry -or $entry -is [System.Array]) { throw 'Moi muc trong server.json phai la mot doi tuong may chu.' }
        $nameProperty = $entry.PSObject.Properties['nameServer']
        $addressProperty = $entry.PSObject.Properties['address']
        $portProperty = $entry.PSObject.Properties['port']
        if ($null -eq $nameProperty -or [string]::IsNullOrWhiteSpace([string]$nameProperty.Value)) { throw 'Thieu nameServer trong server.json.' }
        if ($null -eq $addressProperty -or [string]::IsNullOrWhiteSpace([string]$addressProperty.Value)) { throw 'Thieu address trong server.json.' }
        $portNumber = 0
        if ($null -eq $portProperty -or -not [int]::TryParse([string]$portProperty.Value, [ref]$portNumber) -or $portNumber -lt 1 -or $portNumber -gt 32767) { throw 'Port trong server.json phai tu 1 den 32767.' }
        $count++
        # Emit one object per server; never return a nested array to the caller.
        [pscustomobject]@{
            nameServer = [string]$nameProperty.Value
            address = [string]$addressProperty.Value
            port = $portNumber
        }
    }
    if ($count -lt 1) { throw 'server.json khong co may chu.' }
}

function Read-NroConfig {
    param([string]$ConfigPath)
    $json = [System.IO.File]::ReadAllText($ConfigPath, [System.Text.Encoding]::UTF8)
    $servers = @(Get-NroConfigServers -Json $json)
    return $json
}

function Send-NroConfigResponse {
    param([System.Net.Sockets.TcpClient]$Connection, [string]$ConfigPath)
    $stream = $null
    try {
        $Connection.ReceiveTimeout = 1500
        $Connection.SendTimeout = 1500
        $stream = $Connection.GetStream()
        $buffer = New-Object byte[] 4096
        $request = New-Object System.Text.StringBuilder
        while ($request.Length -lt 16384) {
            $count = $stream.Read($buffer, 0, $buffer.Length)
            if ($count -le 0) { break }
            [void]$request.Append([System.Text.Encoding]::ASCII.GetString($buffer, 0, $count))
            if ($request.ToString().Contains("`r`n`r`n")) { break }
        }
        $firstLine = ($request.ToString() -split "`r`n", 2)[0]
        if ($firstLine -match '^GET /server\.json(?:\?[^ ]*)? HTTP/1\.[01]$') {
            $body = [System.Text.Encoding]::UTF8.GetBytes((Read-NroConfig -ConfigPath $ConfigPath))
            $status = '200 OK'
        }
        else {
            $body = [System.Text.Encoding]::UTF8.GetBytes('{"error":"not found"}')
            $status = '404 Not Found'
        }
        $headerText = "HTTP/1.1 $status`r`nContent-Type: application/json; charset=utf-8`r`nContent-Length: $($body.Length)`r`nCache-Control: no-store`r`nConnection: close`r`n`r`n"
        $header = [System.Text.Encoding]::ASCII.GetBytes($headerText)
        $stream.Write($header, 0, $header.Length)
        $stream.Write($body, 0, $body.Length)
        $stream.Flush()
    }
    finally {
        if ($null -ne $stream) { $stream.Dispose() }
        $Connection.Close()
    }
}

function Test-NroPackage {
    param([string]$PackageRoot)
    $build = ConvertFrom-Json -InputObject ([System.IO.File]::ReadAllText((Join-Path $PackageRoot 'BUILD_INFO.json')))
    foreach ($file in $build.files) {
        $path = Join-Path $PackageRoot ([string]$file.path)
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Thieu file: $($file.path). Hay giai nen day du ZIP." }
        if ((Get-Item -LiteralPath $path).Length -ne [long]$file.size) { throw "Sai kich thuoc: $($file.path). Hay giai nen lai ZIP." }
        if ((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() -ne [string]$file.sha256) {
            throw "File khong dung ban: $($file.path). Hay giai nen lai ZIP."
        }
    }
    Write-Host "Da kiem tra du $($build.files.Count) file client."
}

function Start-NroClient {
    param([string]$PackageRoot, [switch]$VerifyOnly)
    $listener = $null
    $game = $null
    $logRoot = Join-Path $PackageRoot 'Logs'
    [void](New-Item -ItemType Directory -Path $logRoot -Force)
    $launcherLog = Join-Path $logRoot 'launcher-last.log'
    Set-Content -LiteralPath $launcherLog -Value ((Get-Date -Format s) + ' Launcher r1.1; PowerShell ' + $PSVersionTable.PSVersion.ToString()) -Encoding UTF8
    try {
        Test-NroPackage -PackageRoot $PackageRoot
        $configPath = Join-Path $PackageRoot 'server.json'
        $json = Read-NroConfig -ConfigPath $configPath
        $servers = @(Get-NroConfigServers -Json $json)
        if ($VerifyOnly) {
            Write-Host 'Cau hinh va file client hop le.'
            return
        }
        $server = $servers[0]
        Add-Content -LiteralPath $launcherLog -Value ('Game endpoint: ' + $server.address + ':' + $server.port) -Encoding UTF8
        if (-not (Test-NroPort -ServerAddress ([string]$server.address) -ServerPort ([int]$server.port))) {
            throw "Chua ket noi duoc $($server.address):$($server.port). Bat MariaDB, Login va Server Java truoc, sau do mo lai CHOI_GAME.cmd."
        }
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 18888)
        try { $listener.Start() }
        catch { throw 'Khong mo duoc cong cau hinh 18888. Dong cua so CHOI_GAME dang chay roi thu lai.' }
        $clientLog = Join-Path $logRoot ('client-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.log')
        $appRoot = Join-Path $PackageRoot 'AppLocal'
        $exe = Join-Path $appRoot 'NroYanii.exe'
        $arguments = @('-logFile', ('"{0}"' -f $clientLog), '-screen-fullscreen', '0')
        Write-Host "May chu game: $($server.address):$($server.port)"
        Write-Host 'Dang mo client. Giu cua so nay mo trong khi choi.'
        Write-Host "Log: $clientLog"
        $game = Start-Process -FilePath $exe -WorkingDirectory $appRoot -ArgumentList $arguments -PassThru
        while (-not $game.HasExited) {
            if ($listener.Pending()) {
                $connection = $listener.AcceptTcpClient()
                try {
                    Send-NroConfigResponse -Connection $connection -ConfigPath $configPath
                    Add-Content -LiteralPath $launcherLog -Value ((Get-Date -Format s) + ' Served local configuration.') -Encoding UTF8
                }
                catch {
                    Add-Content -LiteralPath $launcherLog -Value ((Get-Date -Format s) + ' HTTP: ' + $_.Exception.Message) -Encoding UTF8
                }
            }
            else { Start-Sleep -Milliseconds 50 }
            $game.Refresh()
        }
        Write-Host "Client da dong. Ma thoat: $($game.ExitCode)"
    }
    catch {
        $message = $_.Exception.Message
        Add-Content -LiteralPath $launcherLog -Value ((Get-Date -Format s) + ' ERROR: ' + $message) -Encoding UTF8
        Write-Host "LOI: $message" -ForegroundColor Red
        Write-Host "Chi tiet: $launcherLog"
        throw
    }
    finally {
        if ($null -ne $listener) { $listener.Stop() }
        if ($null -ne $game) { $game.Dispose() }
    }
}

if ($MyInvocation.InvocationName -ne '.') {
    Start-NroClient -PackageRoot $PSScriptRoot -VerifyOnly:$CheckOnly
}
