package com.guarantee.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class DataScopeVO {
    private Long roleId;
    private String dataScope; // ALL, ORG_AND_CHILD, ORG_ONLY, CUSTOM, SELF
    private List<String> orgCodes; // 自定义数据权限时指定的机构编码列表
}
