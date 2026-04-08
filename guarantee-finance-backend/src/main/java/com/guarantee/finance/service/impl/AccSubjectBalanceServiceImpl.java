package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.SubjectBalanceDTO;
import com.guarantee.finance.entity.AccSubjectBalance;
import com.guarantee.finance.mapper.AccSubjectBalanceMapper;
import com.guarantee.finance.service.AccSubjectBalanceService;
import com.guarantee.finance.vo.SubjectBalanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccSubjectBalanceServiceImpl extends ServiceImpl<AccSubjectBalanceMapper, AccSubjectBalance> implements AccSubjectBalanceService {

    private final AccSubjectBalanceMapper accSubjectBalanceMapper;

    @Override
    public IPage<SubjectBalanceVO> queryBalances(String subjectCode, String period, Page<?> page) {
        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(subjectCode != null, AccSubjectBalance::getSubjectCode, subjectCode)
                .like(period != null, AccSubjectBalance::getPeriod, period)
                .orderByDesc(AccSubjectBalance::getPeriod)
                .orderByAsc(AccSubjectBalance::getSubjectCode);

        IPage<AccSubjectBalance> balancePage = accSubjectBalanceMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return balancePage.convert(this::convertToVO);
    }

    @Override
    public SubjectBalanceVO getBalanceDetail(Long id) {
        AccSubjectBalance balance = accSubjectBalanceMapper.selectById(id);
        return convertToVO(balance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBalance(SubjectBalanceDTO dto) {
        // 检查是否已存在相同科目和期间的余额
        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getSubjectCode, dto.getSubjectCode())
                .eq(AccSubjectBalance::getPeriod, dto.getPeriod());
        if (accSubjectBalanceMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该科目在指定期间的余额已存在");
        }

        AccSubjectBalance balance = new AccSubjectBalance();
        balance.setSubjectCode(dto.getSubjectCode());
        balance.setPeriod(dto.getPeriod());
        balance.setBeginDebit(dto.getBeginDebit());
        balance.setBeginCredit(dto.getBeginCredit());
        balance.setCurrentDebit(dto.getCurrentDebit());
        balance.setCurrentCredit(dto.getCurrentCredit());
        balance.setEndDebit(dto.getEndDebit());
        balance.setEndCredit(dto.getEndCredit());
        balance.setAuxiliaryInfo(dto.getAuxiliaryInfo());
        balance.setYear(dto.getYear());
        balance.setMonth(dto.getMonth());
        balance.setStatus("0");

        accSubjectBalanceMapper.insert(balance);
        return balance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBalance(Long id, SubjectBalanceDTO dto) {
        AccSubjectBalance balance = accSubjectBalanceMapper.selectById(id);
        if (balance == null) {
            throw new RuntimeException("余额记录不存在");
        }

        // 检查是否已被确认
        if ("1".equals(balance.getStatus())) {
            throw new RuntimeException("该余额已确认，不可修改");
        }

        // 检查科目和期间是否变更
        if (!balance.getSubjectCode().equals(dto.getSubjectCode()) || !balance.getPeriod().equals(dto.getPeriod())) {
            LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccSubjectBalance::getSubjectCode, dto.getSubjectCode())
                    .eq(AccSubjectBalance::getPeriod, dto.getPeriod())
                    .ne(AccSubjectBalance::getId, id);
            if (accSubjectBalanceMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("该科目在指定期间的余额已存在");
            }
        }

        balance.setSubjectCode(dto.getSubjectCode());
        balance.setPeriod(dto.getPeriod());
        balance.setBeginDebit(dto.getBeginDebit());
        balance.setBeginCredit(dto.getBeginCredit());
        balance.setCurrentDebit(dto.getCurrentDebit());
        balance.setCurrentCredit(dto.getCurrentCredit());
        balance.setEndDebit(dto.getEndDebit());
        balance.setEndCredit(dto.getEndCredit());
        balance.setAuxiliaryInfo(dto.getAuxiliaryInfo());
        balance.setYear(dto.getYear());
        balance.setMonth(dto.getMonth());

        accSubjectBalanceMapper.updateById(balance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBalance(Long id) {
        AccSubjectBalance balance = accSubjectBalanceMapper.selectById(id);
        if (balance == null) {
            throw new RuntimeException("余额记录不存在");
        }

        // 检查是否已被确认
        if ("1".equals(balance.getStatus())) {
            throw new RuntimeException("该余额已确认，不可删除");
        }

        accSubjectBalanceMapper.deleteById(id);
    }

    @Override
    public void importBalances(MultipartFile file) {
        // TODO: 实现Excel导入功能
        throw new UnsupportedOperationException("Import functionality not implemented yet");
    }

    @Override
    public Map<String, Object> validateBalances(String period) {
        // TODO: 实现余额校验功能
        throw new UnsupportedOperationException("Validation functionality not implemented yet");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBalances(String period) {
        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period)
                .eq(AccSubjectBalance::getStatus, "0");
        List<AccSubjectBalance> balances = accSubjectBalanceMapper.selectList(wrapper);

        for (AccSubjectBalance balance : balances) {
            balance.setStatus("1");
            accSubjectBalanceMapper.updateById(balance);
        }
    }

    @Override
    public List<SubjectBalanceVO> getPeriodBalances(String period) {
        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period);
        List<AccSubjectBalance> balances = accSubjectBalanceMapper.selectList(wrapper);
        return balances.stream().map(this::convertToVO).toList();
    }

    private SubjectBalanceVO convertToVO(AccSubjectBalance balance) {
        if (balance == null) {
            return null;
        }
        SubjectBalanceVO vo = new SubjectBalanceVO();
        vo.setId(balance.getId());
        vo.setSubjectCode(balance.getSubjectCode());
        vo.setPeriod(balance.getPeriod());
        vo.setBeginDebit(balance.getBeginDebit());
        vo.setBeginCredit(balance.getBeginCredit());
        vo.setCurrentDebit(balance.getCurrentDebit());
        vo.setCurrentCredit(balance.getCurrentCredit());
        vo.setEndDebit(balance.getEndDebit());
        vo.setEndCredit(balance.getEndCredit());
        vo.setAuxiliaryInfo(balance.getAuxiliaryInfo());
        vo.setYear(balance.getYear());
        vo.setMonth(balance.getMonth());
        vo.setStatus(balance.getStatus());
        vo.setCreateTime(balance.getCreateTime());
        vo.setUpdateTime(balance.getUpdateTime());
        return vo;
    }
}
