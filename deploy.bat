@echo off
chcp 65001 >nul 2>&1
echo ============================================
echo   担保集团业务财务系统 - 一键部署脚本 (Windows)
echo ============================================

setlocal enabledelayedexpansion

if not exist ".env" (
    echo [INFO] 未检测到.env文件，从.env.example复制...
    copy .env.example .env >nul
    echo [WARN] 请编辑.env文件后重新运行此脚本
    pause
    exit /b 1
)

echo.
echo [1/5] 检查Docker环境...
docker version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker未运行或未安装！请先启动Docker Desktop
    pause
    exit /b 1
)
echo [OK] Docker环境正常

echo.
echo [2/5] 构建后端镜像...
cd guarantee-finance-backend
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Maven构建失败！
    cd ..
    pause
    exit /b 1
)
echo [OK] 后端构建完成
cd ..

echo.
echo [3/5] 构建前端镜像...
cd guarantee-finance-frontend
call npm install --silent 2>nul
if errorlevel 1 (
    echo [ERROR] npm install失败！请检查Node.js环境
    cd ..
    pause
    exit /b 1
)
call npm run build 2>nul
if errorlevel 1 (
    echo [ERROR] 前端构建失败！
    cd ..
    pause
    exit /b 1
)
echo [OK] 前端构建完成
cd ..

echo.
echo [4/5] 启动Docker容器...
docker-compose down -v 2>nul
docker-compose up -d --build
if errorlevel 1 (
    echo [ERROR] Docker Compose启动失败！
    pause
    exit /b 1
)

echo.
echo [5/5] 等待服务启动...
timeout /t 15 /nobreak >nul

echo.
echo ============================================
echo   部署完成！
echo --------------------------------------------
echo   前端地址: http://localhost:%FRONTEND_PORT%
echo   后端API: http://localhost:%BACKEND_PORT%
echo   API文档: http://localhost:%BACKEND_PORT%/doc.html
echo   MySQL:   localhost:%MYSQL_PORT%
echo   Redis:   localhost:%REDIS_PORT%
echo --------------------------------------------
echo   默认账号: admin / admin123
echo ============================================

pause
