package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.entity.BizCustomer;
import com.guarantee.finance.vo.CustomerVO;

public interface BizCustomerService {

    IPage<CustomerVO> queryPage(String keyword, Integer customerType, Integer status, Page<?> page);

    CustomerVO getDetail(Long id);

    void syncAll();

    void syncIncremental(String lastSyncTime);

    boolean checkCodeUnique(String code, Long id);
}
