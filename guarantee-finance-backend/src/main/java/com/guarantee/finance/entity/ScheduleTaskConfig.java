package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_task_config")
public class ScheduleTaskConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode; // 内置任务编码
    private String taskName; // 任务名称
    private String description; // 任务描述
    private String cronExpression; // 默认Cron表达式
    private Integer executeOrder; // 执行顺序
    private String status; // 启用状态 0-禁用 1-启用
    private String lastExecuteTime; // 上次执行时间
    private String nextExecuteTime; // 下次执行时间
    private Integer successCount; // 成功次数
    private Integer failCount; // 失败次数
    private String remark;
}
