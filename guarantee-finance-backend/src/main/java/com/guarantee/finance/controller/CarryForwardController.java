package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.AccCarryForward;
import com.guarantee.finance.service.CarryForwardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "自动结转")
@RestController
@RequestMapping("/accounting/carry-forward")
public class CarryForwardController {

    @Autowired
    private CarryForwardService carryForwardService;

    @Operation(summary = "预览损益结转")
    @PostMapping("/preview")
    public R<List<Map<String, Object>>> preview(@RequestParam String period) {
        return R.ok(carryForwardService.previewProfitLoss(period));
    }

    @Operation(summary = "执行损益结转")
    @PostMapping("/profit-loss")
    public R<Void> profitLoss(@RequestParam String period) {
        carryForwardService.carryForwardProfitLoss(period);
        return R.ok("损益结转成功", null);
    }

    @Operation(summary = "冲回结转")
    @PostMapping("/{id}/reverse")
    public R<Void> reverse(@PathVariable Long id) {
        carryForwardService.reverseCarryForward(id);
        return R.ok("冲回成功", null);
    }

    @Operation(summary = "结转记录列表")
    @GetMapping("/list")
    public R<List<AccCarryForward>> list(@RequestParam(required = false) String period) {
        return R.ok(carryForwardService.getCarryForwardList(period));
    }
}
