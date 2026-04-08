package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product_rate")
public class BizProductRate extends BaseEntity {
    private Long productId; // 关联品种ID
    private String productCode;
    private String versionNo; // 版本号
    private Double baseRate;
    private Double minRate;
    private Double maxRate;
    private String effectiveDate; // 生效日期
    private String expiryDate; // 失效日期
    private Integer status; // 0-失效 1-生效
    private String remark;
}
