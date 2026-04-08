package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CfcaSignDTO {
    private Long paymentId; // 付款单ID
    private String paymentNo; // 付款单号
    private BigDecimal amount; // 支付金额
    private String payeeName; // 收款方
    private String payeeAccountNo; // 收款方账号
    private String payerAccountNo; // 付款方账号
    private Integer signLevel; // 1-单人 2-双人 3-三人
}
