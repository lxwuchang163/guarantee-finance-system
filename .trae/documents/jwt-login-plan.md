# JWT登录功能实现计划

## 目标
实现一个简洁的JWT登录功能，仅支持用户名和密码登录，包含完整的后端认证和前端登录页面。

## 现状分析
- 后端：已有基础的JWT工具类(JwtUtils)和登录接口框架
- 前端：已有登录页面UI，但缺少JWT token的完整处理
- 需要完善：后端登录逻辑、token刷新机制、前端token存储和请求拦截

## 实施步骤

### 步骤1: 完善后端登录服务
**文件**: `SysUserServiceImpl.java`

**修改内容**:
1. 实现 `login(LoginDTO loginDTO)` 方法
   - 验证用户名和密码
   - 使用BCrypt校验密码
   - 生成JWT token
   - 返回token和用户信息

2. 实现 `logout(Long userId)` 方法
   - 将token加入黑名单（使用Redis）
   - 或记录token失效时间

**预期输出**:
- 用户可以通过用户名密码登录
- 登录成功后返回JWT token

### 步骤2: 完善JWT认证过滤器
**文件**: `JwtAuthenticationFilter.java`

**修改内容**:
1. 从请求头中提取token
2. 验证token有效性
3. 设置Spring Security上下文
4. 处理token过期情况

**预期输出**:
- 所有受保护接口都需要有效token
- token无效时返回401错误

### 步骤3: 配置SecurityConfig
**文件**: `SecurityConfig.java`

**检查/修改内容**:
1. 配置登录接口免认证
2. 配置JWT过滤器
3. 配置CORS（跨域）

**预期输出**:
- /auth/login 接口无需认证
- 其他接口需要JWT token

### 步骤4: 完善前端登录页面
**文件**: `login/index.vue`

**当前状态**: UI已完成，逻辑已连接store

**检查内容**:
1. 确认登录表单提交正确
2. 错误提示完善

### 步骤5: 完善前端请求拦截器
**文件**: `utils/request.ts`

**修改内容**:
1. 从localStorage读取token
2. 添加到请求头 Authorization: Bearer {token}
3. 处理401错误（token过期）
4. 自动跳转到登录页

**预期输出**:
- 所有API请求自动携带token
- token过期时自动跳转登录页

### 步骤6: 完善前端用户状态管理
**文件**: `store/user.ts`

**检查内容**:
1. token存储到localStorage ✓ 已有
2. 登录成功后获取用户信息 ✓ 已有
3. 登出时清除token ✓ 已有

### 步骤7: 测试并提交Git
**测试内容**:
1. 使用admin/admin123登录
2. 验证token生成和存储
3. 验证受保护接口访问
4. 验证token过期处理

**Git提交**:
```bash
git add .
git commit -m "feat: 完善JWT登录功能

- 实现后端登录认证和token生成
- 完善JWT过滤器验证机制
- 前端请求自动携带token
- 处理token过期自动跳转"
git push origin master:main
```

## 文件清单

### 后端修改
1. `service/impl/SysUserServiceImpl.java` - 登录/登出实现
2. `security/JwtAuthenticationFilter.java` - token验证过滤器
3. `config/SecurityConfig.java` - 安全配置

### 前端修改
1. `utils/request.ts` - 请求拦截器添加token
2. `login/index.vue` - 错误处理优化（可选）

### 无需修改（已有）
1. `security/JwtUtils.java` - token生成/解析
2. `controller/AuthController.java` - 登录接口
3. `store/user.ts` - 用户状态管理
4. `api/auth.ts` - 登录API

## 验收标准
- [ ] 用户名密码登录成功返回token
- [ ] token存储到localStorage
- [ ] 后续请求自动携带token
- [ ] token无效/过期时返回401并跳转登录页
- [ ] 登出功能正常清除token
- [ ] 所有变更提交到Git
