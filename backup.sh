#!/bin/bash
set -e

echo "============================================"
echo "  担保集团业务财务系统 - 数据库备份"
echo "============================================"

BACKUP_DIR="$(cd "$(dirname "$0")" && pwd)/backups"
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/guarantee_finance_${TIMESTAMP}.sql"

echo ""
echo "[INFO] 备份目标: $BACKUP_FILE"

docker exec gf-mysql mysqldump -u root -p"${MYSQL_ROOT_PASSWORD:-Guarantee@2024}" \
    --single-transaction --routines --triggers guarantee_finance > "$BACKUP_FILE" 2>/dev/null

SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
COUNT=$(ls -1 "$BACKUP_DIR"/*.sql 2>/dev/null | wc -l)

echo "[OK] 备份完成！文件大小: $SIZE"
echo "[OK] 备份路径: $BACKUP_FILE"
echo "[INFO] 备份总数: $COUNT"
