package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VoucherAuditVO {

    private Long id;
    private Long voucherId;
    private String voucherNo;
    private Long auditorId;
    private String auditorName;
    private Integer auditResult;
    private String auditResultText;
    private String auditOpinion;
    private String auditTime;
    private Integer auditLevel;
    private String auditLevelText;
    private String auditStatus;
    private String auditStatusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
