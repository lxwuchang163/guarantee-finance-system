# MySQL 5.5 版本适配计划

## 目标
将项目从MySQL 8.0降级适配到MySQL 5.5版本，确保所有功能正常运行。

## MySQL 5.5与8.0的主要差异

### 1. 字符集和排序规则
- MySQL 5.5默认字符集是`latin1`，需要显式设置为`utf8`
- `utf8mb4`在MySQL 5.5中支持但可能有限制
- 不支持`utf8mb4_0900_ai_ci`等新的排序规则

### 2. 身份验证插件
- MySQL 8.0默认使用`caching_sha2_password`
- MySQL 5.5使用`mysql_native_password`（已兼容）

### 3. SQL语法差异
- MySQL 5.5不支持`DROP TABLE IF EXISTS`在事务中（部分版本）
- `CURRENT_TIMESTAMP`在MySQL 5.5中作为默认值有限制
- 不支持多值索引、降序索引等新特性

### 4. 驱动兼容性
- MySQL 8.0驱动连接MySQL 5.5需要`allowPublicKeyRetrieval=true`
- 建议使用较新的驱动版本但保持兼容参数

## 实施步骤

### 步骤1: 修改Docker Compose配置
**文件**: `docker-compose.yml`

**修改内容**:
1. 将MySQL镜像从`mysql:8.0`改为`mysql:5.5`
2. 移除MySQL 8.0特有的环境变量
3. 调整命令行参数（移除5.5不支持的参数）
4. 更新健康检查命令（5.5的mysqladmin路径可能不同）

**预期输出**:
- Docker使用MySQL 5.5镜像
- 容器能正常启动

### 步骤2: 修改JDBC连接配置
**文件**: 
- `application.yml`
- `application-prod.yml`
- `application-test.yml`

**修改内容**:
1. 将驱动类从`com.mysql.cj.jdbc.Driver`改为`com.mysql.jdbc.Driver`（5.5兼容）
2. 或保持`com.mysql.cj.jdbc.Driver`但确保URL参数兼容
3. 检查并移除MySQL 8.0特有的连接参数

**预期输出**:
- 应用能正常连接MySQL 5.5

### 步骤3: 修改SQL初始化脚本
**文件**: 
- `sql/init.sql`
- `sql/task7-11-supplement.sql`

**修改内容**:
1. 确保所有表使用`utf8`字符集（如utf8mb4不兼容则改用utf8）
2. 检查`CURRENT_TIMESTAMP`默认值的使用
3. 检查`ON UPDATE CURRENT_TIMESTAMP`语法（MySQL 5.5部分支持）
4. 确保索引长度不超过5.5的限制（767字节）
5. 检查VARCHAR长度（5.5中最大65535字节）

**预期输出**:
- SQL脚本能在MySQL 5.5中正常执行

### 步骤4: 修改pom.xml依赖
**文件**: `guarantee-finance-backend/pom.xml`

**检查内容**:
1. 确认MySQL驱动版本与5.5兼容
2. 如需降级驱动版本则修改

**预期输出**:
- 编译通过
- 运行时驱动兼容

### 步骤5: 更新文档
**文件**: 
- `README.md`
- `DEPLOY.md`

**修改内容**:
1. 将MySQL版本要求从8.0+改为5.5+
2. 更新部署说明

### 步骤6: 测试并提交Git
**测试内容**:
1. 使用Docker启动MySQL 5.5
2. 执行SQL初始化脚本
3. 启动应用测试连接
4. 验证基本CRUD功能

**Git提交**:
```bash
git add .
git commit -m "chore: 适配MySQL 5.5版本

- Docker镜像降级到mysql:5.5
- 修改JDBC连接配置兼容5.5
- 调整SQL脚本字符集和语法
- 更新文档说明"
git push origin master:main
```

## 文件清单

### 需要修改的文件
1. `docker-compose.yml` - Docker镜像和配置
2. `application.yml` - JDBC驱动和URL
3. `application-prod.yml` - 生产环境配置
4. `application-test.yml` - 测试环境配置
5. `sql/init.sql` - 字符集和语法调整
6. `sql/task7-11-supplement.sql` - 字符集和语法调整
7. `pom.xml` - 驱动版本检查
8. `README.md` - 文档更新
9. `DEPLOY.md` - 文档更新

## 关键变更点

### Docker Compose变更
```yaml
# 修改前
image: mysql:8.0
command:
  --default-authentication-plugin=mysql_native_password

# 修改后
image: mysql:5.5
# 移除default-authentication-plugin参数（5.5不支持）
```

### JDBC配置变更
```yaml
# 修改前
driver-class-name: com.mysql.cj.jdbc.Driver

# 修改后（可选，cj驱动也可兼容5.5）
driver-class-name: com.mysql.jdbc.Driver
```

### SQL变更
```sql
-- 确保使用utf8而非utf8mb4（如果需要）
DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci

-- 检查CURRENT_TIMESTAMP使用
-- MySQL 5.5中DATETIME类型不支持CURRENT_TIMESTAMP默认值
-- 需要改为TIMESTAMP类型或使用触发器
```

## 验收标准
- [ ] Docker能正常启动MySQL 5.5容器
- [ ] SQL脚本能在5.5中正常执行
- [ ] 应用能正常连接5.5数据库
- [ ] 所有功能测试通过
- [ ] 文档已更新
- [ ] 所有变更提交到Git
