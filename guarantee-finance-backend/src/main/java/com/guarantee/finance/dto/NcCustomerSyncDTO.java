package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class NcCustomerSyncDTO {
    private Long id; // 本地客户ID
    private String customerCode; // 客户编码
    private String customerName; // 客户名称
    private String customerShortName; // 客户简称
    private Integer customerType; // 1-个人 2-企业 3-机构
    private String creditCode; // 统一社会信用代码
    private String industry; // 所属行业
    private String contactPhone; // 联系电话
    private String address; // 地址
    private Integer status; // 1-启用 0-停用
}
