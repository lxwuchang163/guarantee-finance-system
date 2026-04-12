package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.AccPeriod;
import com.guarantee.finance.mapper.AccPeriodMapper;
import com.guarantee.finance.service.PeriodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class PeriodServiceImpl extends ServiceImpl<AccPeriodMapper, AccPeriod> implements PeriodService {

    @Override
    public List<AccPeriod> getPeriodList(Integer year) {
        LambdaQueryWrapper<AccPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccPeriod::getDeleted, 0);
        if (year != null) {
            wrapper.eq(AccPeriod::getYear, year);
        }
        wrapper.orderByAsc(AccPeriod::getYear).orderByAsc(AccPeriod::getMonth);
        return list(wrapper);
    }

    @Override
    public AccPeriod getCurrentPeriod() {
        LambdaQueryWrapper<AccPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccPeriod::getDeleted, 0)
                .eq(AccPeriod::getIsCurrent, 1)
                .eq(AccPeriod::getStatus, "OPEN");
        return getOne(wrapper, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPeriods(Integer year) {
        log.info("初始化{}年会计期间", year);
        LambdaQueryWrapper<AccPeriod> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AccPeriod::getYear, year);
        remove(deleteWrapper);

        for (int month = 1; month <= 12; month++) {
            AccPeriod period = new AccPeriod();
            period.setPeriodCode(String.format("%d%02d", year, month));
            period.setPeriodName(year + "年" + month + "月");
            period.setYear(year);
            period.setMonth(month);
            period.setStartDate(LocalDate.of(year, month, 1));
            period.setEndDate(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
            period.setStatus("OPEN");
            period.setIsCurrent(month == LocalDate.now().getMonthValue() && year == LocalDate.now().getYear() ? 1 : 0);
            period.setClosingType("NONE");
            period.setDeleted(0);
            period.setCreateTime(LocalDateTime.now());
            period.setUpdateTime(LocalDateTime.now());
            save(period);
        }
        log.info("{}年会计期间初始化完成", year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePeriod(String periodCode) {
        AccPeriod period = getByPeriodCode(periodCode);
        if (period == null) throw new RuntimeException("期间不存在");
        if ("CLOSED".equals(period.getStatus())) throw new RuntimeException("期间已结账");
        if (!checkPeriodCanClose(periodCode)) throw new RuntimeException("期间不满足结账条件");

        period.setStatus("CLOSED");
        period.setClosingType("FINAL");
        period.setClosingTime(LocalDateTime.now());
        period.setUpdateTime(LocalDateTime.now());
        updateById(period);

        AccPeriod nextPeriod = getNextPeriod(periodCode);
        if (nextPeriod != null && "OPEN".equals(nextPeriod.getStatus())) {
            LambdaUpdateWrapper<AccPeriod> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccPeriod::getId, nextPeriod.getId())
                    .set(AccPeriod::getIsCurrent, 1);
            update(updateWrapper);
        }

        LambdaUpdateWrapper<AccPeriod> resetWrapper = new LambdaUpdateWrapper<>();
        resetWrapper.eq(AccPeriod::getId, period.getId())
                .set(AccPeriod::getIsCurrent, 0);
        update(resetWrapper);

        log.info("期间{}结账完成", periodCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reopenPeriod(String periodCode) {
        AccPeriod period = getByPeriodCode(periodCode);
        if (period == null) throw new RuntimeException("期间不存在");
        if (!"CLOSED".equals(period.getStatus())) throw new RuntimeException("只有已结账期间可以反结账");

        AccPeriod nextPeriod = getNextPeriod(periodCode);
        if (nextPeriod != null && "CLOSED".equals(nextPeriod.getStatus())) {
            throw new RuntimeException("下一期间已结账，不能反结账当前期间");
        }

        period.setStatus("OPEN");
        period.setClosingType("NONE");
        period.setClosingTime(null);
        period.setClosingUserId(null);
        period.setClosingUserName(null);
        period.setIsCurrent(1);
        period.setUpdateTime(LocalDateTime.now());
        updateById(period);

        if (nextPeriod != null) {
            LambdaUpdateWrapper<AccPeriod> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccPeriod::getId, nextPeriod.getId())
                    .set(AccPeriod::getIsCurrent, 0);
            update(updateWrapper);
        }

        log.info("期间{}反结账完成", periodCode);
    }

    @Override
    public boolean checkPeriodCanClose(String periodCode) {
        AccPeriod period = getByPeriodCode(periodCode);
        if (period == null) return false;
        if (!"OPEN".equals(period.getStatus())) return false;
        return true;
    }

    private AccPeriod getByPeriodCode(String periodCode) {
        LambdaQueryWrapper<AccPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccPeriod::getPeriodCode, periodCode)
                .eq(AccPeriod::getDeleted, 0);
        return getOne(wrapper, false);
    }

    private AccPeriod getNextPeriod(String periodCode) {
        int year = Integer.parseInt(periodCode.substring(0, 4));
        int month = Integer.parseInt(periodCode.substring(4));
        String nextPeriodCode;
        if (month == 12) {
            nextPeriodCode = String.format("%d01", year + 1);
        } else {
            nextPeriodCode = String.format("%d%02d", year, month + 1);
        }
        return getByPeriodCode(nextPeriodCode);
    }
}
