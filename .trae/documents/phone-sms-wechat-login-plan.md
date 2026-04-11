# 手机+短信验证码登录与微信二维码接入实现计划

## 1. 仓库分析结论

### 1.1 现有登录系统架构
- **AuthController**：处理登录认证API，包括 `/auth/login`、`/auth/register` 等接口
- **JwtUtils**：JWT令牌管理工具，负责生成、解析和验证令牌
- **SecurityConfig**：Spring Security配置，设置安全策略和过滤器
- **JwtAuthenticationFilter**：JWT认证过滤器，处理请求的身份验证
- **SysUser**：用户实体类，已包含 `phone` 字段
- **数据库**：`sys_user` 表已存在，包含 `phone` 字段

### 1.2 现有依赖
- Spring Boot 3.2.5
- Spring Security 6.x
- MyBatis-Plus 3.5.6
- Hutool 5.8.25
- JWT 0.12.5

## 2. 实现方案

### 2.1 手机+短信验证码登录

#### 2.1.1 数据库表修改
- **修改 `sys_user` 表**：
  - 无需修改表结构，已有 `phone` 字段
  - 可选：添加 `last_login_time` 字段记录登录时间

- **新增 `sys_sms_code` 表**：
  | 字段名 | 类型 | 长度 | 注释 |
  |--------|------|------|------|
  | `id` | `BIGINT` | - | 主键ID |
  | `phone` | `VARCHAR` | 20 | 手机号 |
  | `code` | `VARCHAR` | 6 | 验证码 |
  | `expire_time` | `DATETIME` | - | 过期时间 |
  | `status` | `INT` | 1 | 状态(0-未使用,1-已使用) |
  | `create_time` | `DATETIME` | - | 创建时间 |
  | `update_time` | `DATETIME` | - | 更新时间 |

#### 2.1.2 后端实现
- **新增 `SmsService` 接口**：
  - `sendSmsCode(String phone)`：发送短信验证码
  - `verifySmsCode(String phone, String code)`：验证短信验证码

- **新增 `SmsServiceImpl` 实现**：
  - 生成6位随机验证码
  - 存储验证码到 `sys_sms_code` 表
  - 调用短信服务API发送验证码（使用第三方短信服务）
  - 验证验证码的有效性和过期时间

- **修改 `AuthController`**：
  - 新增 `/auth/sendSms` 接口：发送短信验证码
  - 新增 `/auth/loginBySms` 接口：短信验证码登录

- **修改 `SysUserService`**：
  - 新增 `getUserByPhone(String phone)` 方法：通过手机号获取用户

- **修改 `SecurityConfig`**：
  - 允许 `/auth/sendSms` 和 `/auth/loginBySms` 接口的匿名访问

#### 2.1.3 前端实现
- **修改 `src/api/auth.ts`**：
  - 新增 `sendSmsCode(phone)` 方法
  - 新增 `loginBySms(phone, code)` 方法

- **修改 `src/views/login/index.vue`**：
  - 添加短信验证码登录选项
  - 实现手机号输入、验证码发送和验证功能
  - 添加倒计时功能

### 2.2 微信二维码登录

#### 2.2.1 数据库表修改
- **新增 `sys_wechat_user` 表**：
  | 字段名 | 类型 | 长度 | 注释 |
  |--------|------|------|------|
  | `id` | `BIGINT` | - | 主键ID |
  | `openid` | `VARCHAR` | 100 | 微信OpenID |
  | `unionid` | `VARCHAR` | 100 | 微信UnionID |
  | `nickname` | `VARCHAR` | 50 | 微信昵称 |
  | `avatar` | `VARCHAR` | 255 | 微信头像 |
  | `sex` | `INT` | 1 | 性别 |
  | `user_id` | `BIGINT` | - | 关联系统用户ID |
  | `create_time` | `DATETIME` | - | 创建时间 |
  | `update_time` | `DATETIME` | - | 更新时间 |

#### 2.2.2 后端实现
- **新增 `WechatService` 接口**：
  - `generateQrCode()`：生成微信登录二维码
  - `getQrCodeStatus(String ticket)`：获取二维码扫描状态
  - `processWechatLogin(String code)`：处理微信登录回调

- **新增 `WechatServiceImpl` 实现**：
  - 集成微信开放平台API
  - 生成二维码并存储临时状态
  - 处理微信登录回调，获取用户信息
  - 绑定或创建系统用户

