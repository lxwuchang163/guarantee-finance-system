-- 银行账户配置表补充字段
-- 执行: mysql -u root -p guarantee_finance < sql/bank-account-config-update.sql

USE `guarantee_finance`;

-- 添加缺失字段到 bank_account_config 表
ALTER TABLE `bank_account_config`
  ADD COLUMN `org_id` BIGINT COMMENT '所属机构ID' AFTER `remark`,
  ADD COLUMN `balance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '账户余额' AFTER `org_id`,
  ADD COLUMN `last_sync_time` DATETIME COMMENT '最后同步时间' AFTER `balance`,
  ADD COLUMN `threshold_warning` DECIMAL(18,2) DEFAULT 0.00 COMMENT '预警阈值' AFTER `last_sync_time`,
  ADD COLUMN `threshold_stop` DECIMAL(18,2) DEFAULT 0.00 COMMENT '停用阈值' AFTER `threshold_warning`,
  ADD COLUMN `api_status` TINYINT DEFAULT 1 COMMENT 'API状态：0-异常 1-正常' AFTER `remark`,
  ADD COLUMN `api_endpoint` VARCHAR(255) COMMENT '银企直连接口地址' AFTER `api_status`;

-- 删除不再需要的旧字段（如果存在）
ALTER TABLE `bank_account_config` DROP COLUMN IF EXISTS `balance_threshold`;
