# 担保集团业务财务系统 - 五大功能模块实施计划

## 概述

本计划涵盖5个主要功能模块的开发，基于现有的Spring Boot + MyBatis-Plus + Vue 3技术栈，在现有47个实体类、24个控制器、30个服务的基础上进行扩展。

---

## 模块一：基础管理中增加公告管理模块

### 现状分析
- 后端已完整实现：NoticeController(/notice)、NoticeService、NoticeServiceImpl
- 前端已存在：views/system/notice/index.vue、api/system/notice.ts
- **问题**：公告管理未出现在侧边栏"基础管理"菜单中，路由已定义但菜单未配置

### 实施步骤

#### 1.1 前端菜单配置
- 文件：`guarantee-finance-frontend/src/layout/index.vue`
- 在menuList的"基础管理"children数组中添加公告管理菜单项：
  ```typescript
  { path: 'system/notice', meta: { title: '公告管理', icon: 'Bell' } }
  ```
- 位置：放在"基础信息同步"之后

#### 1.2 公告管理页面优化
- 文件：`guarantee-finance-frontend/src/views/system/notice/index.vue`
- 检查现有页面功能完整性，确保：
  - 公告列表（分页、搜索、筛选）
  - 新增/编辑公告
  - 发布/取消发布
  - 删除公告
  - 公告类型分类（系统公告、业务通知、紧急公告）

#### 1.3 后端验证
- 验证NoticeController所有接口正常工作
- 验证NoticeServiceImpl的分页查询、发布/取消发布逻辑

---

## 模块二：凭证规则引擎

### 现状分析
- 已存在AccVoucherTemplate（凭证模板表）和AccConversionTemplate（转换模板表）
- 已存在VoucherAutoGenerateService，但规则硬编码在代码中
- 需要构建可配置的规则引擎，让用户通过界面配置业务单据到会计凭证的映射规则

### 实施步骤

#### 2.1 数据库设计

**新增表：acc_voucher_rule（凭证规则表）**
```sql
CREATE TABLE acc_voucher_rule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型(receipt/payment/refund/compensation等)',
  business_subtype VARCHAR(50) COMMENT '业务子类型',
  voucher_type INT COMMENT '凭证类型(1-记账 2-收款 3-付款 4-转账)',
  summary_template VARCHAR(500) COMMENT '摘要模板(支持变量如{businessNo},{customerName})',
  priority INT DEFAULT 0 COMMENT '优先级(数字越大优先级越高)',
  status INT DEFAULT 1 COMMENT '状态(0-禁用 1-启用)',
  effective_date DATE COMMENT '生效日期',
  expiry_date DATE COMMENT '失效日期',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  UNIQUE KEY uk_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证规则表';
```

**新增表：acc_voucher_rule_entry（凭证规则分录表）**
```sql
CREATE TABLE acc_voucher_rule_entry (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rule_id BIGINT NOT NULL COMMENT '规则ID',
  line_no INT NOT NULL COMMENT '行号',
  entry_side VARCHAR(10) NOT NULL COMMENT '借贷方向(debit/credit)',
  subject_source VARCHAR(50) NOT NULL COMMENT '科目来源(fixed-固定/dynamic-动态/formula-公式)',
  subject_code VARCHAR(50) COMMENT '固定科目编码(当subject_source=fixed时)',
  subject_formula VARCHAR(500) COMMENT '科目公式(当subject_source=dynamic/formula时)',
  amount_source VARCHAR(50) NOT NULL COMMENT '金额来源(fixed-固定/field-字段/formula-公式)',
  amount_field VARCHAR(100) COMMENT '金额字段名(当amount_source=field时)',
  amount_formula VARCHAR(500) COMMENT '金额公式(当amount_source=formula时)',
  amount_fixed DECIMAL(18,2) COMMENT '固定金额(当amount_source=fixed时)',
  summary_template VARCHAR(500) COMMENT '分录摘要模板',
  auxiliary_rule VARCHAR(500) COMMENT '辅助核算规则(JSON)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  KEY idx_rule_id (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证规则分录表';
```

#### 2.2 后端实现

**新增实体类：**
- `AccVoucherRule.java` - 凭证规则实体
- `AccVoucherRuleEntry.java` - 凭证规则分录实体

