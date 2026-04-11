package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_sms_code")
public class SysSmsCode {

    private Long id;
    private String phone;
    private String code;
    private LocalDateTime expireTime;
    private Integer status; // 0-未使用,1-已使用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}