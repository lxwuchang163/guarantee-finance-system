package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.BankAccountConfig;
import com.guarantee.finance.service.BankDirectConnectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "银企直连")
@RestController
@RequestMapping("/bank")
public class BankController {

    @Autowired
    private BankDirectConnectService bankService;

    @Operation(summary = "查询账户余额")
    @GetMapping("/balance")
    public R<Map<String, Object>> queryBalance(@RequestParam String accountNo,
                                                @RequestParam(defaultValue = "CNY") String currency) {
        return R.ok(bankService.queryBalance(accountNo, currency));
    }

    @Operation(summary = "批量查询余额")
    @PostMapping("/balance/batch")
    public R<List<Map<String, Object>>> batchQueryBalances(@RequestBody List<String> accountNos) {
        return R.ok(bankService.batchQueryBalances(accountNos));
    }

    @Operation(summary = "下载交易明细")
    @GetMapping("/transactions/download")
    public R<List<Map<String, Object>>> downloadTransactions(
            @RequestParam String accountNo,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.ok(bankService.queryTransactions(accountNo, startDate, endDate));
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/payment/status")
    public R<Map<String, Object>> queryPaymentStatus(@RequestParam String bankCode,
                                                          @RequestParam String paymentOrderNo) {
        return R.ok(bankService.queryPaymentStatus(bankCode, paymentOrderNo));
    }

    @Operation(summary = "获取银行账户配置列表")
    @GetMapping("/account/list")
    public R<List<BankAccountConfig>> getAccountConfigs() {
        return R.ok(bankService.getAccountConfigs());
    }

    @Operation(summary = "保存银行账户配置")
    @PostMapping("/account/save")
    public R<Void> saveAccount(@RequestBody BankAccountConfig config) {
        bankService.saveAccountConfig(config);
        return R.ok("保存成功", null);
    }
}
