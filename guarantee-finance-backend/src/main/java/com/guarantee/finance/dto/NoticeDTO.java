package com.guarantee.finance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDTO {

    private Long id;
    private String title;
    private String content;
    private String noticeType;
    private Integer topFlag;
    private LocalDateTime publishTime;
    private String remark;
}
