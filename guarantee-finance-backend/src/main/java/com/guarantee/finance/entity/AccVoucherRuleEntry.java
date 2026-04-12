package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher_rule_entry")
public class AccVoucherRuleEntry extends BaseEntity {
    private Long ruleId;
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
