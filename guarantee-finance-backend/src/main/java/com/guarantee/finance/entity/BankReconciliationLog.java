package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_reconciliation_log")
public class BankReconciliationLog extends BaseEntity {
    private Long reconciliationId;
    private String logType;
    private Long bankTransactionId;
    private Long bookBillId;
    private String bookBillType;
    private String matchRule;
    private BigDecimal matchScore;
    private Long operatorId;
    private String operatorName;
    private String description;
}
