package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_payment")
public class FinPayment extends BaseEntity {
    private String paymentNo; // 付款单号 FK+年月日+序号
    private Integer businessType; // 1-退费支出 2-代偿支出 3-追回资金分配
    private String businessTypeName;
    private String customerCode;
    private String customerName;
    private String contractNo; // 合同编号
    private String originalReceiptNo; // 原收款单号（退费时关联）
    private String currency;
    private BigDecimal amount;
    private String amountInWords;
    private LocalDate paymentDate;
    private LocalDate actualPaymentDate;
    private Integer paymentMethod; // 1-转账 2-现金 3-支票 4-汇票 5-其他
    private String payeeName; // 收款方名称
    private String payeeAccountNo; // 收款方账号
    private String payeeBankName; // 收款方开户行
    private String payerAccountNo; // 付款方账号
    private String payerBankName; // 付款方开户行
    private String usage;
    private Long voucherId;
    private String voucherNo;
    private Integer status; // 0-草稿 1-已提交 2-已审核 3-已付款 4-已记账 5-已作废
    private Long makerId;
    private String makerName;
    private LocalDateTime makerTime;
    private Long auditorId;
    private String auditorName;
    private LocalDateTime auditorTime;
    private Long posterId;
    private String posterName;
    private LocalDateTime posterTime;
}
