package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_wechat_user")
public class SysWechatUser {

    private Long id;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private Integer sex;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}