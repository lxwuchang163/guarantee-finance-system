package com.guarantee.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class RoleCompareVO {

    private RoleVO sourceRole;

    private RoleVO targetRole;

    private List<RoleDiffVO> differences;
}
