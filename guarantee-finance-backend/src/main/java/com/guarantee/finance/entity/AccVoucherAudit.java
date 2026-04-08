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

    private Long voucherId; // 凭证ID
    private String voucherNo; // 凭证编号
    private Long auditorId; // 审核人ID
    private String auditorName; // 审核人姓名
    private Integer auditResult; // 审核结果：1-通过 2-拒绝
    private String auditOpinion; // 审核意见
    private String auditTime; // 审核时间
    private Integer auditLevel; // 审核级别：1-一级审核 2-二级审核
    private String auditStatus; // 审核状态：0-待审核 1-审核中 2-已完成
}
