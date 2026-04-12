package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher_audit")
public class AccVoucherAudit extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long voucherId;
    private String voucherNo;
    private Integer auditType;
    private Long auditorId;
    private String auditorName;
    private Integer auditResult;
    private String auditOpinion;
    private String auditTime; // 审核时间
    private Integer auditLevel; // 审核级别：1-一级审核 2-二级审核
    private String auditStatus; // 审核状态：0-待审核 1-审核中 2-已完成
}
