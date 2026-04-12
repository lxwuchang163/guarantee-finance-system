package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.PaymentDTO;
import com.guarantee.finance.entity.FinPayment;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.mapper.FinPaymentMapper;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.FinPaymentService;
import com.guarantee.finance.service.VoucherAutoGenerateService;
import com.guarantee.finance.service.TodoService;
import com.guarantee.finance.vo.PaymentVO;
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
public class FinPaymentServiceImpl extends ServiceImpl<FinPaymentMapper, FinPayment> implements FinPaymentService {

    private static final AtomicLong PAYMENT_NO_SEQ = new AtomicLong(0);

    private final VoucherAutoGenerateService voucherAutoGenerateService;
    private final TodoService todoService;

    public FinPaymentServiceImpl(VoucherAutoGenerateService voucherAutoGenerateService, TodoService todoService) {
        this.voucherAutoGenerateService = voucherAutoGenerateService;
        this.todoService = todoService;
    }

    @Override
    public IPage<PaymentVO> queryPage(String keyword, Integer businessType, String customerCode,
                                       Integer status, LocalDate startDate, LocalDate endDate, Page<?> page) {
        LambdaQueryWrapper<FinPayment> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(FinPayment::getPaymentNo, keyword)
                    .or().like(FinPayment::getCustomerName, keyword)
                    .or().like(FinPayment::getContractNo, keyword));
        }
        if (businessType != null) {
            wrapper.eq(FinPayment::getBusinessType, businessType);
        }
        if (StrUtil.isNotBlank(customerCode)) {
            wrapper.eq(FinPayment::getCustomerCode, customerCode);
        }
        if (status != null) {
            wrapper.eq(FinPayment::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(FinPayment::getPaymentDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(FinPayment::getPaymentDate, endDate);
        }
        wrapper.orderByDesc(FinPayment::getCreateTime);

        IPage<FinPayment> paymentPage = page(page.convert(p -> new FinPayment()), wrapper);
        IPage<PaymentVO> voPage = new Page<>(paymentPage.getCurrent(), paymentPage.getSize(), paymentPage.getTotal());
        voPage.setRecords(paymentPage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public PaymentVO getDetail(Long id) {
        FinPayment payment = getById(id);
        return payment != null ? convertToVO(payment) : null;
    }

    @Override
    public Long create(PaymentDTO dto) {
        validatePayment(dto);

        FinPayment payment = new FinPayment();
        BeanUtils.copyProperties(dto, payment);
        payment.setPaymentNo(generatePaymentNo());
        payment.setStatus(0); // 草稿
        payment.setMakerId(getCurrentUserId());
        payment.setMakerName(getCurrentUserName());
        payment.setMakerTime(LocalDateTime.now());
        payment.setBusinessTypeName(getBusinessTypeName(dto.getBusinessType()));

        save(payment);
        return payment.getId();
    }

    @Override
    public void update(PaymentDTO dto) {
        if (dto.getId() == null) throw new RuntimeException("付款单ID不能为空");
        FinPayment existing = getById(dto.getId());
        if (existing == null) throw new RuntimeException("付款单不存在");
        if (existing.getStatus() > 0) throw new RuntimeException("非草稿状态不允许修改");

        validatePayment(dto);
        FinPayment payment = new FinPayment();
        BeanUtils.copyProperties(dto, payment);
        payment.setBusinessTypeName(getBusinessTypeName(dto.getBusinessType()));
        updateById(payment);
    }

    @Override
    public void delete(Long id) {
        FinPayment payment = getById(id);
        if (payment == null) throw new RuntimeException("付款单不存在");
        if (payment.getStatus() > 0) throw new RuntimeException("非草稿状态不允许删除");
        removeById(id);
    }

    @Override
    public void submit(Long id) {
        FinPayment payment = getById(id);
        if (payment == null) throw new RuntimeException("付款单不存在");
        if (payment.getStatus() != 0) throw new RuntimeException("只有草稿状态可以提交审核");
        payment.setStatus(1);
        updateById(payment);
    }

    @Override
    public void audit(Long id, boolean pass) {
        FinPayment payment = getById(id);
        if (payment == null) throw new RuntimeException("付款单不存在");
        if (payment.getStatus() != 1) throw new RuntimeException("只有已提交状态可以审核");

        if (pass) {
            payment.setStatus(2); // 已审核（待付款）
            payment.setAuditorId(getCurrentUserId());
            payment.setAuditorName(getCurrentUserName());
            payment.setAuditorTime(LocalDateTime.now());

            updateById(payment);

            try {
                AccVoucher voucher = voucherAutoGenerateService.generatePaymentVoucher(payment);
                payment.setVoucherId(voucher.getId());
                payment.setVoucherNo(voucher.getVoucherNo());
                updateById(payment);
            } catch (Exception e) {
                throw new RuntimeException("审核通过但自动生成凭证失败: " + e.getMessage(), e);
            }

            try {
                todoService.completeTodoByBusiness(id, "payment");
            } catch (Exception e) {
                // ignore
            }
        } else {
            payment.setStatus(0); // 驳回回草稿
            updateById(payment);
        }
    }

    @Override
    public void pay(Long id) {
        FinPayment payment = getById(id);
        if (payment == null) throw new RuntimeException("付款单不存在");
        if (payment.getStatus() != 2) throw new RuntimeException("只有已审核状态可以执行付款");
        // TODO: 对接银企直连发起支付指令
        payment.setStatus(3); // 已付款
        updateById(payment);
    }

    @Override
    public void post(Long id) {
        FinPayment payment = getById(id);
        if (payment == null) throw new RuntimeException("付款单不存在");
        if (payment.getStatus() != 3) throw new RuntimeException("只有已付款状态可以记账");
        payment.setStatus(4); // 已记账
        payment.setPosterId(getCurrentUserId());
        payment.setPosterName(getCurrentUserName());
        payment.setPosterTime(LocalDateTime.now());
        updateById(payment);
    }

    @Override
    public void reverse(Long id) {
        FinPayment payment = getById(id);
        if (payment == null) throw new RuntimeException("付款单不存在");
        if (payment.getStatus() < 3) throw new RuntimeException("已记账后才能冲销");
        payment.setStatus(5); // 已作废
        updateById(payment);
    }

    private void validatePayment(PaymentDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("付款金额必须大于0");
        }
        if (dto.getPaymentDate() != null && dto.getPaymentDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("付款日期不能晚于当前日期");
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

    private String generatePaymentNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = PAYMENT_NO_SEQ.incrementAndGet();
        return "FK" + datePart + String.format("%04d", seq % 10000);
    }

    private String getBusinessTypeName(Integer type) {
        switch (type) {
            case 1: return "退费支出";
            case 2: return "代偿支出";
            case 3: return "追回资金分配";
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

    private PaymentVO convertToVO(FinPayment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        return vo;
    }
}
