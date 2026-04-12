package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_financial_report")
public class AccFinancialReport extends BaseEntity {
    private String reportCode;
    private String reportName;
    private String reportType;
    private String period;
    private Integer year;
    private Integer month;
    private String reportData;
    private String status;
    private Long confirmUserId;
    private String confirmUserName;
    private LocalDateTime confirmTime;
    private Long approveUserId;
    private String approveUserName;
    private LocalDateTime approveTime;
}
