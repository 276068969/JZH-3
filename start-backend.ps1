[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Continue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $ScriptDir "backend"

function Test-PortOpen {
    param([int]$Port)
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $task = $tcp.ConnectAsync("127.0.0.1", $Port)
        if ($task.Wait(800)) {
            $tcp.Close()
            return $true
        }
        $tcp.Close()
    } catch {}
    return $false
}

function Find-Executable {
    param([string[]]$Names)
    foreach ($name in $Names) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($cmd) { return $cmd.Source }
    }
    return $null
}

function Find-InPaths {
    param([string[]]$Paths)
    foreach ($p in $Paths) {
        if ([string]::IsNullOrWhiteSpace($p)) { continue }
        if (Test-Path $p) { return $p }
        $wild = Split-Path $p -Parent
        $leaf = Split-Path $p -Leaf
        if ($wild -and (Test-Path $wild)) {
            try {
                $m = Get-ChildItem -Path $wild -Filter $leaf -ErrorAction SilentlyContinue
                if ($m) { return $m[0].FullName }
            } catch {}
        }
    }
    return $null
}

function Find-Java {
    $exe = Find-Executable @("java", "java.exe")
    if ($exe) { return $exe }
    if ($env:JAVA_HOME) {
        $p = Join-Path $env:JAVA_HOME "bin\java.exe"
        if (Test-Path $p) { return $p }
    }
    $candidates = @(
        "C:\Program Files\Java\jdk-21\bin\java.exe",
        "C:\Program Files\Java\jdk-17\bin\java.exe",
        "C:\Program Files\Java\jdk-11\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-21\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-11\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-21\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-17\bin\java.exe",
        "C:\Program Files\Zulu\zulu-21\bin\java.exe",
        "C:\Program Files\Zulu\zulu-17\bin\java.exe",
        "C:\Program Files\Zulu\zulu-11\bin\java.exe",
        "$env:USERPROFILE\scoop\apps\openjdk\current\bin\java.exe",
        "$env:USERPROFILE\.sdkman\candidates\java\current\bin\java.exe",
        "$env:USERPROFILE\AppData\Local\Programs\Eclipse Adoptium\jdk-21\bin\java.exe",
        "$env:USERPROFILE\AppData\Local\Programs\Eclipse Adoptium\jdk-17\bin\java.exe"
    )
    return Find-InPaths $candidates
}

function Find-Maven {
    $exe = Find-Executable @("mvn", "mvn.cmd", "mvn.bat")
    if ($exe) { return $exe }
    if ($env:MAVEN_HOME) {
        $p = Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
        if (Test-Path $p) { return $p }
    }
    if ($env:M2_HOME) {
        $p = Join-Path $env:M2_HOME "bin\mvn.cmd"
        if (Test-Path $p) { return $p }
    }
    $wrapper = Join-Path $BackendDir "mvnw.cmd"
    if (Test-Path $wrapper) { return $wrapper }
    $candidates = @(
        "C:\Program Files\Apache\maven\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.6\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.5\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.4\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.3\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.2\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.1\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.0\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.8.8\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.8.7\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.8.6\bin\mvn.cmd",
        "C:\ProgramData\chocolatey\lib\maven\bin\mvn.cmd",
        "$env:USERPROFILE\scoop\apps\maven\current\bin\mvn.cmd",
        "$env:USERPROFILE\.sdkman\candidates\maven\current\bin\mvn",
        "$env:USERPROFILE\.sdkman\candidates\maven\current\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA Community\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd"
    )
    return Find-InPaths $candidates
}

Write-Host "=== Locate Java and Maven ===" -ForegroundColor Cyan
$javaExe = Find-Java
if ($javaExe) {
    Write-Host "Java found: $javaExe" -ForegroundColor Green
} else {
    Write-Host "[WARN] Java not found" -ForegroundColor Yellow
}

$mvnExe = Find-Maven
if ($mvnExe) {
    Write-Host "Maven found: $mvnExe" -ForegroundColor Green
} else {
    Write-Host "[WARN] Maven not found" -ForegroundColor Yellow
}

if (-not (Test-Path $BackendDir)) {
    Write-Host "Backend dir not found: $BackendDir" -ForegroundColor Red
    exit 1
}

Write-Host "Backend dir: $BackendDir"
Set-Location $BackendDir

if (Test-PortOpen -Port 8080) {
    Write-Host ""
    Write-Host "[INFO] Port 8080 already in use, skipping backend launch" -ForegroundColor Yellow
    Write-Host "       Stop the existing service to restart."
    exit 0
}

if (-not $mvnExe -or -not $javaExe) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  Cannot start backend: Maven or Java missing"
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  1) Use mock backend:   node ..\mock-backend-and-start.mjs"
    Write-Host "  2) Install JDK 17+ and Maven 3.9+ then retry"
    Write-Host "  3) Open 'backend' folder in IntelliJ IDEA and run from IDE"
    Write-Host ""
    if (-not $javaExe) { Write-Host "  Java:  https://adoptium.net/" -ForegroundColor Cyan }
    if (-not $mvnExe)  { Write-Host "  Maven: https://maven.apache.org/download.cgi" -ForegroundColor Cyan }
    exit 1
}

if ($javaExe -and -not $env:JAVA_HOME) {
    $javaHome = Split-Path (Split-Path $javaExe -Parent) -Parent
    Write-Host "Auto JAVA_HOME=$javaHome" -ForegroundColor Gray
    $env:JAVA_HOME = $javaHome
}

Write-Host ""
Write-Host "=== Start backend (H2 profile) ===" -ForegroundColor Cyan
Write-Host "  cmd: $mvnExe spring-boot:run -Dspring-boot.run.profiles=h2"
& $mvnExe spring-boot:run "-Dspring-boot.run.profiles=h2"
