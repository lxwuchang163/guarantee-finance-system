package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer")
public class BizCustomer extends BaseEntity {
    private String customerCode;
    private String customerName;
    private String customerShortName;
    private Integer customerType; // 1-个人 2-企业 3-机构
    private String creditCode;
    private String idCard;
    private String contactPhone;
    private String contactPerson;
    private String registerAddress;
    private String businessAddress;
    private String industry;
    private Integer customerLevel; // 1-AAA 2-AA 3-A 4-B
    private Integer status; // 0-禁用 1-启用
    private String sourceSystem; // 来源系统标识
    private String lastSyncTime; // 最后同步时间
}
