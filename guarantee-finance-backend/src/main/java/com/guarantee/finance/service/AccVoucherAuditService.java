package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.VoucherAuditDTO;
import com.guarantee.finance.entity.AccVoucherAudit;
import com.guarantee.finance.vo.VoucherAuditVO;

import java.util.List;

public interface AccVoucherAuditService extends IService<AccVoucherAudit> {

    IPage<VoucherAuditVO> queryAudits(String voucherNo, String auditStatus, Page<?> page);

    List<VoucherAuditVO> getVoucherAudits(Long voucherId);

    void auditVoucher(VoucherAuditDTO dto);

    void approveVoucher(Long voucherId, String opinion);

    void rejectVoucher(Long voucherId, String opinion);

    boolean canAuditVoucher(Long voucherId, Long auditorId);
}
