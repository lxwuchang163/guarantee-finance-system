package com.guarantee.finance.dto;

import lombok.Data;

@Data
public class SubjectDTO {

    private String subjectCode;
    private String subjectName;
    private Integer subjectLevel;
    private String parentCode;
    private Integer subjectType;
    private Integer balanceDirection;
    private String auxiliaryDimension;
    private String description;
    private Integer sortOrder;
    private String category;
    private String systemType;
}
