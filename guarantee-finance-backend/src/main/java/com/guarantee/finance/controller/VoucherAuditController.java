package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.VoucherAuditDTO;
import com.guarantee.finance.service.AccVoucherAuditService;
import com.guarantee.finance.vo.VoucherAuditVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting/audit")
@RequiredArgsConstructor
public class VoucherAuditController {

    private final AccVoucherAuditService accVoucherAuditService;

    @GetMapping("/page")
    public R<com.baomidou.mybatisplus.core.metadata.IPage<VoucherAuditVO>> queryAudits(
            @RequestParam(required = false) String voucherNo,
            @RequestParam(required = false) String auditStatus,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Page<?> pagination = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<VoucherAuditVO> result = accVoucherAuditService.queryAudits(voucherNo, auditStatus, pagination);
        return R.ok(result);
    }

    @GetMapping("/voucher/{voucherId}")
    public R<List<VoucherAuditVO>> getVoucherAudits(@PathVariable Long voucherId) {
        List<VoucherAuditVO> audits = accVoucherAuditService.getVoucherAudits(voucherId);
        return R.ok(audits);
    }

    @PostMapping
    public R<Void> auditVoucher(@RequestBody VoucherAuditDTO dto) {
        accVoucherAuditService.auditVoucher(dto);
        return R.ok();
    }

    @PostMapping("/approve/{voucherId}")
    public R<Void> approveVoucher(@PathVariable Long voucherId, @RequestParam String opinion) {
        accVoucherAuditService.approveVoucher(voucherId, opinion);
        return R.ok();
    }

    @PostMapping("/reject/{voucherId}")
    public R<Void> rejectVoucher(@PathVariable Long voucherId, @RequestParam String opinion) {
        accVoucherAuditService.rejectVoucher(voucherId, opinion);
        return R.ok();
    }

    @GetMapping("/can-audit")
    public R<Boolean> canAuditVoucher(@RequestParam Long voucherId, @RequestParam Long auditorId) {
        boolean canAudit = accVoucherAuditService.canAuditVoucher(voucherId, auditorId);
        return R.ok(canAudit);
    }
}