**新增DTO：**
- `VoucherRuleDTO.java` - 规则数据传输对象（含分录列表）
- `VoucherRuleQueryDTO.java` - 规则查询条件

**新增Mapper：**
- `AccVoucherRuleMapper.java`
- `AccVoucherRuleEntryMapper.java`

**新增Service：**
- `VoucherRuleEngineService.java` - 规则引擎核心接口
  - `generateVoucher(String businessType, String businessSubtype, Map<String, Object> businessData)` - 根据规则生成凭证
  - `matchRule(String businessType, String businessSubtype)` - 匹配规则
  - `evaluateFormula(String formula, Map<String, Object> context)` - 公式求值
- `VoucherRuleService.java` - 规则CRUD接口
  - `queryPage()`, `createRule()`, `updateRule()`, `deleteRule()`, `enableRule()`, `disableRule()`

**新增Controller：**
- `VoucherRuleController.java` - 路径 `/accounting/voucher-rule`
  - GET /page - 规则分页查询
  - GET /{id} - 规则详情（含分录）
  - POST - 创建规则
  - PUT /{id} - 修改规则
  - DELETE /{id} - 删除规则
  - PUT /{id}/enable - 启用规则
  - PUT /{id}/disable - 禁用规则
  - POST /test - 测试规则（传入模拟数据，返回生成的凭证预览）
  - GET /business-types - 获取业务类型列表

**修改现有Service：**
- 修改 `VoucherAutoGenerateServiceImpl.java`，将硬编码逻辑改为调用VoucherRuleEngineService
- 保留原有方法作为默认回退（当无匹配规则时使用默认逻辑）

#### 2.3 前端实现

**新增页面：**
- `views/accounting/voucher-rule/index.vue` - 凭证规则管理页面
  - 规则列表（支持按业务类型、状态筛选）
  - 规则编辑（含分录配置，支持动态添加/删除分录行）
  - 规则测试（输入模拟数据，预览生成的凭证）
  - 规则启用/禁用

**新增API：**
- `api/accounting/voucherRule.ts`

**路由配置：**
- 在会计平台菜单下添加"凭证规则"子菜单

---

## 模块三：银行对账和银企直连优化

### 现状分析
- 已有BankReconciliation、BankTransaction、BankAccountConfig三个实体
- 已有ReconciliationController和BankController
- 已有BankReconciliationService和BankDirectConnectService
- 需要优化：自动对账算法、银企直连API对接、对账结果展示

### 实施步骤

#### 3.1 银行对账优化

**数据库扩展：**
- 为bank_transaction表添加字段：
  ```sql
  ALTER TABLE bank_transaction ADD COLUMN reconciliation_id BIGINT COMMENT '关联对账记录ID';
  ALTER TABLE bank_transaction ADD COLUMN match_rule VARCHAR(50) COMMENT '匹配规则(amount_exact-金额精确/amount_date-金额日期/amount_date_ref-金额日期参考号)';
  ALTER TABLE bank_transaction ADD COLUMN match_score DECIMAL(5,2) COMMENT '匹配得分(0-100)';
  ALTER TABLE bank_transaction ADD COLUMN match_time DATETIME COMMENT '匹配时间';
  ```

- 新增对账日志表：
  ```sql
  CREATE TABLE bank_reconciliation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reconciliation_id BIGINT COMMENT '对账记录ID',
    log_type VARCHAR(30) COMMENT '日志类型(AUTO_MATCH/MANUAL_MATCH/UNMATCH/ADJUST)',
    bank_transaction_id BIGINT COMMENT '银行流水ID',
    book_bill_id BIGINT COMMENT '账面单据ID',
    book_bill_type VARCHAR(30) COMMENT '单据类型(RECEIPT/PAYMENT)',
    match_rule VARCHAR(50) COMMENT '匹配规则',
    match_score DECIMAL(5,2) COMMENT '匹配得分',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted INT DEFAULT 0,
    remark VARCHAR(500)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账日志表';
  ```

**后端优化：**
- 优化 `BankReconciliationServiceImpl.java`：
  - 改进自动对账算法：支持多级匹配策略（精确匹配→金额+日期匹配→模糊匹配）
  - 添加匹配得分计算
  - 支持一对多、多对一匹配
  - 添加对账日志记录

- 新增 `BankReconciliationLogService.java` - 对账日志服务

