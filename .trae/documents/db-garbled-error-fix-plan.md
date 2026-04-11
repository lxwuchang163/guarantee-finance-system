# 数据库乱码修复 + 系统内部错误修复计划

---

## 一、问题根因分析

### 问题 1: 数据库乱码

| 层面 | 原因 | 影响 |
|------|------|------|
| **SQL 文件执行方式** | PowerShell 的 `Get-Content \| mysql` 管道默认用系统编码(GBK)读取 UTF-8 文件，导致中文被转码为乱码 | 所有中文数据（机构名、用户昵称、菜单名称等）变成 `???` |
| **数据库字符集** | 虽然表定义为 `utf8mb4`，但写入时连接未指定字符集 | 数据存储时已损坏 |
| **application.yml** | JDBC URL 已有 `characterEncoding=utf-8`，但数据本身已是乱码 | 后端读出时仍然是乱码 |

**验证方法**: 查询数据库中的中文字段是否显示为 `???` 或乱码字符。

### 问题 2: "系统内部错误，请联系管理员"

这是 [GlobalExceptionHandler.java](file:///c:\Users\Administrator/Desktop/Business-Finance System/guarantee-finance-backend/src/main/java/com/guarantee/finance/config/GlobalExceptionHandler.java) 第 59 行的兜底异常：

```java
@ExceptionHandler(Exception.class)
public R<?> handleException(Exception e) {
    log.error("系统异常: {}", e.getMessage(), e);
    return R.fail("系统内部错误，请联系管理员");
}
```

**可能的触发原因（按优先级排序）：**

| 优先级 | 可能原因 | 触发接口 |
|--------|----------|----------|
| P0 | **实体类字段与数据库列不匹配** → MyBatis 抛出 `BadSqlGrammarException` | 已修复 BankAccountConfig，需检查其他实体 |
| P1 | **Service 层空指针或类型转换异常** | 登录后跳转 Dashboard 时加载各模块数据 |
| P2 | **前端请求了不存在的接口路径** | 某些 API 路径可能与 Controller 不匹配 |
| P3 | **JSON 序列化异常** | 返回数据包含 null 或循环引用 |

---

## 二、实施步骤

### 步骤 1: 彻底解决数据库乱码

#### 1.1 删除旧数据库并重建（确保全新开始）
```sql
DROP DATABASE IF EXISTS guarantee_finance;
CREATE DATABASE guarantee_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

#### 1.2 用正确编码执行 init.sql
**关键**: 不能用 PowerShell 管道！必须用以下任一方式：

**方案 A（推荐）: cmd 直接执行**
```bash
cmd /c "mysql -u root -p123 --default-character-set=utf8mb4 guarantee_finance < c:\path\to\init.sql"
```

**方案 B: mysql source 命令**
```bash
mysql -u root -p123 --default-character-set=utf8mb4 guarantee_finance -e "SOURCE c:/path/to/init.sql"
```

#### 1.3 验证中文数据正确性
```sql
SELECT id, username, nickname FROM sys_user;
-- 期望: 1, admin, 系统管理员 (非乱码)
SELECT id, org_name FROM sys_org;
-- 期望: 中文机构名称
SELECT id, menu_name FROM sys_menu WHERE menu_type = 2;
-- 期望: 银企直连、CFCA认证 等
```

#### 1.4 确保 init.sql 文件编码正确
- 文件必须保存为 **UTF-8 without BOM**（推荐）或 **UTF-8 with BOM**
- 不能是 GBK/GB2312 编码

---

### 步骤 2: 全面排查并修复 "系统内部错误"

#### 2.1 检查所有实体类与数据库表字段一致性

逐个对比以下实体类与 init.sql 中对应的 CREATE TABLE：

| 实体类 | 数据库表 | 需检查项 |
|--------|----------|----------|
| SysUser | sys_user | ✅ 已确认一致 |
| SysMenu | sys_menu | 需检查 |
| AccVoucher | acc_voucher | 需检查（有 remark 重复字段问题！）|
| FinReceipt | fin_receipt | 需检查 |
| FinPayment | fin_payment | 需检查 |
| BizCustomer | biz_customer | 需检查 |
| BankReconciliation | bank_reconciliation | 需检查 |
| CfcaCertificate | cfca_certificate | 需检查 |
| ScheduleJob / ScheduleJobLog | schedule_job / schedule_job_log | 需检查 |

**已知问题**: AccVoucher 实体同时从 BaseEntity 继承了 `remark` 字段，自身也定义了 `remark`，可能导致冲突！

#### 2.2 修复 GlobalExceptionHandler — 增加更详细的异常信息

将兜底异常信息从笼统的 "系统内部错误" 改为包含具体异常类型和消息（开发环境），方便定位：

```java
@ExceptionHandler(Exception.class)
public R<?> handleException(Exception e) {
    log.error("系统异常: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
    String message = e.getMessage();
    if (message != null && message.length() > 100) {
        message = message.substring(0, 100) + "...";
    }
    return R.fail(500, "系统内部错误: " + message);
}
```

#### 2.3 对前端高频接口做逐一测试验证

登录后，按以下顺序测试每个模块页面是否正常加载：

| 序号 | 页面 | 接口 | 预期 |
|------|------|------|------|
| 1 | Dashboard | GET /api/auth/userinfo | 200 + 用户信息 |
| 2 | 银企直连 | GET /api/bank/account/list | 200 + 3条账户 |
| 3 | CFCA认证 | GET /api/cfca/certificates | 200 |
| 4 | 科目管理 | GET /api/accounting/subject/list | 200 |
| 5 | 凭证管理 | GET /api/accounting/voucher/page | 200 |
| 6 | 收款单 | GET /api/receipt/page | 200 |
| 7 | 付款单 | GET /api/payment/page | 200 |
| 8 | 银行对账 | GET /api/reconciliation/page | 200 |
| 9 | 定时任务 | GET /api/schedule/jobs | 200 |
| 10 | 用户管理 | GET /api/system/user/page | 200 |
| 11 | 角色管理 | GET /api/system/role/list | 200 |
| 12 | 菜单管理 | GET /api/system/menu/tree | 200 |
| 13 | 机构管理 | GET /api/system/org/tree | 200 |

对每个返回 500 的接口，查看后端日志定位具体异常栈。

---

## 三、涉及修改的文件清单

| # | 文件 | 操作 | 说明 |
|---|------|------|------|
| 1 | `sql/init.sql` | 检查/确保 UTF-8 编码 | 确保无乱码 |
| 2 | `GlobalExceptionHandler.java` | 修改 | 兜底异常增加详细信息 |
| 3 | `AccVoucher.java` | 检查/修复 | 移除重复 remark 字段 |
| 4 | 其他实体类（按需） | 检查/修复 | @TableField(exist=false) 补充 |

---

## 四、预期效果

1. ✅ 数据库所有中文数据显示正常（机构名、用户昵称、菜单名等）
2. ✅ 登录后所有模块页面不再出现 "系统内部错误"
3. ✅ 即使仍有异常，错误消息会显示具体原因而非笼统提示
