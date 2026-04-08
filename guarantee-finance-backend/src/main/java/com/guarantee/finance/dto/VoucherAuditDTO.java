package com.guarantee.finance.dto;

import lombok.Data;

@Data
public class VoucherAuditDTO {

    private Long voucherId;
    private Integer auditResult; // 1-通过 2-拒绝
    private String auditOpinion;
    private Integer auditLevel; // 1-一级审核 2-二级审核
}
