package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.AccFinancialReport;
import com.guarantee.finance.service.FinancialReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "财务报表管理")
@RestController
@RequestMapping("/accounting/report")
public class FinancialReportController {

    @Autowired
    private FinancialReportService financialReportService;

    @Operation(summary = "生成资产负债表")
    @PostMapping("/balance-sheet")
    public R<AccFinancialReport> balanceSheet(@RequestParam String period) {
        return R.ok("生成成功", financialReportService.generateBalanceSheet(period));
    }

    @Operation(summary = "生成利润表")
    @PostMapping("/income-statement")
    public R<AccFinancialReport> incomeStatement(@RequestParam String period) {
        return R.ok("生成成功", financialReportService.generateIncomeStatement(period));
    }

    @Operation(summary = "生成现金流量表")
    @PostMapping("/cash-flow")
    public R<AccFinancialReport> cashFlow(@RequestParam String period) {
        return R.ok("生成成功", financialReportService.generateCashFlowStatement(period));
    }

    @Operation(summary = "报表详情")
    @GetMapping("/{id}")
    public R<AccFinancialReport> detail(@PathVariable Long id) {
        return R.ok(financialReportService.getById(id));
    }

    @Operation(summary = "确认报表")
    @PutMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        financialReportService.confirmReport(id);
        return R.ok("确认成功", null);
    }

    @Operation(summary = "审批报表")
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        financialReportService.approveReport(id);
        return R.ok("审批成功", null);
    }

    @Operation(summary = "报表分页查询")
    @GetMapping("/page")
    public R<PageResult<AccFinancialReport>> page(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<AccFinancialReport> page = financialReportService.queryPage(reportType, period, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }
}
