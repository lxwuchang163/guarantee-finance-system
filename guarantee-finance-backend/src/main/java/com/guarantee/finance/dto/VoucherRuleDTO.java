package com.guarantee.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VoucherRuleDTO {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String businessType;
    private String businessSubtype;
    private Integer voucherType;
    private String summaryTemplate;
    private Integer priority;
    private Integer status;
    private String remark;
    private List<EntryDTO> entries;

    @Data
    public static class EntryDTO {
        private Long id;
        private Integer lineNo;
        private String entrySide;
        private String subjectSource;
        private String subjectCode;
        private String subjectFormula;
        private String amountSource;
        private String amountField;
        private String amountFormula;
        private BigDecimal amountFixed;
        private String summaryTemplate;
        private String auxiliaryRule;
    }
}
