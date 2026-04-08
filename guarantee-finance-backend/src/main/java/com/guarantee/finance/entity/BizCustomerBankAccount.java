package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer_bank_account")
public class BizCustomerBankAccount extends BaseEntity {
    private Long customerId;
    private String customerCode;
    private String bankName;
    private String bankBranchName;
    private String bankAccountNo;
    private String bankLineNo;
    private Integer accountType; // 1-基本户 2-一般户 3-专用户 4-临时户
    private Integer isDefault; // 0-否 1-是
    private Integer isMainSettlement; // 0-否 1-是
    private Integer status; // 1-正常 2-冻结 3-销户
    private java.time.LocalDate openDate;
    private java.time.LocalDate closeDate;
}
