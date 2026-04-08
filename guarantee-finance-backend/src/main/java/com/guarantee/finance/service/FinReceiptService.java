package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.dto.ReceiptDTO;
import com.guarantee.finance.entity.FinReceipt;
import com.guarantee.finance.vo.ReceiptVO;

public interface FinReceiptService {

    IPage<ReceiptVO> queryPage(String keyword, Integer businessType, String customerCode,
                                Integer status, java.time.LocalDate startDate, java.time.LocalDate endDate, Page<?> page);

    ReceiptVO getDetail(Long id);

    Long create(ReceiptDTO dto);

    void update(ReceiptDTO dto);

    void delete(Long id);

    void submit(Long id);

    void audit(Long id, boolean pass);

    void post(Long id);

    void reverse(Long id);
}
