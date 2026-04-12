package com.guarantee.finance.service;

import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.FinPayment;
import com.guarantee.finance.entity.FinReceipt;

public interface VoucherAutoGenerateService {

    AccVoucher generateReceiptVoucher(FinReceipt receipt);

    AccVoucher generatePaymentVoucher(FinPayment payment);
}
