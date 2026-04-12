package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.entity.AccSubjectBalance;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.mapper.AccSubjectBalanceMapper;
import com.guarantee.finance.mapper.AccVoucherDetailMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.AccountPostingService;
import com.guarantee.finance.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountPostingServiceImpl implements AccountPostingService {

    private final AccVoucherMapper accVoucherMapper;
    private final AccVoucherDetailMapper accVoucherDetailMapper;
    private final AccSubjectBalanceMapper accSubjectBalanceMapper;
    private final TodoService todoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postVoucher(Long voucherId) {
        AccVoucher voucher = accVoucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() != 2) {
            throw new RuntimeException("只有已审核的凭证可以记账");
        }

        List<AccVoucherDetail> details = accVoucherDetailMapper.selectList(
                new LambdaQueryWrapper<AccVoucherDetail>().eq(AccVoucherDetail::getVoucherId, voucherId));

        String period = voucher.getPeriod();
        Integer year = voucher.getYear();
        Integer month = voucher.getMonth();

        for (AccVoucherDetail detail : details) {
            updateSubjectBalance(detail.getSubjectCode(), period, year, month,
                    detail.getDebitAmount() != null ? detail.getDebitAmount() : BigDecimal.ZERO,
                    detail.getCreditAmount() != null ? detail.getCreditAmount() : BigDecimal.ZERO,
                    false);
        }

        voucher.setStatus(3);
        voucher.setPostUserId(1L);
        accVoucherMapper.updateById(voucher);

        try {
            todoService.completeTodoByBusiness(voucherId, "voucher");
        } catch (Exception e) {
            // ignore
        }

        log.info("凭证 {} 记账成功", voucher.getVoucherNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpostVoucher(Long voucherId) {
        AccVoucher voucher = accVoucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() != 3) {
            throw new RuntimeException("只有已记账的凭证可以反记账");
        }

        List<AccVoucherDetail> details = accVoucherDetailMapper.selectList(
                new LambdaQueryWrapper<AccVoucherDetail>().eq(AccVoucherDetail::getVoucherId, voucherId));

        String period = voucher.getPeriod();
        Integer year = voucher.getYear();
        Integer month = voucher.getMonth();

        for (AccVoucherDetail detail : details) {
            updateSubjectBalance(detail.getSubjectCode(), period, year, month,
                    detail.getDebitAmount() != null ? detail.getDebitAmount() : BigDecimal.ZERO,
                    detail.getCreditAmount() != null ? detail.getCreditAmount() : BigDecimal.ZERO,
                    true);
        }

        voucher.setStatus(2);
        accVoucherMapper.updateById(voucher);

        log.info("凭证 {} 反记账成功", voucher.getVoucherNo());
    }

    private void updateSubjectBalance(String subjectCode, String period, Integer year, Integer month,
                                       BigDecimal debitAmount, BigDecimal creditAmount, boolean isReverse) {
        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getSubjectCode, subjectCode)
                .eq(AccSubjectBalance::getPeriod, period);
        AccSubjectBalance balance = accSubjectBalanceMapper.selectOne(wrapper);

        if (isReverse) {
            debitAmount = debitAmount.negate();
            creditAmount = creditAmount.negate();
        }

        if (balance == null) {
            balance = new AccSubjectBalance();
            balance.setSubjectCode(subjectCode);
            balance.setPeriod(period);
            balance.setYear(year);
            balance.setMonth(month);
            balance.setBeginDebit(BigDecimal.ZERO);
            balance.setBeginCredit(BigDecimal.ZERO);
            balance.setCurrentDebit(debitAmount);
            balance.setCurrentCredit(creditAmount);
            balance.setEndDebit(debitAmount);
            balance.setEndCredit(creditAmount);
            balance.setStatus("1");
            accSubjectBalanceMapper.insert(balance);
        } else {
            BigDecimal currentDebit = balance.getCurrentDebit() != null ? balance.getCurrentDebit() : BigDecimal.ZERO;
            BigDecimal currentCredit = balance.getCurrentCredit() != null ? balance.getCurrentCredit() : BigDecimal.ZERO;

            BigDecimal newCurrentDebit = currentDebit.add(debitAmount);
            BigDecimal newCurrentCredit = currentCredit.add(creditAmount);

            balance.setCurrentDebit(newCurrentDebit);
            balance.setCurrentCredit(newCurrentCredit);

            BigDecimal beginDebit = balance.getBeginDebit() != null ? balance.getBeginDebit() : BigDecimal.ZERO;
            BigDecimal beginCredit = balance.getBeginCredit() != null ? balance.getBeginCredit() : BigDecimal.ZERO;

            balance.setEndDebit(beginDebit.add(newCurrentDebit));
            balance.setEndCredit(beginCredit.add(newCurrentCredit));

            accSubjectBalanceMapper.updateById(balance);
        }

        log.info("更新科目余额: 科目={}, 期间={}, 借方={}, 贷方={}", subjectCode, period, debitAmount, creditAmount);
    }
}
