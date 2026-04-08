package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    private String orgName;
    private String orgCode;
    private Long parentId;
    private Integer level;
    private String leader;
    private String phone;
    private String address;
    private Integer status;
    private Integer sortOrder;
}
