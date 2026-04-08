package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_subject_balance")
public class AccSubjectBalance extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String subjectCode; // 科目编码
    private String period; // 会计期间
    private BigDecimal beginDebit; // 期初借方
    private BigDecimal beginCredit; // 期初贷方
    private BigDecimal currentDebit; // 本期借方
    private BigDecimal currentCredit; // 本期贷方
    private BigDecimal endDebit; // 期末借方
    private BigDecimal endCredit; // 期末贷方
    private String auxiliaryInfo; // 辅助核算信息（json格式）
    private Integer year; // 年份
    private Integer month; // 月份
    private String status; // 状态：0-初始化 1-已确认
}
