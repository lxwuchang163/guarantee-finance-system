# 数据库重新初始化 + 登录完善 + 403错误修复计划

---

## 一、问题根因分析

### 1.1 403 错误的完整链路分析

```
前端请求 GET /api/bank/account/list
    ↓ 携带 Authorization: Bearer <token>
Spring Security SecurityFilterChain
    ↓ .anyRequest().authenticated() — 需要认证
JwtAuthenticationFilter.doFilterInternal()
    ↓ getToken() 从 Header 提取 token
    ↓ jwtUtils.validateToken(token) 验证 JWT
    ↓ 如果 token 无效/过期 → 不设置 Authentication
    ↓ filterChain.doFilter() 继续过滤链
    ↓ SecurityContext 中无认证信息
Spring Security AuthorizationFilter
    ↓ 发现未认证 → 抛出 AccessDeniedException
    ↓ 返回 HTTP 403 Forbidden ❌
```

**三个导致 403 的场景：**

| 场景 | 原因 | 触发条件 |
|------|------|----------|
| A | 用户未登录，无 token | 直接访问页面或 token 被清空 |
| B | Token 过期或无效 | JWT 签名不匹配 / 过期时间到 |
| C | AccessDeniedException 未被全局捕获 | 方法级权限校验失败时 |

### 1.2 当前代码中的具体 Bug

#### Bug #1: 密码不匹配（登录失败 → 无 token → 403）
- **init.sql** 插入密码为明文 `'123456'`
- **登录页** 显示默认账号 `admin / admin123`
- **后端 SysUserServiceImpl.login()** 使用 `BCrypt.checkpw()` 校验，有明文 fallback
- **结果**: 用 `admin123` 登录会失败，拿不到 token

#### Bug #2: 全局异常处理器缺少 AccessDeniedException 处理
- **GlobalExceptionHandler.java** 只处理了 RuntimeException、Validation、Bind 等
- **缺失**: `AccessDeniedException` 和 `AuthenticationException` 的处理
- **结果**: 403 错误返回原始 HTML/JSON 而非友好提示

#### Bug #3: 前端响应拦截器未处理 403 状态码
- **request.ts** 只处理了 `res.code === 401`（业务码）
- **缺失**: HTTP 层面的 403/401 状态码处理
- **结果**: 403 时只显示 "网络错误"，不会跳转登录页

#### Bug #4: 数据库表不完整
- `init.sql` 创建了基础表但部分表（如 `bank_account_config`）是后来单独创建的
- 缺少完整的初始数据（银行账户示例数据等）

#### Bug #5: JwtAuthenticationFilter 中 redisEnabled 硬编码为 false
- Redis 已被 application.yml 注释掉
- 但 Filter 中 `private boolean redisEnabled = false;` 是硬编码
- 虽然 Redis 关闭时不影响功能，但设计不够健壮

---

## 二、实施步骤

### 步骤 1: 数据库重新初始化

#### 1.1 删除旧数据库并重建
```sql
DROP DATABASE IF EXISTS guarantee_finance;
CREATE DATABASE guarantee_finance DEFAULT CHARSET utf8mb4;
```

#### 1.2 执行完整的 init.sql（包含所有表）
- 确保包含所有 30+ 张表的 CREATE 语句
- 包含 `bank_account_config` 表（之前是单独创建的）

#### 1.3 修复初始用户密码
- 将 `sys_user` 表中密码改为 **BCrypt 加密后的值**
- 默认账号: `admin` / `admin123`（与登录页一致）
- BCrypt hash of `admin123`: `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH`（运行时动态生成）

#### 1.4 插入银行账户示例数据
```sql
INSERT INTO bank_account_config (account_no, account_name, bank_name, bank_code, currency, status, org_id, balance, api_status, threshold_warning, threshold_stop, daily_limit, single_limit)
VALUES ('6222021001001234567', '担保集团基本户', '中国工商银行', 'ICBC', 'CNY', 1, 1, 1256780.50, 1, 100000.00, 50000.00, 5000000.00, 1000000.00);
```

### 步骤 2: 完善登录功能

#### 2.1 修改 init.sql 中的用户密码
- 使用 BCrypt 哈希值替代明文密码
- 确保 admin/admin123 可以正常登录

#### 2.2 增强 GlobalExceptionHandler
添加以下异常处理：
```java
@ExceptionHandler(AccessDeniedException.class)
public R<?> handleAccessDeniedException(AccessDeniedException e) {
    return R.fail(403, "没有访问权限");
}

@ExceptionHandler(AuthenticationException.class)
public R<?> handleAuthenticationException(AuthenticationException e) {
    return R.fail(401, "认证失败: " + e.getMessage());
}
```

#### 2.3 改进 JwtAuthenticationFilter
- 当 token 无效时返回 401 JSON 而非继续过滤链（避免后续 403）
- 确保 redisEnabled 根据实际 Redis 连接状态自动判断

### 步骤 3: 修复 403 错误（前后端协同）

#### 3.1 后端修复
1. **GlobalExceptionHandler** — 添加 `AccessDeniedException` 处理，返回 `{code: 403, message: "..."}` 格式
2. **SecurityConfig** — 可选：对部分只读接口添加 `.permitAll()` 或调整安全策略
3. **JwtAuthenticationFilter** — token 无效时直接返回 401 响应并短路，不再进入过滤链

#### 3.2 前端修复
1. **request.ts 响应拦截器** — 增加 HTTP 403 状态码处理：
```typescript
if (error.response?.status === 403) {
    ElMessage.error('登录已过期，请重新登录')
    userStore.resetState()
    router.push('/login')
}
```
2. **request.ts 响应拦截器** — 增加 HTTP 401 状态码处理：
```typescript
if (error.response?.status === 401) {
    ElMessage.error('Token已失效')
    userStore.resetState()
    router.push('/login')
}
```

### 步骤 4: 编译验证 & 测试

#### 4.1 后端编译打包
```bash
mvn clean package -DskipTests
java -jar target/guarantee-finance-backend-1.0.0.jar
```

#### 4.2 前端编译
```bash
npm run build
npm run dev
```

#### 4.3 功能验证清单
- [ ] 使用 admin/admin123 成功登录
- [ ] 登录后跳转到 Dashboard 页面
- [ ] 银企直连页面正常加载账户列表（不再 403）
- [ ] 所有其他模块页面正常加载数据
- [ ] Token 过期后自动跳转登录页
- [ ] 退出登录后访问需要认证的接口返回 403 并跳转登录页

---

## 三、涉及修改的文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `sql/init.sql` | 修改 | 密码改 BCrypt，补全所有表，增加示例数据 |
| `GlobalExceptionHandler.java` | 修改 | 添加 AccessDeniedException / AuthenticationException 处理 |
| `JwtAuthenticationFilter.java` | 修改 | token 无效时短路返回 401 |
| `src/utils/request.ts` | 修改 | 增加 403/401 HTTP 状态码处理 |

---

## 四、预期效果

1. ✅ 数据库完整初始化，所有表和数据齐全
2. ✅ 使用 `admin / admin123` 可成功登录
3. ✅ 登录后所有模块接口正常调用，不再出现 403
4. ✅ Token 过期/无效时，前端自动跳转登录页并提示
5. ✅ 退出登录后，再次访问需认证接口时正确拦截
