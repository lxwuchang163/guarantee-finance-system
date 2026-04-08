package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.vo.BankTransactionVO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BankReconciliationService {

    void importTransactions(List<Map<String, Object>> transactionList, String dataSource);

    IPage<BankTransactionVO> queryTransactions(String accountNo, Integer transactionType, LocalDate startDate,
                                           LocalDate endDate, Integer matchStatus, Page<?> page);

    void executeAutoReconciliation(String accountNo, LocalDate reconciliationDate);

    Map<String, Object> getReconciliationResult(String accountNo, LocalDate date);

    void forceMatch(Long transactionId, Long billId, String billType);

    Map<String, Object> generateBalanceAdjustment(String accountNo, LocalDate date);
}
