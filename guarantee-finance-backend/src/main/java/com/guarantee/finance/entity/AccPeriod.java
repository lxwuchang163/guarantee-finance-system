package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_period")
public class AccPeriod extends BaseEntity {
    private String periodCode;
    private String periodName;
    private Integer year;
    private Integer month;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer isCurrent;
    private String closingType;
    private LocalDateTime closingTime;
    private Long closingUserId;
    private String closingUserName;
}
