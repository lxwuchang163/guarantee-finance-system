package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.AccGeneralLedger;

public interface GeneralLedgerService extends IService<AccGeneralLedger> {
    IPage<AccGeneralLedger> queryByPeriod(String period, String subjectCode, IPage<AccGeneralLedger> page);
    void generateGeneralLedger(String period);
}
