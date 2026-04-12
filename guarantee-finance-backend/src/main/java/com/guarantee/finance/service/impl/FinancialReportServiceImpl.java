package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.entity.AccFinancialReport;
import com.guarantee.finance.entity.AccSubjectBalance;
import com.guarantee.finance.mapper.AccAccountSubjectMapper;
import com.guarantee.finance.mapper.AccFinancialReportMapper;
import com.guarantee.finance.mapper.AccSubjectBalanceMapper;
import com.guarantee.finance.service.FinancialReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class FinancialReportServiceImpl extends ServiceImpl<AccFinancialReportMapper, AccFinancialReport> implements FinancialReportService {

    @Autowired
    private AccSubjectBalanceMapper subjectBalanceMapper;
    @Autowired
    private AccAccountSubjectMapper accountSubjectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccFinancialReport generateBalanceSheet(String period) {
        log.info("生成资产负债表，期间：{}", period);
        List<Map<String, Object>> assetItems = new ArrayList<>();
        List<Map<String, Object>> liabilityItems = new ArrayList<>();
        List<Map<String, Object>> equityItems = new ArrayList<>();
        BigDecimal totalAsset = BigDecimal.ZERO;
        BigDecimal totalLiability = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period).eq(AccSubjectBalance::getDeleted, 0);
        List<AccSubjectBalance> balances = subjectBalanceMapper.selectList(wrapper);

        for (AccSubjectBalance balance : balances) {
            AccAccountSubject subject = accountSubjectMapper.selectOne(
                    new LambdaQueryWrapper<AccAccountSubject>()
                            .eq(AccAccountSubject::getSubjectCode, balance.getSubjectCode())
                            .eq(AccAccountSubject::getDeleted, 0).last("LIMIT 1"));
            if (subject == null) continue;

            BigDecimal endBalance;
            if ("1".equals(String.valueOf(subject.getBalanceDirection()))) {
                endBalance = (balance.getEndDebit() != null ? balance.getEndDebit() : BigDecimal.ZERO)
                        .subtract(balance.getEndCredit() != null ? balance.getEndCredit() : BigDecimal.ZERO);
            } else {
                endBalance = (balance.getEndCredit() != null ? balance.getEndCredit() : BigDecimal.ZERO)
                        .subtract(balance.getEndDebit() != null ? balance.getEndDebit() : BigDecimal.ZERO);
            }
            if (endBalance.compareTo(BigDecimal.ZERO) == 0) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("subjectCode", balance.getSubjectCode());
            item.put("subjectName", subject.getSubjectName());
            item.put("amount", endBalance);

            int subjectType = subject.getSubjectType() != null ? subject.getSubjectType() : 0;
            switch (subjectType) {
                case 1:
                    assetItems.add(item);
                    totalAsset = totalAsset.add(endBalance);
                    break;
                case 2:
                    liabilityItems.add(item);
                    totalLiability = totalLiability.add(endBalance);
                    break;
                case 3:
                    equityItems.add(item);
                    totalEquity = totalEquity.add(endBalance);
                    break;
            }
        }

        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("assetItems", assetItems);
        reportData.put("liabilityItems", liabilityItems);
        reportData.put("equityItems", equityItems);
        reportData.put("totalAsset", totalAsset);
        reportData.put("totalLiability", totalLiability);
        reportData.put("totalEquity", totalEquity);

        return saveReport(period, "BALANCE_SHEET", "资产负债表", reportData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccFinancialReport generateIncomeStatement(String period) {
        log.info("生成利润表，期间：{}", period);
        List<Map<String, Object>> revenueItems = new ArrayList<>();
        List<Map<String, Object>> expenseItems = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period).eq(AccSubjectBalance::getDeleted, 0);
        List<AccSubjectBalance> balances = subjectBalanceMapper.selectList(wrapper);

        for (AccSubjectBalance balance : balances) {
            AccAccountSubject subject = accountSubjectMapper.selectOne(
                    new LambdaQueryWrapper<AccAccountSubject>()
                            .eq(AccAccountSubject::getSubjectCode, balance.getSubjectCode())
                            .eq(AccAccountSubject::getDeleted, 0).last("LIMIT 1"));
            if (subject == null || subject.getSubjectType() == null || subject.getSubjectType() != 5) continue;

            BigDecimal currentDebit = balance.getCurrentDebit() != null ? balance.getCurrentDebit() : BigDecimal.ZERO;
            BigDecimal currentCredit = balance.getCurrentCredit() != null ? balance.getCurrentCredit() : BigDecimal.ZERO;
            BigDecimal netAmount;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("subjectCode", balance.getSubjectCode());
            item.put("subjectName", subject.getSubjectName());

            if ("2".equals(String.valueOf(subject.getBalanceDirection()))) {
                netAmount = currentCredit.subtract(currentDebit);
                item.put("amount", netAmount);
                revenueItems.add(item);
                totalRevenue = totalRevenue.add(netAmount.max(BigDecimal.ZERO));
            } else {
                netAmount = currentDebit.subtract(currentCredit);
                item.put("amount", netAmount);
                expenseItems.add(item);
                totalExpense = totalExpense.add(netAmount.max(BigDecimal.ZERO));
            }
        }

        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("revenueItems", revenueItems);
        reportData.put("expenseItems", expenseItems);
        reportData.put("totalRevenue", totalRevenue);
        reportData.put("totalExpense", totalExpense);
        reportData.put("netProfit", totalRevenue.subtract(totalExpense));

        return saveReport(period, "INCOME_STATEMENT", "利润表", reportData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccFinancialReport generateCashFlowStatement(String period) {
        log.info("生成现金流量表，期间：{}", period);
        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("operatingActivities", new ArrayList<>());
        reportData.put("investingActivities", new ArrayList<>());
        reportData.put("financingActivities", new ArrayList<>());
        reportData.put("netCashFlow", BigDecimal.ZERO);

        return saveReport(period, "CASH_FLOW", "现金流量表", reportData);
    }

    @Override
    public IPage<AccFinancialReport> queryPage(String reportType, String period, IPage<AccFinancialReport> page) {
        LambdaQueryWrapper<AccFinancialReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccFinancialReport::getDeleted, 0);
        if (reportType != null && !reportType.isEmpty()) {
            wrapper.eq(AccFinancialReport::getReportType, reportType);
        }
        if (period != null && !period.isEmpty()) {
            wrapper.eq(AccFinancialReport::getPeriod, period);
        }
        wrapper.orderByDesc(AccFinancialReport::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void confirmReport(Long id) {
        AccFinancialReport report = getById(id);
        if (report == null) throw new RuntimeException("报表不存在");
        if (!"DRAFT".equals(report.getStatus())) throw new RuntimeException("只有草稿状态可以确认");
        report.setStatus("CONFIRMED");
        report.setConfirmTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        updateById(report);
    }

    @Override
    public void approveReport(Long id) {
        AccFinancialReport report = getById(id);
        if (report == null) throw new RuntimeException("报表不存在");
        if (!"CONFIRMED".equals(report.getStatus())) throw new RuntimeException("只有已确认状态可以审批");
        report.setStatus("APPROVED");
        report.setApproveTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        updateById(report);
    }

    private AccFinancialReport saveReport(String period, String reportType, String reportName, Map<String, Object> reportData) {
        String year = period.substring(0, 4);
        String month = period.substring(4);

        LambdaQueryWrapper<AccFinancialReport> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(AccFinancialReport::getReportType, reportType)
                .eq(AccFinancialReport::getPeriod, period)
                .eq(AccFinancialReport::getDeleted, 0);
        AccFinancialReport existing = getOne(existWrapper, false);

        if (existing != null) {
            existing.setReportData(cn.hutool.json.JSONUtil.toJsonStr(reportData));
            existing.setUpdateTime(LocalDateTime.now());
            updateById(existing);
            return existing;
        }

        AccFinancialReport report = new AccFinancialReport();
        report.setReportCode(reportType + "_" + period);
        report.setReportName(reportName + " " + period);
        report.setReportType(reportType);
        report.setPeriod(period);
        report.setYear(Integer.parseInt(year));
        report.setMonth(Integer.parseInt(month));
        report.setReportData(cn.hutool.json.JSONUtil.toJsonStr(reportData));
        report.setStatus("DRAFT");
        report.setDeleted(0);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        save(report);
        return report;
    }
}
