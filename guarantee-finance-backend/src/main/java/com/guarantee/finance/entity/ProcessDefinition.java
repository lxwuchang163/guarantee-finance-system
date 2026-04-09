package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("process_definition")
public class ProcessDefinition extends BaseEntity {

    private String name;
    private String businessType;
    private Integer status; // 0-禁用 1-启用
    private Integer version;
    private String description;
    
    // 非数据库字段
    private List<ProcessNode> nodes;
}
