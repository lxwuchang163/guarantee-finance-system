package com.guarantee.finance.service;

import com.guarantee.finance.dto.VoucherDTO;
import com.guarantee.finance.vo.VoucherVO;

import java.util.List;

public interface AccIntegrationService {

    Long createVoucherFromReceipt(Long receiptId);

    Long createVoucherFromPayment(Long paymentId);

    boolean syncVoucherToNc(Long voucherId);

    List<VoucherVO> getVouchersByReceiptId(Long receiptId);

    List<VoucherVO> getVouchersByPaymentId(Long paymentId);

    void batchSyncVouchersToNc(List<Long> voucherIds);
}
