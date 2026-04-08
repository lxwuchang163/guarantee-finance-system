package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReceiptDTO {
    private Long id;
    private Integer businessType; // 1-保费收入 2-分担收入 3-追偿到款
    private String customerCode;
    private String customerName;
    private String contractNo;
    private String projectName;
    private String productCode;
    private String productName;
    private String currency; // 默认CNY
    private BigDecimal amount;
    private LocalDate receiptDate;
    private LocalDate actualArrivalDate;
    private Integer paymentMethod; // 1-转账 2-现金 3-支票 4-汇票 5-其他
    private String payerName;
    private String payerAccountNo;
    private String payerBankName;
    private String payeeAccountNo;
    private String payeeBankName;
    private String usage;

    // 分担收入特有字段
    private String sharerCode;
    private String sharerName;
    private BigDecimal shareRatio;
    private BigDecimal originalAmount;

    // 追偿到款特有字段
    private String compensationNo;
    private String recoveryTargetCode;
    private String recoveryTargetName;
    private Integer recoveryMethod;
}
