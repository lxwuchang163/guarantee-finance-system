package com.guarantee.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class OrgTreeVO {

    private Long id;

    private String label;

    private String orgCode;

    private Integer level;

    private Integer status;

    private List<OrgTreeVO> children;
}
