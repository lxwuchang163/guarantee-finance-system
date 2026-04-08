package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubjectBalanceDTO {

    private String subjectCode;
    private String period;
    private BigDecimal beginDebit;
    private BigDecimal beginCredit;
    private BigDecimal currentDebit;
    private BigDecimal currentCredit;
    private BigDecimal endDebit;
    private BigDecimal endCredit;
    private String auxiliaryInfo;
    private Integer year;
    private Integer month;
    private String status;
}
