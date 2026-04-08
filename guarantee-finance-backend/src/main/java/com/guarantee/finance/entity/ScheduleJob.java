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
@TableName("schedule_job")
public class ScheduleJob extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String jobName; // 任务名称
    private String jobGroup; // 任务分组
    private String beanName; // 执行Bean名称
    private String methodName; // 执行方法名
    private String methodParams; // 方法参数(JSON)
    private String cronExpression; // Cron表达式
    private Integer misfirePolicy; // 失败策略 0-默认 1-立即执行 2-执行一次 3-放弃执行
    private Integer concurrent; // 并发策略 0-允许 1-禁止
    private String status; // 状态 0-暂停 1-正常
    private LocalDateTime nextExecuteTime; // 下次执行时间
    private LocalDateTime prevExecuteTime; // 上次执行时间
    private String remark;
}
