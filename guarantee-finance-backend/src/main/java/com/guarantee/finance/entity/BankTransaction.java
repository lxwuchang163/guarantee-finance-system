package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bank_transaction")
public class BankTransaction extends BaseEntity {
    private String transactionNo; // 银行流水号
    private String bankAccountNo; // 本方账号
    private String bankName; // 开户银行
    private LocalDate transactionDate; // 交易日期
    private LocalDate valueDate; // 记账日期
    private Integer transactionType; // 1-收入 2-支出 3-其他
    private String counterAccountNo; // 对方账号
    private String counterName; // 对方名称
    private String counterBankName; // 对方开户行
    private BigDecimal amount; // 金额
    private String currency; // 币种
    private BigDecimal exchangeRate; // 汇率
    private String summary; // 摘要/用途
    private String remark; // 备注
    private String bankVoucherNo; // 银行凭证号
    private Integer matchStatus; // 0-未匹配 1-已匹配(精确) 2-已匹配(模糊) 3-异常
    private Long matchedBillId; // 关联的单据ID（收款单/付款单）
    private String matchedBillType; // RECEIPT/PAYMENT
    private String dataSource; // AUTO_DOWNLOAD/MANUAL_IMPORT/API_PUSH
}
