package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.SubjectBalanceDTO;
import com.guarantee.finance.entity.AccSubjectBalance;
import com.guarantee.finance.vo.SubjectBalanceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AccSubjectBalanceService extends IService<AccSubjectBalance> {

    IPage<SubjectBalanceVO> queryBalances(String subjectCode, String period, Page<?> page);

    SubjectBalanceVO getBalanceDetail(Long id);

    Long createBalance(SubjectBalanceDTO dto);

    void updateBalance(Long id, SubjectBalanceDTO dto);

    void deleteBalance(Long id);

    void importBalances(MultipartFile file);

    Map<String, Object> validateBalances(String period);

    void confirmBalances(String period);

    List<SubjectBalanceVO> getPeriodBalances(String period);
}
