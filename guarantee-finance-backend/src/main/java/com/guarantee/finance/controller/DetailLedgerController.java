package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.AccDetailLedger;
import com.guarantee.finance.service.DetailLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "明细分类账")
@RestController
@RequestMapping("/accounting/detail-ledger")
public class DetailLedgerController {

    @Autowired
    private DetailLedgerService detailLedgerService;

    @Operation(summary = "明细账分页查询")
    @GetMapping("/page")
    public R<PageResult<AccDetailLedger>> page(
            @RequestParam String period,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        IPage<AccDetailLedger> page = detailLedgerService.queryBySubjectAndPeriod(subjectCode, period, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "生成明细分类账")
    @PostMapping("/generate")
    public R<Void> generate(@RequestParam String period) {
        detailLedgerService.generateDetailLedger(period);
        return R.ok("生成成功", null);
    }
}
