package com.guarantee.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BankDirectConnectService {

    Map<String, Object> queryBalance(String accountNo, String currency);

    List<Map<String, Object>> queryTransactions(String accountNo, LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> batchQueryBalances(List<String> accountNos);

    Map<String, Object> queryPaymentStatus(String bankCode, String paymentOrderNo);

    List<com.guarantee.finance.entity.BankAccountConfig> getAccountConfigs();

    void saveAccountConfig(com.guarantee.finance.entity.BankAccountConfig config);
}
