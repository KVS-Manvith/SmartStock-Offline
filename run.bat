@echo off
setlocal EnableExtensions

cd /d "%~dp0"

if "%JAVAFX_LIB%"=="" (
    if exist "C:\javafx\lib" (
        set "JAVAFX_LIB=C:\javafx\lib"
    ) else if exist "%~dp0javafx\lib" (
        set "JAVAFX_LIB=%~dp0javafx\lib"
    )
)

where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java runtime not found. Install JDK 17+ and ensure java is in PATH.
    pause
    exit /b 1
)

where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java compiler not found. Install JDK 17+ and ensure javac is in PATH.
    pause
    exit /b 1
)

if "%JAVAFX_LIB%"=="" (
    echo [ERROR] JavaFX path not set.
    echo Set JAVAFX_LIB to your JavaFX lib folder, for example:
    echo set JAVAFX_LIB=D:\javafx-sdk-21\lib
    pause
    exit /b 1
)

if not exist "%JAVAFX_LIB%\javafx.controls.jar" (
    echo [ERROR] Invalid JavaFX lib folder: "%JAVAFX_LIB%"
    echo Expected file not found: javafx.controls.jar
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

echo [INFO] Compiling SmartStock Offline...
javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "lib\mysql-connector-j-9.6.0.jar" -d out ^
    src\com\inventory\dao\*.java ^
    src\com\inventory\model\*.java ^
    src\com\inventory\service\*.java ^
    src\com\inventory\ui\*.java ^
    src\com\inventory\util\*.java
set "COMPILE_CODE=%ERRORLEVEL%"

if not "%COMPILE_CODE%"=="0" (
    echo [ERROR] Compilation failed.
    pause
    exit /b %COMPILE_CODE%
)

echo [INFO] Launching SmartStock Offline...
start "SmartStock Offline" java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "out;lib\mysql-connector-j-9.6.0.jar" com.inventory.ui.FXLoginApp
if errorlevel 1 (
    echo [ERROR] Failed to start application.
    pause
    exit /b 1
)

echo [OK] App launch command sent.
exit /b 0
