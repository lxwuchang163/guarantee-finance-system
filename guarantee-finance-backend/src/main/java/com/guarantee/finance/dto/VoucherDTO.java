package com.guarantee.finance.dto;

import lombok.Data;
import java.util.List;

@Data
public class VoucherDTO {

    private String voucherNo;
    private String voucherDate;
    private String period;
    private String summary;
    private Integer voucherType;
    private String sourceType;
    private String sourceId;
    private String remark;
    private List<VoucherDetailDTO> details;
}
