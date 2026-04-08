package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuxiliaryDimensionVO {

    private Long id;
    private String dimensionCode;
    private String dimensionName;
    private String dimensionType;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private String parentCode;
    private Integer dimensionLevel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AuxiliaryDimensionVO> children;
}
