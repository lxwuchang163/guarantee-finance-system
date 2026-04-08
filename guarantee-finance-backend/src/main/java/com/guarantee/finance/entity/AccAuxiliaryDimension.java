package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_auxiliary_dimension")
public class AccAuxiliaryDimension extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dimensionCode; // 维度编码
    private String dimensionName; // 维度名称
    private String dimensionType; // 维度类型：dept-部门 project-项目 customer-客户 supplier-供应商 business-业务 bank-银行
    private String description; // 维度描述
    private Integer status; // 状态：0-停用 1-启用
    private Integer sortOrder; // 排序
    private String parentCode; // 父维度编码
    private Integer dimensionLevel; // 维度层级
}
