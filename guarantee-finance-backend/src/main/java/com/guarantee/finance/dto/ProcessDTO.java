package com.guarantee.finance.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProcessDTO {
    private Long id;
    private String name;
    private String businessType;
    private String description;
    private Integer status;
    private List<NodeDTO> nodes;

    @Data
    public static class NodeDTO {
        private Long id;
        private String nodeName;
        private String nodeType; // approve/notify/cc
        private String approverType; // role/user/specific
        private String approverValue; // 角色编码或用户ID
    }
}
