# 凭证管理功能完善计划

## 一、现状分析

### 已实现功能
1. **凭证录入**
   - 手工录入功能已实现
   - 借贷平衡校验已实现
   - 科目合法性校验已实现
   - 凭证编号自动生成

2. **凭证修改、删除、作废**
   - 修改功能已实现，已记账凭证不可修改
   - 删除功能已实现，已记账凭证不可删除
   - 作废功能已实现
   - 恢复功能已实现

3. **凭证审核**
   - 审核控制器已创建
   - 审核服务接口已定义
   - 审核记录表已创建

4. **凭证记账**
   - 记账功能已实现
   - 反记账功能已实现

5. **数据库表结构**
   - acc_voucher（凭证主表）
   - acc_voucher_detail（凭证明细表）
   - acc_voucher_audit（凭证审核表）

### 待完善功能
1. 凭证导入功能未实现
2. 凭证导出Excel/PDF功能未实现
3. 审核人不可审核自己录入的凭证未实现
4. 单审核、双审核流程配置未实现
5. 删除凭证权限控制未添加
6. 前端审核功能页面缺失
7. 前端记账功能按钮缺失
8. 前端导入功能未完善
9. 凭证预览、打印功能缺失

## 二、实现计划

### 阶段一：后端功能完善（预计2天）

#### 1. 凭证导入功能实现
**文件：** `AccVoucherServiceImpl.java`

**任务：**
- 使用Apache POI读取Excel文件
- 解析凭证数据（包括主表和明细表）
- 批量创建凭证
- 数据校验（科目合法性、借贷平衡）
- 错误数据记录和返回

**依赖：**
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

#### 2. 凭证导出功能实现
**文件：** `AccVoucherServiceImpl.java`

**任务：**
- Excel导出：使用Apache POI创建Excel文件
- PDF导出：使用iText创建PDF文件
- 凭证格式化输出

**依赖：**
```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

#### 3. 审核功能完善
**文件：** `AccVoucherAuditServiceImpl.java`

**任务：**
- 实现审核人不可审核自己录入的凭证校验
- 实现单审核、双审核流程配置
- 添加审核级别控制
- 完善审核记录

**新增配置表：**
```sql
CREATE TABLE `acc_audit_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `org_code` VARCHAR(50) COMMENT '机构编码',
  `audit_type` TINYINT COMMENT '审核类型：1-单审核 2-双审核',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME,
  `update_time` DATETIME,
  PRIMARY KEY (`id`)
) COMMENT='审核配置表';
```

#### 4. 权限控制添加
**文件：** `VoucherController.java`

**任务：**
- 在删除接口添加权限注解 `@PreAuthorize("hasAuthority('voucher:delete')")`
- 在记账接口添加权限注解 `@PreAuthorize("hasAuthority('voucher:post')")`
- 在反记账接口添加权限注解 `@PreAuthorize("hasAuthority('voucher:unpost')")`

#### 5. 新增凭证预览接口
**文件：** `VoucherController.java`

**任务：**
- 添加凭证预览接口，返回HTML格式的凭证内容
- 支持打印样式

### 阶段二：前端功能完善（预计2天）

#### 1. 凭证导入功能
**文件：** `src/views/accounting/voucher.vue`

**任务：**
- 添加导入对话框
- 支持Excel文件上传
- 显示导入结果和错误信息
- 提供模板下载功能

#### 2. 凭证审核功能
**文件：** `src/views/accounting/voucher.vue`

**任务：**
- 添加审核按钮（已提交状态显示）
- 添加审核对话框（审核意见输入）
- 实现审核通过/拒绝功能
- 显示审核历史记录

#### 3. 凭证记账功能
**文件：** `src/views/accounting/voucher.vue`

**任务：**
- 添加记账按钮（已审核状态显示）
- 添加反记账按钮（已记账状态显示）
- 添加权限控制

#### 4. 凭证导出功能
**文件：** `src/views/accounting/voucher.vue`

**任务：**
- 添加导出Excel按钮
- 添加导出PDF按钮
- 实现文件下载功能

#### 5. 凭证预览和打印
**文件：** `src/views/accounting/voucher.vue`

**任务：**
- 添加凭证预览对话框
- 实现凭证打印功能
- 格式化凭证显示

#### 6. 审核配置页面
**文件：** `src/views/accounting/audit-config.vue`（新建）

**任务：**
- 创建审核配置页面
- 支持单审核/双审核切换
- 支持按机构配置

### 阶段三：测试和优化（预计1天）

