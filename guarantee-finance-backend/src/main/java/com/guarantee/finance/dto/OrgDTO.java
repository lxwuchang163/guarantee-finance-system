package com.guarantee.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrgDTO {

    private Long id;

    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称长度不能超过100个字符")
    private String orgName;

    @NotBlank(message = "机构编码不能为空")
    @Size(max = 50, message = "机构编码长度不能超过50个字符")
    private String orgCode;

    private Long parentId;

    private Integer level;

    @Size(max = 50, message = "负责人长度不能超过50个字符")
    private String leader;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "地址长度不能超过255个字符")
    private String address;

    private Integer status;

    private Integer sortOrder;

    private String remark;
}
