package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approve_record")
public class ApproveRecord extends BaseEntity {

    private Long instanceId;
    private Long nodeId;
    private String nodeName;
    private Long approverId;
    private String approverName;
    private String opinion;
    private String result; // approve/reject
    private LocalDateTime approveTime;
}
