package com.guarantee.finance.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoucherVO {

    private Long id;
    private String voucherNo;
    private String voucherDate;
    private String period;
    private String summary;
    private Integer status;
    private String statusText;
    private Long createUserId;
    private String createUserName;
    private Long approveUserId;
    private String approveUserName;
    private Long postUserId;
    private String postUserName;
    private String auditStatus;
    private String auditOpinion;
    private Integer voucherType;
    private String voucherTypeText;
    private String sourceType;
    private String sourceId;
    private String ncSyncStatus;
    private String remark;
    private Integer year;
    private Integer month;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<VoucherDetailVO> details;
}
