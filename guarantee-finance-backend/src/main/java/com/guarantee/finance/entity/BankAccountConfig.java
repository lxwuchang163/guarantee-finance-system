package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_account_config")
public class BankAccountConfig extends BaseEntity {
    private String bankCode; // 银行编码（ICBC/CBC/ABC等）
    private String bankName; // 银行名称
    private String accountNo; // 账号
    private String accountName; // 户名
    private String bankBranchName; // 开户行名称
    private String bankLineNo; // 联行号
    private Integer accountType; // 1-基本户 2-一般户 3-专用户 4-临时户
    private String currency;
    private Boolean isMainAccount; // 是否主结算账户
    private BigDecimal dailyLimit; // 日累计限额
    private BigDecimal singleLimit; // 单笔限额
    private BigDecimal balanceThreshold; // 余额预警阈值
    private String apiEndpoint; // 银企直连接口地址
    private String status; // ACTIVE/DISABLED
}
