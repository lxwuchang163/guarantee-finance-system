package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversionTemplateVO {

    private Long id;
    private String templateCode;
    private String templateName;
    private String businessType;
    private String businessTypeText;
    private String orgCode;
    private String description;
    private String templateContent;
    private Integer status;
    private String statusText;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
