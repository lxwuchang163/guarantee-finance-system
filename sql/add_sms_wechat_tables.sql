USE guarantee_finance;

-- 创建短信验证码表
CREATE TABLE IF NOT EXISTS sys_sms_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expire_time DATETIME NOT NULL,
    status INT DEFAULT 0 COMMENT '0-未使用,1-已使用',
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

-- 创建微信用户表
CREATE TABLE IF NOT EXISTS sys_wechat_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(100) NOT NULL UNIQUE,
    unionid VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    sex INT COMMENT '1-男,2-女,0-未知',
    user_id BIGINT,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_openid (openid),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户表';