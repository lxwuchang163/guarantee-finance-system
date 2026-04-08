-- 担保集团业务财务系统数据库初始化脚本
-- 数据库: guarantee_finance
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS `guarantee_finance` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `guarantee_finance`;

-- =============================================
-- 系统管理相关表
-- =============================================

-- 机构表
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_name` VARCHAR(100) NOT NULL COMMENT '机构名称',
  `org_code` VARCHAR(50) NOT NULL COMMENT '机构编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级机构ID',
  `level` INT DEFAULT 1 COMMENT '层级',
  `leader` VARCHAR(50) COMMENT '负责人',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `address` VARCHAR(255) COMMENT '地址',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(200) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像地址',
  `sex` TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `org_id` BIGINT DEFAULT NULL COMMENT '所属机构ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_username` (`username`),
  INDEX `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `path` VARCHAR(200) COMMENT '路由路径',
  `component` VARCHAR(255) COMMENT '组件路径',
  `icon` VARCHAR(100) COMMENT '图标',
  `permission` VARCHAR(100) COMMENT '权限标识',
  `menu_type` TINYINT DEFAULT 1 COMMENT '类型：1-目录 2-菜单 3-按钮',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-显示 0-隐藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 字典表
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `label` VARCHAR(100) NOT NULL COMMENT '字典标签',
  `value` VARCHAR(100) NOT NULL COMMENT '字典值',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 操作日志表
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` VARCHAR(50) COMMENT '模块标题',
  `business_type` VARCHAR(20) COMMENT '业务类型',
  `method` VARCHAR(200) COMMENT '方法名称',
  `request_method` VARCHAR(10) COMMENT '请求方式',
  `operator_type` TINYINT DEFAULT 1 COMMENT '操作类别：1-其他 2-后台用户',
  `oper_name` VARCHAR(50) COMMENT '操作人员',
  `oper_url` VARCHAR(255) COMMENT '请求URL',
  `oper_ip` VARCHAR(128) COMMENT '主机地址',
  `oper_location` VARCHAR(255) COMMENT '操作地点',
  `oper_param` LONGTEXT COMMENT '请求参数',
  `json_result` LONGTEXT COMMENT '返回参数',
  `status` TINYINT DEFAULT 0 COMMENT '操作状态：0-正常 1-异常',
  `error_msg` VARCHAR(2000) COMMENT '错误消息',
  `cost_time` BIGINT DEFAULT 0 COMMENT '消耗时间（毫秒）',
  `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  INDEX `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 登录日志表
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) COMMENT '用户账号',
  `ipaddr` VARCHAR(128) COMMENT '登录IP地址',
  `login_location` VARCHAR(255) COMMENT '登录地点',
  `browser` VARCHAR(50) COMMENT '浏览器类型',
  `os` VARCHAR(50) COMMENT '操作系统',
  `status` TINYINT DEFAULT 1 COMMENT '登录状态：1-成功 0-失败',
  `msg` VARCHAR(255) COMMENT '提示消息',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  INDEX `idx_username` (`username`),
  INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =============================================
-- 基础信息同步相关表
-- =============================================

-- 客户信息表
DROP TABLE IF EXISTS `biz_customer`;
CREATE TABLE `biz_customer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_code` VARCHAR(50) NOT NULL COMMENT '客户编码',
  `customer_name` VARCHAR(200) NOT NULL COMMENT '客户名称',
  `customer_type` VARCHAR(20) COMMENT '客户类型：企业/个人',
  `tax_no` VARCHAR(50) COMMENT '税号',
  `legal_person` VARCHAR(50) COMMENT '法人代表',
  `contact_person` VARCHAR(50) COMMENT '联系人',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `address` VARCHAR(500) COMMENT '地址',
  `credit_code` VARCHAR(50) COMMENT '统一社会信用代码',
  `sync_status` TINYINT DEFAULT 0 COMMENT '同步状态：0-未同步 1-已同步 2-同步失败',
  `sync_time` DATETIME COMMENT '最后同步时间',
  `source_system` VARCHAR(50) COMMENT '来源系统',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_customer_code` (`customer_code`),
  INDEX `idx_customer_name` (`customer_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 客户银行账号表
