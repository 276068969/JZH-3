$ErrorActionPreference = "SilentlyContinue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $ScriptDir "backend"
$MockScript = Join-Path $ScriptDir "mock-backend-and-start.mjs"
$BackendPort = 8080
$FallbackPort = 8081

function Write-Color {
    param([string]$Text, [ConsoleColor]$Color = [ConsoleColor]::White)
    Write-Host $Text -ForegroundColor $Color
}

function Test-BackendApi {
    param([int]$Port = $BackendPort)
    try {
        $url = "http://127.0.0.1:$Port/api/pollutant-limit-rules/fuel-types"
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500 -and $r.Content -match '汽油') {
            return $true
        }
    } catch {}
    return $false
}

function Get-PortProcess {
    param([int]$Port)
    try {
        $netstat = netstat -ano | Select-String ":$Port\s"
        if ($netstat) {
            $pids = $netstat | ForEach-Object { ($_ -replace '\s+', ' ').Trim() -split ' ' } | Where-Object { $_ -match '^\d+$' } | Select-Object -Unique
            if ($pids) {
                $procs = @()
                foreach ($pid in $pids) {
                    try {
                        $p = Get-Process -Id $pid -ErrorAction Stop
                        $procs += "$($p.ProcessName) (PID:$pid)"
                    } catch {}
                }
                if ($procs) { return ($procs -join ', ') }
            }
        }
    } catch {}
    return "unknown"
}

function Find-Java {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    if ($env:JAVA_HOME) {
        $p = Join-Path $env:JAVA_HOME "bin\java.exe"
        if (Test-Path $p) { return $p }
    }

    if ($env:JDK_HOME) {
        $p = Join-Path $env:JDK_HOME "bin\java.exe"
        if (Test-Path $p) { return $p }
    }

    $candidates = @(
        "C:\Program Files\Java\jdk-23\bin\java.exe",
        "C:\Program Files\Java\jdk-22\bin\java.exe",
        "C:\Program Files\Java\jdk-21\bin\java.exe",
        "C:\Program Files\Java\jdk-20\bin\java.exe",
        "C:\Program Files\Java\jdk-19\bin\java.exe",
        "C:\Program Files\Java\jdk-18\bin\java.exe",
        "C:\Program Files\Java\jdk-17\bin\java.exe",
        "C:\Program Files\Java\jdk-16\bin\java.exe",
        "C:\Program Files\Java\jdk-15\bin\java.exe",
        "C:\Program Files\Java\jdk-14\bin\java.exe",
        "C:\Program Files\Java\jdk-13\bin\java.exe",
        "C:\Program Files\Java\jdk-12\bin\java.exe",
        "C:\Program Files\Java\jdk-11\bin\java.exe",
        "C:\Program Files\Java\jdk1.8.0_301\bin\java.exe",
        "C:\Program Files\Java\jdk1.8.0_291\bin\java.exe",
        "C:\Program Files\Java\jdk1.8.0_281\bin\java.exe",
        "C:\Program Files\Java\jre-1.8\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-23.0.0.37-hotspot\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-22.0.0.36-hotspot\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-21.0.0.35-hotspot\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-20.0.0.36-hotspot\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.0.35-hotspot\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.0.35\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-11.0.0.28-hotspot\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-11.0.0.28\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\temurin-21\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\temurin-17\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\temurin-11\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-23.0.1.11-hotspot\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-21.0.2.12-hotspot\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-17.0.7.7-hotspot\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-11.0.25.9-hotspot\bin\java.exe",
        "C:\Program Files\Zulu\zulu-23\bin\java.exe",
        "C:\Program Files\Zulu\zulu-21\bin\java.exe",
        "C:\Program Files\Zulu\zulu-17\bin\java.exe",
        "C:\Program Files\Zulu\zulu-11\bin\java.exe",
        "C:\Program Files\Zulu\zulu-8\bin\java.exe",
        "C:\Program Files\BellSoft\LibericaJDK-21\bin\java.exe",
        "C:\Program Files\BellSoft\LibericaJDK-17\bin\java.exe",
        "C:\Program Files\BellSoft\LibericaJDK-11\bin\java.exe",
        "C:\Program Files\Amazon Corretto\jdk23.0.1_11\bin\java.exe",
        "C:\Program Files\Amazon Corretto\jdk21.0.2_13\bin\java.exe",
        "C:\Program Files\Amazon Corretto\jdk17.0.7_7\bin\java.exe",
        "C:\Program Files\Amazon Corretto\jdk11.0.19_7\bin\java.exe",
        "C:\Program Files\SapMachine\sapmachine-21\bin\java.exe",
        "C:\Program Files\SapMachine\sapmachine-17\bin\java.exe",
        "C:\Program Files\SapMachine\sapmachine-11\bin\java.exe",
        "C:\Program Files\GraalVM\graalvm-jdk-21\bin\java.exe",
        "C:\Program Files\GraalVM\graalvm-jdk-17\bin\java.exe",
        "C:\Program Files (x86)\Java\jdk-21\bin\java.exe",
        "C:\Program Files (x86)\Java\jdk-17\bin\java.exe",
        "C:\Program Files (x86)\Eclipse Adoptium\jdk-21\bin\java.exe",
        "C:\Program Files (x86)\Eclipse Adoptium\jdk-17\bin\java.exe",
        "${env:USERPROFILE}\scoop\apps\openjdk\current\bin\java.exe",
        "${env:USERPROFILE}\scoop\apps\temurin-jdk\current\bin\java.exe",
        "${env:USERPROFILE}\scoop\apps\oraclejdk\current\bin\java.exe",
        "${env:USERPROFILE}\scoop\apps\zulu-jdk\current\bin\java.exe",
        "${env:USERPROFILE}\.sdkman\candidates\java\current\bin\java.exe",
        "${env:USERPROFILE}\.jdks\corretto-21\bin\java.exe",
        "${env:USERPROFILE}\.jdks\corretto-17\bin\java.exe",
        "${env:USERPROFILE}\.jdks\temurin-21\bin\java.exe",
        "${env:USERPROFILE}\.jdks\temurin-17\bin\java.exe",
        "${env:USERPROFILE}\.jdks\openjdk-21\bin\java.exe",
        "${env:USERPROFILE}\.jdks\openjdk-17\bin\java.exe",
        "${env:USERPROFILE}\.gradle\jdks\jdk-21\bin\java.exe",
        "${env:USERPROFILE}\.gradle\jdks\jdk-17\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\Programs\Eclipse Adoptium\jdk-21\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\Programs\Eclipse Adoptium\jdk-17\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\Programs\Java\jdk-21\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\Programs\Java\jdk-17\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\JetBrains\Toolbox\apps\IDEA-U\ch-0\jbr\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\jbr\bin\java.exe"
    )
    foreach ($p in $candidates) {
        if (Test-Path $p) { return $p }
    }
    return $null
}

