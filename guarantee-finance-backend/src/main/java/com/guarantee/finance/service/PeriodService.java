package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.AccPeriod;

import java.util.List;

public interface PeriodService extends IService<AccPeriod> {
    List<AccPeriod> getPeriodList(Integer year);
    AccPeriod getCurrentPeriod();
    void initPeriods(Integer year);
    void closePeriod(String periodCode);
    void reopenPeriod(String periodCode);
    boolean checkPeriodCanClose(String periodCode);
}