DROP TABLE IF EXISTS `biz_customer_bank_account`;
CREATE TABLE `biz_customer_bank_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `bank_name` VARCHAR(100) NOT NULL COMMENT '开户银行',
  `bank_account` VARCHAR(50) NOT NULL COMMENT '银行账号',
  `account_name` VARCHAR(100) COMMENT '账户名称',
  `account_type` VARCHAR(20) COMMENT '账户类型：基本/一般/专用',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_customer_id` (`customer_id`),
  INDEX `idx_bank_account` (`bank_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户银行账号表';

-- 业务品种表
DROP TABLE IF EXISTS `biz_product`;
CREATE TABLE `biz_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` VARCHAR(50) NOT NULL COMMENT '品种编码',
  `product_name` VARCHAR(100) NOT NULL COMMENT '品种名称',
  `product_category` VARCHAR(50) COMMENT '品种分类',
  `description` VARCHAR(500) COMMENT '品种描述',
  `sync_status` TINYINT DEFAULT 0 COMMENT '同步状态',
  `sync_time` DATETIME COMMENT '最后同步时间',
  `source_system` VARCHAR(50) COMMENT '来源系统',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务品种表';

-- 费率版本表
DROP TABLE IF EXISTS `biz_product_rate`;
CREATE TABLE `biz_product_rate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '业务品种ID',
  `version_no` VARCHAR(20) NOT NULL COMMENT '版本号',
  `rate_type` VARCHAR(20) COMMENT '费率类型',
  `rate_value` DECIMAL(10,6) COMMENT '费率值',
  `min_amount` DECIMAL(18,2) COMMENT '最低金额',
  `max_amount` DECIMAL(18,2) COMMENT '最高金额',
  `effective_date` DATE COMMENT '生效日期',
  `expiry_date` DATE COMMENT '失效日期',
  `sync_status` TINYINT DEFAULT 0 COMMENT '同步状态',
  `sync_time` DATETIME COMMENT '最后同步时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-生效 0-失效',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费率版本表';

-- 同步任务表
DROP TABLE IF EXISTS `sync_task`;
CREATE TABLE `sync_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `task_code` VARCHAR(50) NOT NULL COMMENT '任务编码',
  `task_type` VARCHAR(30) NOT NULL COMMENT '任务类型：客户/产品/费率等',
  `cron_expression` VARCHAR(100) COMMENT 'Cron表达式',
  `source_system` VARCHAR(50) COMMENT '源系统',
  `target_system` VARCHAR(50) COMMENT '目标系统',
  `last_exec_time` DATETIME COMMENT '上次执行时间',
  `next_exec_time` DATETIME COMMENT '下次执行时间',
  `exec_status` TINYINT DEFAULT 0 COMMENT '执行状态：0-待执行 1-执行中 2-成功 3-失败',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_task_code` (`task_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步任务表';

-- 同步任务日志表
DROP TABLE IF EXISTS `sync_task_log`;
CREATE TABLE `sync_task_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `exec_start_time` DATETIME NOT NULL COMMENT '开始时间',
  `exec_end_time` DATETIME COMMENT '结束时间',
  `exec_status` TINYINT DEFAULT 0 COMMENT '执行状态：0-执行中 1-成功 2-失败',
  `total_count` INT DEFAULT 0 COMMENT '总记录数',
  `success_count` INT DEFAULT 0 COMMENT '成功数',
  `fail_count` INT DEFAULT 0 COMMENT '失败数',
  `error_msg` TEXT COMMENT '错误信息',
  `duration` BIGINT COMMENT '耗时（毫秒）',
  `create_by` BIGINT DEFAULT NULL COMMENT '执行人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_exec_status` (`exec_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步任务日志表';

-- 同步异常记录表
DROP TABLE IF EXISTS `sync_error_record`;
CREATE TABLE `sync_error_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT COMMENT '任务ID',
  `task_log_id` BIGINT COMMENT '任务日志ID',
  `data_type` VARCHAR(50) COMMENT '数据类型',
  `data_key` VARCHAR(100) COMMENT '数据标识',
  `data_content` TEXT COMMENT '原始数据内容',
  `error_type` VARCHAR(50) COMMENT '错误类型',
  `error_msg` TEXT COMMENT '错误信息',
  `handle_status` TINYINT DEFAULT 0 COMMENT '处理状态：0-待处理 1-已处理 2-忽略',
  `handle_result` VARCHAR(500) COMMENT '处理结果',
  `handle_by` BIGINT DEFAULT NULL COMMENT '处理人',
  `handle_time` DATETIME COMMENT '处理时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步异常记录表';

-- =============================================
-- 收付款单相关表
-- =============================================

-- 收款单主表
DROP TABLE IF EXISTS `fin_receipt`;
CREATE TABLE `fin_receipt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_no` VARCHAR(50) NOT NULL COMMENT '收款单编号',
  `receipt_date` DATE NOT NULL COMMENT '收款日期',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `customer_name` VARCHAR(200) COMMENT '客户名称',
  `total_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '收款总金额',
  `received_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '已收金额',
  `payment_method` VARCHAR(20) COMMENT '收款方式：银行转账/现金/支票等',
  `bank_account_id` BIGINT COMMENT '收款银行账号ID',
  `bank_account_name` VARCHAR(100) COMMENT '收款银行账号',
  `biz_type` VARCHAR(30) COMMENT '业务类型：担保费/手续费/其他',
  `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿 pending-待审核 approved-已审核 rejected-已驳回 completed-已完成',
  `audit_user_id` BIGINT COMMENT '审核人ID',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',
  `voucher_status` TINYINT DEFAULT 0 COMMENT '凭证状态：0-未生成 1-已生成 2-生成失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_receipt_no` (`receipt_no`),
  INDEX `idx_customer_id` (`customer_id`),
  INDEX `idx_receipt_date` (`receipt_date`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款单主表';

-- 收款单明细表
DROP TABLE IF EXISTS `fin_receipt_detail`;
CREATE TABLE `fin_receipt_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_id` BIGINT NOT NULL COMMENT '收款单主表ID',
  `detail_no` INT NOT NULL COMMENT '明细序号',
  `product_id` BIGINT COMMENT '业务品种ID',
  `product_name` VARCHAR(100) COMMENT '业务品种名称',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `rate` DECIMAL(10,6) COMMENT '费率',
  `base_amount` DECIMAL(18,2) COMMENT '本金/基数',
  `period_start_date` DATE COMMENT '期间开始日期',
  `period_end_date` DATE COMMENT '期间结束日期',
  `remark` VARCHAR(500) COMMENT '明细备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_receipt_id` (`receipt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款单明细表';

-- 付款单主表
DROP TABLE IF EXISTS `fin_payment`;
CREATE TABLE `fin_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '付款单编号',
  `payment_date` DATE NOT NULL COMMENT '付款日期',
  `payee_id` BIGINT NOT NULL COMMENT '收款方ID（客户或供应商）',
  `payee_name` VARCHAR(200) COMMENT '收款方名称',
  `payee_bank_account` VARCHAR(50) COMMENT '收款方银行账号',
  `payee_bank_name` VARCHAR(100) COMMENT '收款方开户银行',
  `total_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '付款总金额',
  `paid_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '已付金额',
  `payment_type` VARCHAR(20) COMMENT '付款类型：退费/代偿/追回分配/其他',
  `pay_bank_account_id` BIGINT COMMENT '付款银行账号ID',
  `pay_bank_account_name` VARCHAR(100) COMMENT '付款银行账号',
  `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿 pending-待审核 approved-已审核 rejected-已驳回 completed-已完成',
  `audit_user_id` BIGINT COMMENT '审核人ID',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',
  `voucher_status` TINYINT DEFAULT 0 COMMENT '凭证状态：0-未生成 1-已生成 2-生成失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_payment_no` (`payment_no`),
  INDEX `idx_payee_id` (`payee_id`),
  INDEX `idx_payment_date` (`payment_date`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款单主表';

-- 付款单明细表
DROP TABLE IF EXISTS `fin_payment_detail`;
CREATE TABLE `fin_payment_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` BIGINT NOT NULL COMMENT '付款单主表ID',
  `detail_no` INT NOT NULL COMMENT '明细序号',
  `expense_type` VARCHAR(50) COMMENT '费用类型',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `related_receipt_no` VARCHAR(50) COMMENT '关联收款单号',
  `remark` VARCHAR(500) COMMENT '明细备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_payment_id` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款单明细表';

-- 分担收入明细表
DROP TABLE IF EXISTS `fin_receipt_share`;
CREATE TABLE `fin_receipt_share` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_id` BIGINT NOT NULL COMMENT '收款单ID',
  `share_org_id` BIGINT NOT NULL COMMENT '分担机构ID',
  `share_org_name` VARCHAR(100) COMMENT '分担机构名称',
  `share_ratio` DECIMAL(5,4) COMMENT '分担比例',
  `share_amount` DECIMAL(18,2) COMMENT '分担金额',
  `settle_status` TINYINT DEFAULT 0 COMMENT '结算状态：0-未结算 1-已结算',
  `settle_time` DATETIME COMMENT '结算时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_receipt_id` (`receipt_id`),
  INDEX `idx_share_org_id` (`share_org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分担收入明细表';

-- 追偿到款明细表
DROP TABLE IF EXISTS `fin_receipt_recovery`;
CREATE TABLE `fin_receipt_recovery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_id` BIGINT NOT NULL COMMENT '收款单ID',
  `guarantee_no` VARCHAR(50) COMMENT '担保合同编号',
  `recovery_amount` DECIMAL(18,2) COMMENT '追偿到款金额',
  `recovery_date` DATE COMMENT '追偿到款日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_receipt_id` (`receipt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追偿到款明细表';

-- 退费支出明细表
DROP TABLE IF EXISTS `fin_payment_refund`;
CREATE TABLE `fin_payment_refund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` BIGINT NOT NULL COMMENT '付款单ID',
  `refund_reason` VARCHAR(500) COMMENT '退费原因',
  `refund_amount` DECIMAL(18,2) COMMENT '退费金额',
  `refund_date` DATE COMMENT '退费日期',
  `related_guarantee_no` VARCHAR(50) COMMENT '关联担保合同编号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_payment_id` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退费支出明细表';

-- 代偿支出明细表
DROP TABLE IF EXISTS `fin_payment_compensation`;
CREATE TABLE `fin_payment_compensation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` BIGINT NOT NULL COMMENT '付款单ID',
  `guarantee_no` VARCHAR(50) COMMENT '担保合同编号',
  `compensation_amount` DECIMAL(18,2) COMMENT '代偿金额',
  `compensation_date` DATE COMMENT '代偿日期',
  `borrower_name` VARCHAR(200) COMMENT '借款人名称',
  `principal_amount` DECIMAL(18,2) COMMENT '代偿本金',
  `interest_amount` DECIMAL(18,2) COMMENT '代偿利息',
  `other_amount` DECIMAL(18,2) COMMENT '其他费用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_payment_id` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代偿支出明细表';

-- 追回资金分配明细表
DROP TABLE IF EXISTS `fin_payment_distribution`;
CREATE TABLE `fin_payment_distribution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` BIGINT NOT NULL COMMENT '付款单ID',
  `distribution_org_id` BIGINT NOT NULL COMMENT '分配机构ID',
  `distribution_org_name` VARCHAR(100) COMMENT '分配机构名称',
  `distribution_ratio` DECIMAL(5,4) COMMENT '分配比例',
  `distribution_amount` DECIMAL(18,2) COMMENT '分配金额',
  `related_compensation_id` BIGINT COMMENT '关联代偿记录ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_payment_id` (`payment_id`),
  INDEX `idx_distribution_org_id` (`distribution_org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追回资金分配明细表';

-- =============================================
-- 凭证相关表
-- =============================================

-- 凭证头表
DROP TABLE IF EXISTS `acc_voucher`;
CREATE TABLE `acc_voucher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_no` VARCHAR(50) NOT NULL COMMENT '凭证编号',
  `voucher_date` DATE NOT NULL COMMENT '凭证日期',
  `voucher_type` VARCHAR(20) NOT NULL COMMENT '凭证类型：收款/付款/转账/通用',
  `biz_source` VARCHAR(50) COMMENT '业务来源：收款单/付款单等',
  `biz_source_id` BIGINT COMMENT '业务来源ID',
  `biz_source_no` VARCHAR(50) COMMENT '业务来源单号',
  `total_debit` DECIMAL(18,2) DEFAULT 0.00 COMMENT '借方合计',
  `total_credit` DECIMAL(18,2) DEFAULT 0.00 COMMENT '贷方合计',
  `attachment_count` INT DEFAULT 0 COMMENT '附件张数',
  `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿 audited-已审核 posted-已过账 cancelled-已作废',
  `nc_sync_status` TINYINT DEFAULT 0 COMMENT 'NC同步状态：0-未同步 1-已同步 2-同步失败',
  `nc_voucher_no` VARCHAR(50) COMMENT 'NC凭证号',
  `cfca_sign_status` TINYINT DEFAULT 0 COMMENT 'CFCA签名状态：0-未签名 1-已签名 2-签名失败',
  `post_user_id` BIGINT COMMENT '过账人ID',
  `post_time` DATETIME COMMENT '过账时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_voucher_no` (`voucher_no`),
  INDEX `idx_biz_source` (`biz_source`, `biz_source_id`),
  INDEX `idx_voucher_date` (`voucher_date`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证头表';

-- 凭证明细表
DROP TABLE IF EXISTS `acc_voucher_detail`;
CREATE TABLE `acc_voucher_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_id` BIGINT NOT NULL COMMENT '凭证头ID',
  `line_no` INT NOT NULL COMMENT '行号',
  `account_code` VARCHAR(50) NOT NULL COMMENT '科目编码',
  `account_name` VARCHAR(100) COMMENT '科目名称',
  `debit_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '借方金额',
  `credit_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '贷方金额',
  `summary` VARCHAR(255) COMMENT '摘要',
  `aux_customer_code` VARCHAR(50) COMMENT '辅助核算-客户编码',
  `aux_customer_name` VARCHAR(200) COMMENT '辅助核算-客户名称',
  `aux_dept_code` VARCHAR(50) COMMENT '辅助核算-部门编码',
  `aux_dept_name` VARCHAR(100) COMMENT '辅助核算-部门名称',
  `aux_project_code` VARCHAR(50) COMMENT '辅助核算-项目编码',
  `aux_project_name` VARCHAR(100) COMMENT '辅助核算-项目名称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_voucher_id` (`voucher_id`),
  INDEX `idx_account_code` (`account_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证明细表';

-- 凭证模板表
DROP TABLE IF EXISTS `acc_voucher_template`;
CREATE TABLE `acc_voucher_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
  `template_type` VARCHAR(20) NOT NULL COMMENT '模板类型：收款/付款/代偿/退费等',
  `description` VARCHAR(500) COMMENT '模板描述',
  `template_content` JSON COMMENT '模板内容（JSON格式）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证模板表';

-- 客户核算设置表
DROP TABLE IF EXISTS `acc_customer_setting`;
CREATE TABLE `acc_customer_setting` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `customer_code` VARCHAR(50) COMMENT '客户编码',
  `customer_name` VARCHAR(200) COMMENT '客户名称',
  `receivable_account_code` VARCHAR(50) COMMENT '应收科目编码',
  `receivable_account_name` VARCHAR(100) COMMENT '应收科目名称',
  `income_account_code` VARCHAR(50) COMMENT '收入科目编码',
  `income_account_name` VARCHAR(100) COMMENT '收入科目名称',
  `tax_account_code` VARCHAR(50) COMMENT '税金科目编码',
  `tax_rate` DECIMAL(5,4) COMMENT '税率',
  `nc_customer_code` VARCHAR(50) COMMENT 'NC客户编码',
  `nc_department_code` VARCHAR(50) COMMENT 'NC部门编码',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户核算设置表';

-- =============================================
-- 银行对账相关表
-- =============================================

-- 银行流水表
DROP TABLE IF EXISTS `bank_transaction`;
CREATE TABLE `bank_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transaction_date` DATE NOT NULL COMMENT '交易日期',
  `transaction_time` DATETIME COMMENT '交易时间',
  `bank_account_id` BIGINT COMMENT '银行账户配置ID',
  `bank_account_no` VARCHAR(50) COMMENT '银行账号',
  `bank_name` VARCHAR(100) COMMENT '开户银行',
  `transaction_type` VARCHAR(20) COMMENT '交易类型：收入/支出',
  `transaction_no` VARCHAR(100) COMMENT '银行流水号',
  `counterparty_name` VARCHAR(200) COMMENT '对方户名',
  `counterparty_account` VARCHAR(50) COMMENT '对方账号',
  `summary` VARCHAR(500) COMMENT '摘要/用途',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `balance` DECIMAL(18,2) COMMENT '余额',
  `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `reconciliation_status` TINYINT DEFAULT 0 COMMENT '对账状态：0-未对账 1-已匹配 2-异常',
  `matched_receipt_id` BIGINT COMMENT '匹配的收款单ID',
  `matched_payment_id` BIGINT COMMENT '匹配的付款单ID',
  `import_batch_no` VARCHAR(50) COMMENT '导入批次号',
  `data_source` VARCHAR(20) COMMENT '数据来源：手工导入/银企直连',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_bank_account_no` (`bank_account_no`),
  INDEX `idx_transaction_date` (`transaction_date`),
  INDEX `idx_transaction_no` (`transaction_no`),
  INDEX `idx_reconciliation_status` (`reconciliation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行流水表';

-- 对账结果表
DROP TABLE IF EXISTS `bank_reconciliation`;
CREATE TABLE `bank_reconciliation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reconciliation_date` DATE NOT NULL COMMENT '对账日期',
  `bank_account_id` BIGINT NOT NULL COMMENT '银行账户ID',
  `bank_account_no` VARCHAR(50) COMMENT '银行账号',
  `bank_balance` DECIMAL(18,2) COMMENT '银行账面余额',
  `book_balance` DECIMAL(18,2) COMMENT '企业账面余额',
  `total_transactions` INT DEFAULT 0 COMMENT '流水总数',
  `matched_count` INT DEFAULT 0 COMMENT '已匹配数',
  `unmatched_count` INT DEFAULT 0 COMMENT '未匹配数',
  `difference_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '差额',
  `reconciliation_status` VARCHAR(20) DEFAULT 'pending' COMMENT '对账状态：pending-待完成 completed-已完成',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operate_time` DATETIME COMMENT '操作时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_bank_account_id` (`bank_account_id`),
  INDEX `idx_reconciliation_date` (`reconciliation_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账结果表';

-- 余额调节表
DROP TABLE IF EXISTS `bank_balance_adjustment`;
CREATE TABLE `bank_balance_adjustment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reconciliation_id` BIGINT NOT NULL COMMENT '对账结果ID',
  `adjustment_type` VARCHAR(20) NOT NULL COMMENT '调节类型：银行未达/企业未达',
  `description` VARCHAR(500) COMMENT '调节说明',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '调节金额',
  `related_transaction_id` BIGINT COMMENT '关联流水ID',
  `related_voucher_id` BIGINT COMMENT '关联凭证ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_reconciliation_id` (`reconciliation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='余额调节表';

-- =============================================
-- 银企直连相关表
-- =============================================

-- 银行账户配置表
DROP TABLE IF EXISTS `bank_account_config`;
CREATE TABLE `bank_account_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
  `account_no` VARCHAR(50) NOT NULL COMMENT '银行账号',
  `bank_name` VARCHAR(100) NOT NULL COMMENT '开户银行',
  `bank_code` VARCHAR(20) COMMENT '银行联行号',
  `account_type` VARCHAR(20) COMMENT '账户类型：基本户/一般户/专用户',
  `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `org_id` BIGINT COMMENT '所属机构ID',
  `is_connection` TINYINT DEFAULT 0 COMMENT '是否开通银企直连：0-否 1-是',
  `connection_type` VARCHAR(20) COMMENT '连接方式：API/文件',
  `connection_config` JSON COMMENT '连接配置信息',
  `balance_query_enabled` TINYINT DEFAULT 0 COMMENT '是否支持余额查询',
  `payment_enabled` TINYINT DEFAULT 0 COMMENT '是否支持支付',
  `statement_download_enabled` TINYINT DEFAULT 0 COMMENT '是否支持下载对账单',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_account_no` (`account_no`),
  INDEX `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行账户配置表';

-- 银企连接日志表
DROP TABLE IF EXISTS `bank_connection_log`;
CREATE TABLE `bank_connection_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bank_account_id` BIGINT NOT NULL COMMENT '银行账户ID',
  `operation_type` VARCHAR(30) NOT NULL COMMENT '操作类型：余额查询/支付/下载对账单等',
  `request_data` TEXT COMMENT '请求数据',
  `response_data` TEXT COMMENT '响应数据',
  `status` TINYINT DEFAULT 0 COMMENT '执行状态：0-成功 1-失败',
  `error_msg` VARCHAR(1000) COMMENT '错误信息',
  `duration` BIGINT COMMENT '耗时（毫秒）',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_bank_account_id` (`bank_account_id`),
  INDEX `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银企连接日志表';

-- 支付指令表
DROP TABLE IF EXISTS `payment_order`;
CREATE TABLE `payment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '支付指令编号',
  `bank_account_id` BIGINT NOT NULL COMMENT '付款账户ID',
  `payee_name` VARCHAR(200) NOT NULL COMMENT '收款人名称',
  `payee_account` VARCHAR(50) NOT NULL COMMENT '收款人账号',
  `payee_bank_name` VARCHAR(100) COMMENT '收款人开户银行',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '支付金额',
  `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `summary` VARCHAR(255) COMMENT '用途/摘要',
  `urgent_flag` TINYINT DEFAULT 0 COMMENT '加急标志：0-普通 1-加急',
  `related_payment_id` BIGINT COMMENT '关联付款单ID',
  `related_payment_no` VARCHAR(50) COMMENT '关联付款单号',
  `submit_status` TINYINT DEFAULT 0 COMMENT '提交状态：0-待提交 1-已提交 2-处理中 3-成功 4-失败',
  `bank_serial_no` VARCHAR(100) COMMENT '银行流水号',
  `bank_result_msg` VARCHAR(500) COMMENT '银行返回结果',
  `submit_time` DATETIME COMMENT '提交时间',
  `complete_time` DATETIME COMMENT '完成时间',
  `cfca_sign_required` TINYINT DEFAULT 0 COMMENT '是否需要CFCA签名',
  `cfca_sign_status` TINYINT DEFAULT 0 COMMENT 'CFCA签名状态',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_order_no` (`order_no`),
  INDEX `idx_bank_account_id` (`bank_account_id`),
  INDEX `idx_submit_status` (`submit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付指令表';

-- =============================================
-- CFCA相关表
-- =============================================

-- 数字证书表
DROP TABLE IF EXISTS `cfca_certificate`;
CREATE TABLE `cfca_certificate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cert_name` VARCHAR(100) NOT NULL COMMENT '证书名称',
  `cert_no` VARCHAR(100) NOT NULL COMMENT '证书序列号',
  `cert_type` VARCHAR(20) COMMENT '证书类型：签名/加密',
  `owner_type` VARCHAR(20) COMMENT '持有者类型：用户/系统',
  `owner_id` BIGINT COMMENT '持有者ID',
  `owner_name` VARCHAR(100) COMMENT '持有者名称',
  `issue_date` DATE COMMENT '签发日期',
  `expire_date` DATE COMMENT '过期日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效 0-失效 2-已注销',
  `cert_file_path` VARCHAR(500) COMMENT '证书文件路径',
  `public_key` TEXT COMMENT '公钥',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_cert_no` (`cert_no`),
  INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字证书表';

-- 签名日志表
DROP TABLE IF EXISTS `cfca_sign_log`;
CREATE TABLE `cfca_sign_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  `business_id` BIGINT COMMENT '业务ID',
  `business_no` VARCHAR(50) COMMENT '业务单号',
  `sign_type` VARCHAR(20) COMMENT '签名类型：RSA/SM2',
  `original_data_hash` VARCHAR(512) COMMENT '原文哈希值',
  `signature_value` TEXT COMMENT '签名值',
  `certificate_id` BIGINT COMMENT '使用证书ID',
  `sign_status` TINYINT DEFAULT 0 COMMENT '签名状态：0-成功 1-失败',
  `error_msg` VARCHAR(1000) COMMENT '错误信息',
  `verify_status` TINYINT DEFAULT 0 COMMENT '验签状态：0-未验签 1-验签通过 2-验签失败',
  `sign_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签名时间',
  `sign_user_id` BIGINT COMMENT '签名人ID',
  `sign_user_name` VARCHAR(50) COMMENT '签名人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_business_type` (`business_type`, `business_id`),
  INDEX `idx_sign_time` (`sign_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签名日志表';

-- =============================================
-- NC Cloud对接相关表
-- =============================================

-- NC同步日志表
DROP TABLE IF EXISTS `nc_sync_log`;
CREATE TABLE `nc_sync_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sync_type` VARCHAR(30) NOT NULL COMMENT '同步类型：凭证/客户/供应商等',
  `sync_direction` VARCHAR(10) COMMENT '同步方向：push-推送 pull-拉取',
  `source_system` VARCHAR(50) DEFAULT 'FINANCE' COMMENT '源系统',
  `target_system` VARCHAR(50) DEFAULT 'NC_CLOUD' COMMENT '目标系统',
  `data_count` INT DEFAULT 0 COMMENT '数据条数',
  `success_count` INT DEFAULT 0 COMMENT '成功数',
  `fail_count` INT DEFAULT 0 COMMENT '失败数',
  `sync_status` TINYINT DEFAULT 0 COMMENT '同步状态：0-进行中 1-成功 2-部分成功 3-失败',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `duration` BIGINT COMMENT '耗时（毫秒）',
  `request_data` TEXT COMMENT '请求报文',
  `response_data` TEXT COMMENT '响应报文',
  `error_summary` VARCHAR(1000) COMMENT '错误汇总',
  `operator_id` BIGINT COMMENT '操作人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_sync_type` (`sync_type`),
  INDEX `idx_sync_status` (`sync_status`),
  INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NC同步日志表';

-- NC同步异常表
DROP TABLE IF EXISTS `nc_sync_error`;
CREATE TABLE `nc_sync_error` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sync_log_id` BIGINT NOT NULL COMMENT '同步日志ID',
  `sync_type` VARCHAR(30) COMMENT '同步类型',
  `data_key` VARCHAR(100) COMMENT '数据标识',
  `data_content` TEXT COMMENT '原始数据内容',
  `error_code` VARCHAR(50) COMMENT '错误代码',
  `error_msg` TEXT COMMENT '错误信息',
  `nc_error_code` VARCHAR(50) COMMENT 'NC错误码',
  `handle_status` TINYINT DEFAULT 0 COMMENT '处理状态：0-待处理 1-已处理 2-忽略',
  `handle_result` VARCHAR(500) COMMENT '处理结果',
  `handle_by` BIGINT DEFAULT NULL COMMENT '处理人',
  `handle_time` DATETIME COMMENT '处理时间',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `retry_status` TINYINT DEFAULT 0 COMMENT '重试状态：0-未重试 1-重试成功 2-重试失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_sync_log_id` (`sync_log_id`),
  INDEX `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NC同步异常表';

-- =============================================
-- 初始化默认数据
-- =============================================

-- 插入默认管理员账号 (密码: admin123 的BCrypt加密结果)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `org_id`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 'admin@guarantee.com', '13800138000', NULL, 1);

-- 插入默认角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `status`) VALUES
('超级管理员', 'ADMIN', '拥有系统所有权限', 1),
('普通用户', 'USER', '普通业务操作人员', 1);

-- 关联管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 插入默认菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `sort_order`, `path`, `component`, `icon`, `permission`, `menu_type`, `status`) VALUES
('仪表盘', 0, 1, '/dashboard', 'views/dashboard/index', 'Odometer', '', 2, 1),
('基础信息同步', 0, 2, '/sync', 'views/sync/index', 'Refresh', '', 2, 1),
('收款单管理', 0, 3, '/receipt', 'views/receipt/index', 'Wallet', '', 2, 1),
('付款单管理', 0, 4, '/payment', 'views/payment/index', 'CreditCard', '', 2, 1),
('银行对账', 0, 5, '/reconciliation', 'views/reconciliation/index', 'Document', '', 2, 1),
('银企直连', 0, 6, '/bank', 'views/bank/index', 'Link', '', 2, 1),
('会计平台', 0, 7, '/accounting', 'views/accounting/index', 'Notebook', '', 2, 1),
('系统管理', 0, 99, '', '', 'Setting', '', 1, 1),
('机构管理', 8, 1, '/system/org', 'views/system/org/index', 'OfficeBuilding', 'system:org:list', 2, 1),
('用户管理', 8, 2, '/system/user', 'views/system/user/index', 'User', 'system:user:list', 2, 1),
('角色管理', 8, 3, '/system/role', 'views/system/role/index', 'UserFilled', 'system:role:list', 2, 1),
('菜单权限', 8, 4, '/system/menu', 'views/system/menu/index', 'Menu', 'system:menu:list', 2, 1),
('审批流程', 8, 5, '/system/process', 'views/system/process/index', 'SetUp', 'system:process:list', 2, 1);
