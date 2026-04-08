package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.dto.VoucherGenerateDTO;
import com.guarantee.finance.vo.AccVoucherVO;

public interface AccountingPlatformService {

    IPage<AccVoucherVO> queryVouchers(String voucherNo, String voucherType, String status,
                                       String startDate, String endDate, Page<?> page);

    AccVoucherVO getVoucherDetail(Long id);

    Long generateVoucher(VoucherGenerateDTO dto);

    boolean batchGenerateVouchers(java.util.List<Long> billIds);

    void auditVoucher(Long id);

    void reverseVoucher(Long id);
}
