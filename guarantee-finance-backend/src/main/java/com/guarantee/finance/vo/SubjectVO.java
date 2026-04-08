package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubjectVO {

    private Long id;
    private String subjectCode;
    private String subjectName;
    private Integer subjectLevel;
    private String parentCode;
    private Integer subjectType;
    private Integer balanceDirection;
    private Integer status;
    private String auxiliaryDimension;
    private String description;
    private Integer sortOrder;
    private String category;
    private String systemType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SubjectVO> children;
}
