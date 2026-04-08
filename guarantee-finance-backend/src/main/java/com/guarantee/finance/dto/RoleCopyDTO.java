package com.guarantee.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleCopyDTO {

    @NotNull(message = "源角色ID不能为空")
    private Long sourceRoleId;

    @NotBlank(message = "新角色名称不能为空")
    private String newRoleName;

    @NotBlank(message = "新角色编码不能为空")
    private String newRoleCode;
}
