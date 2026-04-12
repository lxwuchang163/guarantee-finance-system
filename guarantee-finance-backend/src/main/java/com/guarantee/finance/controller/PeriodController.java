package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.AccPeriod;
import com.guarantee.finance.service.PeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会计期间管理")
@RestController
@RequestMapping("/accounting/period")
public class PeriodController {

    @Autowired
    private PeriodService periodService;

    @Operation(summary = "期间列表")
    @GetMapping("/list")
    public R<List<AccPeriod>> list(@RequestParam(required = false) Integer year) {
        return R.ok(periodService.getPeriodList(year));
    }

    @Operation(summary = "当前期间")
    @GetMapping("/current")
    public R<AccPeriod> current() {
        return R.ok(periodService.getCurrentPeriod());
    }

    @Operation(summary = "初始化年度期间")
    @PostMapping("/init")
    public R<Void> init(@RequestParam Integer year) {
        periodService.initPeriods(year);
        return R.ok("初始化成功", null);
    }

    @Operation(summary = "结账")
    @PutMapping("/{periodCode}/close")
    public R<Void> close(@PathVariable String periodCode) {
        periodService.closePeriod(periodCode);
        return R.ok("结账成功", null);
    }

    @Operation(summary = "反结账")
    @PutMapping("/{periodCode}/reopen")
    public R<Void> reopen(@PathVariable String periodCode) {
        periodService.reopenPeriod(periodCode);
        return R.ok("反结账成功", null);
    }

    @Operation(summary = "检查是否可结账")
    @GetMapping("/{periodCode}/check")
    public R<Boolean> check(@PathVariable String periodCode) {
        return R.ok(periodService.checkPeriodCanClose(periodCode));
    }
}
