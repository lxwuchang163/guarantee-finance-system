package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_customer_setting")
public class AccCustomerSetting extends BaseEntity {
    private String customerCode; // 客户编码
    private String customerName; // 客户名称
    private Integer enableAuxiliaryAccounting; // 是否参与辅助核算 0-否 1-是
    private String arAccountCode; // 应收账款科目编码
    private String apAccountCode; // 预收账款科目编码
    private String otherReceivableCode; // 其他应收款科目编码
    private String creditLimitLevel1; // 账龄区间1（天）
    private String creditLimitLevel2; // 账龄区间2（天）
    private String creditLimitLevel3; // 账龄区间3（天）
    private String creditLimitLevel4; // 账龄区间4（天）
    private BigDecimal creditLimitAmount; // 信用额度
}
