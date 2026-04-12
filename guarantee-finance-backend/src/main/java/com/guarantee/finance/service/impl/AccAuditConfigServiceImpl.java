package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.AccAuditConfig;
import com.guarantee.finance.mapper.AccAuditConfigMapper;
import com.guarantee.finance.service.AccAuditConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccAuditConfigServiceImpl extends ServiceImpl<AccAuditConfigMapper, AccAuditConfig> implements AccAuditConfigService {

    private final AccAuditConfigMapper accAuditConfigMapper;

    @Override
    public AccAuditConfig getAuditConfigByOrgCode(String orgCode) {
        LambdaQueryWrapper<AccAuditConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccAuditConfig::getOrgCode, orgCode)
                .eq(AccAuditConfig::getStatus, 1);
        return accAuditConfigMapper.selectOne(wrapper);
    }

    @Override
    public AccAuditConfig getDefaultAuditConfig() {
        return getAuditConfigByOrgCode("DEFAULT");
    }
}
