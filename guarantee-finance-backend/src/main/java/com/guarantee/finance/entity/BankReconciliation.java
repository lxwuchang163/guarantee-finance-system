package com.guarantee.finance.entity;

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
    private String bankAccountNo; // 银行账号
    private LocalDate reconciliationDate; // 对账日期
    private BigDecimal bankBalance; // 银行账面余额
    private BigDecimal bookBalance; // 企业账面余额
    private BigDecimal adjustedBankBalance; // 调节后银行余额
    private BigDecimal adjustedBookBalance; // 调节后企业余额
    private Integer matchCount; // 已匹配笔数
    private Integer unmatchedBankCount; // 未匹配银行笔数
    private Integer unmatchedBookCount; // 未匹配企业笔数
    private BigDecimal unmatchedBankAmount; // 未匹配银行金额
    private BigDecimal unmatchedBookAmount; // 未匹配企业金额
    private Integer status; // 0-草稿 1-已确认
}