**前端优化：**
- 优化 `views/reconciliation/index.vue`：
  - 添加对账进度展示
  - 添加匹配详情弹窗（显示匹配规则和得分）
  - 添加对账日志查看
  - 优化余额调节表展示
  - 添加未达账项处理功能

#### 3.2 银企直连优化

**数据库扩展：**
- 扩展bank_account_config表：
  ```sql
  ALTER TABLE bank_account_config ADD COLUMN api_version VARCHAR(20) COMMENT 'API版本';
  ALTER TABLE bank_account_config ADD COLUMN cert_path VARCHAR(500) COMMENT '证书路径';
  ALTER TABLE bank_account_config ADD COLUMN cert_password VARCHAR(200) COMMENT '证书密码';
  ALTER TABLE bank_account_config ADD COLUMN sync_interval INT DEFAULT 30 COMMENT '同步间隔(分钟)';
  ALTER TABLE bank_account_config ADD COLUMN last_balance_sync DATETIME COMMENT '最后余额同步时间';
  ALTER TABLE bank_account_config ADD COLUMN last_transaction_sync DATETIME COMMENT '最后流水同步时间';
  ```

- 新增银企直连同步日志表：
  ```sql
  CREATE TABLE bank_sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL COMMENT '银行账户ID',
    sync_type VARCHAR(30) NOT NULL COMMENT '同步类型(BALANCE/TRANSACTION/PAYMENT_STATUS)',
    sync_status VARCHAR(20) NOT NULL COMMENT '同步状态(SUCCESS/FAILED/PARTIAL)',
    request_data TEXT COMMENT '请求数据',
    response_data TEXT COMMENT '响应数据',
    error_message VARCHAR(500) COMMENT '错误信息',
    duration_ms BIGINT COMMENT '耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted INT DEFAULT 0,
    remark VARCHAR(500)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银企直连同步日志表';
  ```

**后端优化：**
- 优化 `BankDirectConnectServiceImpl.java`：
  - 添加余额实时查询
  - 添加流水自动同步（定时任务）
  - 添加付款状态查询
  - 添加电子回单获取
  - 完善异常处理和重试机制

- 新增 `BankSyncLogService.java` - 同步日志服务

**前端优化：**
- 优化 `views/bank/index.vue`：
  - 添加账户余额实时展示
  - 添加流水同步操作按钮
  - 添加同步日志查看
  - 添加付款状态查询
  - 添加账户连接状态监控

---

## 模块四：优化菜单管理

### 现状分析
- 菜单是静态硬编码在layout/index.vue的menuList中
- 后端已有SysMenu实体和SysMenuController，但前端未使用动态菜单
- 需要实现基于权限的动态菜单生成

### 实施步骤

#### 4.1 后端菜单数据完善

**完善sys_menu表数据：**
- 插入所有现有菜单项到sys_menu表
- 确保菜单层级关系正确（parentId）
- 为每个菜单项分配正确的permission标识

**新增/修改接口：**
- `SysMenuController.java` 添加：
  - GET /user-menu - 获取当前用户的菜单树（根据角色权限过滤）
  - GET /user-permissions - 获取当前用户的权限列表

- `SysMenuServiceImpl.java` 添加：
  - `getUserMenuTree(Long userId)` - 根据用户ID获取有权限的菜单树
  - `getUserPermissions(Long userId)` - 获取用户权限标识列表

#### 4.2 前端动态菜单实现

**修改用户Store：**
- 文件：`guarantee-finance-frontend/src/store/user.ts`
- 添加：
  - `menuList` 状态 - 存储用户菜单数据
  - `permissions` 状态 - 存储用户权限列表
  - `loadMenuAndPermissions()` 方法 - 登录后加载菜单和权限

**修改路由守卫：**
- 文件：`guarantee-finance-frontend/src/router/index.ts`
- 在路由守卫中：
  - 登录后调用loadMenuAndPermissions()
  - 根据菜单数据动态注册路由（使用router.addRoute()）
  - 根据权限控制页面访问

**修改侧边栏：**
- 文件：`guarantee-finance-frontend/src/layout/index.vue`
- 将硬编码的menuList改为从store中读取
- 支持从后端菜单数据渲染多级菜单

**添加权限指令：**
- 新增 `directives/permission.ts` - v-permission指令
- 用于按钮级别的权限控制

