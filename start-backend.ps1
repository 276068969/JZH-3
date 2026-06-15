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
    return "未知"
}

function Find-Java {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
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
        "${env:USERPROFILE}\scoop\apps\openjdk\current\bin\java.exe",
        "${env:USERPROFILE}\.sdkman\candidates\java\current\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\Programs\Eclipse Adoptium\jdk-21\bin\java.exe",
        "${env:USERPROFILE}\AppData\Local\Programs\Eclipse Adoptium\jdk-17\bin\java.exe"
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
    if (Test-Path $wrapper) { return $wrapper }
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
        "C:\ProgramData\chocolatey\lib\maven\bin\mvn.cmd",
        "${env:USERPROFILE}\scoop\apps\maven\current\bin\mvn.cmd",
        "${env:USERPROFILE}\.sdkman\candidates\maven\current\bin\mvn",
        "${env:USERPROFILE}\.sdkman\candidates\maven\current\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA Community\plugins\maven\lib\maven3\bin\mvn.cmd",
        "C:\Program Files\JetBrains\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd"
    )
    foreach ($p in $candidates) {
        if (Test-Path $p) { return $p }
    }
    return $null
}

function Start-MockBackend {
    param([switch]$WithFrontend)

    if (-not (Test-Path $MockScript)) {
        Write-Color "未找到 Mock 后端脚本: $MockScript" Red
        return $false
    }

    $node = Get-Command node -ErrorAction SilentlyContinue
    if (-not $node) {
        Write-Color "未找到 Node.js，无法启动 Mock 后端" Red
        return $false
    }

    Write-Color "`n=== 启动 Mock 后端 (Node.js 模拟) ===" Cyan
    $args = @($MockScript)
    if (-not $WithFrontend) { $args += "--backend-only" }
    $proc = Start-Process -FilePath $node.Source -ArgumentList $args -WorkingDirectory $ScriptDir -PassThru -NoNewWindow
    Write-Color "Mock 后端进程已启动 (PID: $($proc.Id))" Green
    Write-Color "  API 地址: http://localhost:$BackendPort/api (若 8080 被占用则使用 8081)" Gray
    if ($WithFrontend) {
        Write-Color "  前端地址: http://localhost:5173" Gray
    }
    return $true
}

Write-Color "=== 机动车尾气监管平台 - 后端启动工具 ===" Cyan
Write-Color ""

Write-Color "[1/4] 检测现有后端服务..." Gray
$backendRunning = Test-BackendApi -Port $BackendPort
if ($backendRunning) {
    $procName = Get-PortProcess -Port $BackendPort
    Write-Color "    检测到后端已在端口 $BackendPort 运行 ($procName)" Green
    Write-Color "    API 地址: http://localhost:$BackendPort/api" Green
    Write-Color ""
    Write-Color "后端服务已就绪，可直接使用。" Green
    Write-Color "如需启动前端，请运行: node mock-backend-and-start.mjs 或启动前端 Vite dev server" Cyan
    exit 0
}
Write-Color "    端口 $BackendPort 无响应" Gray

$fallbackRunning = Test-BackendApi -Port $FallbackPort
if ($fallbackRunning) {
    $procName = Get-PortProcess -Port $FallbackPort
    Write-Color "    检测到后端已在端口 $FallbackPort 运行 ($procName)" Green
    Write-Color "    API 地址: http://localhost:$FallbackPort/api" Green
    Write-Color ""
    Write-Color "后端服务已就绪，可直接使用。" Green
    exit 0
}
Write-Color "    端口 $FallbackPort 无响应" Gray

Write-Color ""
Write-Color "[2/4] 查找 Java 运行时..." Gray
$javaExe = Find-Java
if ($javaExe) {
    Write-Color "    找到 Java: $javaExe" Green
} else {
    Write-Color "    未找到 Java (JDK/JRE)" Yellow
}

Write-Color ""
Write-Color "[3/4] 查找 Maven 构建工具..." Gray
$mvnExe = Find-Maven
if ($mvnExe) {
    Write-Color "    找到 Maven: $mvnExe" Green
} else {
    Write-Color "    未找到 Maven" Yellow
}

Write-Color ""
Write-Color "[4/4] 检查后端项目..." Gray
if (-not (Test-Path $BackendDir)) {
    Write-Color "    后端目录不存在: $BackendDir" Red
    exit 1
}
if (-not (Test-Path (Join-Path $BackendDir "pom.xml"))) {
    Write-Color "    未找到 pom.xml" Red
    exit 1
}
Write-Color "    后端项目就绪" Green

Write-Color ""
Write-Color "========================================" Cyan

if ($javaExe -and $mvnExe) {
    Write-Color "Java 和 Maven 均已找到，启动本地后端..." Green
    Write-Color ""

    if ($javaExe -and -not $env:JAVA_HOME) {
        $javaHome = Split-Path (Split-Path $javaExe -Parent) -Parent
        Write-Color "自动设置 JAVA_HOME=$javaHome" Gray
        $env:JAVA_HOME = $javaHome
    }

    Set-Location $BackendDir
    Write-Color "启动命令: $mvnExe spring-boot:run -Dspring-boot.run.profiles=h2" Gray
    & $mvnExe spring-boot:run "-Dspring-boot.run.profiles=h2"
} else {
    Write-Color "本地 Java/Maven 环境不完整" Yellow
    Write-Color ""
    Write-Color "可用方案:" Cyan
    Write-Color "  1. 使用 Mock 后端 (推荐，无需 Java/Maven)" Green
    Write-Color "     命令: node mock-backend-and-start.mjs" Gray
    Write-Color "  2. 安装 JDK 17+ 和 Maven 3.9+" Yellow
    Write-Color "     Java:  https://adoptium.net/" Gray
    Write-Color "     Maven: https://maven.apache.org/download.cgi" Gray
    Write-Color "  3. 使用 IDE (IntelliJ IDEA) 打开 backend 目录运行" Yellow
    Write-Color ""

    $useMock = Read-Host "是否启动 Mock 后端? (Y/n)"
    if ($useMock -ne 'n' -and $useMock -ne 'N') {
        $ok = Start-MockBackend
        if ($ok) {
            Write-Color ""
            Write-Color "Mock 后端已启动，按 Ctrl+C 停止" Green
            while ($true) { Start-Sleep -Seconds 10 }
        }
    } else {
        Write-Color "已取消。请手动启动后端或安装 Java/Maven 后重试。" Yellow
        exit 1
    }
}
