package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.*;
import com.guarantee.finance.mapper.AccDetailLedgerMapper;
import com.guarantee.finance.mapper.AccVoucherDetailMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.DetailLedgerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DetailLedgerServiceImpl extends ServiceImpl<AccDetailLedgerMapper, AccDetailLedger> implements DetailLedgerService {

    @Autowired
    private AccVoucherMapper voucherMapper;
    @Autowired
    private AccVoucherDetailMapper voucherDetailMapper;

    @Override
    public IPage<AccDetailLedger> queryBySubjectAndPeriod(String subjectCode, String period, IPage<AccDetailLedger> page) {
        LambdaQueryWrapper<AccDetailLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccDetailLedger::getDeleted, 0)
                .eq(AccDetailLedger::getPeriod, period);
        if (subjectCode != null && !subjectCode.isEmpty()) {
            wrapper.likeRight(AccDetailLedger::getSubjectCode, subjectCode);
        }
        wrapper.orderByAsc(AccDetailLedger::getSubjectCode).orderByAsc(AccDetailLedger::getVoucherDate);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDetailLedger(String period) {
        log.info("开始生成明细分类账，期间：{}", period);

        LambdaQueryWrapper<AccDetailLedger> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AccDetailLedger::getPeriod, period);
        remove(deleteWrapper);

        String year = period.substring(0, 4);
        String month = period.substring(4);

        LambdaQueryWrapper<AccVoucher> voucherWrapper = new LambdaQueryWrapper<>();
        voucherWrapper.eq(AccVoucher::getPeriod, period)
                .eq(AccVoucher::getDeleted, 0)
                .in(AccVoucher::getStatus, Arrays.asList(2, 3))
                .orderByAsc(AccVoucher::getVoucherDate);
        List<AccVoucher> vouchers = voucherMapper.selectList(voucherWrapper);

        Map<String, BigDecimal> subjectBalanceMap = new LinkedHashMap<>();

        for (AccVoucher voucher : vouchers) {
            LambdaQueryWrapper<AccVoucherDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(AccVoucherDetail::getVoucherId, voucher.getId())
                    .eq(AccVoucherDetail::getDeleted, 0)
                    .orderByAsc(AccVoucherDetail::getLineNo);
            List<AccVoucherDetail> details = voucherDetailMapper.selectList(detailWrapper);

            for (AccVoucherDetail detail : details) {
                String subjectCode = detail.getSubjectCode();
                BigDecimal debit = detail.getDebitAmount() != null ? detail.getDebitAmount() : BigDecimal.ZERO;
                BigDecimal credit = detail.getCreditAmount() != null ? detail.getCreditAmount() : BigDecimal.ZERO;

                BigDecimal prevBalance = subjectBalanceMap.getOrDefault(subjectCode, BigDecimal.ZERO);
                BigDecimal newBalance = prevBalance.add(debit).subtract(credit);
                subjectBalanceMap.put(subjectCode, newBalance);

                AccDetailLedger ledger = new AccDetailLedger();
                ledger.setSubjectCode(subjectCode);
                ledger.setSubjectName(detail.getSubjectName());
                ledger.setPeriod(period);
                ledger.setVoucherId(voucher.getId());
                ledger.setVoucherNo(voucher.getVoucherNo());
                ledger.setVoucherDate(voucher.getVoucherDate());
                ledger.setSummary(detail.getSummary());
                ledger.setDebitAmount(debit);
                ledger.setCreditAmount(credit);
                ledger.setDirection(newBalance.compareTo(BigDecimal.ZERO) >= 0 ? "借" : "贷");
                ledger.setBalance(newBalance.abs());
                ledger.setCustomerCode(detail.getCustomerCode());
                ledger.setYear(Integer.parseInt(year));
                ledger.setMonth(Integer.parseInt(month));
                ledger.setDeleted(0);
                ledger.setCreateTime(LocalDateTime.now());
                ledger.setUpdateTime(LocalDateTime.now());
                save(ledger);
            }
        }

        log.info("明细分类账生成完成，期间：{}，共处理{}张凭证", period, vouchers.size());
    }
}
