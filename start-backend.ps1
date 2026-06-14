$ErrorActionPreference = "Continue"
Write-Host "=== 查找 Java 和 Maven ===" -ForegroundColor Cyan

$javaExe = Get-Command java -ErrorAction SilentlyContinue
if ($javaExe) {
    Write-Host "Java 路径: $($javaExe.Source)" -ForegroundColor Green
    & java -version
} else {
    Write-Host "未找到 java 命令，搜索常见路径..." -ForegroundColor Yellow
    $possibleJavaPaths = @(
        "C:\Program Files\Java\jdk-17\bin\java.exe",
        "C:\Program Files\Java\jdk-21\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe",
        "C:\Program Files\Microsoft\jdk-17\bin\java.exe"
    )
    foreach ($p in $possibleJavaPaths) {
        if (Test-Path $p) { Write-Host "找到: $p" -ForegroundColor Green ; $javaExe = $p ; break }
    }
}

$mvnExe = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnExe) {
    Write-Host "Maven 路径: $($mvnExe.Source)" -ForegroundColor Green
} else {
    Write-Host "未找到 mvn 命令，搜索常见路径..." -ForegroundColor Yellow
    $possibleMvnPaths = @(
        "C:\Program Files\Apache\maven\bin\mvn.cmd",
        "C:\ProgramData\chocolatey\lib\maven\bin\mvn.cmd",
        "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-*\bin\mvn.cmd"
    )
    foreach ($p in $possibleMvnPaths) {
        $matches = Get-ChildItem -Path $p -ErrorAction SilentlyContinue
        if ($matches) { Write-Host "找到: $($matches.FullName)" -ForegroundColor Green ; $mvnExe = $matches.FullName ; break }
    }
}

$backendDir = "c:\Users\guich\Desktop\title\JZH-3\backend"
Set-Location $backendDir

if ($mvnExe) {
    Write-Host "`n=== 启动后端 (H2 profile) ===" -ForegroundColor Cyan
    & $mvnExe spring-boot:run "-Dspring-boot.run.profiles=h2"
} else {
    Write-Host "无法启动后端：未找到 Maven" -ForegroundColor Red
    exit 1
}
