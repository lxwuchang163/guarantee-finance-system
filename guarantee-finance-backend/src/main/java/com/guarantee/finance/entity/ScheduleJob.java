package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_task_config")
public class ScheduleJob extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskName;
    private String taskCode;
    private String taskType;
    private String cronExpression;
    private String taskClass;
    private String taskParams;
    private String status;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;

    @TableField(exist = false)
    private String jobName;

    @TableField(exist = false)
    private String jobGroup;

    @TableField(exist = false)
    private String beanName;

    @TableField(exist = false)
    private String methodName;

    @TableField(exist = false)
    private String methodParams;

    @TableField(exist = false)
    private Integer misfirePolicy;

    @TableField(exist = false)
    private Integer concurrent;

    @TableField(exist = false)
    private LocalDateTime nextExecuteTime;

    @TableField(exist = false)
    private LocalDateTime prevExecuteTime;
}
