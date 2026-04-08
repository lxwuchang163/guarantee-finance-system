package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sync_error_record")
public class SyncErrorRecord extends BaseEntity {
    private Long taskId; // 关联任务ID
    private Long logId; // 关联日志ID
    private String taskType; // CUSTOMER/PRODUCT/ACCOUNT/ORG
    private String businessKey; // 业务主键（如客户编码）
    private String businessData; // 原始数据JSON
    private String errorType; // VALIDATE/DUPLICATE/SYSTEM
    private String errorMessage;
    private Integer retryCount; // 已重试次数
    private Integer maxRetryCount; // 最大重试次数
    private Integer status; // 0-待重试 1-已解决 2-已忽略 3-最终失败
}
