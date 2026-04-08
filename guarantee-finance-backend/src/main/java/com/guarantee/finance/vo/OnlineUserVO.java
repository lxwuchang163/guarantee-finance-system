package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OnlineUserVO {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private String ipaddr;

    private String loginLocation;

    private String browser;

    private String os;

    private LocalDateTime loginTime;
}
