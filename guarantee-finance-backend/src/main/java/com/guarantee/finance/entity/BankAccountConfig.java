package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_account_config")
public class BankAccountConfig extends BaseEntity {
    private String bankCode;
    private String bankName;
    private String accountNo;
    private String accountName;

    @TableField(exist = false)
    private String bankBranchName;

    @TableField(exist = false)
    private String bankLineNo;

    private Integer accountType;
    private String currency;

    @TableField(exist = false)
    private Boolean isMainAccount;

    private BigDecimal dailyLimit;
    private BigDecimal singleLimit;
    private BigDecimal thresholdWarning;
    private BigDecimal thresholdStop;
    private String apiEndpoint;
    private Integer apiStatus;
    private Long orgId;
    private BigDecimal balance;
    private LocalDateTime lastSyncTime;
    private Integer status;
}
