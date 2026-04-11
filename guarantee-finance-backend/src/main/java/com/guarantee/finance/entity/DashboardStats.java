package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dashboard_stats")
public class DashboardStats extends BaseEntity {

    private BigDecimal todayIncome;
    private BigDecimal todayExpense;
    private Integer pendingDocuments;
    private Integer monthlyReconciliation;
    private BigDecimal reconciliationRate;
    private Map<String, Object> changeRates;
}