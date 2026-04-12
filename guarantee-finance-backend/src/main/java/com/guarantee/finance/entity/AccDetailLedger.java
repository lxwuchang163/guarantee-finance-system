package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_detail_ledger")
public class AccDetailLedger extends BaseEntity {
    private String subjectCode;
    private String subjectName;
    private String period;
    private Long voucherId;
    private String voucherNo;
    private String voucherDate;
    private String summary;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String direction;
    private BigDecimal balance;
    private String auxiliaryInfo;
    private String customerCode;
    private String departmentCode;
    private String projectCode;
    private Integer year;
    private Integer month;
}
