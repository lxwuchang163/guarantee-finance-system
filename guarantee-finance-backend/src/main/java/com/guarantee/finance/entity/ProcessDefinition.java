package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("process_definition")
public class ProcessDefinition extends BaseEntity {

    private String processCode;
    private String processName;
    private String processType; // receipt/payment/compensation等
    private Integer status; // 0-禁用 1-启用
    private Integer version;
    private String description;
}
