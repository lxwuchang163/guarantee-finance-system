package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.AccAuditConfig;

public interface AccAuditConfigService extends IService<AccAuditConfig> {

    AccAuditConfig getAuditConfigByOrgCode(String orgCode);

    AccAuditConfig getDefaultAuditConfig();
}
