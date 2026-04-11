package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dashboard_business_type")
public class BusinessTypeData extends BaseEntity {

    private String type;
    private BigDecimal value;
    private BigDecimal percentage;
}