package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_account_subject")
public class AccAccountSubject extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String subjectCode; // 科目编码
    private String subjectName; // 科目名称
    private Integer subjectLevel; // 科目层级
    private String parentCode; // 父科目编码
    private Integer subjectType; // 科目类型：1-资产 2-负债 3-所有者权益 4-成本 5-损益
    private Integer balanceDirection; // 余额方向：1-借方 2-贷方
    private Integer status; // 状态：0-封存 1-启用
    private String auxiliaryDimension; // 辅助核算维度（json格式）
    private String description; // 科目描述
    private Integer sortOrder; // 排序
    private String category; // 科目类别
    private String systemType; // 系统类型：0-标准 1-自定义
}
