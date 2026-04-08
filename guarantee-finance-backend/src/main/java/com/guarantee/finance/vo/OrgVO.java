package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrgVO {

    private Long id;

    private String orgName;

    private String orgCode;

    private Long parentId;

    private String parentName;

    private Integer level;

    private String leader;

    private String phone;

    private String address;

    private Integer status;

    private Integer sortOrder;

    private List<OrgVO> children;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private String remark;
}
