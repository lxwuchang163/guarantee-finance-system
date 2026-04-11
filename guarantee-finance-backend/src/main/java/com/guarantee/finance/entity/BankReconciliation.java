package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_reconciliation")
public class BankReconciliation extends BaseEntity {
    private String accountNo;
    private LocalDate reconciliationDate;
    private BigDecimal beginBalance;
    private BigDecimal endBalance;
    private BigDecimal bankDebit;
    private BigDecimal bankCredit;
    private BigDecimal bookDebit;
    private BigDecimal bookCredit;
    private BigDecimal unmatchedDebit;
    private BigDecimal unmatchedCredit;
    private Integer status;

    @TableField(exist = false)
    private String bankAccountNo;

    @TableField(exist = false)
    private BigDecimal bankBalance;

    @TableField(exist = false)
    private BigDecimal bookBalance;

    @TableField(exist = false)
    private BigDecimal adjustedBankBalance;

    @TableField(exist = false)
    private BigDecimal adjustedBookBalance;

    @TableField(exist = false
    )
    private Integer matchCount;

    @TableField(exist = false)
    private Integer unmatchedBankCount;

    @TableField(exist = false)
    private Integer unmatchedBookCount;

    @TableField(exist = false)
    private BigDecimal unmatchedBankAmount;

    @TableField(exist = false)
    private BigDecimal unmatchedBookAmount;
}
