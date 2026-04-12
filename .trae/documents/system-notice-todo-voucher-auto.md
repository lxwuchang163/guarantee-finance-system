# 系统公告管理、待办优化、收付款自动生成凭证、凭证审核后自动记账

## 任务概述

1. 增加系统公告管理（CRUD + 前后端完整功能）
2. 代办事项处理优化（根据类型跳转对应业务页面处理）
3. 收款/付款审核后自动生成凭证
4. 凭证审核通过后自动记账（更新科目余额表）

---

## 一、系统公告管理

### 后端改造

1. **扩展 Notice 实体** - 添加 `noticeType`(公告类型)、`topFlag`(是否置顶)、`publishTime`(发布时间) 字段
2. **数据库表变更** - `dashboard_notice` 表新增上述字段
3. **创建 NoticeController** - 新增 `/notice` 路径的完整 CRUD 接口：
   - `GET /notice/page` - 分页查询公告
   - `GET /notice/{id}` - 公告详情
   - `POST /notice` - 新增公告
   - `PUT /notice/{id}` - 修改公告
   - `DELETE /notice/{id}` - 删除公告
   - `PUT /notice/{id}/publish` - 发布公告（status 改为1）
   - `PUT /notice/{id}/unpublish` - 取消发布公告（status 改为0）
4. **扩展 NoticeService** - 添加分页查询、新增、修改、删除、发布/取消发布方法
5. **扩展 NoticeServiceImpl** - 实现上述方法
6. **创建 NoticeDTO** - 公告数据传输对象

### 前端改造

1. **创建公告 API 文件** - `src/api/system/notice.ts`
2. **创建公告管理页面** - `src/views/system/notice/index.vue`，包含：
   - 公告列表（分页、搜索、状态筛选）
   - 新增/编辑公告对话框
   - 发布/取消发布操作
   - 删除确认
3. **路由配置** - 在系统管理下添加公告管理路由

---

## 二、代办事项处理优化

### 后端改造

1. **扩展 TodoItem 实体** - 添加 `businessId`(业务ID)、`businessType`(业务类型：receipt/payment/reconciliation/voucher/bank)、`actionUrl`(跳转路径) 字段
2. **数据库表变更** - `dashboard_todo` 表新增上述字段
3. **优化 TodoServiceImpl.getTodoList()** - 在构建待办事项时填充 `businessType` 和 `actionUrl`
4. **优化 TodoServiceImpl.processTodo()** - 根据待办类型执行对应业务操作：
   - 收款待审核 → 调用收款审核接口
   - 付款待审核 → 调用付款审核接口
   - 凭证待审核 → 调用凭证审核接口
   - 对账待处理 → 更新对账状态
   - 银企直连 → 更新连接状态
5. **新增 TodoController** - 独立的待办事项管理接口（从 DashboardController 分离）：
   - `GET /todo/list` - 待办列表
   - `POST /todo/{id}/process` - 处理待办
   - `GET /todo/{id}` - 待办详情
   - `POST /todo/{id}/redirect` - 获取跳转信息

### 前端改造

1. **优化仪表盘待办处理** - 点击"处理"时根据 `businessType` 跳转到对应业务页面
2. **扩展 dashboard API** - 添加跳转信息获取接口

---

## 三、收款/付款审核后自动生成凭证

### 后端改造

1. **创建 VoucherAutoGenerateService** - 凭证自动生成服务接口
2. **创建 VoucherAutoGenerateServiceImpl** - 实现自动生成逻辑：
   - `generateReceiptVoucher(FinReceipt receipt)` - 根据收款单生成凭证
     - 借：银行存款(1002) - 收款金额
     - 贷：对应收入科目(根据 businessType 确定) - 收款金额
   - `generatePaymentVoucher(FinPayment payment)` - 根据付款单生成凭证
     - 借：对应支出科目(根据 businessType 确定) - 付款金额
     - 贷：银行存款(1002) - 付款金额
3. **修改 FinReceiptServiceImpl.audit()** - 审核通过后调用 `generateReceiptVoucher()` 自动生成凭证，并将凭证ID和凭证号回填到收款单
4. **修改 FinPaymentServiceImpl.audit()** - 审核通过后调用 `generatePaymentVoucher()` 自动生成凭证，并将凭证ID和凭证号回填到付款单
5. **科目映射配置** - 根据业务类型确定会计科目：
   - 收款：保费收入(6001)、分担收入(6002)、追偿到款(6003)
   - 付款：退费支出(6401)、代偿支出(6402)、追回资金分配(6403)

---

## 四、凭证审核通过后自动记账

### 后端改造

1. **创建 AccountPostingService** - 记账服务接口
2. **创建 AccountPostingServiceImpl** - 实现自动记账逻辑：
   - `postVoucher(Long voucherId)` - 凭证记账
     - 获取凭证明细
     - 根据明细更新 `acc_subject_balance` 表
     - 借方金额 → `current_debit` 累加
     - 贷方金额 → `current_credit` 累加
     - 重新计算期末余额：`end_debit = begin_debit + current_debit`，`end_credit = begin_credit + current_credit`
   - `unpostVoucher(Long voucherId)` - 反记账（冲回余额变动）
3. **修改 AccVoucherAuditServiceImpl.auditVoucher()** - 审核通过后（status 变为2时）自动调用 `postVoucher()` 进行记账
4. **修改 AccVoucherServiceImpl.postVoucher()** - 改为调用 AccountPostingService
5. **修改 AccVoucherServiceImpl.unpostVoucher()** - 改为调用 AccountPostingService

---

## 实施顺序

1. 系统公告管理（前后端完整 CRUD）
2. 代办事项处理优化
3. 收款/付款审核后自动生成凭证
4. 凭证审核通过后自动记账
5. 编译测试、接口验证
