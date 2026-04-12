package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("acc_report_template")
public class AccReportTemplate extends BaseEntity {
    private String templateCode;
    private String templateName;
    private String reportType;
    private String templateContent;
    private Integer isDefault;
    private Integer status;
}
