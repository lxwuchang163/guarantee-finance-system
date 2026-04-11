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
@TableName("schedule_job_log")
public class ScheduleJobLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(exist = false)
    private Integer deleted;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Long createBy;

    @TableField(exist = false)
    private Long updateBy;

    @TableField(exist = false)
    private String remark;

    private Long taskId;
    private String taskName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private Integer status;
    private String result;
    private String errorMessage;
}
