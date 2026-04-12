package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.entity.FinPayment;
import com.guarantee.finance.entity.FinReceipt;
import com.guarantee.finance.mapper.AccAccountSubjectMapper;
import com.guarantee.finance.mapper.AccVoucherDetailMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.VoucherAutoGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherAutoGenerateServiceImpl implements VoucherAutoGenerateService {

    private final AccVoucherMapper accVoucherMapper;
    private final AccVoucherDetailMapper accVoucherDetailMapper;
    private final AccAccountSubjectMapper accAccountSubjectMapper;

    private static final String BANK_SUBJECT_CODE = "1002";
    private static final String[] RECEIPT_SUBJECT_CODES = {"6001", "6002", "6003"};
    private static final String[] PAYMENT_SUBJECT_CODES = {"6401", "6402", "6403"};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccVoucher generateReceiptVoucher(FinReceipt receipt) {
        log.info("收款单 {} 审核通过，自动生成凭证", receipt.getReceiptNo());

        String creditSubjectCode = getReceiptCreditSubject(receipt.getBusinessType());
        String creditSubjectName = getSubjectName(creditSubjectCode);
        String bankSubjectName = getSubjectName(BANK_SUBJECT_CODE);

        String voucherDate = receipt.getReceiptDate() != null
                ? receipt.getReceiptDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String period = voucherDate.substring(0, 7).replace("-", "");
        String voucherNo = generateVoucherNo(voucherDate);

        AccVoucher voucher = new AccVoucher();
        voucher.setVoucherNo(voucherNo);
        voucher.setVoucherDate(voucherDate);
        voucher.setPeriod(period);
        voucher.setSummary("收款单 " + receipt.getReceiptNo() + " - " + receipt.getCustomerName());
        voucher.setStatus(1);
        voucher.setVoucherType(2);
        voucher.setSourceType("receipt");
        voucher.setSourceId(String.valueOf(receipt.getId()));
        voucher.setNcSyncStatus("0");
        voucher.setYear(Integer.parseInt(voucherDate.substring(0, 4)));
        voucher.setMonth(Integer.parseInt(voucherDate.substring(5, 7)));
        accVoucherMapper.insert(voucher);

        AccVoucherDetail debitDetail = new AccVoucherDetail();
        debitDetail.setVoucherId(voucher.getId());
        debitDetail.setLineNo(1);
        debitDetail.setSubjectCode(BANK_SUBJECT_CODE);
        debitDetail.setSubjectName(bankSubjectName);
        debitDetail.setSummary("收款 - " + receipt.getCustomerName());
        debitDetail.setDebitAmount(receipt.getAmount() != null ? receipt.getAmount() : BigDecimal.ZERO);
        debitDetail.setCreditAmount(BigDecimal.ZERO);
        debitDetail.setCustomerCode(receipt.getCustomerCode());
        accVoucherDetailMapper.insert(debitDetail);

        AccVoucherDetail creditDetail = new AccVoucherDetail();
        creditDetail.setVoucherId(voucher.getId());
        creditDetail.setLineNo(2);
        creditDetail.setSubjectCode(creditSubjectCode);
        creditDetail.setSubjectName(creditSubjectName);
        creditDetail.setSummary("收款 - " + receipt.getCustomerName());
        creditDetail.setDebitAmount(BigDecimal.ZERO);
        creditDetail.setCreditAmount(receipt.getAmount() != null ? receipt.getAmount() : BigDecimal.ZERO);
        creditDetail.setCustomerCode(receipt.getCustomerCode());
        accVoucherDetailMapper.insert(creditDetail);

        log.info("收款单 {} 自动生成凭证成功，凭证号：{}", receipt.getReceiptNo(), voucherNo);
        return voucher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccVoucher generatePaymentVoucher(FinPayment payment) {
        log.info("付款单 {} 审核通过，自动生成凭证", payment.getPaymentNo());

        String debitSubjectCode = getPaymentDebitSubject(payment.getBusinessType());
        String debitSubjectName = getSubjectName(debitSubjectCode);
        String bankSubjectName = getSubjectName(BANK_SUBJECT_CODE);

        String voucherDate = payment.getPaymentDate() != null
                ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String period = voucherDate.substring(0, 7).replace("-", "");
        String voucherNo = generateVoucherNo(voucherDate);

        AccVoucher voucher = new AccVoucher();
        voucher.setVoucherNo(voucherNo);
        voucher.setVoucherDate(voucherDate);
        voucher.setPeriod(period);
        voucher.setSummary("付款单 " + payment.getPaymentNo() + " - " + payment.getCustomerName());
        voucher.setStatus(1);
        voucher.setVoucherType(3);
        voucher.setSourceType("payment");
        voucher.setSourceId(String.valueOf(payment.getId()));
        voucher.setNcSyncStatus("0");
        voucher.setYear(Integer.parseInt(voucherDate.substring(0, 4)));
        voucher.setMonth(Integer.parseInt(voucherDate.substring(5, 7)));
        accVoucherMapper.insert(voucher);

        AccVoucherDetail debitDetail = new AccVoucherDetail();
        debitDetail.setVoucherId(voucher.getId());
        debitDetail.setLineNo(1);
        debitDetail.setSubjectCode(debitSubjectCode);
        debitDetail.setSubjectName(debitSubjectName);
        debitDetail.setSummary("付款 - " + payment.getCustomerName());
        debitDetail.setDebitAmount(payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO);
        debitDetail.setCreditAmount(BigDecimal.ZERO);
        debitDetail.setCustomerCode(payment.getCustomerCode());
        accVoucherDetailMapper.insert(debitDetail);

        AccVoucherDetail creditDetail = new AccVoucherDetail();
        creditDetail.setVoucherId(voucher.getId());
        creditDetail.setLineNo(2);
        creditDetail.setSubjectCode(BANK_SUBJECT_CODE);
        creditDetail.setSubjectName(bankSubjectName);
        creditDetail.setSummary("付款 - " + payment.getCustomerName());
        creditDetail.setDebitAmount(BigDecimal.ZERO);
        creditDetail.setCreditAmount(payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO);
        creditDetail.setCustomerCode(payment.getCustomerCode());
        accVoucherDetailMapper.insert(creditDetail);

        log.info("付款单 {} 自动生成凭证成功，凭证号：{}", payment.getPaymentNo(), voucherNo);
        return voucher;
    }

    private String getReceiptCreditSubject(Integer businessType) {
        if (businessType == null) return RECEIPT_SUBJECT_CODES[0];
        switch (businessType) {
            case 1: return RECEIPT_SUBJECT_CODES[0];
            case 2: return RECEIPT_SUBJECT_CODES[1];
            case 3: return RECEIPT_SUBJECT_CODES[2];
            default: return RECEIPT_SUBJECT_CODES[0];
        }
    }

    private String getPaymentDebitSubject(Integer businessType) {
        if (businessType == null) return PAYMENT_SUBJECT_CODES[0];
        switch (businessType) {
            case 1: return PAYMENT_SUBJECT_CODES[0];
            case 2: return PAYMENT_SUBJECT_CODES[1];
            case 3: return PAYMENT_SUBJECT_CODES[2];
            default: return PAYMENT_SUBJECT_CODES[0];
        }
    }

    private String getSubjectName(String subjectCode) {
        AccAccountSubject subject = accAccountSubjectMapper.selectOne(
                new LambdaQueryWrapper<AccAccountSubject>()
                        .eq(AccAccountSubject::getSubjectCode, subjectCode)
                        .last("LIMIT 1"));
        return subject != null ? subject.getSubjectName() : subjectCode;
    }

    private String generateVoucherNo(String voucherDate) {
        LocalDate date = LocalDate.parse(voucherDate);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PZ" + dateStr;

        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(AccVoucher::getVoucherNo, prefix)
                .orderByDesc(AccVoucher::getVoucherNo)
                .last("LIMIT 1");
        AccVoucher lastVoucher = accVoucherMapper.selectOne(wrapper);

        if (lastVoucher == null) {
            return prefix + "001";
        }

        String lastNo = lastVoucher.getVoucherNo();
        int seq = Integer.parseInt(lastNo.substring(lastNo.length() - 3)) + 1;
        return prefix + String.format("%03d", seq);
    }
}
