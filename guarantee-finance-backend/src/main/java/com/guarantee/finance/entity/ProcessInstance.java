package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("process_instance")
public class ProcessInstance extends BaseEntity {

    private Long definitionId;
    private Long businessKey;
    private String businessType;
    private Integer status; // 0-进行中 1-待审批 2-已通过 3-已驳回 4-已撤回
    private Integer currentNodeIndex;
    private Long currentNodeId;
    private String currentNodeName;
    private String currentApprover;
    private Long initiatorId;
    private String initiatorName;
    private LocalDateTime initiatorTime;
    
    // 非数据库字段
    private String definitionName;
}
