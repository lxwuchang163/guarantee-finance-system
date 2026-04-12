package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.AccGeneralLedger;
import com.guarantee.finance.service.GeneralLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "总分类账")
@RestController
@RequestMapping("/accounting/general-ledger")
public class GeneralLedgerController {

    @Autowired
    private GeneralLedgerService generalLedgerService;

    @Operation(summary = "总账分页查询")
    @GetMapping("/page")
    public R<PageResult<AccGeneralLedger>> page(
            @RequestParam String period,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        IPage<AccGeneralLedger> page = generalLedgerService.queryByPeriod(period, subjectCode, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "生成总分类账")
    @PostMapping("/generate")
    public R<Void> generate(@RequestParam String period) {
        generalLedgerService.generateGeneralLedger(period);
        return R.ok("生成成功", null);
    }
}