#### 4.3 菜单管理页面优化
- 文件：`guarantee-finance-frontend/src/views/system/menu/index.vue`
- 优化菜单树编辑功能
- 添加菜单图标选择器
- 添加菜单排序功能
- 添加菜单预览功能

---

## 模块五：总账模块

### 现状分析
- 已有AccAccountSubject（科目表）、AccSubjectBalance（科目余额表）、AccVoucher（凭证表）
- 已有AccountPostingService（记账服务）
- 缺少：总分类账、明细分类账、期末处理、自动结转、损益调整、报表管理

### 实施步骤

#### 5.1 数据库设计

**新增表：acc_general_ledger（总分类账表）**
```sql
CREATE TABLE acc_general_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subject_code VARCHAR(50) NOT NULL COMMENT '科目编码',
  subject_name VARCHAR(100) COMMENT '科目名称',
  period VARCHAR(6) NOT NULL COMMENT '会计期间(yyyyMM)',
  year INT NOT NULL COMMENT '年度',
  month INT NOT NULL COMMENT '月份',
  begin_debit DECIMAL(18,2) DEFAULT 0 COMMENT '期初借方余额',
  begin_credit DECIMAL(18,2) DEFAULT 0 COMMENT '期初贷方余额',
  current_debit DECIMAL(18,2) DEFAULT 0 COMMENT '本期借方发生额',
  current_credit DECIMAL(18,2) DEFAULT 0 COMMENT '本期贷方发生额',
  end_debit DECIMAL(18,2) DEFAULT 0 COMMENT '期末借方余额',
  end_credit DECIMAL(18,2) DEFAULT 0 COMMENT '期末贷方余额',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  UNIQUE KEY uk_subject_period (subject_code, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='总分类账';
```

**新增表：acc_detail_ledger（明细分类账表）**
```sql
CREATE TABLE acc_detail_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subject_code VARCHAR(50) NOT NULL COMMENT '科目编码',
  subject_name VARCHAR(100) COMMENT '科目名称',
  period VARCHAR(6) NOT NULL COMMENT '会计期间(yyyyMM)',
  voucher_id BIGINT COMMENT '凭证ID',
  voucher_no VARCHAR(50) COMMENT '凭证编号',
  voucher_date VARCHAR(10) COMMENT '凭证日期',
  summary VARCHAR(500) COMMENT '摘要',
  debit_amount DECIMAL(18,2) DEFAULT 0 COMMENT '借方金额',
  credit_amount DECIMAL(18,2) DEFAULT 0 COMMENT '贷方金额',
  direction VARCHAR(4) COMMENT '余额方向(借/贷)',
  balance DECIMAL(18,2) DEFAULT 0 COMMENT '余额',
  auxiliary_info VARCHAR(500) COMMENT '辅助核算信息',
  customer_code VARCHAR(50) COMMENT '客户编码',
  department_code VARCHAR(50) COMMENT '部门编码',
  project_code VARCHAR(50) COMMENT '项目编码',
  year INT NOT NULL COMMENT '年度',
  month INT NOT NULL COMMENT '月份',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  KEY idx_subject_period (subject_code, period),
  KEY idx_voucher (voucher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明细分类账';
```

**新增表：acc_period（会计期间表）**
```sql
CREATE TABLE acc_period (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  period_code VARCHAR(6) NOT NULL COMMENT '期间编码(yyyyMM)',
  period_name VARCHAR(20) NOT NULL COMMENT '期间名称',
  year INT NOT NULL COMMENT '年度',
  month INT NOT NULL COMMENT '月份',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE NOT NULL COMMENT '结束日期',
  status VARCHAR(20) DEFAULT 'OPEN' COMMENT '状态(OPEN-开启/CLOSED-关闭/TEMP_CLOSE-暂封)',
  is_current INT DEFAULT 0 COMMENT '是否当前期间(0-否 1-是)',
  closing_type VARCHAR(20) COMMENT '结账类型(NONE-未结账/TEMP-暂结/FINAL-最终结账)',
  closing_time DATETIME COMMENT '结账时间',
  closing_user_id BIGINT COMMENT '结账人ID',
  closing_user_name VARCHAR(50) COMMENT '结账人姓名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  UNIQUE KEY uk_period_code (period_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计期间';
```

