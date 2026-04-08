package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentDTO {
    private Long id;
    private Integer businessType; // 1-退费支出 2-代偿支出 3-追回资金分配
    private String customerCode;
    private String customerName;
    private String contractNo;
    private String originalReceiptNo;
    private String currency;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private LocalDate actualPaymentDate;
    private Integer paymentMethod;
    private String payeeName;
    private String payeeAccountNo;
    private String payeeBankName;
    private String payerAccountNo;
    private String payerBankName;
    private String usage;

    // 退费特有字段
    private Integer refundReason; // 1-提前解保 2-部分解除 3-费率调整 4-其他
    private BigDecimal refundRatio;
    private BigDecimal originalAmount;

    // 代偿特有字段
    private String creditorName;
    private String creditorAccountNo;
    private String creditorBankName;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal otherFeeAmount;
    private BigDecimal totalCompensationAmount;
    private Integer compensationBasisType; // 1-判决书 2-调解书 3-和解协议 4-其他

    // 追回分配特有字段
    private BigDecimal returnPrincipalAmount;
    private BigDecimal recoveryFeeAmount;
    private BigDecimal interestReturnAmount;
}
