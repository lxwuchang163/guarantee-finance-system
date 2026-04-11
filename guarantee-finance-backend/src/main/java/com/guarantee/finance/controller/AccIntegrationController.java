package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.service.AccIntegrationService;
import com.guarantee.finance.vo.VoucherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/integration")
@RequiredArgsConstructor
public class AccIntegrationController {

    private final AccIntegrationService accIntegrationService;

    @PostMapping("/receipt/{receiptId}")
    public R<Long> createVoucherFromReceipt(@PathVariable Long receiptId) {
        Long voucherId = accIntegrationService.createVoucherFromReceipt(receiptId);
        return R.ok(voucherId);
    }

    @PostMapping("/payment/{paymentId}")
    public R<Long> createVoucherFromPayment(@PathVariable Long paymentId) {
        Long voucherId = accIntegrationService.createVoucherFromPayment(paymentId);
        return R.ok(voucherId);
    }

    @PostMapping("/sync/nc/{voucherId}")
    public R<Boolean> syncVoucherToNc(@PathVariable Long voucherId) {
        boolean success = accIntegrationService.syncVoucherToNc(voucherId);
        return R.ok(success);
    }

    @PostMapping("/sync/nc/batch")
    public R<Void> batchSyncVouchersToNc(@RequestBody List<Long> voucherIds) {
        accIntegrationService.batchSyncVouchersToNc(voucherIds);
        return R.ok();
    }

    @GetMapping("/receipt/{receiptId}")
    public R<List<VoucherVO>> getVouchersByReceiptId(@PathVariable Long receiptId) {
        List<VoucherVO> vouchers = accIntegrationService.getVouchersByReceiptId(receiptId);
        return R.ok(vouchers);
    }

    @GetMapping("/payment/{paymentId}")
    public R<List<VoucherVO>> getVouchersByPaymentId(@PathVariable Long paymentId) {
        List<VoucherVO> vouchers = accIntegrationService.getVouchersByPaymentId(paymentId);
        return R.ok(vouchers);
    }
}