**新增表：acc_carry_forward（结转记录表）**
```sql
CREATE TABLE acc_carry_forward (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  period VARCHAR(6) NOT NULL COMMENT '会计期间',
  carry_type VARCHAR(30) NOT NULL COMMENT '结转类型(PROFIT_LOSS-损益结转/COST-成本结转/PERIOD_END-期末结转)',
  source_subject_code VARCHAR(50) COMMENT '源科目编码',
  target_subject_code VARCHAR(50) COMMENT '目标科目编码',
  amount DECIMAL(18,2) COMMENT '结转金额',
  voucher_id BIGINT COMMENT '生成的凭证ID',
  voucher_no VARCHAR(50) COMMENT '生成的凭证编号',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING-待处理/COMPLETED-已完成/REVERSED-已冲回)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  KEY idx_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结转记录';
```

**新增表：acc_financial_report（财务报表表）**
```sql
CREATE TABLE acc_financial_report (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_code VARCHAR(50) NOT NULL COMMENT '报表编码',
  report_name VARCHAR(100) NOT NULL COMMENT '报表名称',
  report_type VARCHAR(30) NOT NULL COMMENT '报表类型(BALANCE_SHEET-资产负债表/INCOME_STATEMENT-利润表/CASH_FLOW-现金流量表/PROFIT_DISTRIBUTION-利润分配表/VOUCHER_SUMMARY-凭证汇总表/SUBJECT_SUMMARY-科目汇总表)',
  period VARCHAR(6) NOT NULL COMMENT '会计期间',
  year INT NOT NULL COMMENT '年度',
  month INT NOT NULL COMMENT '月份',
  report_data JSON COMMENT '报表数据(JSON格式)',
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态(DRAFT-草稿/CONFIRMED-已确认/APPROVED-已审批)',
  confirm_user_id BIGINT COMMENT '确认人ID',
  confirm_user_name VARCHAR(50) COMMENT '确认人姓名',
  confirm_time DATETIME COMMENT '确认时间',
  approve_user_id BIGINT COMMENT '审批人ID',
  approve_user_name VARCHAR(50) COMMENT '审批人姓名',
  approve_time DATETIME COMMENT '审批时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  UNIQUE KEY uk_report_code_period (report_code, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务报表';
```

**新增表：acc_report_template（报表模板表）**
```sql
CREATE TABLE acc_report_template (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  report_type VARCHAR(30) NOT NULL COMMENT '报表类型',
  template_content JSON NOT NULL COMMENT '模板内容(行定义、取数公式等)',
  is_default INT DEFAULT 0 COMMENT '是否默认模板(0-否 1-是)',
  status INT DEFAULT 1 COMMENT '状态(0-禁用 1-启用)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_by BIGINT,
  deleted INT DEFAULT 0,
  remark VARCHAR(500),
  UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表模板';
```

#### 5.2 后端实现 - 账簿查询

**新增实体类：**
- `AccGeneralLedger.java` - 总分类账
- `AccDetailLedger.java` - 明细分类账
- `AccPeriod.java` - 会计期间
- `AccCarryForward.java` - 结转记录
- `AccFinancialReport.java` - 财务报表
- `AccReportTemplate.java` - 报表模板

**新增DTO：**
- `GeneralLedgerQueryDTO.java` - 总账查询条件
- `DetailLedgerQueryDTO.java` - 明细账查询条件
- `PeriodDTO.java` - 期间操作DTO
- `CarryForwardDTO.java` - 结转DTO
- `ReportGenerateDTO.java` - 报表生成DTO
- `ReportQueryDTO.java` - 报表查询DTO

**新增Mapper：**
- `AccGeneralLedgerMapper.java`
- `AccDetailLedgerMapper.java`
- `AccPeriodMapper.java`
- `AccCarryForwardMapper.java`
- `AccFinancialReportMapper.java`
- `AccReportTemplateMapper.java`

**新增Service：**

- `GeneralLedgerService.java` - 总分类账服务
  - `queryByPeriod(String period, String subjectCode)` - 按期间查询总账
  - `queryBySubjectRange(String period, String startCode, String endCode)` - 按科目范围查询
  - `generateGeneralLedger(String period)` - 生成总分类账（从科目余额汇总）
  - `exportGeneralLedger(String period, String format)` - 导出总账

