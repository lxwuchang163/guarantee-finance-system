package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dashboard_income_expense")
public class IncomeExpenseData extends BaseEntity {

    private String month;
    private BigDecimal income;
    private BigDecimal expense;
}
