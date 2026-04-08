package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.dto.NcCustomerSyncDTO;
import com.guarantee.finance.dto.NcVoucherSyncDTO;
import com.guarantee.finance.vo.NcSyncLogVO;

public interface NcCloudService {

    boolean login();

    void logout();

    String getToken();

    boolean isTokenValid();

    boolean syncCustomer(NcCustomerSyncDTO dto);

    boolean syncBatchCustomers(java.util.List<NcCustomerSyncDTO> dtoList);

    boolean syncVoucher(NcVoucherSyncDTO dto);

    boolean syncBatchVouchers(java.util.List<NcVoucherSyncDTO> dtoList);

    IPage<NcSyncLogVO> querySyncLog(String syncType, Integer status, Page<?> page);

    NcSyncLogVO getSyncDetail(Long id);

    boolean retrySync(Long logId);

    java.util.Map<String, Object> checkCustomerDiff(String customerCode);

    java.util.Map<String, Object> getNcCustomer(String customerCode);
}
