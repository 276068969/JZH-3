@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM ------------------
@REM   JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM -----------------
@REM   M2_HOME - location of maven2's installed home dir
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@echo off
@REM set title of command window
title %0

@REM Enable delayed expansion so that variables set inside code blocks can be
@REM read inside the same code block.
setlocal EnableDelayedExpansion

@REM Resolve the real directory of this script
set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@REM Load system-wide mavenrc if not skipped
if not "%MAVEN_SKIP_RC%"=="" goto skipRc
if exist "%PROGRAMDATA%\mavenrc" call "%PROGRAMDATA%\mavenrc"
:skipRc

@REM Load user mavenrc if not skipped
if not "%MAVEN_SKIP_RC%"=="" goto skipRcUser
if exist "%USERPROFILE%\mavenrc" call "%USERPROFILE%\mavenrc"
:skipRcUser

@REM Find java.exe
set JAVA_EXE=
if exist "%JAVA_HOME%\bin\java.exe" (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  for %%I in (java.exe) do (
    if not "%%~$PATH:I"=="" (
      set "JAVA_EXE=%%~$PATH:I"
      goto foundJava
    )
  )
)
:foundJava

if not exist "%JAVA_EXE%" (
  echo.
  echo Error: JAVA_HOME is not defined correctly and java.exe not found in PATH.
  echo We cannot execute %%JAVA_HOME%%\bin\java.exe
  echo.
  echo Please install Java 17 or higher and set JAVA_HOME environment variable.
  echo Download: https://adoptium.net/
  echo.
  goto error
)

@REM Verify Java version
"%JAVA_EXE%" -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
  echo Error: java.exe is not executable: %JAVA_EXE%
  goto error
)

@REM Locate maven-wrapper.jar
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPS=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_JAR%" (
  echo.
  echo Maven Wrapper jar not found: %WRAPPER_JAR%
  echo Attempting to download...
  echo.

  @REM Try to download wrapper jar using PowerShell
  if exist "%WRAPPER_PROPS%" (
    for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_PROPS%") do (
      if "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
    )
  )

  if not "%WRAPPER_URL%"=="" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
       Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%' -UseBasicParsing" 2>nul

    if exist "%WRAPPER_JAR%" (
      echo Download successful.
    ) else (
      echo Failed to download Maven Wrapper jar.
    )
  )
)

if not exist "%WRAPPER_JAR%" (
  echo.
  echo Error: Maven Wrapper jar not found at: %WRAPPER_JAR%
  echo.
  echo Please run this command to install Maven Wrapper:
  echo   mvn wrapper:wrapper
  echo.
  echo Or install Maven manually and add it to PATH.
  echo Download: https://maven.apache.org/download.cgi
  echo.
  goto error
)

@REM Build classpath
set "CLASSPATH=%WRAPPER_JAR%"

@REM Set MAVEN_PROJECTBASEDIR as environment variable for Maven
set "MAVEN_BASEDIR=%MAVEN_PROJECTBASEDIR%"

@REM Execute Maven
"%JAVA_EXE%" %MAVEN_OPTS% ^
  -classpath "%CLASSPATH%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

:end
@REM End local scope and preserve exit code
endlocal & exit /b %ERRORLEVEL%

:error
endlocal & exit /b 1
