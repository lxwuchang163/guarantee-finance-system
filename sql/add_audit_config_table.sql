-- 凭证审核配置表
DROP TABLE IF EXISTS `acc_audit_config`;
CREATE TABLE `acc_audit_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_code` VARCHAR(50) COMMENT '机构编码',
  `audit_type` TINYINT DEFAULT 1 COMMENT '审核类型：1-单审核 2-双审核',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `remark` VARCHAR(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证审核配置表';

-- 插入默认配置
INSERT INTO `acc_audit_config` (`org_code`, `audit_type`, `status`, `create_time`, `remark`) 
VALUES ('DEFAULT', 1, 1, NOW(), '默认单审核配置');
