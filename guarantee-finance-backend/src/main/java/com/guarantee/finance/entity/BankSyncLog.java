package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_sync_log")
public class BankSyncLog extends BaseEntity {
    private Long accountId;
    private String syncType;
    private String syncStatus;
    private String requestData;
    private String responseData;
    private String errorMessage;
    private Long durationMs;
}
