package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_conversion_template")
public class AccConversionTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateCode; // 模板编码
    private String templateName; // 模板名称
    private String businessType; // 业务类型：receipt-收款 payment-付款 refund-退款 compensation-代偿
    private String orgCode; // 机构编码
    private String description; // 模板描述
    private String templateContent; // 模板内容（JSON格式）
    private Integer status; // 状态：0-停用 1-启用
    private Integer sortOrder; // 排序
}
