package com.guarantee.finance.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerVO {
    private Long id;
    private String customerCode;
    private String customerName;
    private String customerShortName;
    private Integer customerType;
    private String customerTypeName; // 1-个人 2-企业 3-机构
    private String creditCode;
    private String idCard;
    private String contactPhone;
    private String contactPerson;
    private String registerAddress;
    private String businessAddress;
    private String industry;
    private Integer customerLevel;
    private Integer status;
    private String sourceSystem;
    private String lastSyncTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
