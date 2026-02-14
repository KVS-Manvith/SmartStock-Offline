@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

if "%JAVAFX_LIB%"=="" set "JAVAFX_LIB=C:\javafx\lib"

if not exist "%JAVAFX_LIB%" (
    echo [ERROR] JavaFX lib folder not found: "%JAVAFX_LIB%"
    echo Set JAVAFX_LIB to your JavaFX lib path and try again.
    echo Example: set JAVAFX_LIB=D:\javafx-sdk-21\lib
    pause
    exit /b 1
)

if not exist "lib\mysql-connector-j-9.6.0.jar" (
    echo [ERROR] Missing MySQL connector jar: lib\mysql-connector-j-9.6.0.jar
    pause
    exit /b 1
)

if not exist "src" (
    echo [ERROR] Source folder not found: src
    pause
    exit /b 1
)

if not exist "out" mkdir out

set "SRC_LIST=%TEMP%\inventory_sources_%RANDOM%%RANDOM%.txt"
if exist "%SRC_LIST%" del /f /q "%SRC_LIST%"

for /r "src" %%F in (*.java) do (
    >> "%SRC_LIST%" echo %%F
)

echo [INFO] Compiling project...
javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "lib\mysql-connector-j-9.6.0.jar" -d out @"%SRC_LIST%"
set "COMPILE_CODE=%ERRORLEVEL%"
del /f /q "%SRC_LIST%" >nul 2>&1

if not "%COMPILE_CODE%"=="0" (
    echo [ERROR] Compilation failed.
    pause
    exit /b %COMPILE_CODE%
)

echo [INFO] Launching application...
start "SmartStock Offline" java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "out;lib/mysql-connector-j-9.6.0.jar" com.inventory.ui.FXLoginApp

echo [OK] App launch command sent.
exit /b 0
