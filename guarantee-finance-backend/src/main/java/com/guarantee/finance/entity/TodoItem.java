package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dashboard_todo")
public class TodoItem extends BaseEntity {

    private String title;
    private String type;
    private String priority;
    private String status;
    private String description;
    private Long businessId;
    private String businessType;
    private String actionUrl;

    @TableField(exist = false)
    private String displayTime;
}
