package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nc_sync_error")
public class NcSyncError extends BaseEntity {
    private Long logId; // 关联同步日志ID
    private String syncType;
    private String businessKey;
    private String errorType; // AUTH/NETWORK/DATA_FORMAT/BUSINESS_RULE/NC_ERROR
    private String errorCode;
    private String errorMessage;
    private String ncErrorResponse;
    private Integer status; // 0-待重试 1-已解决 2-已忽略 3-最终失败
    private Integer retryCount;
    private Integer maxRetryCount;
}
