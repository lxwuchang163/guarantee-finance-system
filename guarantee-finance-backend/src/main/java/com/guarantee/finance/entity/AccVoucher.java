package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher")
public class AccVoucher extends BaseEntity {
    private String voucherNo; // 凭证编号
    private String voucherDate; // 凭证日期
    private Integer voucherType; // 1-收款 2-付款 3-转账
    private String voucherTypeName; // 凭证类型名称
    private String accountingPeriod; // 会计期间
    private String companyCode; // 公司编码
    private String maker; // 制单人
    private String auditor; // 审核人
    private String poster; // 记账人
    private Integer attachmentCount; // 附件张数
    private java.math.BigDecimal totalDebit; // 借方合计
    private java.math.BigDecimal totalCredit; // 贷方合计
    private Integer status; // 0-未审核 1-已审核 2-已记账 3-已作废
    private String remark; // 备注
    private String sourceBillType; // 来源单据类型（RECEIPT/PAYMENT）
    private String sourceBillNo; // 来源单据号
    private Integer ncSyncStatus; // 0-未同步 1-已同步 2-同步失败
}
