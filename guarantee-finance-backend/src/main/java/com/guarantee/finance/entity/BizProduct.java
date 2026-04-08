package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product")
public class BizProduct extends BaseEntity {
    private String productCode;
    private String productName;
    private Long parentId; // 父级品种ID
    private Integer productType; // 1-融资担保 2-非融资担保 3-其他
    private Double baseRate; // 基准费率
    private Double minRate; // 最低费率
    private Double maxRate; // 最高费率
    private Integer minTermMonths; // 最短期限（月）
    private Integer maxTermMonths; // 最长期限（月）
    private String guaranteeType; // 担保方式，逗号分隔
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
}