- `DetailLedgerService.java` - 明细分类账服务
  - `queryBySubjectAndPeriod(String subjectCode, String period)` - 按科目和期间查询
  - `queryBySubjectAndDateRange(String subjectCode, String startDate, String endDate)` - 按科目和日期范围查询
  - `generateDetailLedger(String period)` - 生成明细分类账（从凭证明细汇总）
  - `exportDetailLedger(String subjectCode, String period, String format)` - 导出明细账

- `SubjectBalanceReportService.java` - 科目余额表服务
  - `queryBalanceSheet(String period)` - 查询科目余额表
  - `queryTrialBalance(String period)` - 试算平衡表
  - `exportBalanceSheet(String period, String format)` - 导出

**新增Controller：**

- `GeneralLedgerController.java` - 路径 `/accounting/general-ledger`
  - GET /page - 总账分页查询
  - GET /subject - 按科目查询
  - POST /generate - 生成总账
  - GET /export - 导出

- `DetailLedgerController.java` - 路径 `/accounting/detail-ledger`
  - GET /page - 明细账分页查询
  - GET /subject - 按科目查询
  - POST /generate - 生成明细账
  - GET /export - 导出

- `SubjectBalanceReportController.java` - 路径 `/accounting/balance-report`
  - GET /sheet - 科目余额表
  - GET /trial - 试算平衡表
  - GET /export - 导出

#### 5.3 后端实现 - 期末处理

**新增Service：**

- `PeriodService.java` - 会计期间服务
  - `initPeriods(Integer year)` - 初始化年度会计期间
  - `getCurrentPeriod()` - 获取当前期间
  - `closePeriod(String period)` - 结账
  - `reopenPeriod(String period)` - 反结账
  - `checkPeriodCanClose(String period)` - 检查是否可以结账
  - `getPeriodStatus(String period)` - 获取期间状态

- `CarryForwardService.java` - 结转服务
  - `carryForwardProfitLoss(String period)` - 损益结转（将损益类科目余额结转至本年利润）
  - `carryForwardCost(String period)` - 成本结转
  - `carryForwardPeriodEnd(String period)` - 期末结转
  - `reverseCarryForward(Long carryForwardId)` - 冲回结转
  - `previewCarryForward(String period, String carryType)` - 预览结转

**新增Controller：**

- `PeriodController.java` - 路径 `/accounting/period`
  - GET /list - 期间列表
  - GET /current - 当前期间
  - POST /init - 初始化年度期间
  - PUT /{period}/close - 结账
  - PUT /{period}/reopen - 反结账
  - GET /{period}/check - 检查是否可结账
  - GET /{period}/status - 期间状态

- `CarryForwardController.java` - 路径 `/accounting/carry-forward`
  - POST /profit-loss - 损益结转
  - POST /cost - 成本结转
  - POST /period-end - 期末结转
  - POST /{id}/reverse - 冲回结转
  - POST /preview - 预览结转
  - GET /list - 结转记录列表

#### 5.4 后端实现 - 报表管理

**新增Service：**

- `FinancialReportService.java` - 财务报表服务
  - `generateBalanceSheet(String period)` - 生成资产负债表
  - `generateIncomeStatement(String period)` - 生成利润表
  - `generateCashFlowStatement(String period)` - 生成现金流量表
  - `generateProfitDistribution(String period)` - 生成利润分配表
  - `getReport(Long id)` - 获取报表
  - `confirmReport(Long id)` - 确认报表
  - `approveReport(Long id)` - 审批报表
  - `exportReport(Long id, String format)` - 导出报表(PDF/Excel)

- `ReportTemplateService.java` - 报表模板服务
  - `queryPage()` - 模板列表
  - `createTemplate()` - 创建模板
  - `updateTemplate()` - 修改模板
  - `initDefaultTemplates()` - 初始化默认模板

**新增Controller：**

- `FinancialReportController.java` - 路径 `/accounting/report`
  - POST /balance-sheet - 生成资产负债表
  - POST /income-statement - 生成利润表
  - POST /cash-flow - 生成现金流量表
  - POST /profit-distribution - 生成利润分配表
  - GET /{id} - 获取报表详情
  - PUT /{id}/confirm - 确认报表
  - PUT /{id}/approve - 审批报表
  - GET /{id}/export - 导出报表
  - GET /page - 报表分页查询

