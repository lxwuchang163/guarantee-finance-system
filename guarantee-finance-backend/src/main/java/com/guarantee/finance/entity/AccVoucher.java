package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_voucher")
public class AccVoucher extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String voucherNo; // 凭证编号
    private String voucherDate; // 凭证日期
    private String period; // 会计期间
    private String summary; // 摘要
    private Integer status; // 状态：0-草稿 1-已提交 2-已审核 3-已记账 4-已作废
    private Long createUserId; // 创建人
    private Long approveUserId; // 审核人
    private Long postUserId; // 记账人
    private String auditStatus; // 审核状态：0-待审核 1-已审核 2-审核拒绝
    private String auditOpinion; // 审核意见
    private Integer voucherType; // 凭证类型：1-记账凭证 2-收款凭证 3-付款凭证 4-转账凭证
    private String sourceType; // 来源类型：0-手工 1-自动 2-导入
    private String sourceId; // 来源ID
    private String ncSyncStatus; // NC同步状态：0-未同步 1-已同步 2-同步失败
    private String remark; // 备注
    private Integer year; // 年份
    private Integer month; // 月份
}
