package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sync_task_log")
public class SyncTaskLog extends BaseEntity {
    private Long taskId; // 关联任务ID
    private String taskCode;
    private String executeType; // MANUAL/SCHEDULED
    private Integer status; // 0-执行中 1-成功 2-失败 3-部分成功
    private Integer totalCount; // 总处理数
    private Integer successCount; // 成功数
    private Integer failCount; // 失败数
    private Long durationMs; // 执行耗时(毫秒)
    private String startTime;
    private String endTime;
    private String errorMessage;
}
