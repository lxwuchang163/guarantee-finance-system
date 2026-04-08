package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_receipt_share")
public class FinReceiptShare extends BaseEntity {
    private Long receiptId; // 关联收款单ID
    private String receiptNo;
    private String sharerCode; // 分担方编码
    private String sharerName; // 分担方名称
    private BigDecimal shareRatio; // 分担比例（%）
    private BigDecimal originalAmount; // 原担保金额
    private BigDecimal shareAmount; // 分担金额
    private BigDecimal remainingAmount; // 剩余自留金额
    private String agreementNo; // 分担协议编号
    private Integer shareType; // 1-再担保分入 2-联合担保分成 3-其他
}
