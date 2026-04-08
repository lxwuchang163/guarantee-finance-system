@echo off
chcp 65001 >nul 2>&1
echo ============================================
echo   担保集团业务财务系统 - 数据库备份脚本
echo ============================================

set BACKUP_DIR=%~dp0backups
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%
set BACKUP_FILE=%BACKUP_DIR%\guarantee_finance_%TIMESTAMP%.sql

echo.
echo [INFO] 备份目标: %BACKUP_FILE%

docker exec gf-mysql mysqldump -u root -pGuarantee@2024 --single-transaction --routines --triggers guarantee_finance > "%BACKUP_FILE%" 2>nul

if errorlevel 1 (
    echo [ERROR] 备份失败！请检查MySQL容器是否运行
    pause
    exit /b 1
)

for %%A in ("%BACKUP_FILE%") do set SIZE=%%~zA
set /a SIZE_MB=%SIZE% / 1048576

echo [OK] 备份完成！文件大小: %SIZE_MB% MB
echo [OK] 备份路径: %BACKUP_FILE%
echo.
echo [INFO] 最近7天备份数量:
for /f %%i in ('dir /b /o-d "%BACKUP_DIR%\*.sql" 2^>nul ^| findstr /c:".sql" ^| find /c /v ""') do echo   共计 %%i 个备份文件

pause
