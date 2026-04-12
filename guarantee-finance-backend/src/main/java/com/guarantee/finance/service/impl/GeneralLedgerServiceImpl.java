package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.AccGeneralLedger;
import com.guarantee.finance.entity.AccSubjectBalance;
import com.guarantee.finance.mapper.AccGeneralLedgerMapper;
import com.guarantee.finance.mapper.AccSubjectBalanceMapper;
import com.guarantee.finance.service.GeneralLedgerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class GeneralLedgerServiceImpl extends ServiceImpl<AccGeneralLedgerMapper, AccGeneralLedger> implements GeneralLedgerService {

    @Autowired
    private AccSubjectBalanceMapper subjectBalanceMapper;

    @Override
    public IPage<AccGeneralLedger> queryByPeriod(String period, String subjectCode, IPage<AccGeneralLedger> page) {
        LambdaQueryWrapper<AccGeneralLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccGeneralLedger::getDeleted, 0)
                .eq(AccGeneralLedger::getPeriod, period);
        if (subjectCode != null && !subjectCode.isEmpty()) {
            wrapper.likeRight(AccGeneralLedger::getSubjectCode, subjectCode);
        }
        wrapper.orderByAsc(AccGeneralLedger::getSubjectCode);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateGeneralLedger(String period) {
        log.info("开始生成总分类账，期间：{}", period);

        LambdaQueryWrapper<AccGeneralLedger> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AccGeneralLedger::getPeriod, period);
        remove(deleteWrapper);

        LambdaQueryWrapper<AccSubjectBalance> balanceWrapper = new LambdaQueryWrapper<>();
        balanceWrapper.eq(AccSubjectBalance::getPeriod, period)
                .eq(AccSubjectBalance::getDeleted, 0);
        List<AccSubjectBalance> balances = subjectBalanceMapper.selectList(balanceWrapper);

        for (AccSubjectBalance balance : balances) {
            AccGeneralLedger ledger = new AccGeneralLedger();
            ledger.setSubjectCode(balance.getSubjectCode());
            ledger.setPeriod(period);
            ledger.setYear(balance.getYear());
            ledger.setMonth(balance.getMonth());
            ledger.setBeginDebit(balance.getBeginDebit() != null ? balance.getBeginDebit() : BigDecimal.ZERO);
            ledger.setBeginCredit(balance.getBeginCredit() != null ? balance.getBeginCredit() : BigDecimal.ZERO);
            ledger.setCurrentDebit(balance.getCurrentDebit() != null ? balance.getCurrentDebit() : BigDecimal.ZERO);
            ledger.setCurrentCredit(balance.getCurrentCredit() != null ? balance.getCurrentCredit() : BigDecimal.ZERO);
            ledger.setEndDebit(balance.getEndDebit() != null ? balance.getEndDebit() : BigDecimal.ZERO);
            ledger.setEndCredit(balance.getEndCredit() != null ? balance.getEndCredit() : BigDecimal.ZERO);
            ledger.setDeleted(0);
            ledger.setCreateTime(LocalDateTime.now());
            ledger.setUpdateTime(LocalDateTime.now());
            save(ledger);
        }

        log.info("总分类账生成完成，期间：{}，共{}条记录", period, balances.size());
    }
}
