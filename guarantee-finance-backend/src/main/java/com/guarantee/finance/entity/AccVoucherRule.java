package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher_rule")
public class AccVoucherRule extends BaseEntity {
    private String ruleCode;
    private String ruleName;
    private String businessType;
    private String businessSubtype;
    private Integer voucherType;
    private String summaryTemplate;
    private Integer priority;
    private Integer status;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
}
