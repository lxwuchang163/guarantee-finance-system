package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer sex;

    private Long orgId;

    private String orgName;

    private Integer status;

    private List<RoleSimpleVO> roles;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private String remark;
}
