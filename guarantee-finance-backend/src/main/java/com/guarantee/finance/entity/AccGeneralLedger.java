package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_general_ledger")
public class AccGeneralLedger extends BaseEntity {
    private String subjectCode;
    private String subjectName;
    private String period;
    private Integer year;
    private Integer month;
    private BigDecimal beginDebit;
    private BigDecimal beginCredit;
    private BigDecimal currentDebit;
    private BigDecimal currentCredit;
    private BigDecimal endDebit;
    private BigDecimal endCredit;
}
