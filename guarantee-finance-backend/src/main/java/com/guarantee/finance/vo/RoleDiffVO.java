package com.guarantee.finance.vo;

import lombok.Data;

@Data
public class RoleDiffVO {

    private String field;

    private String fieldName;

    private String sourceValue;

    private String targetValue;

    private String diffType; // different, onlyInSource, onlyInTarget
}
