-- 担保集团业务财务系统数据库初始化脚本
-- 数据库: guarantee_finance
-- 字符集: utf8mb4
-- MySQL 5.5+ 兼容

CREATE DATABASE IF NOT EXISTS `guarantee_finance` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `guarantee_finance`;

-- =============================================
-- 一、系统管理相关表
-- =============================================

-- 1.1 机构表
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
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';

-- 1.2 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像',
  `sex` TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `org_id` BIGINT COMMENT '所属机构ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.3 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 1.4 菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `path` VARCHAR(100) COMMENT '路由路径',
  `component` VARCHAR(200) COMMENT '组件路径',
  `icon` VARCHAR(50) COMMENT '菜单图标',
  `permission` VARCHAR(100) COMMENT '权限标识',
  `menu_type` TINYINT DEFAULT 1 COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 1.5 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 1.6 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 二、会计平台相关表
-- =============================================

-- 2.1 科目表
DROP TABLE IF EXISTS `acc_account_subject`;
CREATE TABLE `acc_account_subject` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject_code` VARCHAR(20) NOT NULL COMMENT '科目编码',
  `subject_name` VARCHAR(100) NOT NULL COMMENT '科目名称',
  `subject_level` TINYINT NOT NULL COMMENT '科目级别',
  `parent_code` VARCHAR(20) COMMENT '父科目编码',
  `subject_type` TINYINT COMMENT '科目类型：1-资产 2-负债 3-所有者权益 4-成本 5-损益',
  `balance_direction` TINYINT COMMENT '余额方向：1-借方 2-贷方',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-封存 1-启用',
  `auxiliary_dimension` VARCHAR(500) COMMENT '辅助核算维度（JSON格式）',
  `description` VARCHAR(500) COMMENT '科目描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `category` VARCHAR(50) COMMENT '科目类别',
  `system_type` VARCHAR(10) DEFAULT '0' COMMENT '系统类型：0-标准 1-自定义',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_code` (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

-- 2.2 辅助核算维度表
DROP TABLE IF EXISTS `acc_auxiliary_dimension`;
CREATE TABLE `acc_auxiliary_dimension` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dimension_code` VARCHAR(50) NOT NULL COMMENT '维度编码',
  `dimension_name` VARCHAR(100) NOT NULL COMMENT '维度名称',
  `parent_code` VARCHAR(50) COMMENT '父维度编码',
  `level` INT DEFAULT 1 COMMENT '维度层级',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dimension_code` (`dimension_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅助核算维度表';

-- 2.3 科目余额表
DROP TABLE IF EXISTS `acc_subject_balance`;
CREATE TABLE `acc_subject_balance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject_code` VARCHAR(20) NOT NULL COMMENT '科目编码',
  `period` VARCHAR(7) NOT NULL COMMENT '会计期间',
  `begin_debit` DECIMAL(20,2) DEFAULT 0.00 COMMENT '期初借方',
  `begin_credit` DECIMAL(20,2) DEFAULT 0.00 COMMENT '期初贷方',
  `current_debit` DECIMAL(20,2) DEFAULT 0.00 COMMENT '本期借方',
  `current_credit` DECIMAL(20,2) DEFAULT 0.00 COMMENT '本期贷方',
  `end_debit` DECIMAL(20,2) DEFAULT 0.00 COMMENT '期末借方',
  `end_credit` DECIMAL(20,2) DEFAULT 0.00 COMMENT '期末贷方',
  `auxiliary_info` VARCHAR(500) COMMENT '辅助核算信息',
  `year` INT COMMENT '年份',
  `month` INT COMMENT '月份',
  `status` VARCHAR(10) DEFAULT '0' COMMENT '状态：0-初始化 1-已确认',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_period` (`subject_code`, `period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目余额表';

-- 2.4 凭证表
DROP TABLE IF EXISTS `acc_voucher`;
CREATE TABLE `acc_voucher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_no` VARCHAR(30) NOT NULL COMMENT '凭证编号',
  `voucher_date` DATE COMMENT '凭证日期',
  `period` VARCHAR(7) COMMENT '会计期间',
  `summary` VARCHAR(255) COMMENT '摘要',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已提交 2-已审核 3-已记账 4-已作废',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `approve_user_id` BIGINT COMMENT '审核人ID',
  `post_user_id` BIGINT COMMENT '记账人ID',
  `audit_status` VARCHAR(10) DEFAULT '0' COMMENT '审核状态：0-待审核 1-已审核 2-审核拒绝',
  `audit_opinion` VARCHAR(500) COMMENT '审核意见',
  `voucher_type` TINYINT DEFAULT 1 COMMENT '凭证类型：1-记账凭证 2-收款凭证 3-付款凭证 4-转账凭证',
  `source_type` VARCHAR(10) DEFAULT '0' COMMENT '来源类型：0-手工 1-自动 2-导入',
  `source_id` VARCHAR(50) COMMENT '来源ID',
  `nc_sync_status` VARCHAR(10) DEFAULT '0' COMMENT 'NC同步状态：0-未同步 1-已同步 2-同步失败',
  `year` INT COMMENT '年份',
  `month` INT COMMENT '月份',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_no` (`voucher_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证表';

-- 2.5 凭证明细表
DROP TABLE IF EXISTS `acc_voucher_detail`;
CREATE TABLE `acc_voucher_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_id` BIGINT NOT NULL COMMENT '凭证ID',
  `line_no` INT NOT NULL COMMENT '行号',
  `subject_code` VARCHAR(20) NOT NULL COMMENT '科目编码',
  `subject_name` VARCHAR(100) NOT NULL COMMENT '科目名称',
  `summary` VARCHAR(255) COMMENT '摘要',
  `debit_amount` DECIMAL(20,2) DEFAULT 0.00 COMMENT '借方金额',
  `credit_amount` DECIMAL(20,2) DEFAULT 0.00 COMMENT '贷方金额',
  `auxiliary_info` VARCHAR(500) COMMENT '辅助核算信息（JSON格式）',
  `department_code` VARCHAR(50) COMMENT '部门编码',
  `project_code` VARCHAR(50) COMMENT '项目编码',
  `customer_code` VARCHAR(50) COMMENT '客户编码',
  `supplier_code` VARCHAR(50) COMMENT '供应商编码',
  `business_code` VARCHAR(50) COMMENT '业务编码',
  `bank_code` VARCHAR(50) COMMENT '银行编码',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_voucher_id` (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证明细表';

-- 2.6 凭证审核表
DROP TABLE IF EXISTS `acc_voucher_audit`;
CREATE TABLE `acc_voucher_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `voucher_id` BIGINT NOT NULL COMMENT '凭证ID',
  `audit_type` TINYINT COMMENT '审核类型：1-单审核 2-双审核',
  `audit_level` TINYINT COMMENT '审核级别：1-一级 2-二级',
  `auditor_id` BIGINT COMMENT '审核人ID',
  `audit_status` TINYINT COMMENT '审核状态：1-通过 2-拒绝',
  `audit_opinion` VARCHAR(500) COMMENT '审核意见',
  `audit_time` DATETIME COMMENT '审核时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_voucher_id` (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证审核表';

-- 2.7 转换模板表
DROP TABLE IF EXISTS `acc_conversion_template`;
CREATE TABLE `acc_conversion_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `business_type` VARCHAR(20) COMMENT '业务类型：receipt-收款 payment-付款 refund-退款 compensation-代偿',
  `org_code` VARCHAR(50) COMMENT '机构编码',
  `description` VARCHAR(500) COMMENT '模板描述',
  `template_content` TEXT COMMENT '模板内容（JSON格式）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转换模板表';

-- 2.8 凭证模板表
DROP TABLE IF EXISTS `acc_voucher_template`;
CREATE TABLE `acc_voucher_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `voucher_type` TINYINT DEFAULT 1 COMMENT '凭证类型',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证模板表';

-- 2.9 客户设置表
DROP TABLE IF EXISTS `acc_customer_setting`;
CREATE TABLE `acc_customer_setting` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
  `accounting_method` VARCHAR(20) COMMENT '核算方式',
  `default_subject` VARCHAR(20) COMMENT '默认科目',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户设置表';

-- =============================================
-- 三、业务管理相关表
-- =============================================

-- 3.1 客户表
DROP TABLE IF EXISTS `biz_customer`;
CREATE TABLE `biz_customer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_code` VARCHAR(50) NOT NULL COMMENT '客户编码',
  `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
  `customer_short_name` VARCHAR(50) COMMENT '客户简称',
  `customer_type` TINYINT DEFAULT 2 COMMENT '客户类型：1-个人 2-企业 3-机构',
  `credit_code` VARCHAR(50) COMMENT '统一社会信用代码',
  `id_card` VARCHAR(20) COMMENT '身份证号',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `contact_person` VARCHAR(50) COMMENT '联系人',
  `register_address` VARCHAR(255) COMMENT '注册地址',
  `business_address` VARCHAR(255) COMMENT '经营地址',
  `industry` VARCHAR(100) COMMENT '行业',
  `customer_level` TINYINT DEFAULT 3 COMMENT '客户等级：1-AAA 2-AA 3-A 4-B',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `source_system` VARCHAR(50) COMMENT '来源系统标识',
  `last_sync_time` DATETIME COMMENT '最后同步时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 3.2 客户银行账户表
DROP TABLE IF EXISTS `biz_customer_bank_account`;
CREATE TABLE `biz_customer_bank_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `account_no` VARCHAR(50) NOT NULL COMMENT '银行账号',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
  `bank_name` VARCHAR(100) COMMENT '开户行名称',
  `bank_code` VARCHAR(20) COMMENT '开户行联号',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户银行账户表';

-- 3.3 产品表
DROP TABLE IF EXISTS `biz_product`;
CREATE TABLE `biz_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` VARCHAR(50) NOT NULL COMMENT '产品编码',
  `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `product_type` VARCHAR(20) COMMENT '产品类型',
  `description` VARCHAR(500) COMMENT '产品描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- 3.4 产品费率表
DROP TABLE IF EXISTS `biz_product_rate`;
CREATE TABLE `biz_product_rate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '产品ID',
  `rate_type` VARCHAR(20) NOT NULL COMMENT '费率类型',
  `rate_value` DECIMAL(10,6) NOT NULL COMMENT '费率值',
  `effective_date` DATE COMMENT '生效日期',
  `expire_date` DATE COMMENT '失效日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品费率表';

-- =============================================
-- 四、财务管理相关表
-- =============================================

-- 4.1 收款单表
DROP TABLE IF EXISTS `fin_receipt`;
CREATE TABLE `fin_receipt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_no` VARCHAR(30) NOT NULL COMMENT '收款单号',
  `business_type` TINYINT COMMENT '业务类型：1-保费收入 2-分担收入 3-追偿到款',
  `business_type_name` VARCHAR(50) COMMENT '业务类型名称',
  `customer_code` VARCHAR(50) COMMENT '客户编码',
  `customer_name` VARCHAR(100) COMMENT '客户名称',
  `contract_no` VARCHAR(50) COMMENT '合同编号',
  `project_name` VARCHAR(200) COMMENT '项目名称',
  `product_code` VARCHAR(50) COMMENT '业务品种编码',
  `product_name` VARCHAR(100) COMMENT '业务品种名称',
  `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '收款金额',
  `amount_in_words` VARCHAR(200) COMMENT '大写金额',
  `receipt_date` DATE COMMENT '收款日期',
  `actual_arrival_date` DATE COMMENT '实际到账日期',
  `payment_method` TINYINT COMMENT '支付方式：1-转账 2-现金 3-支票 4-汇票 5-其他',
  `payer_name` VARCHAR(100) COMMENT '付款方名称',
  `payer_account_no` VARCHAR(50) COMMENT '付款方账号',
  `payer_bank_name` VARCHAR(100) COMMENT '付款方开户行',
  `payee_account_no` VARCHAR(50) COMMENT '收款方账号',
  `payee_bank_name` VARCHAR(100) COMMENT '收款方开户行',
  `usage` VARCHAR(255) COMMENT '用途/摘要',
  `voucher_id` BIGINT COMMENT '关联凭证ID',
  `voucher_no` VARCHAR(30) COMMENT '凭证号',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已提交 2-已审核 3-已记账 4-已作废',
  `maker_id` BIGINT COMMENT '制单人ID',
  `maker_name` VARCHAR(50) COMMENT '制单人姓名',
  `maker_time` DATETIME COMMENT '制单时间',
  `auditor_id` BIGINT COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) COMMENT '审核人姓名',
  `auditor_time` DATETIME COMMENT '审核时间',
  `poster_id` BIGINT COMMENT '记账人ID',
  `poster_name` VARCHAR(50) COMMENT '记账人姓名',
  `poster_time` DATETIME COMMENT '记账时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_receipt_no` (`receipt_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款单表';

-- 4.2 收款追回表
DROP TABLE IF EXISTS `fin_receipt_recovery`;
CREATE TABLE `fin_receipt_recovery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_id` BIGINT NOT NULL COMMENT '收款单ID',
  `recovery_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '追回金额',
  `recovery_date` DATE COMMENT '追回日期',
  `reason` VARCHAR(500) COMMENT '追回原因',
  `status` TINYINT DEFAULT 0 COMMENT '状态',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款追回表';

-- 4.3 收款分摊表
DROP TABLE IF EXISTS `fin_receipt_share`;
CREATE TABLE `fin_receipt_share` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_id` BIGINT NOT NULL COMMENT '收款单ID',
  `share_org_id` BIGINT COMMENT '分摊机构ID',
  `share_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '分摊金额',
  `share_ratio` DECIMAL(5,2) DEFAULT 0.00 COMMENT '分摊比例',
  `status` TINYINT DEFAULT 0 COMMENT '状态',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款分摊表';

-- 4.4 付款单表
DROP TABLE IF EXISTS `fin_payment`;
CREATE TABLE `fin_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_no` VARCHAR(30) NOT NULL COMMENT '付款单号',
  `business_type` TINYINT COMMENT '业务类型：1-退费支出 2-代偿支出 3-追回资金分配',
  `business_type_name` VARCHAR(50) COMMENT '业务类型名称',
  `customer_code` VARCHAR(50) COMMENT '客户编码',
  `customer_name` VARCHAR(100) COMMENT '客户名称',
  `contract_no` VARCHAR(50) COMMENT '合同编号',
  `original_receipt_no` VARCHAR(30) COMMENT '原收款单号（退费时关联）',
  `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '付款金额',
  `amount_in_words` VARCHAR(200) COMMENT '大写金额',
  `payment_date` DATE COMMENT '付款日期',
  `actual_payment_date` DATE COMMENT '实际付款日期',
  `payment_method` TINYINT COMMENT '支付方式：1-转账 2-现金 3-支票 4-汇票 5-其他',
  `payee_name` VARCHAR(100) COMMENT '收款方名称',
  `payee_account_no` VARCHAR(50) COMMENT '收款方账号',
  `payee_bank_name` VARCHAR(100) COMMENT '收款方开户行',
  `payer_account_no` VARCHAR(50) COMMENT '付款方账号',
  `payer_bank_name` VARCHAR(100) COMMENT '付款方开户行',
  `usage` VARCHAR(255) COMMENT '用途/摘要',
  `voucher_id` BIGINT COMMENT '关联凭证ID',
  `voucher_no` VARCHAR(30) COMMENT '凭证号',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已提交 2-已审核 3-已付款 4-已记账 5-已作废',
  `maker_id` BIGINT COMMENT '制单人ID',
  `maker_name` VARCHAR(50) COMMENT '制单人姓名',
  `maker_time` DATETIME COMMENT '制单时间',
  `auditor_id` BIGINT COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) COMMENT '审核人姓名',
  `auditor_time` DATETIME COMMENT '审核时间',
  `poster_id` BIGINT COMMENT '记账人ID',
  `poster_name` VARCHAR(50) COMMENT '记账人姓名',
  `poster_time` DATETIME COMMENT '记账时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款单表';

-- =============================================
-- 五、基础初始数据
-- =============================================

-- 5.1 插入机构数据
INSERT INTO `sys_org` (`org_name`, `org_code`, `parent_id`, `level`, `status`) VALUES
('担保集团总部', 'GUARANTEE_GROUP', 0, 1, 1),
('北京分公司', 'BEIJING_BRANCH', 1, 2, 1),
('上海分公司', 'SHANGHAI_BRANCH', 1, 2, 1),
('广州分公司', 'GUANGZHOU_BRANCH', 1, 2, 1);

-- 5.2 插入用户数据（密码为123456的BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `org_id`, `status`) VALUES
('admin', '123456', '系统管理员', 1, 1),
('zhangsan', '123456', '张三', 2, 1),
('lisi', '123456', '李四', 3, 1);

-- 5.3 插入角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('财务主管', 'FINANCE_MANAGER', '财务管理权限', 1),
('财务人员', 'FINANCE_STAFF', '财务操作权限', 1),
('普通用户', 'NORMAL_USER', '基本查看权限', 1);

-- 5.4 插入菜单数据
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `path`, `component`, `icon`, `menu_type`, `status`) VALUES
('首页', 0, '/dashboard', 'views/dashboard/index', 'HomeFilled', 2, 1),
('系统管理', 0, '/system', '', 'Setting', 1, 1),
('用户管理', 2, '/system/user', 'views/system/user/index', 'User', 2, 1),
('角色管理', 2, '/system/role', 'views/system/role/index', 'UserFilled', 2, 1),
('菜单管理', 2, '/system/menu', 'views/system/menu/index', 'Menu', 2, 1),
('机构管理', 2, '/system/org', 'views/system/org/index', 'OfficeBuilding', 2, 1),
('会计平台', 0, '/accounting', '', 'Money', 1, 1),
('科目管理', 7, '/accounting/subject', 'views/accounting/subject', 'Document', 2, 1),
('辅助核算', 7, '/accounting/auxiliary', 'views/accounting/auxiliary', 'Grid', 2, 1),
('凭证管理', 7, '/accounting/voucher', 'views/accounting/voucher', 'Tickets', 2, 1),
('转换模板', 7, '/accounting/template', 'views/accounting/template', 'SetUp', 2, 1),
('业务管理', 0, '/business', '', 'Briefcase', 1, 1),
('客户管理', 12, '/business/customer', 'views/business/customer/index', 'Avatar', 2, 1),
('产品管理', 12, '/business/product', 'views/business/product/index', 'Goods', 2, 1),
('财务管理', 0, '/finance', '', 'Wallet', 1, 1),
('收款管理', 15, '/finance/receipt', 'views/finance/receipt/index', 'CollectionTag', 2, 1),
('付款管理', 15, '/finance/payment', 'views/finance/payment/index', 'CreditCard', 2, 1),
('银行对账', 15, '/finance/bank-reconciliation', 'views/finance/bank-reconciliation/index', 'BankCard', 2, 1);

-- 5.5 插入用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2), (3, 3);

-- 5.6 插入角色菜单关联（超级管理员拥有所有菜单）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 5.7 插入辅助核算维度数据
INSERT INTO `acc_auxiliary_dimension` (`dimension_code`, `dimension_name`, `level`, `status`) VALUES
('DEPT', '部门', 1, 1),
('PROJECT', '项目', 1, 1),
('CUSTOMER', '客户', 1, 1),
('SUPPLIER', '供应商', 1, 1),
('BUSINESS', '业务', 1, 1),
('BANK', '银行', 1, 1);

-- 5.8 插入标准会计科目数据（国家会计准则）
INSERT INTO `acc_account_subject` (`subject_code`, `subject_name`, `subject_level`, `parent_code`, `subject_type`, `balance_direction`, `status`, `system_type`, `category`) VALUES
-- 资产类科目
('1001', '库存现金', 1, NULL, 1, 1, 1, '0', '货币资金'),
('1002', '银行存款', 1, NULL, 1, 1, 1, '0', '货币资金'),
('1012', '其他货币资金', 1, NULL, 1, 1, 1, '0', '货币资金'),
('1122', '应收账款', 1, NULL, 1, 1, 1, '0', '应收款项'),
('1221', '其他应收款', 1, NULL, 1, 1, 1, '0', '应收款项'),
('1401', '材料采购', 1, NULL, 1, 1, 1, '0', '存货'),
('1601', '固定资产', 1, NULL, 1, 1, 1, '0', '固定资产'),
('1602', '累计折旧', 1, NULL, 1, 2, 1, '0', '固定资产'),
-- 负债类科目
('2001', '短期借款', 1, NULL, 2, 2, 1, '0', '借款'),
('2202', '应付账款', 1, NULL, 2, 2, 1, '0', '应付款项'),
('2241', '其他应付款', 1, NULL, 2, 2, 1, '0', '应付款项'),
('2221', '应交税费', 1, NULL, 2, 2, 1, '0', '税费'),
-- 所有者权益类科目
('3001', '实收资本', 1, NULL, 3, 2, 1, '0', '资本'),
('3101', '盈余公积', 1, NULL, 3, 2, 1, '0', '留存收益'),
('3103', '本年利润', 1, NULL, 3, 2, 1, '0', '留存收益'),
('3104', '利润分配', 1, NULL, 3, 2, 1, '0', '留存收益'),
-- 成本类科目
('4001', '生产成本', 1, NULL, 4, 1, 1, '0', '成本'),
('4101', '制造费用', 1, NULL, 4, 1, 1, '0', '成本'),
-- 损益类科目（收入）
('6001', '主营业务收入', 1, NULL, 5, 2, 1, '0', '营业收入'),
('6051', '其他业务收入', 1, NULL, 5, 2, 1, '0', '营业收入'),
('6111', '投资收益', 1, NULL, 5, 2, 1, '0', '投资损益'),
-- 损益类科目（成本费用）
('6401', '主营业务成本', 1, NULL, 5, 1, 1, '0', '营业成本'),
('6402', '其他业务成本', 1, NULL, 5, 1, 1, '0', '营业成本'),
('6601', '销售费用', 1, NULL, 5, 1, 1, '0', '期间费用'),
('6602', '管理费用', 1, NULL, 5, 1, 1, '0', '期间费用'),
('6603', '财务费用', 1, NULL, 5, 1, 1, '0', '期间费用');

-- 5.9 插入转换模板数据
INSERT INTO `acc_conversion_template` (`template_code`, `template_name`, `business_type`, `description`, `template_content`, `status`) VALUES
('TPL_RECEIPT_001', '收款凭证模板', 'receipt', '标准收款业务凭证模板', '{"debit_subject":"1002","credit_subject":"6001"}', 1),
('TPL_PAYMENT_001', '付款凭证模板', 'payment', '标准付款业务凭证模板', '{"debit_subject":"6601","credit_subject":"1002"}', 1),
('TPL_REFUND_001', '退费凭证模板', 'refund', '退费业务凭证模板', '{"debit_subject":"2241","credit_subject":"1002"}', 1),
('TPL_COMPENSATION_001', '代偿凭证模板', 'compensation', '代偿业务凭证模板', '{"debit_subject":"1221","credit_subject":"1002"}', 1);