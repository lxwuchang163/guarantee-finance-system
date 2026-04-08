package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NcSyncLogVO {
    private Long id;
    private String syncType; // CUSTOMER/VOUCHER
    private String businessKey;
    private Integer status; // 0-待同步 1-同步中 2-成功 3-失败 4-部分成功
    private String statusText;
    private String errorMessage;
    private Integer retryCount;
    private Long durationMs;
    private String syncTime;
    private LocalDateTime createTime;
}
