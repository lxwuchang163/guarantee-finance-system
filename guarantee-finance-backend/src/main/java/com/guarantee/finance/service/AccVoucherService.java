package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.VoucherDTO;
import com.guarantee.finance.dto.VoucherDetailDTO;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.vo.VoucherVO;

import java.util.List;

public interface AccVoucherService extends IService<AccVoucher> {

    IPage<VoucherVO> queryVouchers(String voucherNo, String period, String voucherDate, Integer status, Page<?> page);

    VoucherVO getVoucherDetail(Long id);

    Long createVoucher(VoucherDTO dto);

    void updateVoucher(Long id, VoucherDTO dto);

    void deleteVoucher(Long id);

    void submitVoucher(Long id);

    void voidVoucher(Long id);

    void restoreVoucher(Long id);

    List<VoucherVO> getVouchersByPeriod(String period);

    boolean checkVoucherNoUnique(String voucherNo, Long id);

    void postVoucher(Long id);

    void unpostVoucher(Long id);

    void importVouchers(org.springframework.web.multipart.MultipartFile file);

    byte[] exportVouchersToExcel(String period);

    byte[] exportVoucherToPdf(Long id);
}
