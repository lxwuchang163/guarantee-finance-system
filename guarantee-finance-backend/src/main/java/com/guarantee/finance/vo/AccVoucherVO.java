package com.guarantee.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccVoucherVO {
    private Long id;
    private String voucherNo;
    private String voucherDate;
    private Integer voucherType;
    private String voucherTypeName;
    private String accountingPeriod;
    private String companyCode;
    private String maker;
    private String auditor;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private Integer status; // 0-未审核 1-已审核 2-已记账 3-已作废
    private String statusText;
    private String sourceBillType;
    private String sourceBillNo;
    private Integer ncSyncStatus;
    private LocalDateTime createTime;
}
