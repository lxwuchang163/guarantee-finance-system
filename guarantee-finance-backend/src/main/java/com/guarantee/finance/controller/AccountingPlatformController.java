package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.VoucherGenerateDTO;
import com.guarantee.finance.service.AccountingPlatformService;
import com.guarantee.finance.vo.AccVoucherVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会计平台管理")
@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
public class AccountingPlatformController {

    private final AccountingPlatformService accountingPlatformService;

    @Operation(summary = "分页查询凭证列表")
    @GetMapping("/voucher/list")
    public R<IPage<AccVoucherVO>> listVouchers(
            @RequestParam(required = false) String voucherNo,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<?> page = Page.of(current, size);
        IPage<AccVoucherVO> result = accountingPlatformService.queryVouchers(
                voucherNo, voucherType, status, startDate, endDate, page);
        return R.ok(result);
    }

    @Operation(summary = "获取凭证详情")
    @GetMapping("/voucher/{id}")
    public R<AccVoucherVO> getVoucherDetail(@PathVariable Long id) {
        AccVoucherVO vo = accountingPlatformService.getVoucherDetail(id);
        return R.ok(vo);
    }

    @Operation(summary = "生成凭证")
    @PostMapping("/voucher/generate")
    public R<Long> generateVoucher(@RequestBody VoucherGenerateDTO dto) {
        Long voucherId = accountingPlatformService.generateVoucher(dto);
        return R.ok(voucherId);
    }

    @Operation(summary = "批量生成凭证")
    @PostMapping("/voucher/batch-generate")
    public R<Boolean> batchGenerateVouchers(@RequestBody List<Long> billIds) {
        accountingPlatformService.batchGenerateVouchers(billIds);
        return R.ok(true);
    }

    @Operation(summary = "审核凭证")
    @PutMapping("/voucher/{id}/audit")
    public R<Void> auditVoucher(@PathVariable Long id) {
        accountingPlatformService.auditVoucher(id);
        return R.ok();
    }

    @Operation(summary = "冲销凭证")
    @PutMapping("/voucher/{id}/reverse")
    public R<Void> reverseVoucher(@PathVariable Long id) {
        accountingPlatformService.reverseVoucher(id);
        return R.ok();
    }
}
