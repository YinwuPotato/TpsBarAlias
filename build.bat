@echo off
rem ===== TpsBarAlias 编译脚本 =====
rem 依赖：本机 JDK（Van 跑 Java 25，直接用 PATH 上的 javac）
rem 依赖：Canvas 的 API jar + 服务端 jar（Bukkit/Paper API 在 canvas-api 里）
rem       两个 jar 都会自动在 ..\..\libraries 与 ..\..\versions 下找，不用手改路径
chcp 65001 >nul
setlocal

set SRC=%~dp0src
set OUT=%~dp0build
set JARNAME=TpsBarAlias-1.0.2.jar
set VANROOT=%~dp0..\..

for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-ChildItem '%VANROOT%\libraries' -Recurse -Filter 'canvas-api-*.jar' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName"') do set API_JAR=%%i
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-ChildItem '%VANROOT%\versions' -Recurse -Filter 'canvas-*.jar' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName"') do set SERVER_JAR=%%i

if not defined API_JAR (
  echo [错误] 没找到 canvas-api jar（..\..\libraries\**\canvas-api-*.jar）
  pause & exit /b 1
)
echo API    jar: %API_JAR%
echo SERVER jar: %SERVER_JAR%

if exist "%OUT%\classes" rmdir /s /q "%OUT%\classes"
mkdir "%OUT%\classes"

javac -encoding UTF-8 -cp "%API_JAR%;%SERVER_JAR%" -d "%OUT%\classes" "%SRC%\org\yinwu\tpsbar\TpsBarPlugin.java"
if errorlevel 1 ( echo [错误] 编译失败 & pause & exit /b 1 )

copy /y "%SRC%\plugin.yml" "%OUT%\classes\plugin.yml" >nul
jar --create --file "%~dp0%JARNAME%" -C "%OUT%\classes" .
if errorlevel 1 ( echo [错误] 打包失败 & pause & exit /b 1 )

echo.
echo [完成] %~dp0%JARNAME%
echo 复制到 Van\plugins\ 后重启服务器即可生效。
pause
