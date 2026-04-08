package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.dto.PaymentDTO;
import com.guarantee.finance.vo.PaymentVO;

public interface FinPaymentService {

    IPage<PaymentVO> queryPage(String keyword, Integer businessType, String customerCode,
                                Integer status, java.time.LocalDate startDate, java.time.LocalDate endDate, Page<?> page);

    PaymentVO getDetail(Long id);

    Long create(PaymentDTO dto);

    void update(PaymentDTO dto);

    void delete(Long id);

    void submit(Long id);

    void audit(Long id, boolean pass);

    void pay(Long id); // 付款（对接银企直连）

    void post(Long id);

    void reverse(Long id);
}
