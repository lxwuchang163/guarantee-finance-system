package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoucherDetailDTO {

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
}
