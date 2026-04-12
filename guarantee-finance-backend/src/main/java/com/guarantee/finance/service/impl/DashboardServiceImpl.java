package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.*;
import com.guarantee.finance.mapper.*;
import com.guarantee.finance.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl extends ServiceImpl<DashboardStatsMapper, DashboardStats> implements DashboardService {

    @Resource
    private FinReceiptMapper finReceiptMapper;

    @Resource
    private FinPaymentMapper finPaymentMapper;

    @Resource
    private BankReconciliationMapper bankReconciliationMapper;

    @Resource
    private BankAccountConfigMapper bankAccountConfigMapper;

    @Resource
    private AccVoucherMapper accVoucherMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String monthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        LambdaQueryWrapper<FinReceipt> receiptTodayWrapper = new LambdaQueryWrapper<>();
        receiptTodayWrapper.eq(FinReceipt::getDeleted, 0)
                .apply("DATE(receipt_date) = {0}", todayStr);
        List<FinReceipt> todayReceipts = finReceiptMapper.selectList(receiptTodayWrapper);
        BigDecimal todayIncome = todayReceipts.stream()
                .map(FinReceipt::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<FinPayment> paymentTodayWrapper = new LambdaQueryWrapper<>();
        paymentTodayWrapper.eq(FinPayment::getDeleted, 0)
                .apply("DATE(payment_date) = {0}", todayStr);
        List<FinPayment> todayPayments = finPaymentMapper.selectList(paymentTodayWrapper);
        BigDecimal todayExpense = todayPayments.stream()
                .map(FinPayment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingReceipts = finReceiptMapper.selectCount(
                new LambdaQueryWrapper<FinReceipt>().eq(FinReceipt::getDeleted, 0).in(FinReceipt::getStatus, 0, 1));
        long pendingPayments = finPaymentMapper.selectCount(
                new LambdaQueryWrapper<FinPayment>().eq(FinPayment::getDeleted, 0).in(FinPayment::getStatus, 0, 1));
        long pendingVouchers = accVoucherMapper.selectCount(
                new LambdaQueryWrapper<AccVoucher>().eq(AccVoucher::getDeleted, 0).eq(AccVoucher::getStatus, 1));
        long pendingDocuments = pendingReceipts + pendingPayments + pendingVouchers;

        long monthReconciliation = bankReconciliationMapper.selectCount(
                new LambdaQueryWrapper<BankReconciliation>().eq(BankReconciliation::getDeleted, 0)
                        .apply("DATE_FORMAT(reconciliation_date, '%Y-%m') = {0}", monthStr));
        long completedReconciliation = bankReconciliationMapper.selectCount(
                new LambdaQueryWrapper<BankReconciliation>().eq(BankReconciliation::getDeleted, 0)
                        .eq(BankReconciliation::getStatus, 1)
                        .apply("DATE_FORMAT(reconciliation_date, '%Y-%m') = {0}", monthStr));
        BigDecimal reconciliationRate = monthReconciliation > 0
                ? new BigDecimal(completedReconciliation).divide(new BigDecimal(monthReconciliation), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                : BigDecimal.ZERO;

        long bankAccounts = bankAccountConfigMapper.selectCount(
                new LambdaQueryWrapper<BankAccountConfig>().eq(BankAccountConfig::getDeleted, 0).eq(BankAccountConfig::getStatus, 1));

        LambdaQueryWrapper<FinReceipt> receiptMonthWrapper = new LambdaQueryWrapper<>();
        receiptMonthWrapper.eq(FinReceipt::getDeleted, 0)
                .apply("DATE_FORMAT(receipt_date, '%Y-%m') = {0}", monthStr);
        List<FinReceipt> monthReceipts = finReceiptMapper.selectList(receiptMonthWrapper);
        BigDecimal monthIncome = monthReceipts.stream()
                .map(FinReceipt::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<FinPayment> paymentMonthWrapper = new LambdaQueryWrapper<>();
        paymentMonthWrapper.eq(FinPayment::getDeleted, 0)
                .apply("DATE_FORMAT(payment_date, '%Y-%m') = {0}", monthStr);
        List<FinPayment> monthPayments = finPaymentMapper.selectList(paymentMonthWrapper);
        BigDecimal monthExpense = monthPayments.stream()
                .map(FinPayment::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalReceipts = finReceiptMapper.selectCount(
                new LambdaQueryWrapper<FinReceipt>().eq(FinReceipt::getDeleted, 0));
        long totalPayments = finPaymentMapper.selectCount(
                new LambdaQueryWrapper<FinPayment>().eq(FinPayment::getDeleted, 0));
        long totalVouchers = accVoucherMapper.selectCount(
                new LambdaQueryWrapper<AccVoucher>().eq(AccVoucher::getDeleted, 0));

        stats.put("todayIncome", todayIncome);
        stats.put("todayExpense", todayExpense);
        stats.put("pendingDocuments", pendingDocuments);
        stats.put("pendingReceipts", pendingReceipts);
        stats.put("pendingPayments", pendingPayments);
        stats.put("pendingVouchers", pendingVouchers);
        stats.put("monthlyReconciliation", monthReconciliation);
        stats.put("completedReconciliation", completedReconciliation);
        stats.put("reconciliationRate", reconciliationRate.setScale(2, BigDecimal.ROUND_HALF_UP));
        stats.put("bankAccounts", bankAccounts);
        stats.put("monthIncome", monthIncome);
        stats.put("monthExpense", monthExpense);
        stats.put("totalReceipts", totalReceipts);
        stats.put("totalPayments", totalPayments);
        stats.put("totalVouchers", totalVouchers);

        Map<String, String> changeRates = new HashMap<>();
        changeRates.put("todayIncome", "+0%");
        changeRates.put("todayExpense", "+0%");
        changeRates.put("pendingDocuments", String.valueOf(pendingDocuments));
        changeRates.put("monthlyReconciliation", reconciliationRate.setScale(0, BigDecimal.ROUND_HALF_UP) + "%");
        stats.put("changeRates", changeRates);

        return stats;
    }

    @Override
    public List<IncomeExpenseData> getIncomeExpenseData() {
        List<IncomeExpenseData> dataList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            String monthStr = monthDate.format(monthFormatter);
            String label = monthDate.getMonthValue() + "月";

            LambdaQueryWrapper<FinReceipt> receiptWrapper = new LambdaQueryWrapper<>();
            receiptWrapper.eq(FinReceipt::getDeleted, 0)
                    .apply("DATE_FORMAT(receipt_date, '%Y-%m') = {0}", monthStr);
            List<FinReceipt> receipts = finReceiptMapper.selectList(receiptWrapper);
            BigDecimal income = receipts.stream()
                    .map(FinReceipt::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

            LambdaQueryWrapper<FinPayment> paymentWrapper = new LambdaQueryWrapper<>();
            paymentWrapper.eq(FinPayment::getDeleted, 0)
                    .apply("DATE_FORMAT(payment_date, '%Y-%m') = {0}", monthStr);
            List<FinPayment> payments = finPaymentMapper.selectList(paymentWrapper);
            BigDecimal expense = payments.stream()
                    .map(FinPayment::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

            IncomeExpenseData data = new IncomeExpenseData();
            data.setMonth(label);
            data.setIncome(income);
            data.setExpense(expense);
            dataList.add(data);
        }

        return dataList;
    }

    @Override
    public List<BusinessTypeData> getBusinessTypeData() {
        List<BusinessTypeData> dataList = new ArrayList<>();

        LambdaQueryWrapper<FinReceipt> receiptWrapper = new LambdaQueryWrapper<>();
        receiptWrapper.eq(FinReceipt::getDeleted, 0);
        List<FinReceipt> allReceipts = finReceiptMapper.selectList(receiptWrapper);
        Map<String, Long> receiptByType = allReceipts.stream()
                .filter(r -> r.getBusinessTypeName() != null)
                .collect(Collectors.groupingBy(FinReceipt::getBusinessTypeName, Collectors.counting()));

        LambdaQueryWrapper<FinPayment> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(FinPayment::getDeleted, 0);
        List<FinPayment> allPayments = finPaymentMapper.selectList(paymentWrapper);
        Map<String, Long> paymentByType = allPayments.stream()
                .filter(p -> p.getBusinessTypeName() != null)
                .collect(Collectors.groupingBy(FinPayment::getBusinessTypeName, Collectors.counting()));

        Map<String, Long> merged = new LinkedHashMap<>();
        receiptByType.forEach((k, v) -> merged.merge(k, v, Long::sum));
        paymentByType.forEach((k, v) -> merged.merge(k, v, Long::sum));

        final long total = merged.values().stream().mapToLong(Long::longValue).sum() == 0 ? 1 : merged.values().stream().mapToLong(Long::longValue).sum();

        merged.forEach((type, count) -> {
            BusinessTypeData data = new BusinessTypeData();
            data.setType(type);
            data.setValue(new BigDecimal(count));
            data.setPercentage(new BigDecimal(count * 100).divide(new BigDecimal(total), 2, BigDecimal.ROUND_HALF_UP));
            dataList.add(data);
        });

        if (dataList.isEmpty()) {
            BusinessTypeData d1 = new BusinessTypeData();
            d1.setType("担保业务"); d1.setValue(new BigDecimal(45)); d1.setPercentage(new BigDecimal("45.00"));
            dataList.add(d1);
            BusinessTypeData d2 = new BusinessTypeData();
            d2.setType("贷款业务"); d2.setValue(new BigDecimal(30)); d2.setPercentage(new BigDecimal("30.00"));
            dataList.add(d2);
            BusinessTypeData d3 = new BusinessTypeData();
            d3.setType("理财业务"); d3.setValue(new BigDecimal(20)); d3.setPercentage(new BigDecimal("20.00"));
            dataList.add(d3);
            BusinessTypeData d4 = new BusinessTypeData();
            d4.setType("其他业务"); d4.setValue(new BigDecimal(5)); d4.setPercentage(new BigDecimal("5.00"));
            dataList.add(d4);
        }

        return dataList;
    }
}
