package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.*;
import com.guarantee.finance.mapper.AccAccountSubjectMapper;
import com.guarantee.finance.mapper.AccCarryForwardMapper;
import com.guarantee.finance.mapper.AccSubjectBalanceMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.CarryForwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class CarryForwardServiceImpl extends ServiceImpl<AccCarryForwardMapper, AccCarryForward> implements CarryForwardService {

    @Autowired
    private AccSubjectBalanceMapper subjectBalanceMapper;
    @Autowired
    private AccAccountSubjectMapper accountSubjectMapper;
    @Autowired
    private AccVoucherMapper voucherMapper;

    private static final String PROFIT_LOSS_SUBJECT_TYPE = "5";
    private static final String PROFIT_SUBJECT_CODE = "3103";

    @Override
    public List<Map<String, Object>> previewProfitLoss(String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period)
                .eq(AccSubjectBalance::getDeleted, 0);
        List<AccSubjectBalance> balances = subjectBalanceMapper.selectList(wrapper);

        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (AccSubjectBalance balance : balances) {
            AccAccountSubject subject = accountSubjectMapper.selectOne(
                    new LambdaQueryWrapper<AccAccountSubject>()
                            .eq(AccAccountSubject::getSubjectCode, balance.getSubjectCode())
                            .eq(AccAccountSubject::getDeleted, 0)
                            .last("LIMIT 1"));
            if (subject == null || !PROFIT_LOSS_SUBJECT_TYPE.equals(String.valueOf(subject.getSubjectType()))) continue;

            BigDecimal netAmount = BigDecimal.ZERO;
            String direction;
            if ("1".equals(String.valueOf(subject.getBalanceDirection()))) {
                netAmount = balance.getCurrentDebit() != null ? balance.getCurrentDebit() : BigDecimal.ZERO;
                netAmount = netAmount.subtract(balance.getCurrentCredit() != null ? balance.getCurrentCredit() : BigDecimal.ZERO);
                direction = "借方";
                totalLoss = totalLoss.add(netAmount.max(BigDecimal.ZERO));
            } else {
                netAmount = balance.getCurrentCredit() != null ? balance.getCurrentCredit() : BigDecimal.ZERO;
                netAmount = netAmount.subtract(balance.getCurrentDebit() != null ? balance.getCurrentDebit() : BigDecimal.ZERO);
                direction = "贷方";
                totalProfit = totalProfit.add(netAmount.max(BigDecimal.ZERO));
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("subjectCode", balance.getSubjectCode());
            item.put("subjectName", subject.getSubjectName());
            item.put("direction", direction);
            item.put("netAmount", netAmount);
            result.add(item);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("subjectCode", "合计");
        summary.put("subjectName", "损益净额");
        summary.put("direction", "");
        summary.put("netAmount", totalProfit.subtract(totalLoss));
        result.add(summary);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void carryForwardProfitLoss(String period) {
        log.info("开始损益结转，期间：{}", period);

        LambdaQueryWrapper<AccCarryForward> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(AccCarryForward::getPeriod, period)
                .eq(AccCarryForward::getCarryType, "PROFIT_LOSS")
                .eq(AccCarryForward::getStatus, "COMPLETED");
        if (count(existWrapper) > 0) {
            throw new RuntimeException("该期间已进行损益结转");
        }

        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period)
                .eq(AccSubjectBalance::getDeleted, 0);
        List<AccSubjectBalance> balances = subjectBalanceMapper.selectList(wrapper);

        BigDecimal totalProfitAmount = BigDecimal.ZERO;
        BigDecimal totalLossAmount = BigDecimal.ZERO;

        for (AccSubjectBalance balance : balances) {
            AccAccountSubject subject = accountSubjectMapper.selectOne(
                    new LambdaQueryWrapper<AccAccountSubject>()
                            .eq(AccAccountSubject::getSubjectCode, balance.getSubjectCode())
                            .eq(AccAccountSubject::getDeleted, 0)
                            .last("LIMIT 1"));
            if (subject == null || !PROFIT_LOSS_SUBJECT_TYPE.equals(String.valueOf(subject.getSubjectType()))) continue;

            BigDecimal currentDebit = balance.getCurrentDebit() != null ? balance.getCurrentDebit() : BigDecimal.ZERO;
            BigDecimal currentCredit = balance.getCurrentCredit() != null ? balance.getCurrentCredit() : BigDecimal.ZERO;
            BigDecimal netAmount;

            if ("1".equals(String.valueOf(subject.getBalanceDirection()))) {
                netAmount = currentDebit.subtract(currentCredit);
                if (netAmount.compareTo(BigDecimal.ZERO) > 0) {
                    totalLossAmount = totalLossAmount.add(netAmount);
                }
            } else {
                netAmount = currentCredit.subtract(currentDebit);
                if (netAmount.compareTo(BigDecimal.ZERO) > 0) {
                    totalProfitAmount = totalProfitAmount.add(netAmount);
                }
            }

            AccCarryForward cf = new AccCarryForward();
            cf.setPeriod(period);
            cf.setCarryType("PROFIT_LOSS");
            cf.setSourceSubjectCode(balance.getSubjectCode());
            cf.setTargetSubjectCode(PROFIT_SUBJECT_CODE);
            cf.setAmount(netAmount.abs());
            cf.setStatus("COMPLETED");
            cf.setDeleted(0);
            cf.setCreateTime(LocalDateTime.now());
            cf.setUpdateTime(LocalDateTime.now());
            save(cf);
        }

        log.info("损益结转完成，期间：{}，收入净额：{}，支出净额：{}", period, totalProfitAmount, totalLossAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseCarryForward(Long id) {
        AccCarryForward cf = getById(id);
        if (cf == null) throw new RuntimeException("结转记录不存在");
        if ("REVERSED".equals(cf.getStatus())) throw new RuntimeException("已冲回的记录不能再次冲回");

        cf.setStatus("REVERSED");
        cf.setUpdateTime(LocalDateTime.now());
        updateById(cf);

        log.info("结转记录{}冲回完成", id);
    }

    @Override
    public List<AccCarryForward> getCarryForwardList(String period) {
        LambdaQueryWrapper<AccCarryForward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccCarryForward::getDeleted, 0);
        if (period != null && !period.isEmpty()) {
            wrapper.eq(AccCarryForward::getPeriod, period);
        }
        wrapper.orderByDesc(AccCarryForward::getCreateTime);
        return list(wrapper);
    }
}
