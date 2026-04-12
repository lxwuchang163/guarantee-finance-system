package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubjectBalanceInitDTO {

    private String subjectCode;
    private String subjectName;
    private BigDecimal beginDebit;
    private BigDecimal beginCredit;
    private String period;
    private Integer year;
    private Integer month;
}
