package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoucherGenerateDTO {
    private Long sourceBillId; // 来源单据ID（收款单/付款单）
    private String sourceBillType; // RECEIPT / PAYMENT
    private String sourceBillNo;
    private String voucherType; // 收款/付款/转账
    private String customerCode;
    private String customerName;
    private BigDecimal amount;
    private String summaryTemplate;
}
