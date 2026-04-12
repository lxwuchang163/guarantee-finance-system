package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_carry_forward")
public class AccCarryForward extends BaseEntity {
    private String period;
    private String carryType;
    private String sourceSubjectCode;
    private String targetSubjectCode;
    private BigDecimal amount;
    private Long voucherId;
    private String voucherNo;
    private String status;
}
