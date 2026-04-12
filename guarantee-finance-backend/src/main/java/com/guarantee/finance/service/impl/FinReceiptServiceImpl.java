package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.ReceiptDTO;
import com.guarantee.finance.entity.FinReceipt;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.mapper.FinReceiptMapper;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.FinReceiptService;
import com.guarantee.finance.service.VoucherAutoGenerateService;
import com.guarantee.finance.service.TodoService;
import com.guarantee.finance.vo.ReceiptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FinReceiptServiceImpl extends ServiceImpl<FinReceiptMapper, FinReceipt> implements FinReceiptService {

    private static final AtomicLong RECEIPT_NO_SEQ = new AtomicLong(0);

    private final VoucherAutoGenerateService voucherAutoGenerateService;
    private final TodoService todoService;

    public FinReceiptServiceImpl(VoucherAutoGenerateService voucherAutoGenerateService, TodoService todoService) {
        this.voucherAutoGenerateService = voucherAutoGenerateService;
        this.todoService = todoService;
    }

    @Override
    public IPage<ReceiptVO> queryPage(String keyword, Integer businessType, String customerCode,
                                       Integer status, LocalDate startDate, LocalDate endDate, Page<?> page) {
        LambdaQueryWrapper<FinReceipt> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(FinReceipt::getReceiptNo, keyword)
                    .or().like(FinReceipt::getCustomerName, keyword)
                    .or().like(FinReceipt::getContractNo, keyword));
        }
        if (businessType != null) {
            wrapper.eq(FinReceipt::getBusinessType, businessType);
        }
        if (StrUtil.isNotBlank(customerCode)) {
            wrapper.eq(FinReceipt::getCustomerCode, customerCode);
        }
        if (status != null) {
            wrapper.eq(FinReceipt::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(FinReceipt::getReceiptDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(FinReceipt::getReceiptDate, endDate);
        }
        wrapper.orderByDesc(FinReceipt::getCreateTime);

        IPage<FinReceipt> receiptPage = page(page.convert(p -> new FinReceipt()), wrapper);
        IPage<ReceiptVO> voPage = new Page<>(receiptPage.getCurrent(), receiptPage.getSize(), receiptPage.getTotal());
        voPage.setRecords(receiptPage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public ReceiptVO getDetail(Long id) {
        FinReceipt receipt = getById(id);
        return receipt != null ? convertToVO(receipt) : null;
    }

    @Override
    public Long create(ReceiptDTO dto) {
        validateReceipt(dto);

        FinReceipt receipt = new FinReceipt();
        BeanUtils.copyProperties(dto, receipt);
        receipt.setReceiptNo(generateReceiptNo());
        receipt.setStatus(0); // 草稿
        receipt.setMakerId(getCurrentUserId());
        receipt.setMakerName(getCurrentUserName());
        receipt.setMakerTime(LocalDateTime.now());

        // 设置业务类型名称
        receipt.setBusinessTypeName(getBusinessTypeName(dto.getBusinessType()));

        save(receipt);
        return receipt.getId();
    }

    @Override
    public void update(ReceiptDTO dto) {
        if (dto.getId() == null) throw new RuntimeException("收款单ID不能为空");
        FinReceipt existing = getById(dto.getId());
        if (existing == null) throw new RuntimeException("收款单不存在");
        if (existing.getStatus() > 0) throw new RuntimeException("非草稿状态不允许修改");

        validateReceipt(dto);
        FinReceipt receipt = new FinReceipt();
        BeanUtils.copyProperties(dto, receipt);
        receipt.setBusinessTypeName(getBusinessTypeName(dto.getBusinessType()));
        updateById(receipt);
    }

    @Override
    public void delete(Long id) {
        FinReceipt receipt = getById(id);
        if (receipt == null) throw new RuntimeException("收款单不存在");
        if (receipt.getStatus() > 0) throw new RuntimeException("非草稿状态不允许删除");
        removeById(id);
    }

    @Override
    public void submit(Long id) {
        FinReceipt receipt = getById(id);
        if (receipt == null) throw new RuntimeException("收款单不存在");
        if (receipt.getStatus() != 0) throw new RuntimeException("只有草稿状态可以提交审核");

        receipt.setStatus(1); // 已提交
        updateById(receipt);
    }

    @Override
    public void audit(Long id, boolean pass) {
        FinReceipt receipt = getById(id);
        if (receipt == null) throw new RuntimeException("收款单不存在");
        if (receipt.getStatus() != 1) throw new RuntimeException("只有已提交状态可以审核");

        if (pass) {
            receipt.setStatus(2); // 已审核
            receipt.setAuditorId(getCurrentUserId());
            receipt.setAuditorName(getCurrentUserName());
            receipt.setAuditorTime(LocalDateTime.now());

            updateById(receipt);

            try {
                AccVoucher voucher = voucherAutoGenerateService.generateReceiptVoucher(receipt);
                receipt.setVoucherId(voucher.getId());
                receipt.setVoucherNo(voucher.getVoucherNo());
                updateById(receipt);
            } catch (Exception e) {
                throw new RuntimeException("审核通过但自动生成凭证失败: " + e.getMessage(), e);
            }

            try {
                todoService.completeTodoByBusiness(id, "receipt");
            } catch (Exception e) {
                // ignore
            }
        } else {
            receipt.setStatus(0); // 驳回回草稿
            updateById(receipt);
        }
    }

    @Override
    public void post(Long id) {
        FinReceipt receipt = getById(id);
        if (receipt == null) throw new RuntimeException("收款单不存在");
        if (receipt.getStatus() != 2) throw new RuntimeException("只有已审核状态可以记账");

        receipt.setStatus(3); // 已记账
        receipt.setPosterId(getCurrentUserId());
        receipt.setPosterName(getCurrentUserName());
        receipt.setPosterTime(LocalDateTime.now());
        updateById(receipt);
    }

    @Override
    public void reverse(Long id) {
        FinReceipt receipt = getById(id);
        if (receipt == null) throw new RuntimeException("收款单不存在");
        if (receipt.getStatus() != 3) throw new RuntimeException("只有已记账状态可以冲销");

        receipt.setStatus(4); // 已作废（红字冲销）
        updateById(receipt);
    }

    private void validateReceipt(ReceiptDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("收款金额必须大于0");
        }
        if (dto.getReceiptDate() != null && dto.getReceiptDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("收款日期不能晚于当前日期");
        }
        if (dto.getBusinessType() == null || dto.getBusinessType() < 1 || dto.getBusinessType() > 3) {
            throw new RuntimeException("业务类型不合法");
        }
        if (StrUtil.isBlank(dto.getCustomerCode())) {
            throw new RuntimeException("客户编码不能为空");
        }
        if (StrUtil.isBlank(dto.getCustomerName())) {
            throw new RuntimeException("客户名称不能为空");
        }
    }

    private String generateReceiptNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = RECEIPT_NO_SEQ.incrementAndGet();
        return "SK" + datePart + String.format("%04d", seq % 10000);
    }

    private String getBusinessTypeName(Integer type) {
        switch (type) {
            case 1: return "保费收入";
            case 2: return "分担收入";
            case 3: return "追偿到款收入";
            default: return "未知";
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) auth.getPrincipal()).getUserId();
        }
        return 0L;
    }

    private String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) auth.getPrincipal()).getUsername();
        }
        return "system";
    }

    private ReceiptVO convertToVO(FinReceipt receipt) {
        ReceiptVO vo = new ReceiptVO();
        BeanUtils.copyProperties(receipt, vo);
        return vo;
    }
}
