package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_receipt_recovery")
public class FinReceiptRecovery extends BaseEntity {
    private Long receiptId; // 关联收款单ID
    private String receiptNo;
    private String compensationNo; // 代偿单号
    private String recoveryTargetCode; // 追偿对象编码
    private String recoveryTargetName; // 追偿对象名称
    private Integer recoveryMethod; // 1-协商还款 2-诉讼执行 3-资产处置 4-其他
    private BigDecimal currentAmount; // 本次追偿金额
    private BigDecimal totalAmount; // 累计追偿金额
    private BigDecimal principalBalance; // 代偿本金余额
    private BigDecimal totalBalance; // 代偿余额（含利息费用）
    private BigDecimal recoveryFee; // 追偿费用（律师费/诉讼费等）
}