- **修改 `AuthController`**：
  - 新增 `/auth/wechat/qrCode` 接口：获取微信登录二维码
  - 新增 `/auth/wechat/callback` 接口：微信登录回调
  - 新增 `/auth/wechat/checkStatus` 接口：检查二维码扫描状态

- **修改 `SecurityConfig`**：
  - 允许微信登录相关接口的匿名访问

#### 2.2.3 前端实现
- **修改 `src/api/auth.ts`**：
  - 新增 `getWechatQrCode()` 方法
  - 新增 `checkWechatLoginStatus(ticket)` 方法

- **修改 `src/views/login/index.vue`**：
  - 添加微信登录选项
  - 实现二维码显示和状态轮询
  - 处理微信登录成功后的跳转

## 3. 依赖添加

### 3.1 后端依赖
- **短信服务依赖**：
  - 可选：阿里云短信服务SDK或其他短信服务SDK

- **微信SDK依赖**：
  ```xml
  <dependency>
      <groupId>com.github.binarywang</groupId>
      <artifactId>weixin-java-mp</artifactId>
      <version>4.5.0</version>
  </dependency>
  ```

### 3.2 前端依赖
- **二维码生成库**：
  ```bash
  npm install qrcode
  ```

## 4. 风险处理

### 4.1 短信验证码风险
- **频率限制**：限制同一手机号的短信发送频率（如每分钟1条，每天10条）
- **验证码有效期**：设置合理的验证码有效期（如5分钟）
- **防恶意攻击**：添加IP限制和验证码验证

### 4.2 微信登录风险
- **回调URL验证**：确保回调URL的安全性
- **状态验证**：使用随机状态参数防止CSRF攻击
- **用户绑定**：首次微信登录时引导用户绑定手机号

### 4.3 数据库风险
- **数据验证**：确保手机号和微信OpenID的唯一性
- **索引优化**：为 `phone` 和 `openid` 字段添加索引

## 5. 实现步骤

### 5.1 数据库修改
1. 创建 `sys_sms_code` 表
2. 创建 `sys_wechat_user` 表
3. 可选：修改 `sys_user` 表，添加 `last_login_time` 字段

### 5.2 后端实现
1. 新增短信服务相关类
2. 新增微信服务相关类
3. 修改 AuthController，添加新接口
4. 修改 SysUserService，添加通过手机号获取用户的方法
5. 修改 SecurityConfig，配置新接口的权限
6. 添加必要的配置项（如短信服务API密钥、微信AppID等）

### 5.3 前端实现
1. 修改 auth.ts，添加新的API方法
2. 修改 login.vue，添加短信验证码登录和微信登录选项
3. 实现相关的UI和交互逻辑

### 5.4 测试和验证
1. 测试短信验证码发送和登录功能
2. 测试微信二维码登录功能
3. 验证安全性和性能

## 6. 配置项

### 6.1 后端配置
- **短信服务配置**：
  - `sms.accessKeyId`：短信服务API密钥ID
  - `sms.accessKeySecret`：短信服务API密钥
  - `sms.signName`：短信签名
  - `sms.templateCode`：短信模板ID

- **微信配置**：
  - `wechat.appId`：微信AppID
  - `wechat.appSecret`：微信AppSecret
  - `wechat.redirectUri`：微信回调URL

### 6.2 前端配置
- **API配置**：
  - 微信登录相关API路径
  - 短信验证码相关API路径

## 7. 预期成果

### 7.1 功能实现
- ✅ 手机+短信验证码登录
- ✅ 微信二维码登录
- ✅ 完整的登录流程
- ✅ 安全的身份验证

### 7.2 技术指标
- 短信验证码发送响应时间：< 3秒
- 微信二维码生成响应时间：< 1秒
- 登录成功率：> 99%
- 安全性：符合企业级应用标准

## 8. 注意事项

- **短信服务费用**：使用第三方短信服务会产生费用，请确保有足够的余额
- **微信开发者账号**：需要微信开放平台开发者账号和已认证的应用
- **域名配置**：微信回调URL需要使用已备案的域名
- **安全性**：所有敏感操作都需要进行安全验证
- **兼容性**：确保在不同设备和浏览器上的兼容性

## 9. 后续优化

- **记住登录状态**：实现记住密码功能
- **多因素认证**：支持多种认证方式组合
- **登录日志**：记录详细的登录日志，便于审计
- **异常处理**：完善异常处理和错误提示
- **性能优化**：优化登录流程的响应速度