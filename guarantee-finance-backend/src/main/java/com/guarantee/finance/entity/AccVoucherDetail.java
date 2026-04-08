package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher_detail")
public class AccVoucherDetail extends BaseEntity {
    private Long voucherId; // 凭证ID
    private String voucherNo; // 凭证编号
    private Integer lineNo; // 行号
    private String accountCode; // 科目编码
    private String accountName; // 科目名称
    private String direction; // DEBIT/CREDIT
    private BigDecimal amount; // 金额
    private BigDecimal debitAmount; // 借方金额
    private BigDecimal creditAmount; // 贷方金额
    private String summary; // 摘要
    private String customerCode; // 客户编码
    private String departmentCode; // 部门编码
    private String projectCode; // 项目编码
}
