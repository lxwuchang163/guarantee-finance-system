package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher_template")
public class AccVoucherTemplate extends BaseEntity {
    private String templateCode; // 模板编码
    private String templateName; // 模板名称
    private Integer businessType; // 1-保费收入 2-分担收入 3-追偿到款 4-退费支出 5-代偿支出 6-追回分配
    private String voucherType; // 收款/付款/转账
    private String summaryTemplate; // 摘要模板（支持变量{客户名称}{合同编号}等）
    private String debitAccountRule; // 借方科目规则（JSON）
    private String creditAccountRule; // 贷方科目规则（JSON）
    private String auxiliaryRule; // 辅助核算规则（JSON）
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
}
