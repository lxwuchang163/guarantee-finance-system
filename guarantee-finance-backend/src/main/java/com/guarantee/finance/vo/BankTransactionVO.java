package com.guarantee.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BankTransactionVO {
    private Long id;
    private String transactionNo;
    private String bankAccountNo;
    private String bankName;
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private Integer transactionType; // 1-收入 2-支出 3-其他
    private String transactionTypeName;
    private String counterAccountNo;
    private String counterName;
    private String counterBankName;
    private BigDecimal amount;
    private String currency;
    private String summary;
    private Integer matchStatus; // 0-未匹配 1-精确匹配 2-模糊匹配 3-异常
    private String matchStatusText;
}
