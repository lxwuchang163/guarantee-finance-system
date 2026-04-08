package com.guarantee.finance.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String username;

    private String nickname;

    private String phone;

    private Long orgId;

    private Integer status;

    private Integer sex;
}
