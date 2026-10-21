@echo off
REM ------------------------------------------------------------------
REM ShopSphere - database provisioning helper
REM
REM Creates the shopsphere database and the least-privilege application
REM account. The root password is asked for interactively and is never
REM stored in this repository.
REM
REM Usage:  db\setup-database.cmd
REM ------------------------------------------------------------------
setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0.."
set "TEMPLATE=%PROJECT_DIR%\db\setup-database.sql"
set "MYSQL_BIN=%ProgramFiles%\MySQL\MySQL Server 26.7\bin\mysql.exe"
if not exist "%MYSQL_BIN%" set "MYSQL_BIN=mysql"

echo.
echo ShopSphere database setup
echo =========================
echo.

if not exist "%PROJECT_DIR%\config" mkdir "%PROJECT_DIR%\config"

set /p "APP_PASSWORD=Password for the application account 'shopsphere_app' (leave empty to generate one): "
if "%APP_PASSWORD%"=="" (
    set "APP_PASSWORD=Ss_%RANDOM%%RANDOM%%RANDOM%"
    echo Generated password: %APP_PASSWORD%
)

echo.
set /p "MYSQL_PWD=Password of the MySQL administrator account: "
if "%MYSQL_PWD%"=="" (
    echo A MySQL administrator password is required.
    exit /b 2
)

set "TEMP_SQL=%TEMP%\shopsphere-setup-%RANDOM%.sql"
powershell -NoProfile -Command ^
  "$t=[IO.File]::ReadAllText('%TEMPLATE%'); $t=$t.Replace('__APP_DB_PASSWORD__', '%APP_PASSWORD%'); [IO.File]::WriteAllText('%TEMP_SQL%', $t)"

echo.
echo Applying the setup script ...
"%MYSQL_BIN%" -u root -h 127.0.0.1 -P 3306 < "%TEMP_SQL%"
if errorlevel 1 (
    echo The setup failed. Nothing was changed.
    del "%TEMP_SQL%" >nul 2>&1
    exit /b 1
)
del "%TEMP_SQL%" >nul 2>&1

REM Store the application credentials in the local (git-ignored) configuration file.
set "CONFIG_FILE=%PROJECT_DIR%\config\db.properties"
if exist "%CONFIG_FILE%" (
    echo.
    echo The file %CONFIG_FILE% already exists and was not overwritten.
    echo Please make sure it contains the password above.
) else (
    (
        echo # Local configuration - contains secrets, do not commit.
        echo db.url=jdbc:mysql://localhost:3306/shopsphere?useUnicode=true^&characterEncoding=UTF-8^&serverTimezone=UTC^&useSSL=false^&allowPublicKeyRetrieval=true^&rewriteBatchedStatements=true
        echo db.user=shopsphere_app
        echo db.password=%APP_PASSWORD%
    ) > "%CONFIG_FILE%"
    echo.
    echo Wrote %CONFIG_FILE%
)

echo.
echo Done. Start the application with:
echo   set SHOPSPHERE_CONFIG=%CONFIG_FILE%
echo.
endlocal
