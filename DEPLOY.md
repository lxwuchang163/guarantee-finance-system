# 担保集团业务财务系统
# Docker部署快速参考

## 环境要求
- Docker >= 24.0
- Docker Compose >= 2.20
- Node.js >= 18 (仅构建前端时需要)
- JDK 17 (仅本地开发/测试时需要)

## 快速部署

### Windows环境
```bash
# 1. 复制并编辑环境变量
copy .env.example .env
# 编辑.env文件中的密码等配置

# 2. 一键部署
deploy.bat
```

### Linux环境
```bash
# 1. 复制并编辑环境变量
cp .env.example .env
vim .env

# 2. 一键部署
chmod +x deploy.sh deploy.sh backup.sh
./deploy.sh
```

## 手动分步部署

```bash
# 1. 构建后端JAR包
cd guarantee-finance-backend
mvn clean package -DskipTests
cd ..

# 2. 构建前端静态文件
cd guarantee-finance-frontend
npm install && npm run build
cd ..

# 3. 启动所有服务
docker-compose up -d --build

# 4. 查看日志
docker-compose logs -f backend
```

## 服务端口说明
| 服务     | 容器名       | 内部端口 | 外部端口(默认) |
|----------|-------------|---------|---------------|
| MySQL    | gf-mysql    | 3306    | 3306          |
| Redis    | gf-redis    | 6379    | 6379          |
| 后端API  | gf-backend  | 8080    | 8080          |
| 前端页面 | gf-frontend | 80      | 80            |

## 常用运维命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 重启某个服务
docker-compose restart backend

# 停止所有服务
docker-compose down

# 停止并删除数据卷（慎用！）
docker-compose down -v

# 数据库备份（Windows）
backup.bat

# 数据库备份（Linux）
./backup.sh

# 进入MySQL执行SQL
docker exec -it gf-mysql mysql -u root -p guarantee_finance

# 进入Redis
docker exec -it gf-redis redis-cli -a your_redis_password
```

## 默认账号
- 用户名: admin
- 密码: admin123
- **生产环境部署后请立即修改默认密码！**

## 安全检查清单
- [ ] 修改.env中所有默认密码
- [ ] JWT_SECRET改为复杂随机字符串
- [ ] NC Cloud连接信息配置正确
- [ ] CFCA证书文件放置到certs目录
- [ ] 防火墙开放80、8080端口（如需外部访问）
- [ ] 配置HTTPS证书（生产环境必须）
- [ ] 定期执行数据库备份
