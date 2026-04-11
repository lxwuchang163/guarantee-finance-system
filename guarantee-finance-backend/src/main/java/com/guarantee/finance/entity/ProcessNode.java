package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("process_node")
public class ProcessNode extends BaseEntity {

    private Long definitionId;
    private String nodeName;
    private String nodeType;
    private String approverType; // role/user/specific
    private String approverValue; // 角色编码或用户ID
    private Integer sortOrder;
    
    @TableField(exist = false)
    private String remark;
}
