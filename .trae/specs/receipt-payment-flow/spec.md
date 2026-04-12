# 收款付款全流程 - 产品需求文档

## Overview
- **Summary**: 实现收款和付款业务的完整流程，从新建、审核到自动记账、凭证生成、凭证审核、总账更新和财务报表生成的全自动化处理。
- **Purpose**: 解决财务处理效率低下、人工操作繁琐、容易出错的问题，实现财务流程的自动化和规范化。
- **Target Users**: 财务人员、审批人员、系统管理员

## Goals
- 实现收款和付款业务的完整流程自动化
- 确保财务数据的准确性和一致性
- 提高财务处理效率和透明度
- 支持财务报表的自动生成

## Non-Goals (Out of Scope)
- 不涉及外部银行系统的直接对接
- 不改变现有的用户权限体系
- 不涉及税务申报和发票管理

## Background & Context
- 系统已有的实体包括：FinReceipt（收款）、FinPayment（付款）、AccVoucher（凭证）、AccVoucherAudit（凭证审核）、AccGeneralLedger（总账）、AccFinancialReport（财务报表）
- 现有的服务包括：FinReceiptService、FinPaymentService、AccVoucherService、AccVoucherAuditService、GeneralLedgerService、FinancialReportService
- 系统使用Spring Boot + MyBatis Plus技术栈

## Functional Requirements
- **FR-1**: 收款新建与审核
- **FR-2**: 付款新建与审核
- **FR-3**: 审核通过后自动记账处理
- **FR-4**: 自动生成会计凭证
- **FR-5**: 凭证自动审核
- **FR-6**: 自动更新总账账簿
- **FR-7**: 自动生成财务报表

## Non-Functional Requirements
- **NFR-1**: 流程处理时间不超过5秒
- **NFR-2**: 数据一致性保证，确保各环节数据准确同步
- **NFR-3**: 系统稳定性，支持并发处理
- **NFR-4**: 操作可追溯性，记录所有流程操作日志

## Constraints
- **Technical**: 基于现有的Spring Boot + MyBatis Plus架构
- **Business**: 遵循企业财务管理制度和会计准则
- **Dependencies**: 依赖现有的账户科目体系和凭证模板

## Assumptions
- 账户科目体系已设置完成
- 凭证生成规则已配置
- 审核流程已定义

## Acceptance Criteria

### AC-1: 收款新建与审核
- **Given**: 财务人员登录系统
- **When**: 创建收款单并提交审核
- **Then**: 审核人员收到审核任务，审核通过后状态更新
- **Verification**: `programmatic`
- **Notes**: 审核流程应支持多级审批

### AC-2: 付款新建与审核
- **Given**: 财务人员登录系统
- **When**: 创建付款单并提交审核
- **Then**: 审核人员收到审核任务，审核通过后状态更新
- **Verification**: `programmatic`
- **Notes**: 审核流程应支持多级审批

### AC-3: 自动记账处理
- **Given**: 收款或付款审核通过
- **When**: 系统触发自动记账处理
- **Then**: 系统根据业务类型和金额自动计算借贷方
- **Verification**: `programmatic`
- **Notes**: 记账处理应遵循会计准则

### AC-4: 自动生成会计凭证
- **Given**: 记账处理完成
- **When**: 系统生成会计凭证
- **Then**: 凭证包含完整的借贷信息和业务关联
- **Verification**: `programmatic`
- **Notes**: 凭证应符合会计凭证规范

### AC-5: 凭证自动审核
- **Given**: 会计凭证生成完成
- **When**: 系统触发凭证审核
- **Then**: 凭证状态更新为已审核
- **Verification**: `programmatic`
- **Notes**: 审核过程应记录审核人和审核时间

### AC-6: 自动更新总账账簿
- **Given**: 凭证审核通过
- **When**: 系统更新总账账簿
- **Then**: 总账数据与凭证数据一致
- **Verification**: `programmatic`
- **Notes**: 总账更新应实时反映最新财务状况

### AC-7: 自动生成财务报表
- **Given**: 总账数据更新完成
- **When**: 系统生成财务报表
- **Then**: 报表数据准确反映企业财务状况
- **Verification**: `human-judgment`
- **Notes**: 报表应包括资产负债表、利润表等

## Open Questions
- [ ] 审核流程的具体审批级数和人员配置
- [ ] 凭证生成的具体规则和模板
- [ ] 财务报表的具体类型和生成周期