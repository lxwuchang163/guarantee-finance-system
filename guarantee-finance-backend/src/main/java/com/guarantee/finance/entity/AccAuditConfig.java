package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_audit_config")
public class AccAuditConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orgCode;
    private Integer auditType;
    private Integer status;
}