function Find-Maven {
    $cmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    if ($env:MAVEN_HOME) {
        $p = Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
        if (Test-Path $p) { return $p }
    }
    if ($env:M2_HOME) {
        $p = Join-Path $env:M2_HOME "bin\mvn.cmd"
        if (Test-Path $p) { return $p }
    }

    $wrapper = Join-Path $BackendDir "mvnw.cmd"
    $wrapperJar = Join-Path $BackendDir ".mvn\wrapper\maven-wrapper.jar"
    if ((Test-Path $wrapper) -and (Test-Path $wrapperJar)) {
        return $wrapper
    }
    if (Test-Path $wrapper) {
        Write-Color "    [mvnw] Found mvnw.cmd but wrapper jar missing, will attempt download" Gray
        return $wrapper
    }

    $candidates = @(
        "C:\Program Files\Apache\maven\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.9\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.8\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.9.7\bin\mvn.cmd",
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
        "C:\Program Files\Apache Software Foundation\maven-3.8.5\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.8.4\bin\mvn.cmd",
        "C:\Program Files\Apache Software Foundation\maven-3.6.3\bin\mvn.cmd",
        "C:\ProgramData\chocolatey\lib\maven\bin\mvn.cmd",
        "C:\ProgramData\chocolatey\lib\maven\tools\apache-maven\bin\mvn.cmd",
        "${env:USERPROFILE}\scoop\apps\maven\current\bin\mvn.cmd",
        "${env:USERPROFILE}\.sdkman\candidates\maven\current\bin\mvn.cmd",
        "${env:USERPROFILE}\.sdkman\candidates\maven\current\bin\mvn",
        "C:\Program Files\JetBrains\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA Community\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files (x86)\JetBrains\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files (x86)\JetBrains\IntelliJ IDEA Community\plugins\maven\lib\maven3\bin\mvn.cmd",
        "${env:USERPROFILE}\AppData\Local\JetBrains\Toolbox\apps\IDEA-U\ch-0\plugins\maven\lib\maven3\bin\mvn.cmd",
        "${env:USERPROFILE}\AppData\Local\JetBrains\Toolbox\apps\IDEA-C\ch-0\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\NetBeans\java\maven\bin\mvn.cmd",
        "C:\Program Files\NetBeans-21\netbeans\java\maven\bin\mvn.cmd",
        "C:\Program Files\Eclipse\plugins\org.apache.maven.core_*\bin\mvn.cmd",
        "C:\Program Files\Spring Tools 4\plugins\org.apache.maven.core_*\bin\mvn.cmd",
        "${env:USERPROFILE}\.m2\wrapper\dists\apache-maven-*\*\apache-maven-*\bin\mvn.cmd"
    )
    foreach ($p in $candidates) {
        if (Test-Path $p) { return $p }
    }
    return $null
}

