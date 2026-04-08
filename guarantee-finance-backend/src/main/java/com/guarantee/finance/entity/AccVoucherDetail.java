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
@TableName("acc_voucher_detail")
public class AccVoucherDetail extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long voucherId; // 凭证ID
    private Integer lineNo; // 行号
    private String subjectCode; // 科目编码
    private String subjectName; // 科目名称
    private String summary; // 摘要
    private BigDecimal debitAmount; // 借方金额
    private BigDecimal creditAmount; // 贷方金额
    private String auxiliaryInfo; // 辅助核算信息（json格式）
    private String departmentCode; // 部门编码
    private String projectCode; // 项目编码
    private String customerCode; // 客户编码
    private String supplierCode; // 供应商编码
    private String businessCode; // 业务编码
    private String bankCode; // 银行编码
    private String remark; // 备注
}
