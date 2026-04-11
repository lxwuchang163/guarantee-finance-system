# 数据库更新、后端Bug修复、前后端接口修复计划

## 一、问题分析

### 1.1 BankAccountConfig 实体与数据库不匹配

| 问题 | 数据库字段 | 实体字段 | 说明 |
|------|----------|----------|------|
| 1 | `org_id` | 缺失 | 需要添加 |
| 2 | `balance` | 缺失 | 需要添加 |
| 3 | `last_sync_time` | 缺失 | 需要添加 |
| 4 | 无 | `bankBranchName` | 数据库无此字段 |
| 5 | 无 | `bankLineNo` | 数据库无此字段 |
| 6 | 无 | `isMainAccount` | 数据库无此字段 |
| 7 | 无 | `balanceThreshold` | 前端用 `thresholdWarning` |
| 8 | 无 | `apiEndpoint` | 前端用 `apiEndpoint` |
| 9 | `status` (0/1) | `status` (ACTIVE/DISABLED) | 类型不匹配 |
| 10 | 无 | `apiStatus` | 前端需要 |

### 1.2 前后端接口不匹配

**前端 BankAccountConfigVO 期望字段**:
```typescript
{
  id, accountNo, accountName, bankName, bankCode,
  currency, balance, availableBalance,
  thresholdWarning, thresholdStop, dailyLimit, singleLimit,
  apiEndpoint, apiStatus, status, remark
}
```

**后端 queryBalance 返回字段**:
```java
{
  accountName, bankName, availableBalance, bookBalance,
  frozenBalance, queryTime, alert, alertMessage  // 缺少 balance!
}
```

**后端 getAccountConfigs 返回 BankAccountConfig 实体**:
```java
{
  bankCode, bankName, accountNo, accountName,
  bankBranchName, bankLineNo, accountType, currency,
  isMainAccount, dailyLimit, singleLimit,
  balanceThreshold, apiEndpoint, status  // 与前端期望不符!
}
```

---

## 二、实施步骤

### 步骤 1: 更新数据库结构 - 修改 bank_account_config 表
- [ ] 1.1 添加 `org_id` 字段
- [ ] 1.2 添加 `balance` 字段
- [ ] 1.3 添加 `last_sync_time` 字段
- [ ] 1.4 添加 `threshold_warning` 字段 (预警阈值)
- [ ] 1.5 添加 `threshold_stop` 字段 (停用阈值)
- [ ] 1.6 添加 `api_status` 字段 (API状态: 0-异常 1-正常)
- [ ] 1.7 添加 `api_endpoint` 字段

### 步骤 2: 更新后端 BankAccountConfig 实体类
- [ ] 2.1 添加 `orgId` 字段
- [ ] 2.2 添加 `balance` 字段
- [ ] 2.3 添加 `lastSyncTime` 字段
- [ ] 2.4 将 `balanceThreshold` 改为 `thresholdWarning`
- [ ] 2.5 添加 `thresholdStop` 字段
- [ ] 2.6 添加 `apiStatus` 字段
- [ ] 2.7 将 `status` 从 String 改为 Integer (0-禁用 1-启用)
- [ ] 2.8 保留 `apiEndpoint` 字段

### 步骤 3: 修复 BankDirectConnectServiceImpl
- [ ] 3.1 修复 queryBalance 方法 - 添加 `balance` 字段返回
- [ ] 3.2 修复 queryBalance 方法 - 返回正确的字段名 (thresholdWarning)
- [ ] 3.3 确保 getAccountConfigs 返回的数据包含前端需要的所有字段

### 步骤 4: 验证并测试
- [ ] 4.1 编译后端项目
- [ ] 4.2 检查是否有其他编译错误
- [ ] 4.3 确认接口返回格式与前端期望一致

---

## 三、SQL 变更脚本

```sql
-- 银行账户配置表补充字段
ALTER TABLE `bank_account_config`
  ADD COLUMN `org_id` BIGINT COMMENT '所属机构ID' AFTER `status`,
  ADD COLUMN `balance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '账户余额' AFTER `org_id`,
  ADD COLUMN `last_sync_time` DATETIME COMMENT '最后同步时间' AFTER `balance`,
  ADD COLUMN `threshold_warning` DECIMAL(18,2) DEFAULT 0.00 COMMENT '预警阈值' AFTER `balance_threshold`,
  ADD COLUMN `threshold_stop` DECIMAL(18,2) DEFAULT 0.00 COMMENT '停用阈值' AFTER `threshold_warning`,
  ADD COLUMN `api_status` TINYINT DEFAULT 0 COMMENT 'API状态：0-异常 1-正常' AFTER `api_endpoint`,
  ADD COLUMN `api_endpoint` VARCHAR(255) COMMENT '银企直连接口地址' AFTER `threshold_stop`;

-- 注意：删除不再需要的 balance_threshold 字段（如果存在）
-- ALTER TABLE `bank_account_config` DROP COLUMN IF EXISTS `balance_threshold`;
```

---

## 四、预期结果

1. 数据库表结构与前端期望一致
2. 后端接口返回字段与前端接口定义一致
3. `getBankAccountList` 接口返回:
   ```json
   {
     "code": 200,
     "message": "success",
     "data": [{
       "id": 1,
       "accountNo": "123456789",
       "accountName": "xxx",
       "bankName": "xxx",
       "bankCode": "xxx",
       "currency": "CNY",
       "balance": 10000.00,
       "availableBalance": 9500.00,
       "thresholdWarning": 1000.00,
       "thresholdStop": 500.00,
       "dailyLimit": 500000.00,
       "singleLimit": 100000.00,
       "apiEndpoint": "http://xxx",
       "apiStatus": 1,
       "status": 1
     }]
   }
   ```

4. `queryBalance` 接口返回:
   ```json
   {
     "code": 200,
     "message": "success",
     "data": {
       "accountNo": "123456789",
       "accountName": "xxx",
       "bankName": "xxx",
       "balance": 10000.00,
       "availableBalance": 9500.00,
       "updateTime": "2026-04-11 10:00:00",
       "isBelowThreshold": false
     }
   }
   ```
