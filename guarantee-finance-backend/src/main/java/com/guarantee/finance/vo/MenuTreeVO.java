package com.guarantee.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class MenuTreeVO {
    private Long id;
    private String menuName;
    private Long parentId;
    private Integer sortOrder;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer menuType; // 1-目录 2-菜单 3-按钮
    private Integer status;
    private List<MenuTreeVO> children;
}
