DROP TABLE IF EXISTS `acc_audit_config`;
CREATE TABLE `acc_audit_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `org_code` VARCHAR(50),
  `audit_type` TINYINT DEFAULT 1,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  `deleted` TINYINT DEFAULT 0,
  `remark` VARCHAR(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `acc_audit_config` (`org_code`, `audit_type`, `status`, `create_time`) 
VALUES ('DEFAULT', 1, 1, NOW());
