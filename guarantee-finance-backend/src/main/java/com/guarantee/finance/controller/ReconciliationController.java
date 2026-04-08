package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.service.BankReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "银行对账")
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    @Autowired
    private BankReconciliationService reconciliationService;

    @Operation(summary = "导入银行流水")
    @PostMapping("/transaction/import")
    public R<Void> importTransactions(@RequestBody List<Map<String, Object>> transactionList) {
        reconciliationService.importTransactions(transactionList, "MANUAL_IMPORT");
        return R.ok("导入成功");
    }

    @Operation(summary = "银行流水查询")
    @GetMapping("/transaction/list")
    public R<?> queryTransactions(
            @RequestParam(required = false) String accountNo,
            @RequestParam(required = false) Integer transactionType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer matchStatus,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return R.ok(reconciliationService.queryTransactions(accountNo, transactionType, startDate, endDate, matchStatus,
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size)));
    }

    @Operation(summary = "执行自动对账")
    @PostMapping("/auto")
    public R<Void> autoReconciliation(@RequestParam String accountNo,
                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        reconciliationService.executeAutoReconciliation(accountNo, date);
        return R.ok("自动对账完成");
    }

    @Operation(summary = "获取对账结果")
    @GetMapping("/result")
    public R<Map<String, Object>> getReconciliationResult(@RequestParam String accountNo,
                                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return R.ok(reconciliationService.getReconciliationResult(accountNo, date));
    }

    @Operation(summary = "手工强制勾兑")
    @PutMapping("/forceMatch")
    public R<Void> forceMatch(@RequestParam Long transactionId, @RequestParam Long billId, @RequestParam String billType) {
        reconciliationService.forceMatch(transactionId, billId, billType);
        return R.ok("勾兑成功");
    }

    @Operation(summary = "生成余额调节表")
    @GetMapping("/balanceAdjustment")
    public R<Map<String, Object>> balanceAdjustment(@RequestParam String accountNo,
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return R.ok(reconciliationService.generateBalanceAdjustment(accountNo, date));
    }
}
