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

    private String instanceNo;
    private Long definitionId;
    private String businessType;
    private String businessNo;
    private Long currentNodeId;
    private String currentNodeName;
    private Integer status; // 0-进行中 1-已通过 2-已驳回 3-已撤回
    private Long initiatorId;
    private String initiatorName;
    private LocalDateTime initiatorTime;
}
