# 系统全面修复计划

## 目标
修复所有后端编译错误，补全缺失的数据库表，修复前端API不匹配问题，确保所有模块功能正常使用。

---

## 第一阶段：修复后端编译错误（P0 - 最高优先级）

### 任务1：修复 R.ok() 方法调用错误
**涉及文件**：
- `ReceiptController.java` - 7处错误（行50参数顺序错误，行57/64/71/78/85/92类型不兼容）
- `PaymentController.java` - 7处类型不兼容（行57/64/71/78/85/92/99）
- `BankController.java` - 1处错误（行63）
- `SyncController.java` - 2处错误（行44/51）
- `ReconciliationController.java` - 3处错误（行27/49/63）
- `CfcaController.java` - 2处错误（行31参数顺序，行50类型不兼容）

**修复方式**：
1. `R.ok(data, "消息")` → `R.ok("消息", data)` （交换参数顺序，匹配 `R.ok(String message, T data)`）
2. `R.ok("消息")` → `R.ok("消息", null)` （匹配 `R.ok(String message, T data)`，避免返回 `R<String>` 赋值给 `R<Void>`）

### 任务2：修复 AccountingPlatformServiceImpl.java 实体方法调用错误（18处）
**涉及文件**：`AccountingPlatformServiceImpl.java`

**修复方式**：
- `voucher.setVoucherTypeName(...)` → 删除（VO层展示字段）
- `voucher.setAccountingPeriod(...)` → `voucher.setPeriod(...)`
- `voucher.setMaker(...)` → `voucher.setCreateUserId(...)`
- `voucher.setSourceBillType(...)` → `voucher.setSourceType(...)`
- `voucher.setSourceBillNo(...)` → `voucher.setSourceId(...)`
- `voucher.setTotalDebit(amount)` → 删除或添加字段到实体
- `voucher.setTotalCredit(amount)` → 删除或添加字段到实体
- `voucher.setAuditor(...)` → `voucher.setApproveUserId(...)`
- `debitDetail.setAccountCode("1002")` → `debitDetail.setSubjectCode("1002")`
- `debitDetail.setAccountName(...)` → `debitDetail.setSubjectName(...)`
- `debitDetail.getAccountCode()` → `debitDetail.getSubjectCode()`
- `debitDetail.setDirection("DEBIT")` → 删除，改用 `setDebitAmount`
- `debitDetail.setAmount(...)` → `debitDetail.setDebitAmount(...)`
- creditDetail 同理修复
- `log.error(String, Long, Exception)` → `log.error(String, Throwable)`
- `log.info(String, int, int)` → `log.info(String.format(...))`

### 任务3：修复 BizCustomerServiceImpl.java 日志调用错误
**修复方式**：在类上添加 `@Slf4j` 注解

### 任务4：修复 NcCloudServiceImpl.java 方法名错误
**修复方式**：`syncLog.setErrorMsg(errorMsg)` → `syncLog.setErrorMessage(errorMsg)`

### 任务5：修复 AccVoucherServiceImpl.java Mapper方法缺失
**修复方式**：在 `AccVoucherDetailMapper` 中添加 `selectByVoucherId` 方法声明

### 任务6：修复 SysOrgController.java DTO字段缺失
**修复方式**：在 `OrgDTO` 中添加 `id` 字段

### 任务7：修复 BankReconciliationServiceImpl.java BigDecimal类型错误
**修复方式**：`BigDecimal.valueOf(v.toString())` → `BigDecimal.valueOf(((Number) v).doubleValue())`

---

## 第二阶段：补全缺失的数据库表（P0 - 最高优先级）

### 任务8：创建审批流程相关表
需新增4张表：
- `process_definition` - 流程定义表
- `process_node` - 流程节点表
- `process_instance` - 流程实例表
- `approve_record` - 审批记录表

### 任务9：创建银行相关表
需新增3张表：
- `bank_account_config` - 银行账户配置表
- `bank_transaction` - 银行流水表
- `bank_reconciliation` - 银行对账表

### 任务10：创建NC同步相关表
需新增1张表：
- `nc_sync_log` - NC同步日志表

### 任务11：创建其他缺失表
- `nc_sync_error` - NC同步错误表
- `cfca_certificate` - CFCA证书表
- `schedule_task_config` / `schedule_job` / `schedule_job_log` - 定时任务相关表
- `sync_task` / `sync_task_log` / `sync_error_record` - 同步任务相关表

---

## 第三阶段：修复前端API不匹配问题（P1 - 高优先级）

### 任务12：修复银行对账模块前端API（6处）
- 导入流水路径：`/api/reconciliation/import` → `/api/reconciliation/transaction/import`
- 手工匹配方法：`POST` → `PUT`，参数改为 `@RequestParam` 格式
- 对账结果路径：`/api/reconciliation/result/list` → `/api/reconciliation/result`
- 余额调节表：`POST /{id}/balance-adjustment` → `GET /balanceAdjustment`
- 查询参数名：`bankAccountNo` → `accountNo`，`direction` → `transactionType`

### 任务13：修复银企直连模块前端API（3处）
- 批量余额路径：`/api/bank/batch-balance` → `/api/bank/balance/batch`
- 下载流水路径：`/api/bank/transactions` → `/api/bank/transactions/download`
- 付款状态路径：`/api/bank/payment-status` → `/api/bank/payment/status`

### 任务14：修复CFCA认证模块前端API（4处）
- 到期检查路径：`/api/cfca/expiry-check` → `/api/cfca/expiry/check`
- 刷新证书：`PUT /api/cfca/refresh` → `POST /api/cfca/expiry/refresh`
- 签名响应结构：后端返回 `R<Boolean>`，前端期望 `CfcaSignResult`
- 验签参数格式：JSON body → `@RequestParam`

---

## 第四阶段：修复前端页面问题（P1 - 高优先级）

### 任务15：修复机构管理页面
- 添加 `exportOrgs` 函数导入
- 替换 `context-menu` 组件为 `el-dropdown`

### 任务16：修复用户管理页面
- 添加 `UserQueryDTO` 类型导入
- 新增用户时密码设为必填

### 任务17：修复审批管理页面
- 修复HTML闭合标签：`</button>` → `</el-button>`
- 添加驳回审批必填验证
- 移除无效的分页UI或后端添加分页支持

### 任务18：修复值类型不一致问题
- 科目管理：`value="1"` → `:value="1"`（状态、类型、余额方向）
- 凭证管理：`value="1"` → `:value="1"`（凭证类型）
- 辅助核算：`value="1"` → `:value="1"`（状态）
- 菜单权限：`value="1"` → `:value="1"`（菜单类型、状态）

### 任务19：完善菜单权限表单验证
- 添加 `menuFormRules` 验证规则

### 任务20：完善凭证管理功能
- 实现查看详情对话框
- 添加借贷平衡校验

---

## 第五阶段：验证与测试（P2 - 中优先级）

### 任务21：后端编译验证
- 运行 `mvn clean compile` 确保无编译错误
- 启动后端服务确认无运行时错误

### 任务22：前端编译验证
- 运行 `npm run build` 确保无编译错误
- 启动前端服务确认页面正常加载

### 任务23：功能验证
- 验证登录功能
- 验证系统管理各模块的CRUD操作
- 验证业务模块的API调用

---

## 执行顺序
1. 第一阶段（任务1-7）→ 确保后端编译通过
2. 第二阶段（任务8-11）→ 确保数据库表完整
3. 第三阶段（任务12-14）→ 确保前后端API对接
4. 第四阶段（任务15-20）→ 确保前端页面功能正常
5. 第五阶段（任务21-23）→ 全面验证