function Start-MockBackend {
    param([switch]$WithFrontend)

    if (-not (Test-Path $MockScript)) {
        Write-Color "Mock backend script not found: $MockScript" Red
        return $false
    }

    $node = Get-Command node -ErrorAction SilentlyContinue
    if (-not $node) {
        Write-Color "Node.js not found, cannot start mock backend" Red
        return $false
    }

    Write-Color "`n=== Starting Mock Backend (Node.js simulation) ===" Cyan
    $args = @($MockScript)
    if (-not $WithFrontend) { $args += "--backend-only" }
    $args += "--force-mock"
    $proc = Start-Process -FilePath $node.Source -ArgumentList $args -WorkingDirectory $ScriptDir -PassThru -NoNewWindow
    Write-Color "Mock backend started (PID: $($proc.Id))" Green
    Write-Color "  API URL: http://localhost:$BackendPort/api (fallback: $FallbackPort)" Gray
    if ($WithFrontend) {
        Write-Color "  Frontend: http://localhost:5173" Gray
    }
    return $true
}

Write-Color "=== Emission Regulatory Platform - Backend Starter ===" Cyan
Write-Color ""

Write-Color "[1/4] Checking existing backend..." Gray
$backendRunning = Test-BackendApi -Port $BackendPort
if ($backendRunning) {
    $procName = Get-PortProcess -Port $BackendPort
    Write-Color "    Backend already running on port $BackendPort ($procName)" Green
    Write-Color "    API URL: http://localhost:$BackendPort/api" Green
    Write-Color ""
    Write-Color "Backend is ready." Green
    Write-Color "To start frontend: node start-services.mjs" Cyan
    exit 0
}
Write-Color "    Port $BackendPort not responding" Gray

$fallbackRunning = Test-BackendApi -Port $FallbackPort
if ($fallbackRunning) {
    $procName = Get-PortProcess -Port $FallbackPort
    Write-Color "    Backend already running on port $FallbackPort ($procName)" Green
    Write-Color "    API URL: http://localhost:$FallbackPort/api" Green
    Write-Color ""
    Write-Color "Backend is ready." Green
    exit 0
}
Write-Color "    Port $FallbackPort not responding" Gray

Write-Color ""
Write-Color "[2/4] Finding Java runtime..." Gray
$javaExe = Find-Java
if ($javaExe) {
    Write-Color "    Found Java: $javaExe" Green
    try {
        $ver = & $javaExe -version 2>&1
        $verStr = ($ver | Select-Object -First 1).ToString()
        Write-Color "    Version: $verStr" Gray
    } catch {}
} else {
    Write-Color "    Java (JDK/JRE) not found" Yellow
}

Write-Color ""
Write-Color "[3/4] Finding Maven build tool..." Gray
$mvnExe = Find-Maven
if ($mvnExe) {
    Write-Color "    Found Maven: $mvnExe" Green
} else {
    Write-Color "    Maven not found" Yellow
}

Write-Color ""
Write-Color "[4/4] Checking backend project..." Gray
if (-not (Test-Path $BackendDir)) {
    Write-Color "    Backend directory not found: $BackendDir" Red
    exit 1
}
if (-not (Test-Path (Join-Path $BackendDir "pom.xml"))) {
    Write-Color "    pom.xml not found" Red
    exit 1
}
$hasMvnw = Test-Path (Join-Path $BackendDir "mvnw.cmd")
$hasWrapperJar = Test-Path (Join-Path $BackendDir ".mvn\wrapper\maven-wrapper.properties")
Write-Color "    Backend project: ready (Maven Wrapper: $(if ($hasMvnw) {'YES'} else {'NO'}))" Green

Write-Color ""
Write-Color "========================================" Cyan

if ($javaExe -and $mvnExe) {
    Write-Color "Java and Maven both found, starting local backend..." Green
    Write-Color ""

    if ($javaExe -and -not $env:JAVA_HOME) {
        $javaHome = Split-Path (Split-Path $javaExe -Parent) -Parent
        Write-Color "Auto-set JAVA_HOME=$javaHome" Gray
        $env:JAVA_HOME = $javaHome
    }

    Set-Location $BackendDir
    Write-Color "Command: $mvnExe spring-boot:run -Dspring-boot.run.profiles=h2" Gray
    Write-Color ""
    & $mvnExe spring-boot:run "-Dspring-boot.run.profiles=h2"
} else {
    Write-Color "Local Java/Maven environment incomplete" Yellow
    Write-Color ""
    Write-Color "Available options:" Cyan
    Write-Color "  1. Use Mock Backend (recommended, no Java/Maven needed)" Green
    Write-Color "     Command: node mock-backend-and-start.mjs --force-mock" Gray
    Write-Color "  2. Install JDK 17+ and Maven 3.9+" Yellow
    Write-Color "     Java:  https://adoptium.net/" Gray
    Write-Color "     Maven: https://maven.apache.org/download.cgi" Gray
    Write-Color "  3. Use IDE (IntelliJ IDEA) to open backend directory" Yellow
    Write-Color ""

    $useMock = Read-Host "Start Mock Backend? (Y/n)"
    if ($useMock -ne 'n' -and $useMock -ne 'N') {
        $ok = Start-MockBackend
        if ($ok) {
            Write-Color ""
            Write-Color "Mock backend started. Press Ctrl+C to stop." Green
            while ($true) { Start-Sleep -Seconds 10 }
        }
    } else {
        Write-Color "Cancelled. Please start backend manually or install Java/Maven." Yellow
        exit 1
    }
}
