package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher")
public class AccVoucher extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String voucherNo;
    private String voucherDate;
    private String period;
    private String summary;
    private Integer status;
    private Long createUserId;
    private Long approveUserId;
    private Long postUserId;
    private String auditStatus;
    private String auditOpinion;
    private Integer voucherType;
    private String sourceType;
    private String sourceId;
    private String ncSyncStatus;

    @TableField(exist = false)
    private String remark;

    private Integer year;
    private Integer month;
}
