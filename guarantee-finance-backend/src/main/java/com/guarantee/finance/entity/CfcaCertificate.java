package com.guarantee.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guarantee.finance.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfca_certificate")
public class CfcaCertificate extends BaseEntity {
    private String certNo;
    private String certType;
    private String ownerName;
    private Long ownerId;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private Integer status;
    private String publicKey;
    private String privateKey;

    @TableField(exist = false)
    private String certName;

    @TableField(exist = false)
    private String subjectDn;

    @TableField(exist = false)
    private String issuerDn;

    @TableField(exist = false)
    private LocalDate validFrom;

    @TableField(exist = false)
    private LocalDate validTo;

    @TableField(exist = false)
    private String keyAlias;

    @TableField(exist = false)
    private String storagePath;
}
