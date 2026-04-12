package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.AccDetailLedger;

public interface DetailLedgerService extends IService<AccDetailLedger> {
    IPage<AccDetailLedger> queryBySubjectAndPeriod(String subjectCode, String period, IPage<AccDetailLedger> page);
    void generateDetailLedger(String period);
}
