package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nc_sync_log")
public class NcSyncLog extends BaseEntity {
    private String syncType; // CUSTOMER/VOUCHER
    private String businessKey; // 业务主键（客户编码或凭证号）
    private String businessData; // 同步数据JSON
    private String ncResponse; // NC返回结果JSON
    private Integer status; // 0-待同步 1-同步中 2-成功 3-失败 4-部分成功
    private String errorMessage;
    private Integer retryCount;
    private Long durationMs;
    private String syncTime;
}