- `ReportTemplateController.java` - 路径 `/accounting/report-template`
  - GET /page - 模板列表
  - POST - 创建模板
  - PUT /{id} - 修改模板
  - DELETE /{id} - 删除模板
  - POST /init-defaults - 初始化默认模板

#### 5.5 前端实现 - 总账模块

**新增页面：**

- `views/accounting/general-ledger/index.vue` - 总分类账查询
  - 期间选择器
  - 科目范围筛选
  - 总账数据表格（科目编码、科目名称、期初余额、本期借方、本期贷方、期末余额）
  - 导出功能（Excel/PDF）

- `views/accounting/detail-ledger/index.vue` - 明细分类账查询
  - 科目选择器（支持科目树选择）
  - 期间/日期范围选择
  - 明细账数据表格（日期、凭证号、摘要、借方、贷方、方向、余额）
  - 点击凭证号跳转凭证详情
  - 导出功能

- `views/accounting/balance-report/index.vue` - 科目余额表
  - 期间选择器
  - 科目级别筛选
  - 科目余额表（科目编码、科目名称、期初借方/贷方、本期借方/贷方、期末借方/贷方）
  - 试算平衡表切换
  - 导出功能

- `views/accounting/period/index.vue` - 期末处理
  - 会计期间列表（状态标识：开启/关闭/暂封）
  - 结账操作（检查→损益结转→期末结转→结账）
  - 反结账操作
  - 期间初始化

- `views/accounting/carry-forward/index.vue` - 自动结转
  - 损益结转（列出损益类科目余额，预览结转凭证）
  - 成本结转
  - 期末结转
  - 结转记录查询
  - 冲回操作

- `views/accounting/report/index.vue` - 报表管理
  - 报表类型选择（资产负债表、利润表、现金流量表、利润分配表）
  - 期间选择
  - 报表生成和预览
  - 报表确认和审批
  - 报表导出（PDF/Excel）

- `views/accounting/report-template/index.vue` - 报表模板管理
  - 模板列表
  - 模板编辑（行定义、取数公式）
  - 默认模板初始化

**新增API：**
- `api/accounting/generalLedger.ts`
- `api/accounting/detailLedger.ts`
- `api/accounting/balanceReport.ts`
- `api/accounting/period.ts`
- `api/accounting/carryForward.ts`
- `api/accounting/financialReport.ts`
- `api/accounting/reportTemplate.ts`

**路由配置：**
- 会计平台菜单扩展为以下子菜单：
  - 科目管理
  - 辅助核算
  - 凭证管理
  - 凭证规则（新增）
  - 总分类账（新增）
  - 明细分类账（新增）
  - 科目余额表（新增）
  - 期末处理（新增）
  - 报表管理（新增）

---

## 实施优先级与依赖关系

### 第一阶段（基础功能）
1. **模块一：公告管理** - 最简单，只需修改菜单配置
2. **模块四：菜单管理优化** - 基础设施，影响后续所有模块的菜单展示

### 第二阶段（核心功能）
3. **模块二：凭证规则引擎** - 核心业务逻辑，优化凭证自动生成
4. **模块五（部分）：账簿查询** - 总分类账、明细分类账、科目余额表

### 第三阶段（高级功能）
5. **模块三：银行对账和银企直连优化** - 优化现有功能
6. **模块五（部分）：期末处理** - 自动结转、损益调整
7. **模块五（部分）：报表管理** - 法定财务报表、管理会计报表

---

## 技术要点

### 后端技术
- Spring Boot + MyBatis-Plus（现有技术栈）
- 事务管理：@Transactional确保数据一致性
- 公式引擎：使用Spring Expression Language (SpEL) 或 Janino 实现动态公式计算
- 报表生成：使用Apache POI生成Excel，iText生成PDF
- 定时任务：使用Spring @Scheduled实现银企直连自动同步

### 前端技术
- Vue 3 + TypeScript + Element Plus（现有技术栈）
- ECharts：报表图表展示
- 动态路由：router.addRoute()实现权限菜单
- Pinia：状态管理（菜单、权限）

### 数据一致性保障
- 会计期间控制：结账后不允许修改该期间凭证
- 凭证号连续性：使用数据库序列确保凭证号不中断
- 余额计算：每次记账后重新计算科目余额，确保借贷平衡
- 结转校验：结转前检查试算平衡
