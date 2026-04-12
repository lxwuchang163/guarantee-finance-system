package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dashboard_notice")
public class Notice extends BaseEntity {

    private String title;
    private String content;
    private Integer status;
    private String noticeType;
    private Integer topFlag;
    private LocalDateTime publishTime;
}