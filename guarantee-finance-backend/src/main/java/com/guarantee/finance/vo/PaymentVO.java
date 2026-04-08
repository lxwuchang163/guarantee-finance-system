package com.guarantee.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private Long id;
    private String paymentNo;
    private Integer businessType;
    private String businessTypeName;
    private String customerCode;
    private String customerName;
    private String contractNo;
    private String originalReceiptNo;
    private String currency;
    private BigDecimal amount;
    private String amountInWords;
    private LocalDate paymentDate;
    private LocalDate actualPaymentDate;
    private Integer paymentMethod;
    private String payeeName;
    private String payeeAccountNo;
    private String payeeBankName;
    private String payerAccountNo;
    private String payerBankName;
    private String usage;
    private Long voucherId;
    private String voucherNo;
    private Integer status; // 0-草稿 1-已提交 2-已审核 3-已付款 4-已记账 5-已作废
    private Long makerId;
    private String makerName;
    private LocalDateTime makerTime;
    private Long auditorId;
    private String auditorName;
    private LocalDateTime auditorTime;
    private Long posterId;
    private String posterName;
    private LocalDateTime posterTime;
    private LocalDateTime createTime;
}