#### 1. 单元测试
- 凭证导入测试
- 凭证导出测试
- 审核流程测试
- 权限控制测试

#### 2. 集成测试
- 完整业务流程测试
- 边界条件测试
- 异常情况测试

#### 3. 性能优化
- 批量导入性能优化
- 导出性能优化
- 查询性能优化

## 三、详细实现步骤

### 步骤1：添加依赖
在 `pom.xml` 中添加Apache POI和iText依赖。

### 步骤2：创建审核配置表
执行SQL创建 `acc_audit_config` 表。

### 步骤3：实现凭证导入
- 创建 `VoucherImportService` 接口和实现类
- 实现Excel解析逻辑
- 实现数据校验和批量插入

### 步骤4：实现凭证导出
- 实现 `exportVouchersToExcel` 方法
- 实现 `exportVoucherToPdf` 方法

### 步骤5：完善审核功能
- 实现 `canAuditVoucher` 方法（审核人校验）
- 实现双审核流程逻辑
- 添加审核配置查询

### 步骤6：添加权限控制
- 在控制器方法添加权限注解
- 在菜单表添加权限配置

### 步骤7：前端导入功能
- 创建导入对话框组件
- 实现文件上传和结果展示

### 步骤8：前端审核功能
- 添加审核按钮和对话框
- 实现审核通过/拒绝逻辑

### 步骤9：前端记账功能
- 添加记账/反记账按钮
- 添加权限控制

### 步骤10：前端导出和打印
- 实现导出Excel/PDF功能
- 实现凭证预览和打印

### 步骤11：测试验证
- 编写单元测试
- 执行集成测试
- 修复发现的问题

## 四、技术要点

### 1. 借贷平衡校验
```java
private void validateDebitCreditBalance(List<VoucherDetailDTO> details) {
    BigDecimal totalDebit = details.stream()
        .map(d -> d.getDebitAmount() != null ? d.getDebitAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    BigDecimal totalCredit = details.stream()
        .map(d -> d.getCreditAmount() != null ? d.getCreditAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    if (totalDebit.compareTo(totalCredit) != 0) {
        throw new RuntimeException("借贷金额不平衡");
    }
}
```

### 2. 审核人校验
```java
public boolean canAuditVoucher(Long voucherId, Long auditorId) {
    AccVoucher voucher = accVoucherMapper.selectById(voucherId);
    // 审核人不可审核自己录入的凭证
    return !voucher.getCreateUserId().equals(auditorId);
}
```

### 3. 双审核流程
```java
public void auditVoucher(VoucherAuditDTO dto) {
    AccAuditConfig config = getAuditConfig();
    if (config.getAuditType() == 2) {
        // 双审核流程
        int auditCount = countAudits(dto.getVoucherId());
        if (auditCount < 2) {
            // 需要第二次审核
        }
    }
}
```

### 4. 权限控制
```java
@PreAuthorize("hasAuthority('voucher:delete')")
@DeleteMapping("/{id}")
public R<Void> deleteVoucher(@PathVariable Long id) {
    accVoucherService.deleteVoucher(id);
    return R.ok();
}
```

## 五、验收标准

### 功能验收
- [ ] 支持手工录入凭证，自动校验科目合法性和借贷平衡
- [ ] 支持Excel导入凭证，显示导入结果和错误信息
- [ ] 支持凭证修改、删除、作废，已记账凭证不可修改
- [ ] 删除凭证需要特定权限
- [ ] 作废凭证可以恢复
- [ ] 支持单审核、双审核流程配置
- [ ] 审核人不可审核自己录入的凭证
- [ ] 支持凭证记账和反记账
- [ ] 支持凭证预览、打印
- [ ] 支持导出Excel和PDF

### 性能验收
- 单次导入1000条凭证，响应时间<10秒
- 导出1000条凭证，响应时间<5秒
- 凭证查询响应时间<1秒

### 安全验收
- 所有接口都有权限控制
- 敏感操作有日志记录
- 数据校验完整

## 六、风险评估

### 技术风险
1. Excel导入大数据量可能内存溢出 - 使用流式读取
2. PDF生成性能问题 - 使用模板缓存
3. 并发审核冲突 - 使用乐观锁

### 业务风险
1. 审核流程配置复杂 - 提供默认配置
2. 权限配置遗漏 - 完善权限检查

## 七、时间安排

- 第1-2天：后端功能完善
- 第3-4天：前端功能完善
- 第5天：测试和优化

总计：5个工作日
