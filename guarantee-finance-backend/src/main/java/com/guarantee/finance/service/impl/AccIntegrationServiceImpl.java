package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.dto.VoucherDTO;
import com.guarantee.finance.dto.VoucherDetailDTO;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.FinReceipt;
import com.guarantee.finance.entity.FinPayment;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.mapper.FinReceiptMapper;
import com.guarantee.finance.mapper.FinPaymentMapper;
import com.guarantee.finance.service.AccIntegrationService;
import com.guarantee.finance.service.AccVoucherService;
import com.guarantee.finance.service.NcCloudService;
import com.guarantee.finance.vo.VoucherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccIntegrationServiceImpl implements AccIntegrationService {

    private final AccVoucherService accVoucherService;
    private final AccVoucherMapper accVoucherMapper;
    private final FinReceiptMapper finReceiptMapper;
    private final FinPaymentMapper finPaymentMapper;
    private final NcCloudService ncCloudService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucherFromReceipt(Long receiptId) {
        FinReceipt receipt = finReceiptMapper.selectById(receiptId);
        if (receipt == null) {
            throw new RuntimeException("收款单不存在");
        }

        // 创建凭证
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setVoucherDate(LocalDate.now().toString());
        voucherDTO.setPeriod(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        voucherDTO.setSummary("收款单生成凭证: " + receipt.getReceiptNo());
        voucherDTO.setVoucherType(1); // 记账凭证
        voucherDTO.setSourceType("1"); // 自动
        voucherDTO.setSourceId(receiptId.toString());

        // 构建凭证分录
        List<VoucherDetailDTO> details = new ArrayList<>();

        // 借方：银行存款
        VoucherDetailDTO debitDetail = new VoucherDetailDTO();
        debitDetail.setLineNo(1);
        debitDetail.setSubjectCode("1002"); // 银行存款
        debitDetail.setSubjectName("银行存款");
        debitDetail.setSummary("收到款项");
        debitDetail.setDebitAmount(receipt.getAmount());
        debitDetail.setCreditAmount(BigDecimal.ZERO);
        details.add(debitDetail);

        // 贷方：应收账款或其他科目
        VoucherDetailDTO creditDetail = new VoucherDetailDTO();
        creditDetail.setLineNo(2);
        creditDetail.setSubjectCode("1122"); // 应收账款
        creditDetail.setSubjectName("应收账款");
        creditDetail.setSummary("应收账款减少");
        creditDetail.setDebitAmount(BigDecimal.ZERO);
        creditDetail.setCreditAmount(receipt.getAmount());
        details.add(creditDetail);

        voucherDTO.setDetails(details);

        // 创建凭证
        return accVoucherService.createVoucher(voucherDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucherFromPayment(Long paymentId) {
        FinPayment payment = finPaymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new RuntimeException("付款单不存在");
        }

        // 创建凭证
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setVoucherDate(LocalDate.now().toString());
        voucherDTO.setPeriod(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        voucherDTO.setSummary("付款单生成凭证: " + payment.getPaymentNo());
        voucherDTO.setVoucherType(1); // 记账凭证
        voucherDTO.setSourceType("1"); // 自动
        voucherDTO.setSourceId(paymentId.toString());

        // 构建凭证分录
        List<VoucherDetailDTO> details = new ArrayList<>();

        // 借方：应付账款或其他科目
        VoucherDetailDTO debitDetail = new VoucherDetailDTO();
        debitDetail.setLineNo(1);
        debitDetail.setSubjectCode("2202"); // 应付账款
        debitDetail.setSubjectName("应付账款");
        debitDetail.setSummary("应付账款减少");
        debitDetail.setDebitAmount(payment.getAmount());
        debitDetail.setCreditAmount(BigDecimal.ZERO);
        details.add(debitDetail);

        // 贷方：银行存款
        VoucherDetailDTO creditDetail = new VoucherDetailDTO();
        creditDetail.setLineNo(2);
        creditDetail.setSubjectCode("1002"); // 银行存款
        creditDetail.setSubjectName("银行存款");
        creditDetail.setSummary("支付款项");
        creditDetail.setDebitAmount(BigDecimal.ZERO);
        creditDetail.setCreditAmount(payment.getAmount());
        details.add(creditDetail);

        voucherDTO.setDetails(details);

        // 创建凭证
        return accVoucherService.createVoucher(voucherDTO);
    }

    @Override
    public boolean syncVoucherToNc(Long voucherId) {
        try {
            // 调用NC Cloud服务同步凭证
            // TODO: 实现具体的同步逻辑
            log.info("同步凭证到NC Cloud: {}", voucherId);
            return true;
        } catch (Exception e) {
            log.error("同步凭证到NC Cloud失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<VoucherVO> getVouchersByReceiptId(Long receiptId) {
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccVoucher::getSourceType, "1")
                .eq(AccVoucher::getSourceId, receiptId.toString());
        List<AccVoucher> vouchers = accVoucherMapper.selectList(wrapper);
        return vouchers.stream().map(voucher -> {
            VoucherVO vo = new VoucherVO();
            vo.setId(voucher.getId());
            vo.setVoucherNo(voucher.getVoucherNo());
            vo.setVoucherDate(voucher.getVoucherDate());
            vo.setPeriod(voucher.getPeriod());
            vo.setSummary(voucher.getSummary());
            vo.setStatus(voucher.getStatus());
            vo.setStatusText(getStatusText(voucher.getStatus()));
            return vo;
        }).toList();
    }

    @Override
    public List<VoucherVO> getVouchersByPaymentId(Long paymentId) {
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccVoucher::getSourceType, "1")
                .eq(AccVoucher::getSourceId, paymentId.toString());
        List<AccVoucher> vouchers = accVoucherMapper.selectList(wrapper);
        return vouchers.stream().map(voucher -> {
            VoucherVO vo = new VoucherVO();
            vo.setId(voucher.getId());
            vo.setVoucherNo(voucher.getVoucherNo());
            vo.setVoucherDate(voucher.getVoucherDate());
            vo.setPeriod(voucher.getPeriod());
            vo.setSummary(voucher.getSummary());
            vo.setStatus(voucher.getStatus());
            vo.setStatusText(getStatusText(voucher.getStatus()));
            return vo;
        }).toList();
    }

    @Override
    public void batchSyncVouchersToNc(List<Long> voucherIds) {
        for (Long voucherId : voucherIds) {
            syncVoucherToNc(voucherId);
        }
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "已提交";
            case 2: return "已审核";
            case 3: return "已记账";
            case 4: return "已作废";
            default: return "未知";
        }
    }
}
