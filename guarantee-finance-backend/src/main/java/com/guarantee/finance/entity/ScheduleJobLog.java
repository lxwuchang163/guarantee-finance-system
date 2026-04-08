package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_job_log")
public class ScheduleJobLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId; // 任务ID
    private String jobName; // 任务名称
    private String jobGroup; // 任务分组
    private String invokeTarget; // 调用目标字符串
    private String jobMessage; // 日志信息
    private Integer status; // 执行状态 0-成功 1-失败
    private String exceptionInfo; // 异常信息
    private Long startTime; // 开始时间戳(ms)
    private Long endTime; // 结束时间戳(ms)
}
