package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarantee.finance.entity.BankReconciliation;
import com.guarantee.finance.entity.BankTransaction;
import com.guarantee.finance.mapper.BankReconciliationMapper;
import com.guarantee.finance.mapper.BankTransactionMapper;
import com.guarantee.finance.service.BankReconciliationService;
import com.guarantee.finance.vo.BankTransactionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BankReconciliationServiceImpl implements BankReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(BankReconciliationServiceImpl.class);

    @jakarta.annotation.Resource
    private BankTransactionMapper transactionMapper;

    @jakarta.annotation.Resource
    private BankReconciliationMapper reconciliationMapper;

    @jakarta.annotation.Resource
    private ObjectMapper objectMapper;

    @Override
    public void importTransactions(List<Map<String, Object>> transactionList, String dataSource) {
        int successCount = 0;
        int failCount = 0;
        for (Map<String, Object> txData : transactionList) {
            try {
                BankTransaction tx = new BankTransaction();
                tx.setTransactionNo(getString(txData, "transactionNo"));
                tx.setBankAccountNo(getString(txData, "accountNo"));
                tx.setBankName(getString(txData, "bankName"));
                tx.setTransactionDate(parseLocalDate(getString(txData, "transactionDate")));
                tx.setValueDate(parseLocalDate(getString(txData, "valueDate")));
                tx.setTransactionType(getInt(txData, "transactionType"));
                tx.setCounterAccountNo(getString(txData, "counterAccountNo"));
                tx.setCounterName(getString(txData, "counterName"));
                tx.setCounterBankName(getString(txData, "counterBankName"));
                tx.setAmount(getBigDecimal(txData, "amount"));
                tx.setCurrency(getString(txData, "currency", "CNY"));
                tx.setSummary(getString(txData, "summary"));
                tx.setRemark(getString(txData, "remark"));
                tx.setBankVoucherNo(getString(txData, "bankVoucherNo"));
                tx.setMatchStatus(0);
                tx.setDataSource(dataSource != null ? dataSource : "MANUAL_IMPORT");
                transactionMapper.insert(tx);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("导入银行流水失败: {}", e.getMessage());
            }
        }
        log.info("银行流水导入完成：成功{}，失败{}", successCount, failCount);
    }

    @Override
    public IPage<BankTransactionVO> queryTransactions(String accountNo, Integer transactionType,
                                                     LocalDate startDate, LocalDate endDate,
                                                     Integer matchStatus, Page<?> page) {
        LambdaQueryWrapper<BankTransaction> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(accountNo)) wrapper.eq(BankTransaction::getBankAccountNo, accountNo);
        if (transactionType != null) wrapper.eq(BankTransaction::getTransactionType, transactionType);
        if (startDate != null) wrapper.ge(BankTransaction::getTransactionDate, startDate);
        if (endDate != null) wrapper.le(BankTransaction::getTransactionDate, endDate);
        if (matchStatus != null) wrapper.eq(BankTransaction::getMatchStatus, matchStatus);
        wrapper.orderByDesc(BankTransaction::getTransactionDate);

        IPage<BankTransaction> txPage = transactionMapper.selectPage(page.convert(p -> new BankTransaction()), wrapper);
        IPage<BankTransactionVO> voPage = new Page<>(txPage.getCurrent(), txPage.getSize(), txPage.getTotal());
        voPage.setRecords(txPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void executeAutoReconciliation(String accountNo, LocalDate reconciliationDate) {
        log.info("开始执行自动对账，账号：{}，日期：{}", accountNo, reconciliationDate);

        LambdaQueryWrapper<BankTransaction> bankWrapper = new LambdaQueryWrapper<>();
        bankWrapper.eq(BankTransaction::getBankAccountNo, accountNo)
                .eq(BankTransaction::getMatchStatus, 0)
                .ge(BankTransaction::getTransactionDate, reconciliationDate.minusDays(7))
                .le(BankTransaction::getTransactionDate, reconciliationDate.plusDays(7));
        List<BankTransaction> unmatchedBanks = transactionMapper.selectList(bankWrapper);

        int exactMatched = 0;
        int fuzzyMatched = 0;

        for (BankTransaction bankTx : unmatchedBanks) {
            BigDecimal bankAmount = bankTx.getAmount() != null ? bankTx.getAmount() : BigDecimal.ZERO;
            int bankType = bankTx.getTransactionType() != null ? bankTx.getTransactionType() : 0;

            LambdaQueryWrapper<BankTransaction> matchWrapper = new LambdaQueryWrapper<>();
            matchWrapper.eq(BankTransaction::getMatchStatus, 0)
                    .ne(BankTransaction::getId, bankTx.getId())
                    .eq(BankTransaction::getAmount, bankAmount);

            if (bankType == 1) {
                matchWrapper.eq(BankTransaction::getTransactionType, 2);
            } else if (bankType == 2) {
                matchWrapper.eq(BankTransaction::getTransactionType, 1);
            }

            List<BankTransaction> candidates = transactionMapper.selectList(matchWrapper);

            BankTransaction bestMatch = null;
            String matchRule = null;
            BigDecimal bestScore = BigDecimal.ZERO;

            for (BankTransaction candidate : candidates) {
                BigDecimal score = BigDecimal.valueOf(100);
                if (bankTx.getTransactionDate() != null && candidate.getTransactionDate() != null) {
                    long daysDiff = Math.abs(ChronoUnit.DAYS.between(bankTx.getTransactionDate(), candidate.getTransactionDate()));
                    if (daysDiff == 0) {
                        score = score.add(BigDecimal.ZERO);
                        matchRule = "amount_date";
                    } else if (daysDiff <= 3) {
                        score = score.subtract(BigDecimal.valueOf(daysDiff * 5));
                        matchRule = "amount_date_fuzzy";
                    } else {
                        continue;
                    }
                } else {
                    matchRule = "amount_exact";
                }

                if (bankTx.getCounterAccountNo() != null && candidate.getCounterAccountNo() != null
                        && bankTx.getCounterAccountNo().equals(candidate.getCounterAccountNo())) {
                    score = score.add(BigDecimal.TEN);
                    matchRule = "amount_date_ref";
                }

                if (score.compareTo(bestScore) > 0) {
                    bestScore = score;
                    bestMatch = candidate;
                }
            }

            if (bestMatch != null) {
                if ("amount_exact".equals(matchRule) || "amount_date".equals(matchRule) || "amount_date_ref".equals(matchRule)) {
                    bankTx.setMatchStatus(1);
                    bestMatch.setMatchStatus(1);
                    exactMatched++;
                } else {
                    bankTx.setMatchStatus(2);
                    bestMatch.setMatchStatus(2);
                    fuzzyMatched++;
                }
                bankTx.setMatchRule(matchRule);
                bankTx.setMatchScore(bestScore);
                bankTx.setMatchTime(java.time.LocalDateTime.now());
                bestMatch.setMatchRule(matchRule);
                bestMatch.setMatchScore(bestScore);
                bestMatch.setMatchTime(java.time.LocalDateTime.now());
                transactionMapper.updateById(bankTx);
                transactionMapper.updateById(bestMatch);
            }
        }

        log.info("自动对账完成，精确匹配：{}笔，模糊匹配：{}笔", exactMatched, fuzzyMatched);

        BankReconciliation recon = new BankReconciliation();
        recon.setBankAccountNo(accountNo);
        recon.setReconciliationDate(reconciliationDate);
        recon.setStatus(0);
        reconciliationMapper.insert(recon);
    }

    @Override
    public Map<String, Object> getReconciliationResult(String accountNo, LocalDate date) {
        Map<String, Object> result = new HashMap<>();

        // 银行流水统计
        LambdaQueryWrapper<BankTransaction> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(BankTransaction::getBankAccountNo, accountNo)
                .ge(BankTransaction::getTransactionDate, date.minusDays(3))
                .le(BankTransaction::getTransactionDate, date.plusDays(3));
        long totalBank = transactionMapper.selectCount(allWrapper);

        LambdaQueryWrapper<BankTransaction> matchedWrapper = new LambdaQueryWrapper<>();
        matchedWrapper.eq(BankTransaction::getBankAccountNo, accountNo)
                .ge(BankTransaction::getTransactionDate, date.minusDays(3))
                .le(BankTransaction::getTransactionDate, date.plusDays(3))
                .in(BankTransaction::getMatchStatus, Arrays.asList(1, 2));
        long matchedCount = transactionMapper.selectCount(matchedWrapper);

        result.put("totalBankTransactions", totalBank);
        result.put("matchedCount", matchedCount);
        result.put("unmatchedCount", totalBank - matchedCount);
        result.put("matchRate", totalBank > 0 ? String.format("%.1f%%", matchedCount * 100.0 / totalBank) : "N/A");
        return result;
    }

    @Override
    public IPage<BankReconciliation> queryReconciliationResults(String accountNo, LocalDate startDate, LocalDate endDate, Page<?> page) {
        LambdaQueryWrapper<BankReconciliation> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(accountNo)) wrapper.eq(BankReconciliation::getAccountNo, accountNo);
        if (startDate != null) wrapper.ge(BankReconciliation::getReconciliationDate, startDate);
        if (endDate != null) wrapper.le(BankReconciliation::getReconciliationDate, endDate);
        wrapper.orderByDesc(BankReconciliation::getCreateTime);
        return reconciliationMapper.selectPage(page.convert(p -> new BankReconciliation()), wrapper);
    }

    @Override
    public void forceMatch(Long transactionId, Long billId, String billType) {
        BankTransaction tx = transactionMapper.selectById(transactionId);
        if (tx == null) throw new RuntimeException("银行流水不存在");
        tx.setMatchStatus(2); // 人工强制匹配
        tx.setMatchedBillId(billId);
        tx.setMatchedBillType(billType);
        transactionMapper.updateById(tx);
    }

    @Override
    public Map<String, Object> generateBalanceAdjustment(String accountNo, LocalDate date) {
        Map<String, Object> adjustment = new HashMap<>();
        adjustment.put("bankAccountNo", accountNo);
        adjustment.put("reconciliationDate", date.toString());

        // 计算未达账项
        LambdaQueryWrapper<BankTransaction> unmatchWrapper = new LambdaQueryWrapper<>();
        unmatchWrapper.eq(BankTransaction::getBankAccountNo, accountNo)
                .eq(BankTransaction::getMatchStatus, 0)
                .between(BankTransaction::getTransactionDate, date.minusDays(3), date.plusDays(3));
        List<BankTransaction> unmatchedList = transactionMapper.selectList(unmatchWrapper);

        BigDecimal bankUnmatched = unmatchedList.stream()
                .filter(t -> t.getTransactionType() == 1 || t.getTransactionType() == 2)
                .map(BankTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        adjustment.put("unmatchedBankAmount", bankUnmatched);
        adjustment.put("unmatchedBankCount", unmatchedList.size());
        return adjustment;
    }

    private BankTransactionVO convertToVO(BankTransaction tx) {
        BankTransactionVO vo = new BankTransactionVO();
        BeanUtils.copyProperties(tx, vo);
        return vo;
    }

    private String getString(Map<String, Object> map, String key) { return getString(map, key, null); }
    private String getString(Map<String, Object> map, String key, String def) { Object v = map.get(key); return v != null ? v.toString() : def; }
    private Integer getInt(Map<String, Object> map, String key) { Object v = map.get(key); return v instanceof Number ? ((Number) v).intValue() : null; }
    private BigDecimal getBigDecimal(Map<String, Object> map, String key) { Object v = map.get(key); return v instanceof Number ? BigDecimal.valueOf(((Number) v).doubleValue()) : null; }
    private LocalDate parseLocalDate(String s) { try { return s != null ? LocalDate.parse(s) : null; } catch (Exception e) { return null; } }
}
