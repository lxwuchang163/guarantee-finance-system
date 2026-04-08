package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.VoucherAuditDTO;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherAudit;
import com.guarantee.finance.mapper.AccVoucherAuditMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.AccVoucherAuditService;
import com.guarantee.finance.vo.VoucherAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccVoucherAuditServiceImpl extends ServiceImpl<AccVoucherAuditMapper, AccVoucherAudit> implements AccVoucherAuditService {

    private final AccVoucherAuditMapper accVoucherAuditMapper;
    private final AccVoucherMapper accVoucherMapper;

    @Override
    public IPage<VoucherAuditVO> queryAudits(String voucherNo, String auditStatus, Page<?> page) {
        return accVoucherAuditMapper.selectAuditPage(voucherNo, auditStatus, page);
    }

    @Override
    public List<VoucherAuditVO> getVoucherAudits(Long voucherId) {
        return accVoucherAuditMapper.selectByVoucherId(voucherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditVoucher(VoucherAuditDTO dto) {
        AccVoucher voucher = accVoucherMapper.selectById(dto.getVoucherId());
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        // 检查凭证状态
        if (voucher.getStatus() != 1) {
            throw new RuntimeException("只有已提交的凭证可以审核");
        }

        // 创建审核记录
        AccVoucherAudit audit = new AccVoucherAudit();
        audit.setVoucherId(dto.getVoucherId());
        audit.setVoucherNo(voucher.getVoucherNo());
        audit.setAuditorId(getCurrentUserId()); // 从上下文获取当前用户ID
        audit.setAuditorName(getCurrentUserName()); // 从上下文获取当前用户姓名
        audit.setAuditResult(dto.getAuditResult());
        audit.setAuditOpinion(dto.getAuditOpinion());
        audit.setAuditLevel(dto.getAuditLevel());
        audit.setAuditStatus("2"); // 已完成
        audit.setAuditTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        accVoucherAuditMapper.insert(audit);

        // 更新凭证状态
        if (dto.getAuditResult() == 1) { // 审核通过
            // 如果是二级审核，直接标记为已审核
            if (dto.getAuditLevel() == 2) {
                voucher.setStatus(2); // 已审核
                voucher.setApproveUserId(getCurrentUserId());
            }
            // 一级审核通过后，状态仍为已提交，等待二级审核
        } else { // 审核拒绝
            voucher.setStatus(0); // 退回为草稿
        }

        voucher.setAuditStatus(dto.getAuditResult() == 1 ? "1" : "2");
        voucher.setAuditOpinion(dto.getAuditOpinion());
        accVoucherMapper.updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveVoucher(Long voucherId, String opinion) {
        VoucherAuditDTO dto = new VoucherAuditDTO();
        dto.setVoucherId(voucherId);
        dto.setAuditResult(1);
        dto.setAuditOpinion(opinion);
        dto.setAuditLevel(1); // 默认一级审核
        auditVoucher(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectVoucher(Long voucherId, String opinion) {
        VoucherAuditDTO dto = new VoucherAuditDTO();
        dto.setVoucherId(voucherId);
        dto.setAuditResult(2);
        dto.setAuditOpinion(opinion);
        dto.setAuditLevel(1); // 默认一级审核
        auditVoucher(dto);
    }

    @Override
    public boolean canAuditVoucher(Long voucherId, Long auditorId) {
        AccVoucher voucher = accVoucherMapper.selectById(voucherId);
        if (voucher == null) {
            return false;
        }

        // 审核人不能审核自己的凭证
        if (voucher.getCreateUserId() != null && voucher.getCreateUserId().equals(auditorId)) {
            return false;
        }

        // 只有已提交的凭证可以审核
        return voucher.getStatus() == 1;
    }

    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文获取当前用户ID
        return 1L; // 临时返回
    }

    private String getCurrentUserName() {
        // TODO: 从Spring Security上下文获取当前用户姓名
        return "admin"; // 临时返回
    }
}
