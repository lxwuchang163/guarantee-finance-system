package com.guarantee.finance.dto;

import lombok.Data;

@Data
public class OrgQueryDTO {

    private String orgName;

    private String orgCode;

    private Long parentId;

    private Integer status;

    private Integer level;
}
