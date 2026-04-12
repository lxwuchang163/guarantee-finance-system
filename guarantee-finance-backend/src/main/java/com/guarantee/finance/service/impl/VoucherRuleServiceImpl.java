package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.VoucherRuleDTO;
import com.guarantee.finance.entity.AccVoucherRule;
import com.guarantee.finance.entity.AccVoucherRuleEntry;
import com.guarantee.finance.mapper.AccVoucherRuleMapper;
import com.guarantee.finance.mapper.AccVoucherRuleEntryMapper;
import com.guarantee.finance.service.VoucherRuleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VoucherRuleServiceImpl extends ServiceImpl<AccVoucherRuleMapper, AccVoucherRule> implements VoucherRuleService {

    @Autowired
    private AccVoucherRuleEntryMapper ruleEntryMapper;

    @Override
    public IPage<AccVoucherRule> queryPage(String keyword, String businessType, Integer status, IPage<AccVoucherRule> page) {
        LambdaQueryWrapper<AccVoucherRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccVoucherRule::getDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(AccVoucherRule::getRuleName, keyword).or().like(AccVoucherRule::getRuleCode, keyword));
        }
        if (businessType != null && !businessType.isEmpty()) {
            wrapper.eq(AccVoucherRule::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(AccVoucherRule::getStatus, status);
        }
        wrapper.orderByDesc(AccVoucherRule::getPriority).orderByDesc(AccVoucherRule::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public VoucherRuleDTO getRuleDetail(Long id) {
        AccVoucherRule rule = getById(id);
        if (rule == null) return null;
        VoucherRuleDTO dto = new VoucherRuleDTO();
        BeanUtils.copyProperties(rule, dto);

        LambdaQueryWrapper<AccVoucherRuleEntry> entryWrapper = new LambdaQueryWrapper<>();
        entryWrapper.eq(AccVoucherRuleEntry::getRuleId, id)
                .eq(AccVoucherRuleEntry::getDeleted, 0)
                .orderByAsc(AccVoucherRuleEntry::getLineNo);
        List<AccVoucherRuleEntry> entries = ruleEntryMapper.selectList(entryWrapper);
        List<VoucherRuleDTO.EntryDTO> entryDTOs = new ArrayList<>();
        for (AccVoucherRuleEntry entry : entries) {
            VoucherRuleDTO.EntryDTO entryDTO = new VoucherRuleDTO.EntryDTO();
            BeanUtils.copyProperties(entry, entryDTO);
            entryDTOs.add(entryDTO);
        }
        dto.setEntries(entryDTOs);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(VoucherRuleDTO dto) {
        AccVoucherRule rule = new AccVoucherRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        if (rule.getStatus() == null) rule.setStatus(1);
        if (rule.getPriority() == null) rule.setPriority(0);
        save(rule);

        if (dto.getEntries() != null) {
            for (VoucherRuleDTO.EntryDTO entryDTO : dto.getEntries()) {
                AccVoucherRuleEntry entry = new AccVoucherRuleEntry();
                BeanUtils.copyProperties(entryDTO, entry);
                entry.setRuleId(rule.getId());
                entry.setDeleted(0);
                entry.setCreateTime(LocalDateTime.now());
                entry.setUpdateTime(LocalDateTime.now());
                ruleEntryMapper.insert(entry);
            }
        }
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(VoucherRuleDTO dto) {
        AccVoucherRule rule = getById(dto.getId());
        if (rule == null) throw new RuntimeException("规则不存在");
        if (dto.getRuleName() != null) rule.setRuleName(dto.getRuleName());
        if (dto.getBusinessType() != null) rule.setBusinessType(dto.getBusinessType());
        if (dto.getBusinessSubtype() != null) rule.setBusinessSubtype(dto.getBusinessSubtype());
        if (dto.getVoucherType() != null) rule.setVoucherType(dto.getVoucherType());
        if (dto.getSummaryTemplate() != null) rule.setSummaryTemplate(dto.getSummaryTemplate());
        if (dto.getPriority() != null) rule.setPriority(dto.getPriority());
        if (dto.getStatus() != null) rule.setStatus(dto.getStatus());
        if (dto.getRemark() != null) rule.setRemark(dto.getRemark());
        rule.setUpdateTime(LocalDateTime.now());
        updateById(rule);

        if (dto.getEntries() != null) {
            LambdaQueryWrapper<AccVoucherRuleEntry> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(AccVoucherRuleEntry::getRuleId, dto.getId());
            ruleEntryMapper.delete(deleteWrapper);

            for (VoucherRuleDTO.EntryDTO entryDTO : dto.getEntries()) {
                AccVoucherRuleEntry entry = new AccVoucherRuleEntry();
                BeanUtils.copyProperties(entryDTO, entry);
                entry.setId(null);
                entry.setRuleId(dto.getId());
                entry.setDeleted(0);
                entry.setCreateTime(LocalDateTime.now());
                entry.setUpdateTime(LocalDateTime.now());
                ruleEntryMapper.insert(entry);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        AccVoucherRule rule = getById(id);
        if (rule == null) throw new RuntimeException("规则不存在");
        rule.setDeleted(1);
        rule.setUpdateTime(LocalDateTime.now());
        updateById(rule);
    }

    @Override
    public void enableRule(Long id) {
        AccVoucherRule rule = getById(id);
        if (rule == null) throw new RuntimeException("规则不存在");
        rule.setStatus(1);
        rule.setUpdateTime(LocalDateTime.now());
        updateById(rule);
    }

    @Override
    public void disableRule(Long id) {
        AccVoucherRule rule = getById(id);
        if (rule == null) throw new RuntimeException("规则不存在");
        rule.setStatus(0);
        rule.setUpdateTime(LocalDateTime.now());
        updateById(rule);
    }

    @Override
    public AccVoucherRule matchRule(String businessType, String businessSubtype) {
        LambdaQueryWrapper<AccVoucherRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccVoucherRule::getDeleted, 0)
                .eq(AccVoucherRule::getStatus, 1)
                .eq(AccVoucherRule::getBusinessType, businessType);
        if (businessSubtype != null && !businessSubtype.isEmpty()) {
            wrapper.eq(AccVoucherRule::getBusinessSubtype, businessSubtype);
        }
        wrapper.orderByDesc(AccVoucherRule::getPriority).last("LIMIT 1");
        return getOne(wrapper, false);
    }

    @Override
    public List<Map<String, Object>> getBusinessTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("value", "receipt", "label", "收款"));
        types.add(Map.of("value", "payment", "label", "付款"));
        types.add(Map.of("value", "refund", "label", "退费"));
        types.add(Map.of("value", "compensation", "label", "代偿"));
        return types;
    }
}
