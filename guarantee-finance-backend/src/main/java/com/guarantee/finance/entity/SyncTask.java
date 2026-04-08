package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sync_task")
public class SyncTask extends BaseEntity {
    private String taskCode; // 任务编码
    private String taskName; // 任务名称
    private String taskType; // CUSTOMER/PRODUCT/ACCOUNT/ORG
    private String syncMode; // FULL/INCREMENTAL/REALTIME
    private String cronExpression; // Cron表达式
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
    private Integer lastSuccessCount;
    private Integer lastFailCount;
    private String lastErrorMessage;
    private Long executeDurationMs; // 执行耗时(毫秒)
}
