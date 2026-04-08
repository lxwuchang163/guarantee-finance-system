package com.guarantee.finance.dto;

import lombok.Data;

@Data
public class AuxiliaryDimensionDTO {

    private String dimensionCode;
    private String dimensionName;
    private String dimensionType;
    private String description;
    private Integer sortOrder;
    private String parentCode;
    private Integer dimensionLevel;
}
