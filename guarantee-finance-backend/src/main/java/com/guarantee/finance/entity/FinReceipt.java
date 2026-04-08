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
@TableName("fin_receipt")
public class FinReceipt extends BaseEntity {
    private String receiptNo; // 收款单号 SK+年月日+序号
    private Integer businessType; // 1-保费收入 2-分担收入 3-追偿到款
    private String businessTypeName;
    private String customerCode;
    private String customerName;
    private String contractNo; // 合同编号
    private String projectName; // 项目名称
    private String productCode; // 业务品种编码
    private String productName;
    private String currency; // 币种
    private BigDecimal amount; // 收款金额
    private String amountInWords; // 大写金额
    private LocalDate receiptDate; // 收款日期
    private LocalDate actualArrivalDate; // 实际到账日期
    private Integer paymentMethod; // 1-转账 2-现金 3-支票 4-汇票 5-其他
    private String payerName; // 付款方名称
    private String payerAccountNo; // 付款方账号
    private String payerBankName; // 付款方开户行
    private String payeeAccountNo; // 收款方账号
    private String payeeBankName; // 收款方开户行
    private String usage; // 用途/摘要
    private Long voucherId; // 关联凭证ID
    private String voucherNo; // 凭证号
    private Integer status; // 0-草稿 1-已提交 2-已审核 3-已记账 4-已作废
    private Long makerId; // 制单人ID
    private String makerName; // 制单人姓名
    private LocalDateTime makerTime; // 制单时间
    private Long auditorId; // 审核人ID
    private String auditorName; // 审核人姓名
    private LocalDateTime auditorTime; // 审核时间
    private Long posterId; // 记账人ID
    private String posterName; // 记账人姓名
    private LocalDateTime posterTime; // 记账时间
}
