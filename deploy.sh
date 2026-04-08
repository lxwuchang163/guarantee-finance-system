#!/bin/bash
set -e

echo "============================================"
echo "  担保集团业务财务系统 - 部署脚本 (Linux)"
echo "============================================"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f ".env" ]; then
    echo "[INFO] 未检测到.env文件，从.env.example复制..."
    cp .env.example .env
    echo "[WARN] 请编辑.env文件后重新运行此脚本"
    exit 1
fi

echo ""
echo "[1/5] 检查Docker环境..."
if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker未安装！"
    exit 1
fi
if ! docker info &> /dev/null; then
    echo "[ERROR] Docker未运行！请先启动Docker服务"
    exit 1
fi
echo "[OK] Docker环境正常"

echo ""
echo "[2/5] 构建后端..."
cd guarantee-finance-backend
./mvnw clean package -DskipTests -q || mvn clean package -DskipTests -q
echo "[OK] 后端构建完成"
cd ..

echo ""
echo "[3/5] 构建前端..."
cd guarantee-finance-frontend
npm install --silent
npm run build
echo "[OK] 前端构建完成"
cd ..

echo ""
echo "[4/5] 启动Docker容器..."
docker-compose down -v 2>/dev/null || true
docker-compose up -d --build

echo ""
echo "[5/5] 等待服务就绪..."
sleep 20

echo ""
echo "============================================"
echo "  部署完成！"
echo "--------------------------------------------"
source .env
echo "  前端地址: http://localhost:${FRONTEND_PORT:-80}"
echo "  后端API: http://localhost:${BACKEND_PORT:-8080}"
echo "  API文档: http://localhost:${BACKEND_PORT:-8080}/doc.html"
echo "  MySQL:   localhost:${MYSQL_PORT:-3306}"
echo "  Redis:   localhost:${REDIS_PORT:-6379}"
echo "--------------------------------------------"
echo "  默认账号: admin / admin123"
echo "============================================"
