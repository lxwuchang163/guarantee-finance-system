package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.VoucherGenerateDTO;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.mapper.AccVoucherDetailMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.AccountingPlatformService;
import com.guarantee.finance.vo.AccVoucherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AccountingPlatformServiceImpl extends ServiceImpl<AccVoucherMapper, AccVoucher> implements AccountingPlatformService {

    private static final AtomicLong VOUCHER_NO_SEQ = new AtomicLong(0);

    @jakarta.annotation.Resource
    private AccVoucherDetailMapper detailMapper;

    @Override
    public IPage<AccVoucherVO> queryVouchers(String voucherNo, String voucherType, String status,
                                            String startDate, String endDate, Page<?> page) {
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(voucherNo)) wrapper.like(AccVoucher::getVoucherNo, voucherNo);
        if (StrUtil.isNotBlank(voucherType)) wrapper.eq(AccVoucher::getVoucherType, Integer.parseInt(voucherType));
        if (StrUtil.isNotBlank(status)) wrapper.eq(AccVoucher::getStatus, Integer.parseInt(status));
        if (StrUtil.isNotBlank(startDate)) wrapper.ge(AccVoucher::getVoucherDate, startDate);
        if (StrUtil.isNotBlank(endDate)) wrapper.le(AccVoucher::getVoucherDate, endDate);
        wrapper.orderByDesc(AccVoucher::getCreateTime);

        IPage<AccVoucher> voucherPage = this.page(page.convert(p -> new AccVoucher()), wrapper);
        IPage<AccVoucherVO> voPage = new Page<>(voucherPage.getCurrent(), voucherPage.getSize(), voucherPage.getTotal());
        voPage.setRecords(voucherPage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public AccVoucherVO getVoucherDetail(Long id) {
        AccVoucher voucher = getById(id);
        return voucher != null ? convertToVO(voucher) : null;
    }

    @Override
    public Long generateVoucher(VoucherGenerateDTO dto) {
        AccVoucher voucher = new AccVoucher();
        voucher.setVoucherNo(generateVoucherNo());
        voucher.setVoucherDate(LocalDateTime.now().toLocalDate().toString());
        voucher.setVoucherType(dto.getVoucherType() != null ? Integer.parseInt(dto.getVoucherType()) : 3);
        voucher.setPeriod(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        voucher.setCreateUserId(getCurrentUserId());
        voucher.setStatus(0);
        voucher.setSourceType(dto.getSourceBillType() != null ? "1" : "0");
        voucher.setSourceId(dto.getSourceBillNo());

        save(voucher);

        generateDetails(voucher.getId(), dto);

        return voucher.getId();
    }

    @Override
    public boolean batchGenerateVouchers(List<Long> billIds) {
        int successCount = 0;
        for (Long billId : billIds) {
            try {
                VoucherGenerateDTO dto = new VoucherGenerateDTO();
                dto.setSourceBillId(billId);
                generateVoucher(dto);
                successCount++;
            } catch (Exception e) {
                log.error("批量生成凭证失败，单据ID：{}", billId, e);
            }
        }
        log.info("批量生成凭证完成，成功{}，总数{}", successCount, billIds.size());
        return true;
    }

    @Override
    public void auditVoucher(Long id) {
        AccVoucher voucher = getById(id);
        if (voucher == null) throw new RuntimeException("凭证不存在");
        if (voucher.getStatus() != 0) throw new RuntimeException("只有未审核状态可以审核");
        voucher.setStatus(1);
        voucher.setApproveUserId(getCurrentUserId());
        updateById(voucher);
    }

    @Override
    public void reverseVoucher(Long id) {
        AccVoucher voucher = getById(id);
        if (voucher == null) throw new RuntimeException("凭证不存在");
        if (voucher.getStatus() < 1) throw new RuntimeException("已审核后才能冲销");
        voucher.setStatus(3);
        updateById(voucher);
    }

    private void generateDetails(Long voucherId, VoucherGenerateDTO dto) {
        AccVoucherDetail debitDetail = new AccVoucherDetail();
        debitDetail.setVoucherId(voucherId);
        debitDetail.setLineNo(1);
        if ("RECEIPT".equals(dto.getSourceBillType())) {
            debitDetail.setSubjectCode("1002");
            debitDetail.setSubjectName("银行存款");
        } else {
            debitDetail.setSubjectCode(getDebitAccountByType(dto.getSourceBillType()));
            debitDetail.setSubjectName(debitDetail.getSubjectCode());
        }
        debitDetail.setDebitAmount(dto.getAmount());
        debitDetail.setSummary(buildSummary(dto));
        debitDetail.setCustomerCode(dto.getCustomerCode());
        detailMapper.insert(debitDetail);

        AccVoucherDetail creditDetail = new AccVoucherDetail();
        creditDetail.setVoucherId(voucherId);
        creditDetail.setLineNo(2);
        if ("RECEIPT".equals(dto.getSourceBillType())) {
            creditDetail.setSubjectCode("6001");
            creditDetail.setSubjectName("保费收入");
        } else {
            creditDetail.setSubjectCode("1002");
            creditDetail.setSubjectName("银行存款");
        }
        creditDetail.setCreditAmount(dto.getAmount());
        creditDetail.setSummary(buildSummary(dto));
        detailMapper.insert(creditDetail);
    }

    private String buildSummary(VoucherGenerateDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(dto.getCustomerName())) sb.append(dto.getCustomerName());
        if (StrUtil.isNotBlank(dto.getSourceBillNo())) sb.append(dto.getSourceBillNo());
        if (dto.getAmount() != null) sb.append(dto.getAmount().toString());
        return sb.toString();
    }

    private String generateVoucherNo() {
        return "PZ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", VOUCHER_NO_SEQ.incrementAndGet() % 10000);
    }

    private String getDebitAccountByType(String type) {
        switch (type) {
            case "REFUND": return "6001";
            case "COMPENSATION": return "1221";
            case "DISTRIBUTION": return "2203";
            default: return "6601";
        }
    }

    private AccVoucherVO convertToVO(AccVoucher v) {
        AccVoucherVO vo = new AccVoucherVO();
        BeanUtils.copyProperties(v, vo);
        vo.setStatusText(getStatusText(v.getStatus()));
        return vo;
    }

    private String getStatusText(Integer s) {
        switch (s) { case 0: return "未审核"; case 1: return "已审核"; case 2: return "已记账"; case 3: return "已作废"; default: return "未知"; }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) return ((LoginUser) auth.getPrincipal()).getUserId();
        return 0L;
    }

    private String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) return ((LoginUser) auth.getPrincipal()).getUsername();
        return "system";
    }
}
