package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfca_certificate")
public class CfcaCertificate extends BaseEntity {
    private String certType; // ENTERPRISE/OPERATOR/API
    private String certName; // 证书名称
    private String certNo; // 证书序列号
    private String ownerName; // 持有人姓名
    private String ownerId; // 持有人ID（用户ID）
    private String subjectDN; // 证书主题
    private String issuerDN; // 颁发者
    private LocalDate validFrom; // 生效日期
    private LocalDate validTo; // 到期日期
    private Integer status; // 0-已过期 1-正常 2-已吊销 3-待更新
    private String keyAlias; // 密钥别名
    private String storagePath; // 存储路径
}
