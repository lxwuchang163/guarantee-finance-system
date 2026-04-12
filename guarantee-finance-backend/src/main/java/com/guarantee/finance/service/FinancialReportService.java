package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.AccFinancialReport;

import java.util.Map;

public interface FinancialReportService extends IService<AccFinancialReport> {
    AccFinancialReport generateBalanceSheet(String period);
    AccFinancialReport generateIncomeStatement(String period);
    AccFinancialReport generateCashFlowStatement(String period);
    IPage<AccFinancialReport> queryPage(String reportType, String period, IPage<AccFinancialReport> page);
    void confirmReport(Long id);
    void approveReport(Long id);
}
