package com.guarantee.finance.dto;

import lombok.Data;

@Data
public class ConversionTemplateDTO {

    private String templateCode;
    private String templateName;
    private String businessType;
    private String orgCode;
    private String description;
    private String templateContent;
    private Integer sortOrder;
}
