package com.guarantee.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubjectBalanceVO {

    private Long id;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
