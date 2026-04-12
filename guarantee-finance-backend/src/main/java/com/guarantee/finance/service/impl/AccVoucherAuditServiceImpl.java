package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.VoucherAuditDTO;
import com.guarantee.finance.entity.AccAuditConfig;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherAudit;
import com.guarantee.finance.mapper.AccVoucherAuditMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.AccAuditConfigService;
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
    private final AccAuditConfigService accAuditConfigService;

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

        if (voucher.getStatus() != 1) {
            throw new RuntimeException("只有已提交的凭证可以审核");
        }

        Long currentUserId = getCurrentUserId();
        if (!canAuditVoucher(dto.getVoucherId(), currentUserId)) {
            throw new RuntimeException("您不能审核此凭证");
        }

        AccAuditConfig auditConfig = accAuditConfigService.getDefaultAuditConfig();
        int auditType = auditConfig != null ? auditConfig.getAuditType() : 1;

        int currentAuditLevel = dto.getAuditLevel() != null ? dto.getAuditLevel() : 1;

        if (auditType == 2) {
            LambdaQueryWrapper<AccVoucherAudit> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccVoucherAudit::getVoucherId, dto.getVoucherId())
                    .eq(AccVoucherAudit::getAuditResult, 1);
            long passedCount = accVoucherAuditMapper.selectCount(wrapper);
            
            if (passedCount >= 2) {
                throw new RuntimeException("该凭证已完成双审核");
            }
            
            if (passedCount == 1 && currentAuditLevel == 1) {
                currentAuditLevel = 2;
            }
        }

        AccVoucherAudit audit = new AccVoucherAudit();
        audit.setVoucherId(dto.getVoucherId());
        audit.setVoucherNo(voucher.getVoucherNo());
        audit.setAuditType(auditType);
        audit.setAuditorId(currentUserId);
        audit.setAuditorName(getCurrentUserName());
        audit.setAuditResult(dto.getAuditResult());
        audit.setAuditOpinion(dto.getAuditOpinion());
        audit.setAuditLevel(currentAuditLevel);
        audit.setAuditStatus("2");
        audit.setAuditTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        accVoucherAuditMapper.insert(audit);

        if (dto.getAuditResult() == 1) {
            if (auditType == 1) {
                voucher.setStatus(2);
                voucher.setApproveUserId(currentUserId);
            } else {
                LambdaQueryWrapper<AccVoucherAudit> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AccVoucherAudit::getVoucherId, dto.getVoucherId())
                        .eq(AccVoucherAudit::getAuditResult, 1);
                long passedCount = accVoucherAuditMapper.selectCount(wrapper);
                
                if (passedCount >= 2) {
                    voucher.setStatus(2);
                    voucher.setApproveUserId(currentUserId);
                }
            }
        } else {
            voucher.setStatus(0);
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
        dto.setAuditLevel(1);
        auditVoucher(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectVoucher(Long voucherId, String opinion) {
        VoucherAuditDTO dto = new VoucherAuditDTO();
        dto.setVoucherId(voucherId);
        dto.setAuditResult(2);
        dto.setAuditOpinion(opinion);
        dto.setAuditLevel(1);
        auditVoucher(dto);
    }

    @Override
    public boolean canAuditVoucher(Long voucherId, Long auditorId) {
        AccVoucher voucher = accVoucherMapper.selectById(voucherId);
        if (voucher == null) {
            return false;
        }

        if (voucher.getCreateUserId() != null && voucher.getCreateUserId().equals(auditorId)) {
            return false;
        }

        return voucher.getStatus() == 1;
    }

    private Long getCurrentUserId() {
        return 1L;
    }

    private String getCurrentUserName() {
        return "admin";
    }
}
