package com.guarantee.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NcVoucherSyncDTO {
    private Long id; // 本地凭证ID
    private String voucherNo; // 凭证编号
    private String voucherDate; // 凭证日期
    private Integer voucherType; // 1-收款 2-付款 3-转账
    private String accountingPeriod; // 会计期间
    private String companyCode; // 公司编码
    private String maker; // 制单人
    private String remark; // 备注
    private BigDecimal totalDebit; // 借方合计
    private BigDecimal totalCredit; // 贷方合计
    private String sourceBillType; // 来源单据类型
    private String sourceBillNo; // 来源单据号
    private List<VoucherDetailSyncDTO> details;

    @Data
    public static class VoucherDetailSyncDTO {
        private Integer lineNo;
        private String accountCode;
        private String accountName;
        private String direction; // DEBIT/CREDIT
        private BigDecimal amount;
        private String summary;
        private String customerCode;
        private String departmentCode;
        private String projectCode;
    }
}
