-- =============================================
-- Task 7-11 补充表结构（追加到init.sql）
-- =============================================

-- 定时任务表
DROP TABLE IF EXISTS `schedule_job`;
CREATE TABLE `schedule_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `job_group` VARCHAR(50) DEFAULT 'DEFAULT' COMMENT '任务分组',
  `bean_name` VARCHAR(100) NOT NULL COMMENT '执行Bean名称',
  `method_name` VARCHAR(100) NOT NULL COMMENT '执行方法名',
  `method_params` VARCHAR(500) COMMENT '方法参数(JSON)',
  `cron_expression` VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
  `misfire_policy` TINYINT DEFAULT 0 COMMENT '失败策略：0-默认 1-立即执行 2-执行一次 3-放弃执行',
  `concurrent` TINYINT DEFAULT 0 COMMENT '并发策略：0-允许 1-禁止',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态：0-暂停 1-正常',
  `next_execute_time` DATETIME COMMENT '下次执行时间',
  `prev_execute_time` DATETIME COMMENT '上次执行时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 定时任务执行日志表
DROP TABLE IF EXISTS `schedule_job_log`;
CREATE TABLE `schedule_job_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_id` BIGINT NOT NULL COMMENT '任务ID',
  `job_name` VARCHAR(100) COMMENT '任务名称',
  `job_group` VARCHAR(50) COMMENT '任务分组',
  `invoke_target` VARCHAR(255) COMMENT '调用目标字符串',
  `job_message` VARCHAR(500) COMMENT '日志信息',
  `status` TINYINT DEFAULT 0 COMMENT '执行状态：0-成功 1-失败',
  `exception_info` TEXT COMMENT '异常信息',
  `start_time` BIGINT COMMENT '开始时间戳(ms)',
  `end_time` BIGINT COMMENT '结束时间戳(ms)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_job_id` (`job_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';

-- 内置任务配置表
DROP TABLE IF EXISTS `schedule_task_config`;
CREATE TABLE `schedule_task_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_code` VARCHAR(50) NOT NULL COMMENT '内置任务编码',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `description` VARCHAR(500) COMMENT '任务描述',
  `cron_expression` VARCHAR(100) NOT NULL COMMENT '默认Cron表达式',
  `execute_order` INT DEFAULT 0 COMMENT '执行顺序',
  `status` CHAR(1) DEFAULT '1' COMMENT '启用状态：0-禁用 1-启用',
  `last_execute_time` DATETIME COMMENT '上次执行时间',
  `next_execute_time` DATETIME COMMENT '下次执行时间',
  `success_count` INT DEFAULT 0 COMMENT '成功次数',
  `fail_count` INT DEFAULT 0 COMMENT '失败次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_task_code` (`task_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内置定时任务配置表';

-- =============================================
-- 补充银行账户配置表的额外字段（银企直连增强）
-- =============================================
ALTER TABLE `bank_account_config`
  ADD COLUMN `balance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '当前余额' AFTER `currency`,
  ADD COLUMN `available_balance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '可用余额' AFTER `balance`,
  ADD COLUMN `threshold_warning` DECIMAL(18,2) DEFAULT 0.00 COMMENT '预警阈值' AFTER `statement_download_enabled`,
  ADD COLUMN `threshold_stop` DECIMAL(18,2) DEFAULT 0.00 COMMENT '停用阈值' AFTER `threshold_warning`,
  ADD COLUMN `daily_limit` DECIMAL(18,2) DEFAULT 0.00 COMMENT '日累计限额' AFTER `threshold_stop`,
  ADD COLUMN `single_limit` DECIMAL(18,2) DEFAULT 0.00 COMMENT '单笔限额' AFTER `daily_limit`,
  ADD COLUMN `api_endpoint` VARCHAR(255) COMMENT 'API接口地址' AFTER `single_limit`,
  ADD COLUMN `api_status` TINYINT DEFAULT 0 COMMENT '接口状态：0-未配置 1-正常 2-异常' AFTER `api_endpoint`;

-- =============================================
-- 审批流程相关表（组织管理模块补充）
-- =============================================
DROP TABLE IF EXISTS `process_definition`;
CREATE TABLE `process_definition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_name` VARCHAR(100) NOT NULL COMMENT '流程名称',
  `process_key` VARCHAR(50) NOT NULL COMMENT '流程标识',
  `process_type` VARCHAR(30) COMMENT '流程类型：收款审批/付款审批/凭证审核等',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `definition_json` TEXT COMMENT '流程定义JSON',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_process_key_version` (`process_key`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

DROP TABLE IF EXISTS `process_instance`;
CREATE TABLE `process_instance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
  `business_key` VARCHAR(50) COMMENT '业务主键',
  `business_type` VARCHAR(30) COMMENT '业务类型',
  `current_node` VARCHAR(50) COMMENT '当前节点',
  `initiator_id` BIGINT COMMENT '发起人ID',
  `initiator_name` VARCHAR(50) COMMENT '发起人姓名',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-进行中 1-已完成 2-已驳回 3-已撤回',
  `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
  `end_time` DATETIME COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_process_def_id` (`process_def_id`),
  INDEX `idx_business_key` (`business_key`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

DROP TABLE IF EXISTS `process_node`;
CREATE TABLE `process_node` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
  `node_name` VARCHAR(50) NOT NULL COMMENT '节点名称',
  `node_key` VARCHAR(50) COMMENT '节点标识',
  `node_type` VARCHAR(20) COMMENT '节点类型：start/end/approve/condition等',
  `assignee_type` VARCHAR(20) COMMENT '审批人类型：user/role/dept/leader',
  `assignee_value` VARCHAR(200) COMMENT '审批人值',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-已通过 2-已驳回 3-已跳过',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approver_name` VARCHAR(50) COMMENT '审批人姓名',
  `approve_time` DATETIME COMMENT '审批时间',
  `approve_comment` VARCHAR(500) COMMENT '审批意见',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_instance_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表';

DROP TABLE IF EXISTS `approve_record`;
CREATE TABLE `approve_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
  `node_id` BIGINT COMMENT '节点ID',
  `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
  `approver_name` VARCHAR(50) COMMENT '审批人姓名',
  `action` VARCHAR(20) NOT NULL COMMENT '操作：agree/reject/return/withdraw',
  `comment` VARCHAR(500) COMMENT '审批意见',
  `approve_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_instance_id` (`instance_id`),
  INDEX `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- =============================================
-- 初始化内置定时任务数据
-- =============================================
INSERT INTO `schedule_task_config` (`task_code`, `task_name`, `description`, `cron_expression`, `execute_order`, `status`) VALUES
('SYNC_CUSTOMER_FULL', '客户信息全量同步', '每日凌晨2点全量同步NC Cloud客户档案到本地', '0 0 2 * * ?', 1, '1'),
('SYNC_CUSTOMER_INCREMENTAL', '客户信息增量同步', '每30分钟增量同步变更的客户数据', '0 */30 * * * ?', 2, '1'),
('SYNC_PRODUCT', '产品信息同步', '每日凌晨3点同步产品及费率信息', '0 0 3 * * ?', 3, '1'),
('SYNC_VOUCHER_TO_NC', '凭证数据推送NC Cloud', '每小时将已审核凭证推送到NC Cloud生成会计凭证', '0 0 * * * ?', 4, '1'),
('BANK_RECONCILIATION_AUTO', '银行自动对账', '每日凌晨5点自动下载银行流水并执行对账', '0 0 5 * * ?', 5, '1'),
('BANK_BALANCE_CHECK', '银行余额监控', '每10分钟检查各账户余额是否低于预警阈值', '0 */10 * * * ?', 6, '1'),
('CFCA_CERT_EXPIRY_CHECK', 'CFCA证书到期检查', '每天上午9点检查CFCA数字证书有效期，提前30天预警', '0 0 9 * * ?', 7, '1'),
('AUTO_GENERATE_VOUCHER', '自动生成凭证', '每日凌晨4点对已审核的收付款单自动生成会计凭证', '0 0 4 * * ?', 8, '1'),
('DATA_CLEAN_EXPIRED', '过期数据清理', '每月1号凌晨清理90天前的临时数据和日志', '0 0 2 1 * ?', 9, '1'),
('SYSTEM_HEALTH_CHECK', '系统健康检查', '每30分钟检查数据库连接、Redis、外部接口可用性', '0 */30 * * * ?', 10, '1');

-- =============================================
-- 补充菜单数据（CFCA认证 + 定时任务）
-- =============================================
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `sort_order`, `path`, `component`, `icon`, `permission`, `menu_type`, `status`) VALUES
('CFCA认证', 0, 8, '/cfca', 'views/cfca/index', 'Key', '', 2, 1),
('定时任务', 0, 9, '/schedule', 'views/schedule/index', 'Timer', '', 2, 1);
