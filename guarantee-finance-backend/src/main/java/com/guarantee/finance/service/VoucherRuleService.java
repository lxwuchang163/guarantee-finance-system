package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.VoucherRuleDTO;
import com.guarantee.finance.entity.AccVoucherRule;

import java.util.List;
import java.util.Map;

public interface VoucherRuleService extends IService<AccVoucherRule> {
    IPage<AccVoucherRule> queryPage(String keyword, String businessType, Integer status, IPage<AccVoucherRule> page);
    VoucherRuleDTO getRuleDetail(Long id);
    Long createRule(VoucherRuleDTO dto);
    void updateRule(VoucherRuleDTO dto);
    void deleteRule(Long id);
    void enableRule(Long id);
    void disableRule(Long id);
    AccVoucherRule matchRule(String businessType, String businessSubtype);
    List<Map<String, Object>> getBusinessTypes();
}
