package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_transaction")
public class BankTransaction extends BaseEntity {
    private String transactionNo;
    private String bankAccountNo;
    private String bankName;
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private Integer transactionType;
    private String counterAccountNo;
    private String counterName;
    private String counterBankName;
    private BigDecimal amount;
    private String currency;
    private BigDecimal exchangeRate;
    private String summary;
    private String remark;
    private String bankVoucherNo;
    private Integer matchStatus;
    private Long matchedBillId;
    private String matchedBillType;
    private String dataSource;
    private Long reconciliationId;
    private String matchRule;
    private BigDecimal matchScore;
    private LocalDateTime matchTime;
}
