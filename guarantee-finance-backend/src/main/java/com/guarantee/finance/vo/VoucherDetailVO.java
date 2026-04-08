package com.guarantee.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherDetailVO {

    private Long id;
    private Long voucherId;
    private Integer lineNo;
    private String subjectCode;
    private String subjectName;
    private String summary;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String auxiliaryInfo;
    private String departmentCode;
    private String projectCode;
    private String customerCode;
    private String supplierCode;
    private String businessCode;
    private String bankCode;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
